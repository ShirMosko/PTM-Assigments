package test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class Commands {

    // the default IO to be used in all commands
    DefaultIO dio;
    private SharedState sharedState = new SharedState();

    public Commands(DefaultIO dio) {
        this.dio = dio;
    }

    // you may add other helper classes here


    // Default IO interface
    public interface DefaultIO {
        public String readText();

        public void write(String text);

        public float readVal();

        public void write(float val);

    }

    // the shared state of all commands
    private class SharedState {
        private final SimpleAnomalyDetector anomalyDetector = new SimpleAnomalyDetector();
        private float currentThreshold = 0.9F;
        private TimeSeries trainTimeSeries;
        private TimeSeries testTimeSeries;
        private List<AnomalyReport> currentDetections;

        // -------- Getters ---------------//
        public float getCurrentThreshold() {
            return currentThreshold;
        }

        // -------- Setters ---------------//
        public void setCurrentThreshold(float newThreshold) {
            this.currentThreshold = newThreshold;
        }

        ;

        public TimeSeries getTestTimeSeries() {
            return testTimeSeries;
        }

        ;

        public void setTestTimeSeries(TimeSeries timeSeries) {
            this.testTimeSeries = timeSeries;
        }

        ;

        public TimeSeries getTrainTimeSeries() {
            return trainTimeSeries;
        }

        ;

        public void setTrainTimeSeries(TimeSeries timeSeries) {
            this.trainTimeSeries = timeSeries;
        }

        ;

        public SimpleAnomalyDetector getAnomalyDetector() {
            return anomalyDetector;
        }

        ;

        public List<AnomalyReport> getCurrentDetections() {
            return currentDetections;
        }

        ;

        public void setCurrentDetections(List<AnomalyReport> detections) {
            currentDetections = detections;
        }

        ;

        public void writeToFile(String filename) throws IOException {
            BufferedWriter pw = new BufferedWriter(new FileWriter(filename));
            while (true) {
                String data = dio.readText();
                if (data.equalsIgnoreCase("done")) {
                    dio.write("Upload complete.\n");
                    break;
                }
                pw.write(String.format("%s\n", data));
            }
            pw.close();
        }
    }

    // Command abstract class
    public abstract class Command {
        protected String description;

        public Command(String description) {
            this.description = description;
        }

        public abstract void execute() throws IOException;
    }

    public class UploadTimeSeriesCsvFile extends Command {

        /**
         * Command class for uploading a time series CSV file
         */
        public UploadTimeSeriesCsvFile() {
            super("upload a time series csv file");
        }

        @Override
        public void execute() throws IOException {
            // User instruction
            dio.write("Please upload your local train CSV file.\n");
            // Read the train anomaly data
            sharedState.writeToFile("./trainAnomaly.csv");
            sharedState.setTrainTimeSeries(new TimeSeries("./trainAnomaly.csv"));
            // Read the test anomaly data
            dio.write("Please upload your local test CSV file.\n");
            sharedState.writeToFile("./testAnomaly.csv");
            sharedState.setTestTimeSeries(new TimeSeries("./testAnomaly.csv"));
        }
    }

    public class ChangeAlgorithmSettings extends Command {

        public ChangeAlgorithmSettings() {
            super("algorithm settings");
        }

        @Override
        public void execute() {
            dio.write(String.format("The current correlation threshold is %f\n", sharedState.currentThreshold));
            dio.write("Type a new threshold\n");
            while (true) {
                try {
                    float newThreshold = Float.parseFloat(dio.readText());
                    // Validate that the new threshold is in the range of (0,1)
                    if (!(newThreshold >= 0 && newThreshold <= 1))
                        throw new NumberFormatException();
                    sharedState.setCurrentThreshold(newThreshold);
                    // The new value is valid
                    break;
                } catch (NumberFormatException ex) {
                    dio.write("please choose a value between 0 and 1.\n");
                }
            }

        }
    }

    public class DetectAnomalies extends Command {

        public DetectAnomalies() {
            super("detect anomalies");
        }

        @Override
        public void execute() {
            sharedState.getAnomalyDetector().learnNormal(sharedState.getTrainTimeSeries());
            sharedState.setCurrentDetections(sharedState.getAnomalyDetector().detect(sharedState.getTestTimeSeries()));
            dio.write("anomaly detection complete.\n");
        }
    }

    public class DisplayResults extends Command {

        public DisplayResults() {
            super("display results");
        }

        @Override
        public void execute() {
            for (AnomalyReport ar : sharedState.currentDetections)
                dio.write(ar.timeStep + "\t" + " " + ar.description + '\n');
            dio.write("Done.\n");
        }
    }

    public class AnalyzeAnomalies extends Command {

        public AnalyzeAnomalies() {
            super("upload anomalies and analyze results");
        }

        private List<Long[]> storeAllRanges() {
            List<Long[]> lineRangePairs = new ArrayList<>();
            String range = dio.readText();
            // Retrieve all user input line ranges
            while (!range.equals("done")) {
                String[] splitter = range.split(",");
                try {
                    lineRangePairs.add(new Long[]{Long.parseLong(splitter[0]), Long.parseLong(splitter[1])});
                    range = dio.readText();
                } catch (NullPointerException ex) {
                    // Validate the format of the given range
                    dio.write("Invalid range format was given\n");
                }
            }
            return lineRangePairs;
        }

        private int calculateNegativeRange(int originalRowNum, List<Long[]> analyzeLineRangePairs) {
            int negativeCounter = originalRowNum;
            for (Long[] range : analyzeLineRangePairs)
                negativeCounter -= (range[1] - range[0] + 1);
            return negativeCounter;
        }

        private List<Long[]> groupRangesByDetections(Map<String, List<AnomalyReport>> groupedDetections) {
            List<Long[]> rowRanges = new ArrayList<>();
            for (Map.Entry<String, List<AnomalyReport>> description : groupedDetections.entrySet()) {
                // Iterate over all AnomalyReport objects and get range of timesteps
                List<AnomalyReport> currentReports = description.getValue();
                // Append the first and last report row range
                rowRanges.add(new Long[]{currentReports.get(0).timeStep, currentReports.get(currentReports.size() - 1).timeStep});
            }
            return rowRanges;
        }

        private float parseDoubleValue(float value) {
            String format = String.format("%f", value);
            String decimal = format.substring(format.lastIndexOf("."), format.lastIndexOf(".") + 4);
            String finalNum = format.substring(0, format.indexOf('.')) + decimal;
            // Important to parse to float since it rounds up the decimal points with zeros
            return Float.parseFloat(finalNum);
        }

        private void calculateTP(float truePositive, int positiveCounter) {
            dio.write("True Positive Rate: " + parseDoubleValue((truePositive / positiveCounter)) + '\n');
        }

        private void calculateFN(float falsePositive, int negativeCounter) {
            dio.write("False Positive Rate: " + parseDoubleValue((falsePositive / negativeCounter)) + '\n');
        }

        private int getAllTruePositives(List<Long[]> analyzeLineRangePairs, List<Long[]> groupedRangesByDetections) {
            // Iterate over the given ranges and check if they were detected
            int truePositive = 0;
            for (Long[] requiredRange : analyzeLineRangePairs) {
                ValueRange range = ValueRange.of(requiredRange[0], requiredRange[1]);
                for (Long[] detectedRange : groupedRangesByDetections) {
                    // Validate that there were true positives in the range
                    if ((int) LongStream.range(detectedRange[0], detectedRange[1]).filter(range::isValidValue).count() > 0)
                        truePositive++;
                }
            }
            return truePositive;
        }

        @Override
        public void execute() throws IOException {
            dio.write("Please upload your local anomalies file.\n");
            float truePositive = 0, falsePositive = 0;
            // Store all the ranges in a Map format of -> { start: end} inputted by the user
            List<Long[]> analyzeLineRangePairs = storeAllRanges();
            dio.write("Upload complete.\n");
            // Group all the same detection descriptions together, format -> { description: List of AnomalyReport Objects}
            Map<String, List<AnomalyReport>> groupedDetections =
                    sharedState.getCurrentDetections()
                            .stream()
                            .collect(Collectors.groupingBy(w -> w.description));
            // Store all the line ranges of the timeSteps in the detections in order to count true positives, format -> [[start,end], [start,end]]
            List<Long[]> groupedRangesByDetections = groupRangesByDetections(groupedDetections);
            truePositive = getAllTruePositives(analyzeLineRangePairs, groupedRangesByDetections);
            // Get the false positives depending on the amount of positive ones that were found
            falsePositive = groupedRangesByDetections.size() - truePositive;
            // Remove 1 in order to not count the indexes row
            int dataRowNumber = sharedState.getTestTimeSeries().map.entrySet().iterator().next().getValue().size() - 1;
            calculateTP(truePositive, analyzeLineRangePairs.size());
            calculateFN(falsePositive, calculateNegativeRange(dataRowNumber, analyzeLineRangePairs));
        }
    }

    public class Exit extends Command {

        public Exit() {
            super("exit");
        }

        @Override
        public void execute() {
            // TODO: display results of detection
        }
    }

}