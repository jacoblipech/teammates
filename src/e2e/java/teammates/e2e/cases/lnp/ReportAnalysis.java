package teammates.e2e.cases.lnp;

/**
 * Report analysis class for L&P tests.
 */
public class ReportAnalysis {

    private double meanResTime;
    private double pct1ResTime; // 90th percentile
    private double throughput;
    private int errorCount;
    private int sampleCount;
    private String analysisFeedback = "";
    private String testName;

    /**
     * Check mean response time threshold.
     */
    public void checkMeanResTimeLimit(double meanResTimeLimit) {
        if (meanResTimeLimit < pct1ResTime) {
            double exceededMeanResTime = pct1ResTime - meanResTimeLimit;
            analysisFeedback += " You caused " + exceededMeanResTime + "ms higher in mean response time.\n";

        }
    }

    /**
     * Check error rate threshold.
     */
    public void checkErrorLimit(int errorRateLimit) {
        if (errorRateLimit < getErrorRate()) {
            double exceededErrorRate = getErrorRate() - errorRateLimit;
            analysisFeedback += " You caused " + exceededErrorRate + "% higher in errors.\n";
        }
    }

    /**
     * Generate a feedback message based on test analysis.
     */
    public void generateAnalysisFeedback() {
        if ("".equals(analysisFeedback)) {
            System.out.println("You have successfully passed the default profiling threshold for " + testName);
        }
        System.out.println(analysisFeedback);
    }

    /**
     * Generate an analysis of the test result.
     */
    public void generateCompleteAnalysis() {
        System.out.print(formatAnalysis());
    }

    public double getErrorRate() {
        return 1.0 * errorCount / sampleCount;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    private String formatAnalysis() {
        return testName + ": " + sampleCount + " samples, throughput: " + throughput + " mean res time: " + meanResTime
                + " 90th Percentile: " + pct1ResTime + " Err: " + errorCount + " (" + getErrorRate() + "%)\n";
    }
}