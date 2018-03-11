package teammates.test.cases.util;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.testng.annotations.Test;

import teammates.common.util.TimeHelper;
import teammates.test.cases.BaseTestCase;

/**
 * SUT: {@link TimeHelper}.
 */
public class TimeHelperTest extends BaseTestCase {

    @Test
    public void testCombineDateTime() {
        String testDate = "Fri, 01 Feb, 2013";
        String testTime = "0";
        LocalDateTime expectedOutput = LocalDateTime.of(2013, 2, 1, 0, 0);

        testTime = "0";
        ______TS("boundary case: time = 0");
        assertEquals(expectedOutput, TimeHelper.combineDateTimeNew(testDate, testTime));

        ______TS("boundary case: time = 24");
        testTime = "24";
        expectedOutput = LocalDateTime.of(2013, 2, 1, 23, 59);
        assertEquals(expectedOutput, TimeHelper.combineDateTimeNew(testDate, testTime));

        ______TS("negative time");
        assertNull(TimeHelper.combineDateTimeNew(testDate, "-5"));

        ______TS("large time");
        assertNull(TimeHelper.combineDateTimeNew(testDate, "68"));

        ______TS("date null");
        assertNull(TimeHelper.combineDateTimeNew(null, testTime));

        ______TS("time null");
        assertNull(TimeHelper.combineDateTimeNew(testDate, null));

        ______TS("invalid time");
        assertNull(TimeHelper.combineDateTimeNew(testDate, "invalid time"));

        ______TS("fractional time");
        assertNull(TimeHelper.combineDateTimeNew(testDate, "5.5"));

        ______TS("invalid date");
        assertNull(TimeHelper.combineDateTimeNew("invalid date", testDate));
    }

    @Test
    public void testIsTimeWithinPeriod() {
        Calendar startCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Calendar endCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Calendar timeCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        // Set start time to 5 days before today and end time to 5 days after today
        startCalendar.add(Calendar.DAY_OF_MONTH, -5);
        endCalendar.add(Calendar.DAY_OF_MONTH, 5);

        Date startTime = startCalendar.getTime();
        Date endTime = endCalendar.getTime();
        Date time;

        ______TS("Time within period test");
        time = timeCalendar.getTime();

        assertTrue(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, true, true));
        assertTrue(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, true, false));
        assertTrue(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, false, true));
        assertTrue(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, false, false));

        ______TS("Time on start time test");
        timeCalendar = startCalendar;
        time = timeCalendar.getTime();

        assertTrue(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, true, true));
        assertTrue(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, true, false));
        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, false, true));
        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, false, false));

        ______TS("Time before start time test");
        timeCalendar.add(Calendar.DAY_OF_MONTH, -10);
        time = timeCalendar.getTime();

        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, true, true));
        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, true, false));
        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, false, true));
        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, false, false));

        ______TS("Time on end time test");
        timeCalendar = endCalendar;
        time = timeCalendar.getTime();

        assertTrue(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, true, true));
        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, true, false));
        assertTrue(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, false, true));
        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, false, false));

        ______TS("Time after start time test");
        timeCalendar.add(Calendar.DAY_OF_MONTH, 10);
        time = timeCalendar.getTime();

        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, true, true));
        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, true, false));
        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, false, true));
        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, endTime, time, false, false));

        ______TS("Start time null test");
        assertFalse(TimeHelper.isTimeWithinPeriod(null, endTime, time, true, true));
        assertFalse(TimeHelper.isTimeWithinPeriod(null, endTime, time, true, false));
        assertFalse(TimeHelper.isTimeWithinPeriod(null, endTime, time, false, true));
        assertFalse(TimeHelper.isTimeWithinPeriod(null, endTime, time, false, false));

        ______TS("End time null test");
        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, null, time, true, true));
        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, null, time, true, false));
        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, null, time, false, true));
        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, null, time, false, false));

        ______TS("Time null test");
        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, endTime, null, true, true));
        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, endTime, null, true, false));
        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, endTime, null, false, true));
        assertFalse(TimeHelper.isTimeWithinPeriod(startTime, endTime, null, false, false));
    }

    @Test
    public void testEndOfYearDates() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.clear();
        cal.set(2015, 11, 30, 12, 0, 0);
        Date date = cal.getTime();
        assertEquals("30/12/2015", TimeHelper.formatDate(date));
        assertEquals("Wed, 30 Dec, 2015", TimeHelper.formatDateForSessionsForm(date));
        assertEquals("Wed, 30 Dec 2015, 12:00 NOON", TimeHelper.formatTime12H(date));
        assertEquals("Wed, 30 Dec 2015, 12:00 NOON UTC+0000", TimeHelper.formatDateTimeForSessions(date, 0));
        assertEquals("30 Dec 12:00 NOON", TimeHelper.formatDateTimeForInstructorHomePage(date));
    }

    @Test
    public void testFormatDateTimeForSessions() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.clear();
        cal.set(2015, 10, 30, 12, 0, 0);
        Date date = cal.getTime();
        assertEquals("Mon, 30 Nov 2015, 12:00 NOON UTC+0000", TimeHelper.formatDateTimeForSessions(date, 0));

        cal.clear();
        cal.set(2015, 10, 30, 4, 0, 0);
        date = cal.getTime();
        assertEquals("Mon, 30 Nov 2015, 12:00 NOON UTC+0800", TimeHelper.formatDateTimeForSessions(date, 8));

        cal.clear();
        cal.set(2015, 10, 30, 4, 0, 0);
        date = cal.getTime();
        assertEquals("Mon, 30 Nov 2015, 04:00 PM UTC+1200", TimeHelper.formatDateTimeForSessions(date, 12));

        cal.clear();
        cal.set(2015, 10, 30, 16, 0, 0);
        date = cal.getTime();
        assertEquals("Mon, 30 Nov 2015, 12:00 NOON UTC-0400", TimeHelper.formatDateTimeForSessions(date, -4));

        cal.clear();
        cal.set(2015, 10, 30, 16, 0, 0);
        date = cal.getTime();
        assertEquals("Mon, 30 Nov 2015, 11:45 AM UTC-0415", TimeHelper.formatDateTimeForSessions(date, -4.25));
    }

}
