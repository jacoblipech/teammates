package teammates.test.cases.action;

import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.Test;

import teammates.common.datatransfer.attributes.FeedbackSessionAttributes;
import teammates.common.datatransfer.attributes.InstructorAttributes;
import teammates.common.util.Const;
import teammates.test.driver.AssertHelper;
import teammates.ui.controller.AjaxResult;
import teammates.ui.controller.InstructorFeedbackEditSaveAction;
import teammates.ui.pagedata.InstructorFeedbackEditPageData;

/**
 * SUT: {@link InstructorFeedbackEditSaveAction}.
 */
public class InstructorFeedbackEditSaveActionTest extends BaseActionTest {

    @Override
    protected String getActionUri() {
        return Const.ActionURIs.INSTRUCTOR_FEEDBACK_EDIT_SAVE;
    }

    @Override
    @Test
    public void testExecuteAndPostProcess() {
        InstructorAttributes instructor1ofCourse1 = typicalBundle.instructors.get("instructor1OfCourse1");
        FeedbackSessionAttributes session = typicalBundle.feedbackSessions.get("session1InCourse1");

        String expectedString = "";

        ______TS("Not enough parameters");

        gaeSimulation.loginAsInstructor(instructor1ofCourse1.googleId);
        verifyAssumptionFailure();
        //TODO make sure IFESA does assertNotNull for required parameters then uncomment
        //verifyAssumptionFailure(Const.ParamsNames.COURSE_ID, instructor1ofCourse1.courseId,
        //                        Const.ParamsNames.FEEDBACK_SESSION_NAME, session.feedbackSessionName);

        ______TS("success: Typical case");

        String[] params = createParamsForTypicalFeedbackSession(instructor1ofCourse1.courseId,
                                                                session.getFeedbackSessionName());

        InstructorFeedbackEditSaveAction a = getAction(params);
        AjaxResult ar = getAjaxResult(a);
        InstructorFeedbackEditPageData pageData = (InstructorFeedbackEditPageData) ar.data;

        assertEquals(Const.StatusMessages.FEEDBACK_SESSION_EDITED, pageData.getStatusForAjax());
        assertFalse(pageData.getHasError());

        expectedString =
                "TEAMMATESLOG|||instructorFeedbackEditSave|||instructorFeedbackEditSave|||true|||"
                + "Instructor|||Instructor 1 of Course 1|||idOfInstructor1OfCourse1|||"
                + "instr1@course1.tmt|||Updated Feedback Session "
                + "<span class=\"bold\">(First feedback session)</span> for Course "
                + "<span class=\"bold\">[idOfTypicalCourse1]</span> created.<br>"
                + "<span class=\"bold\">From:</span> 2012-01-31T16:00:00Z"
                + "<span class=\"bold\"> to</span> 2014-12-31T16:00:00Z<br>"
                + "<span class=\"bold\">Session visible from:</span> 2011-12-31T16:00:00Z<br>"
                + "<span class=\"bold\">Results visible from:</span> 1970-06-22T00:00:00Z<br><br>"
                + "<span class=\"bold\">Instructions:</span> "
                + "<Text: instructions>|||/page/instructorFeedbackEditSave";
        AssertHelper.assertLogMessageEquals(expectedString, a.getLogMessage());

        ______TS("failure: invalid parameters");

        params[15] = "Thu, 01 Mar, 2012";

        a = getAction(params);
        ar = getAjaxResult(a);
        pageData = (InstructorFeedbackEditPageData) ar.data;

        expectedString = "The start time for this feedback session cannot be "
                         + "earlier than the time when the session will be visible.";
        assertEquals(expectedString, pageData.getStatusForAjax());
        assertTrue(pageData.getHasError());

        ______TS("success: Timzone with offset, 'never' show session, 'custom' show results");

        params = createParamsForTypicalFeedbackSession(instructor1ofCourse1.courseId,
                                                       session.getFeedbackSessionName());
        params[25] = "5.75";
        params[13] = Const.INSTRUCTOR_FEEDBACK_SESSION_VISIBLE_TIME_NEVER;
        params[19] = Const.INSTRUCTOR_FEEDBACK_RESULTS_VISIBLE_TIME_LATER;

        //remove instructions, grace period, start time to test null conditions

        a = getAction(params);
        ar = getAjaxResult(a);
        pageData = (InstructorFeedbackEditPageData) ar.data;

        assertEquals(Const.StatusMessages.FEEDBACK_SESSION_EDITED, pageData.getStatusForAjax());
        assertFalse(pageData.getHasError());

        expectedString =
                "TEAMMATESLOG|||instructorFeedbackEditSave|||instructorFeedbackEditSave|||true|||"
                + "Instructor|||Instructor 1 of Course 1|||idOfInstructor1OfCourse1|||"
                + "instr1@course1.tmt|||Updated Feedback Session "
                + "<span class=\"bold\">(First feedback session)</span> for Course "
                + "<span class=\"bold\">[idOfTypicalCourse1]</span> created.<br>"
                + "<span class=\"bold\">From:</span> 2012-01-31T18:15:00Z"
                + "<span class=\"bold\"> to</span> 2014-12-31T18:15:00Z<br>"
                + "<span class=\"bold\">Session visible from:</span> 1970-11-27T00:00:00Z<br>"
                + "<span class=\"bold\">Results visible from:</span> 1970-01-01T00:00:00Z<br><br>"
                + "<span class=\"bold\">Instructions:</span> "
                + "<Text: instructions>|||/page/instructorFeedbackEditSave";
        AssertHelper.assertLogMessageEquals(expectedString, a.getLogMessage());

        ______TS("success: atopen session visible time, custom results visible time, null timezone, null grace period");

        params = createParamsCombinationForFeedbackSession(
                         instructor1ofCourse1.courseId, session.getFeedbackSessionName(), 1);

        //remove grace period (first) and then time zone
        params = ArrayUtils.remove(params, 26);
        params = ArrayUtils.remove(params, 26);
        params = ArrayUtils.remove(params, 24);
        params = ArrayUtils.remove(params, 24);

        a = getAction(params);
        ar = getAjaxResult(a);
        pageData = (InstructorFeedbackEditPageData) ar.data;

        assertEquals(Const.StatusMessages.FEEDBACK_SESSION_EDITED, pageData.getStatusForAjax());
        assertFalse(pageData.getHasError());

        expectedString =
                "TEAMMATESLOG|||instructorFeedbackEditSave|||instructorFeedbackEditSave|||true|||"
                + "Instructor|||Instructor 1 of Course 1|||idOfInstructor1OfCourse1|||"
                + "instr1@course1.tmt|||Updated Feedback Session "
                + "<span class=\"bold\">(First feedback session)</span> for Course "
                + "<span class=\"bold\">[idOfTypicalCourse1]</span> created.<br>"
                + "<span class=\"bold\">From:</span> 2012-02-01T00:00:00Z"
                + "<span class=\"bold\"> to</span> 2015-01-01T00:00:00Z<br>"
                + "<span class=\"bold\">Session visible from:</span> 1970-12-31T00:00:00Z<br>"
                + "<span class=\"bold\">Results visible from:</span> 2014-05-08T02:00:00Z<br><br>"
                + "<span class=\"bold\">Instructions:</span> "
                + "<Text: instructions>|||/page/instructorFeedbackEditSave";
        AssertHelper.assertLogMessageEquals(expectedString, a.getLogMessage());

        ______TS("success: Masquerade mode, manual release results, invalid timezone and graceperiod");

        String adminUserId = "admin.user";
        gaeSimulation.loginAsAdmin(adminUserId);

        params = createParamsForTypicalFeedbackSession(instructor1ofCourse1.courseId,
                                                       session.getFeedbackSessionName());
        params[19] = Const.INSTRUCTOR_FEEDBACK_RESULTS_VISIBLE_TIME_LATER;
        params[25] = " ";
        params[27] = "12dsf";

        params = addUserIdToParams(instructor1ofCourse1.googleId, params);

        a = getAction(params);
        ar = getAjaxResult(a);
        pageData = (InstructorFeedbackEditPageData) ar.data;

        assertEquals(Const.StatusMessages.FEEDBACK_SESSION_EDITED, pageData.getStatusForAjax());
        assertFalse(pageData.getHasError());

        expectedString =
                "TEAMMATESLOG|||instructorFeedbackEditSave|||instructorFeedbackEditSave|||true|||"
                + "Instructor(M)|||Instructor 1 of Course 1|||idOfInstructor1OfCourse1|||"
                + "instr1@course1.tmt|||Updated Feedback Session "
                + "<span class=\"bold\">(First feedback session)</span> for Course "
                + "<span class=\"bold\">[idOfTypicalCourse1]</span> created.<br>"
                + "<span class=\"bold\">From:</span> 2012-02-01T00:00:00Z"
                + "<span class=\"bold\"> to</span> 2015-01-01T00:00:00Z<br>"
                + "<span class=\"bold\">Session visible from:</span> 2012-01-01T00:00:00Z<br>"
                + "<span class=\"bold\">Results visible from:</span> 1970-01-01T00:00:00Z<br><br>"
                + "<span class=\"bold\">Instructions:</span> "
                + "<Text: instructions>|||/page/instructorFeedbackEditSave";
        AssertHelper.assertLogMessageEqualsInMasqueradeMode(expectedString, a.getLogMessage(), adminUserId);
    }

    @Override
    protected InstructorFeedbackEditSaveAction getAction(String... params) {
        return (InstructorFeedbackEditSaveAction) gaeSimulation.getActionObject(getActionUri(), params);
    }

    @Override
    @Test
    protected void testAccessControl() throws Exception {
        FeedbackSessionAttributes fs = typicalBundle.feedbackSessions.get("session1InCourse1");
        String[] submissionParams =
                createParamsForTypicalFeedbackSession(fs.getCourseId(), fs.getFeedbackSessionName());

        verifyUnaccessibleWithoutModifySessionPrivilege(submissionParams);
        verifyOnlyInstructorsOfTheSameCourseCanAccess(submissionParams);
    }
}
