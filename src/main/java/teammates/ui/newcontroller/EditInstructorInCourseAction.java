package teammates.ui.newcontroller;

import java.util.List;

import org.apache.http.HttpStatus;

import teammates.common.datatransfer.InstructorPrivileges;
import teammates.common.datatransfer.attributes.InstructorAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.exception.UnauthorizedAccessException;
import teammates.common.util.Const;
import teammates.common.util.SanitizationHelper;

/**
 * Edits an instructor in a course.
 */
public class EditInstructorInCourseAction extends UpdateInstructorPrivilegesAbstractAction {

    @Override
    protected AuthType getMinAuthLevel() {
        return AuthType.LOGGED_IN;
    }

    @Override
    public void checkSpecificAccessControl() {
        if (!userInfo.isInstructor) {
            throw new UnauthorizedAccessException("Instructor privilege is required to access this resource.");
        }

        String courseId = getNonNullRequestParamValue(Const.ParamsNames.COURSE_ID);

        InstructorAttributes instructor = logic.getInstructorForGoogleId(courseId, userInfo.id);
        gateKeeper.verifyAccessible(instructor, logic.getCourse(courseId),
                Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_INSTRUCTOR);
    }

    @Override
    public ActionResult execute() {
        String courseId = getNonNullRequestParamValue(Const.ParamsNames.COURSE_ID);
        String instructorId = getRequestParamValue(Const.ParamsNames.INSTRUCTOR_ID);
        String instructorName = getNonNullRequestParamValue(Const.ParamsNames.INSTRUCTOR_NAME);
        String instructorEmail = getNonNullRequestParamValue(Const.ParamsNames.INSTRUCTOR_EMAIL);

        InstructorAttributes instructorToEdit =
                extractUpdatedInstructor(courseId, instructorId, instructorName, instructorEmail);
        updateToEnsureValidityOfInstructorsForTheCourse(courseId, instructorToEdit);

        try {
            if (instructorId == null) {
                logic.updateInstructorByEmail(instructorEmail, instructorToEdit);
            } else {
                logic.updateInstructorByGoogleId(instructorId, instructorToEdit);
            }
        } catch (InvalidParametersException e) {
            return new JsonResult(e.getMessage(), HttpStatus.SC_BAD_REQUEST);
        } catch (EntityDoesNotExistException ednee) {
            return new JsonResult(ednee.getMessage(), HttpStatus.SC_NOT_FOUND);
        }

        return new JsonResult("The changes to the instructor " + instructorName + " has been updated.",
                HttpStatus.SC_OK);
    }

    /**
     * Checks if there are any other registered instructors that can modify instructors.
     * If there are none, the instructor currently being edited will be granted the privilege
     * of modifying instructors automatically.
     *
     * @param courseId         Id of the course.
     * @param instructorToEdit Instructor that will be edited.
     *                             This may be modified within the method.
     */
    private void updateToEnsureValidityOfInstructorsForTheCourse(String courseId, InstructorAttributes instructorToEdit) {
        List<InstructorAttributes> instructors = logic.getInstructorsForCourse(courseId);
        int numOfInstrCanModifyInstructor = 0;
        InstructorAttributes instrWithModifyInstructorPrivilege = null;
        for (InstructorAttributes instructor : instructors) {
            if (instructor.isAllowedForPrivilege(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_INSTRUCTOR)) {
                numOfInstrCanModifyInstructor++;
                instrWithModifyInstructorPrivilege = instructor;
            }
        }
        boolean isLastRegInstructorWithPrivilege = numOfInstrCanModifyInstructor <= 1
                && instrWithModifyInstructorPrivilege != null
                && (!instrWithModifyInstructorPrivilege.isRegistered()
                || instrWithModifyInstructorPrivilege.googleId
                .equals(instructorToEdit.googleId));
        if (isLastRegInstructorWithPrivilege) {
            instructorToEdit.privileges.updatePrivilege(Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_INSTRUCTOR, true);
        }
    }

    /**
     * Creates a new instructor representing the updated instructor with all information filled in,
     * using request parameters.
     * This includes basic information as well as custom privileges (if applicable).
     *
     * @param courseId        Id of the course the instructor is being added to.
     * @param instructorId    Id of the instructor.
     * @param instructorName  Name of the instructor.
     * @param instructorEmail Email of the instructor.
     * @return The updated instructor with all relevant info filled in.
     */
    private InstructorAttributes extractUpdatedInstructor(String courseId, String instructorId,
                                                          String instructorName, String instructorEmail) {
        String instructorRole = getNonNullRequestParamValue(Const.ParamsNames.INSTRUCTOR_ROLE_NAME);
        boolean isDisplayedToStudents = getRequestParamValue(Const.ParamsNames.INSTRUCTOR_IS_DISPLAYED_TO_STUDENT) != null;
        String displayedName = getRequestParamValue(Const.ParamsNames.INSTRUCTOR_DISPLAY_NAME);
        if (displayedName == null || displayedName.isEmpty()) {
            displayedName = InstructorAttributes.DEFAULT_DISPLAY_NAME;
        }
        instructorRole = SanitizationHelper.sanitizeName(instructorRole);
        displayedName = SanitizationHelper.sanitizeName(displayedName);

        InstructorAttributes instructorToEdit =
                updateBasicInstructorAttributes(courseId, instructorId, instructorName, instructorEmail,
                        instructorRole, isDisplayedToStudents, displayedName);

        if (instructorRole.equals(Const.InstructorPermissionRoleNames.INSTRUCTOR_PERMISSION_ROLE_CUSTOM)) {
            updateInstructorCourseLevelPrivileges(instructorToEdit);
        }

        updateInstructorWithSectionLevelPrivileges(courseId, instructorToEdit);

        instructorToEdit.privileges.validatePrivileges();

        return instructorToEdit;
    }

    /**
     * Edits an existing instructor's basic information.
     * This consists of everything apart from custom privileges.
     *
     * @param courseId              Id of the course the instructor is being added to.
     * @param instructorId          Id of the instructor.
     * @param instructorName        Name of the instructor.
     * @param instructorEmail       Email of the instructor.
     * @param instructorRole        Role of the instructor.
     * @param isDisplayedToStudents Whether the instructor should be visible to students.
     * @param displayedName         Name to be visible to students.
     *                                  Should not be {@code null} even if {@code isDisplayedToStudents} is false.
     * @return The edited instructor with updated basic info, and its old custom privileges (if applicable)
     */
    private InstructorAttributes updateBasicInstructorAttributes(String courseId,
                                                                 String instructorId, String instructorName,
                                                                 String instructorEmail, String instructorRole,
                                                                 boolean isDisplayedToStudents, String displayedName) {
        InstructorAttributes instructorToEdit = null;
        if (instructorId == null) {
            instructorToEdit = logic.getInstructorForEmail(courseId, instructorEmail);
        } else {
            instructorToEdit = logic.getInstructorForGoogleId(courseId, instructorId);
        }
        instructorToEdit.name = SanitizationHelper.sanitizeName(instructorName);
        instructorToEdit.email = SanitizationHelper.sanitizeEmail(instructorEmail);
        instructorToEdit.role = SanitizationHelper.sanitizeName(instructorRole);
        instructorToEdit.displayedName = SanitizationHelper.sanitizeName(displayedName);
        instructorToEdit.isDisplayedToStudents = isDisplayedToStudents;
        instructorToEdit.privileges = new InstructorPrivileges(instructorToEdit.role);

        return instructorToEdit;
    }

}
