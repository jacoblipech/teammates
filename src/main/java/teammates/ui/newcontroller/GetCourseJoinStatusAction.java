package teammates.ui.newcontroller;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;

import teammates.common.datatransfer.attributes.InstructorAttributes;
import teammates.common.datatransfer.attributes.StudentAttributes;
import teammates.common.util.Const;

/**
 * Action: Gets the "join" status of a student/instructor.
 */
public class GetCourseJoinStatusAction extends Action {

    @Override
    protected AuthType getMinAuthLevel() {
        return AuthType.LOGGED_IN;
    }

    @Override
    protected boolean checkSpecificAccessControl() {
        return true;
    }

    @Override
    protected ActionResult execute() {
        String regkey = getNonNullRequestParamValue(Const.ParamsNames.REGKEY);
        String entityType = getNonNullRequestParamValue(Const.ParamsNames.ENTITY_TYPE);
        switch (entityType) {
        case Const.EntityType.STUDENT:
            return getStudentJoinStatus(regkey);
        case Const.EntityType.INSTRUCTOR:
            return getInstructorJoinStatus(regkey);
        default:
            return new JsonResult("Error: invalid entity type", HttpStatus.SC_BAD_REQUEST);
        }
    }

    private JsonResult getStudentJoinStatus(String regkey) {
        StudentAttributes student = logic.getStudentForRegistrationKey(regkey);
        if (student == null) {
            return new JsonResult("No student with given registration key: " + regkey, HttpStatus.SC_NOT_FOUND);
        }
        return getJoinStatusResult(student.googleId != null);
    }

    private JsonResult getInstructorJoinStatus(String regkey) {
        InstructorAttributes instructor = logic.getInstructorForRegistrationKey(regkey);
        if (instructor == null) {
            return new JsonResult("No instructor with given registration key: " + regkey, HttpStatus.SC_NOT_FOUND);
        }
        return getJoinStatusResult(instructor.googleId != null);
    }

    private JsonResult getJoinStatusResult(boolean hasJoined) {
        Map<String, Object> result = new HashMap<>();
        result.put("hasJoined", hasJoined);
        if (!hasJoined) {
            result.put("userId", userInfo.id);
        }
        return new JsonResult(result);
    }

}
