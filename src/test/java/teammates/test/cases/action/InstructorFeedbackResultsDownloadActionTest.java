package teammates.test.cases.action;

import java.net.URL;

import org.testng.annotations.Test;

import teammates.common.datatransfer.attributes.FeedbackSessionAttributes;
import teammates.common.datatransfer.attributes.StudentAttributes;
import teammates.common.exception.NullHttpParameterException;
import teammates.common.util.Config;
import teammates.common.util.Const;
import teammates.logic.core.FeedbackQuestionsLogic;
import teammates.logic.core.StudentsLogic;
import teammates.test.driver.CsvChecker;
import teammates.ui.controller.FileDownloadResult;
import teammates.ui.controller.InstructorFeedbackResultsDownloadAction;
import teammates.ui.controller.RedirectResult;

/**
 * SUT: {@link InstructorFeedbackResultsDownloadAction}.
 */
public class InstructorFeedbackResultsDownloadActionTest extends BaseActionTest {

    private static FeedbackQuestionsLogic fqLogic = FeedbackQuestionsLogic.inst();

    @Override
    protected String getActionUri() {
        return Const.ActionURIs.INSTRUCTOR_FEEDBACK_RESULTS_DOWNLOAD;
    }

    @Override
    @Test
    public void testExecuteAndPostProcess() throws Exception {
        gaeSimulation.loginAsInstructor(typicalBundle.instructors.get("instructor1OfCourse1").googleId);
        FeedbackSessionAttributes session = typicalBundle.feedbackSessions.get("session1InCourse1");
        String[] paramsNormal = {
                Const.ParamsNames.COURSE_ID, session.getCourseId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session.getFeedbackSessionName()
        };
        String[] paramsNormalWithinSection = {
                Const.ParamsNames.COURSE_ID, session.getCourseId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session.getFeedbackSessionName(),
                Const.ParamsNames.SECTION_NAME, "Section 1"
        };

        String[] paramsWithLargeData = {
                Const.ParamsNames.COURSE_ID, session.getCourseId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session.getFeedbackSessionName(),
                "simulateExcessDataForTesting", "true"
        };

        String[] paramsWithNullCourseId = {
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session.getFeedbackSessionName()
        };
        String[] paramsWithNullFeedbackSessionName = {
                Const.ParamsNames.COURSE_ID, session.getCourseId()
        };

        String[] paramsWithMissingResponsesShown = {
                Const.ParamsNames.COURSE_ID, session.getCourseId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session.getFeedbackSessionName(),
                Const.ParamsNames.FEEDBACK_RESULTS_INDICATE_MISSING_RESPONSES, "true"
        };

        String[] paramsWithMissingResponsesHidden = {
                Const.ParamsNames.COURSE_ID, session.getCourseId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session.getFeedbackSessionName(),
                Const.ParamsNames.FEEDBACK_RESULTS_INDICATE_MISSING_RESPONSES, "false"
        };

        ______TS("Typical case: results downloadable");

        InstructorFeedbackResultsDownloadAction action = getAction(paramsNormal);
        FileDownloadResult result = getFileDownloadResult(action);

        String expectedDestination = getPageResultDestination("filedownload", false, "idOfInstructor1OfCourse1");
        assertEquals(expectedDestination, result.getDestinationWithParams());
        assertFalse(result.isError);
        assertEquals("", result.getStatusMessage());

        String expectedFileName = session.getCourseId() + "_" + session.getFeedbackSessionName();
        assertEquals(expectedFileName, result.getFileName());
        CsvChecker.verifyCsvContent(result.getFileContent(), "/feedbackSessionResultsC1S1_actionTest.csv");

        ______TS("Typical successful case: student last name displayed properly after being specified with braces");

        StudentAttributes student1InCourse1 = typicalBundle.students.get("student1InCourse1");
        student1InCourse1.name = "new name {new last name}";
        StudentsLogic studentsLogic = StudentsLogic.inst();
        studentsLogic.updateStudentCascade(student1InCourse1.email, student1InCourse1);

        action = getAction(paramsNormal);
        result = getFileDownloadResult(action);

        expectedDestination = getPageResultDestination("filedownload", false, "idOfInstructor1OfCourse1");
        assertEquals(expectedDestination, result.getDestinationWithParams());
        assertFalse(result.isError);
        assertEquals("", result.getStatusMessage());

        expectedFileName = session.getCourseId() + "_" + session.getFeedbackSessionName();
        assertEquals(expectedFileName, result.getFileName());
        CsvChecker.verifyCsvContent(result.getFileContent(), "/feedbackSessionResultsC1S1NewLastName_actionTest.csv");

        removeAndRestoreTypicalDataBundle();

        ______TS("Typical case: results within section downloadable");

        action = getAction(paramsNormalWithinSection);
        result = getFileDownloadResult(action);

        expectedDestination = getPageResultDestination("filedownload", false, "idOfInstructor1OfCourse1");
        assertEquals(expectedDestination, result.getDestinationWithParams());
        assertFalse(result.isError);

        expectedFileName = session.getCourseId() + "_" + session.getFeedbackSessionName() + "_Section 1";
        assertEquals(expectedFileName, result.getFileName());
        CsvChecker.verifyCsvContent(result.getFileContent(), "/feedbackSessionResultsC1S1S1_actionTest.csv");

        ______TS("Mock case to throw ExceedingRangeException: data is too large to be downloaded in one go");

        action = getAction(paramsWithLargeData);
        RedirectResult r = getRedirectResult(action);

        expectedDestination = getPageResultDestination(Const.ActionURIs.INSTRUCTOR_FEEDBACK_RESULTS_PAGE, true, "");
        expectedDestination = Config.getFrontEndAppUrl(expectedDestination)
                .withCourseId(session.getCourseId()).withUserId("idOfInstructor1OfCourse1")
                .withSessionName(session.getFeedbackSessionName()).toAbsoluteString();
        assertEquals(new URL(expectedDestination).getFile(), r.getDestinationWithParams());
        assertTrue(r.isError);

        assertEquals(Const.StatusMessages.FEEDBACK_SESSION_DOWNLOAD_FILE_SIZE_EXCEEDED, r.getStatusMessage());

        ______TS("Failure case: params with null course id");

        try {
            action = getAction(paramsWithNullCourseId);
            result = getFileDownloadResult(action);
            signalFailureToDetectException("Did not detect that parameters are null.");
        } catch (NullHttpParameterException e) {
            assertEquals(String.format(Const.StatusCodes.NULL_POST_PARAMETER,
                                       Const.ParamsNames.COURSE_ID),
                         e.getMessage());
        }

        ______TS("Failure case: params with null feedback session name");

        try {
            action = getAction(paramsWithNullFeedbackSessionName);
            result = getFileDownloadResult(action);
            signalFailureToDetectException("Did not detect that parameters are null.");
        } catch (NullHttpParameterException e) {
            assertEquals(String.format(Const.StatusCodes.NULL_POST_PARAMETER,
                    Const.ParamsNames.FEEDBACK_SESSION_NAME), e.getMessage());
        }

        ______TS("Typical case: results with missing responses shown");
        action = getAction(paramsWithMissingResponsesShown);
        result = getFileDownloadResult(action);
        expectedDestination = getPageResultDestination("filedownload", false, "idOfInstructor1OfCourse1");
        assertEquals(expectedDestination, result.getDestinationWithParams());
        assertFalse(result.isError);

        expectedFileName = session.getCourseId() + "_" + session.getFeedbackSessionName();
        assertEquals(expectedFileName, result.getFileName());
        CsvChecker.verifyCsvContent(result.getFileContent(), "/feedbackSessionResultsMissingResponsesShown_actionTest.csv");

        ______TS("Typical case: results with missing responses hidden");
        action = getAction(paramsWithMissingResponsesHidden);
        result = getFileDownloadResult(action);
        expectedDestination = getPageResultDestination("filedownload", false, "idOfInstructor1OfCourse1");
        assertEquals(expectedDestination, result.getDestinationWithParams());
        assertFalse(result.isError);

        expectedFileName = session.getCourseId() + "_" + session.getFeedbackSessionName();
        assertEquals(expectedFileName, result.getFileName());
        CsvChecker.verifyCsvContent(result.getFileContent(), "/feedbackSessionResultsMissingResponsesHidden_actionTest.csv");

        ______TS("Typical case: results downloadable by question");

        int questionNum2 = typicalBundle.feedbackQuestions.get("qn2InSession1InCourse1").getQuestionNumber();
        String question2Id = fqLogic.getFeedbackQuestion(session.getFeedbackSessionName(),
                session.getCourseId(), questionNum2).getId();
        String[] paramsQuestion2 = {
                Const.ParamsNames.COURSE_ID, session.getCourseId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session.getFeedbackSessionName(),
                Const.ParamsNames.FEEDBACK_QUESTION_NUMBER, "2",
                Const.ParamsNames.FEEDBACK_QUESTION_ID, question2Id
        };

        action = getAction(paramsQuestion2);
        result = getFileDownloadResult(action);

        expectedDestination = getPageResultDestination("filedownload", false, "idOfInstructor1OfCourse1");
        assertEquals(expectedDestination, result.getDestinationWithParams());
        assertFalse(result.isError);
        assertEquals("", result.getStatusMessage());

        expectedFileName = session.getCourseId() + "_" + session.getFeedbackSessionName() + "_question2";
        assertEquals(expectedFileName, result.getFileName());
        CsvChecker.verifyCsvContent(result.getFileContent(), "/feedbackSessionResultsC1S1Q2_actionTest.csv");

        ______TS("Typical case: results within section downloadable by question");

        int questionNum1 = typicalBundle.feedbackQuestions.get("qn1InSession1InCourse1").getQuestionNumber();
        String question1Id = fqLogic.getFeedbackQuestion(session.getFeedbackSessionName(),
                session.getCourseId(), questionNum1).getId();

        String[] paramsQuestion1WithinSection = {
                Const.ParamsNames.COURSE_ID, session.getCourseId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session.getFeedbackSessionName(),
                Const.ParamsNames.SECTION_NAME, "Section 1",
                Const.ParamsNames.FEEDBACK_QUESTION_NUMBER, "1",
                Const.ParamsNames.FEEDBACK_QUESTION_ID, question1Id
        };

        action = getAction(paramsQuestion1WithinSection);
        result = getFileDownloadResult(action);

        expectedDestination = getPageResultDestination("filedownload", false, "idOfInstructor1OfCourse1");
        assertEquals(expectedDestination, result.getDestinationWithParams());
        assertFalse(result.isError);

        expectedFileName = session.getCourseId() + "_" + session.getFeedbackSessionName() + "_Section 1" + "_question1";
        assertEquals(expectedFileName, result.getFileName());
        CsvChecker.verifyCsvContent(result.getFileContent(), "/feedbackSessionResultsC1S1S1Q1_actionTest.csv");
    }

    @Override
    protected InstructorFeedbackResultsDownloadAction getAction(String... params) {
        return (InstructorFeedbackResultsDownloadAction) gaeSimulation.getActionObject(getActionUri(), params);
    }

    @Override
    @Test
    protected void testAccessControl() throws Exception {
        FeedbackSessionAttributes session = typicalBundle.feedbackSessions.get("session1InCourse1");

        String[] submissionParams = new String[] {
                Const.ParamsNames.COURSE_ID, session.getCourseId(),
                Const.ParamsNames.FEEDBACK_SESSION_NAME, session.getFeedbackSessionName()
        };

        verifyOnlyInstructorsOfTheSameCourseCanAccess(submissionParams);
    }

}
