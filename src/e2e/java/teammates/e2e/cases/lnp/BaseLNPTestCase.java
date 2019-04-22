package teammates.e2e.cases.lnp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringJoiner;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.report.dashboard.ReportGenerator;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import teammates.common.datatransfer.DataBundle;
import teammates.common.exception.TeammatesException;
import teammates.common.util.JsonUtils;
import teammates.common.util.Logger;
import teammates.e2e.util.BackDoor;
import teammates.e2e.util.LNPResultsStatistics;
import teammates.e2e.util.LNPTestData;
import teammates.e2e.util.TestProperties;
import teammates.test.cases.BaseTestCase;

/**
 * Base class for all L&P test cases.
 */
public abstract class BaseLNPTestCase extends BaseTestCase {

    protected static final String GET = HttpGet.METHOD_NAME;
    protected static final String POST = HttpPost.METHOD_NAME;
    protected static final String PUT = HttpPut.METHOD_NAME;
    protected static final String DELETE = HttpDelete.METHOD_NAME;

    private static final Logger log = Logger.getLogger();

    protected abstract LNPTestData getTestData();

    /**
     * Returns the JMeter test plan for the L&P test case.
     * @return A nested tree structure that consists of the various elements that are used in the JMeter test.
     */
    protected abstract ListedHashTree getLnpTestPlan();

    /**
     * Returns the path to the generated JSON data bundle file.
     */
    protected String getJsonDataPath() {
        return "/" + getClass().getSimpleName() + ".json";
    }

    /**
     * Returns the limit of the ratio of the error samples against total number of samples.
     */
    protected abstract double getErrorRateLimit();

    /**
     * Returns the maximum allowable threshold for the mean response time (in seconds) for the test endpoint.
     */
    protected abstract double getMeanRespTimeLimit();

    /**
     * Returns the path to the generated JMeter CSV config file.
     */
    protected String getCsvConfigPath() {
        return "/" + getClass().getSimpleName() + "Config.csv";
    }

    /**
     * Returns the path to the generated JTL test results file.
     */
    protected String getJtlResultsPath() {
        return "/" + getClass().getSimpleName() + ".jtl";
    }

    @Override
    protected String getTestDataFolder() {
        return TestProperties.LNP_TEST_DATA_FOLDER;
    }

    /**
     * Returns the path to the data file, relative to the project root directory.
     */
    protected String getPathToTestDataFile(String fileName) {
        return getTestDataFolder() + fileName;
    }

    /**
     * Returns the path of the renamed JSON file that contains the test results statistics.
     */
    private String getPathToTestStatisticsResultsFile() {
        return TestProperties.LNP_TEST_RESULTS_FOLDER + "/" + getClass().getSimpleName() + "Statistics.json";
    }

    private String createFileAndDirectory(String directory, String fileName) throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdir();
        }

        String pathToFile = directory + fileName;
        File file = new File(pathToFile);

        // Write data to the file; overwrite if it already exists
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        return pathToFile;
    }

    /**
     * Creates the JSON data and writes it to the file specified by {@link #getJsonDataPath()}.
     */
    private void createJsonDataFile(LNPTestData testData) throws IOException {
        DataBundle jsonData = testData.generateJsonData();

        String pathToResultFile = createFileAndDirectory(TestProperties.LNP_TEST_DATA_FOLDER, getJsonDataPath());
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(pathToResultFile))) {
            bw.write(JsonUtils.toJson(jsonData));
            bw.flush();
        }
    }

    /**
     * Creates the CSV data and writes it to the file specified by {@link #getCsvConfigPath()}.
     */
    private void createCsvConfigDataFile(LNPTestData testData) throws IOException {
        List<String> headers = testData.generateCsvHeaders();
        List<List<String>> valuesList = testData.generateCsvData();

        String pathToCsvFile = createFileAndDirectory(TestProperties.LNP_TEST_DATA_FOLDER, getCsvConfigPath());
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(pathToCsvFile))) {
            // Write headers and data to the CSV file
            bw.write(convertToCsv(headers));

            for (List<String> values : valuesList) {
                bw.write(convertToCsv(values));
            }

            bw.flush();
        }
    }

    /**
     * Converts the list of {@code values} to a CSV row.
     * @return A single string containing {@code values} separated by pipelines and ending with newline.
     */
    private String convertToCsv(List<String> values) {
        StringJoiner csvRow = new StringJoiner("|", "", "\n");
        for (String value : values) {
            csvRow.add(value);
        }
        return csvRow.toString();
    }

    /**
     * Returns the L&P test results statistics.
     * @return The initialized result analysis from the L&P test results.
     * @throws IOException if there is an error when loading the result file.
     */
    private LNPResultsStatistics getResultsStatistics() throws IOException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(Files.newBufferedReader(Paths.get(getPathToTestStatisticsResultsFile())));
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

        JsonObject endPointAnalysis = jsonObject.getAsJsonObject("Test Endpoint");
        return gson.fromJson(endPointAnalysis, LNPResultsStatistics.class);
    }

    /**
     * Renames the default statistics.json file to the name of the test.
     */
    private boolean renameStatisticsFile() {
        File defaultFile = new File(TestProperties.LNP_TEST_RESULTS_FOLDER + "/statistics.json");
        File lnpStatisticsFile = new File(getPathToTestStatisticsResultsFile());

        if (lnpStatisticsFile.exists()) {
            lnpStatisticsFile.delete();
        }
        return defaultFile.renameTo(lnpStatisticsFile);
    }


    /**
     * Setup and load the JMeter configuration and property files to run the Jmeter test.
     * @throws IOException if the save service properties file cannot be loaded.
     */
    private void setJmeterProperties() throws IOException {
        JMeterUtils.loadJMeterProperties(TestProperties.JMETER_PROPERTIES_PATH);
        JMeterUtils.setJMeterHome(TestProperties.JMETER_HOME);
        JMeterUtils.initLocale();
        SaveService.loadProperties();
    }

    /**
     * Creates the JSON test data and CSV config data files for the performance test from {@code testData}.
     */
    protected void createTestData() {
        LNPTestData testData = getTestData();
        try {
            createJsonDataFile(testData);
            createCsvConfigDataFile(testData);
        } catch (IOException ex) {
            log.severe(TeammatesException.toStringWithStackTrace(ex));
        }
    }

    /**
     * Creates the entities in the datastore from the JSON data file.
     */
    protected void persistTestData() {
        DataBundle dataBundle = loadDataBundle(getJsonDataPath());
        BackDoor.removeAndRestoreDataBundle(dataBundle);
    }

    /**
     * Display the L&P results on the console.
     */
    protected void displayLnpResults() throws IOException {
        LNPResultsStatistics reportAnalysis = getResultsStatistics();
        reportAnalysis.setTestName(getClass().getSimpleName());

        reportAnalysis.generateResultsStatistics();
        reportAnalysis.displayLnpResultsStatistics(getErrorRateLimit(), getMeanRespTimeLimit());
    }

    /**
     * Runs the JMeter test.
     * @param shouldCreateJmxFile true if the generated test plan should be saved to a `.jmx` file which
     *                            can be opened in the JMeter GUI, and false otherwise.
     */
    protected void runJmeter(boolean shouldCreateJmxFile) throws IOException {
        StandardJMeterEngine jmeter = new StandardJMeterEngine();
        setJmeterProperties();

        HashTree testPlan = getLnpTestPlan();

        if (shouldCreateJmxFile) {
            String pathToConfigFile = createFileAndDirectory(
                    TestProperties.LNP_TEST_CONFIG_FOLDER, "/" + getClass().getSimpleName() + ".jmx");
            SaveService.saveTree(testPlan, Files.newOutputStream(Paths.get(pathToConfigFile)));
        }

        // Add result collector to the test plan for generating results file
        Summariser summariser = null;
        String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
        if (summariserName.length() > 0) {
            summariser = new Summariser(summariserName);
        }

        String resultsFile = createFileAndDirectory(TestProperties.LNP_TEST_RESULTS_FOLDER, getJtlResultsPath());
        ResultCollector resultCollector = new ResultCollector(summariser);
        resultCollector.setFilename(resultsFile);
        testPlan.add(testPlan.getArray()[0], resultCollector);

        jmeter.configure(testPlan);
        jmeter.run();

        try {
            ReportGenerator reportGenerator = new ReportGenerator(resultsFile, null);
            reportGenerator.generate();
        } catch (Exception e) {
            log.warning(e.getMessage());
        }
        if (!renameStatisticsFile()) {
            log.warning("Failed in renaming original file.");
        }
    }

    /**
     * Deletes the data that was created in the datastore from the JSON data file.
     */
    protected void deleteTestData() {
        DataBundle dataBundle = loadDataBundle(getJsonDataPath());
        BackDoor.removeDataBundle(dataBundle);
    }

    /**
     * Deletes the JSON and CSV data files that were created.
     */
    protected void deleteDataFiles() throws IOException {
        String pathToJsonFile = getPathToTestDataFile(getJsonDataPath());
        String pathToCsvFile = getPathToTestDataFile(getCsvConfigPath());

        Files.delete(Paths.get(pathToJsonFile));
        Files.delete(Paths.get(pathToCsvFile));
    }

    /**
     * Sanitize the string to be CSV-safe string.
     */
    protected String sanitizeForCsv(String originalString) {
        return String.format("\"%s\"", originalString.replace(System.lineSeparator(), "").replace("\"", "\"\""));
    }

}
