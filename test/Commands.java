package test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
        this.dio=dio;
    }

    // Default IO interface
    public interface DefaultIO{
        public String readText();
        public void write(String text);
        public float readVal();
        public void write(float val);

    }

    // helper classes:
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
            BufferedWriter write = new BufferedWriter(new FileWriter(filename));
            while (true) {
                String info = dio.readText();
                if (info.equalsIgnoreCase("done")) {
                    dio.write("Upload complete.\n");
                    break;
                }
                write.write(String.format("%s\n",info));
            }
            write.close();
        }
    }


    // Command abstract class
    public abstract class Command{
        protected String description;

        public Command(String description) {
            this.description=description;
        }

        public abstract void execute() throws IOException;
    }


    public class UploadTimeSeriesCsvFile extends Command{

        public UploadTimeSeriesCsvFile() {
            super("upload a time series csv file");
        }

        @Override
        public void execute() throws IOException {
            dio.write(("Please upload your local train CSV file.\n"));
            sharedState.writeToFile("./trainAnomaly.csv");
            sharedState.setTrainTimeSeries(new TimeSeries("./trainAnomaly.csv"));
            dio.write("upload complete");

            dio.write("Please upload your local test CSV file.\n");
            sharedState.writeToFile("./testAnomaly.csv");
            sharedState.setTestTimeSeries(new TimeSeries("./testAnomaly.csv"));

            dio.write("upload complete");
        }
    }

    public class algorithm_settings extends Command{

        public algorithm_settings() {
            super("Algorithm settings");
        }

        @Override
        public void execute() {

            dio.write("The current correlation threshold is 0.9\n" +
                    "Type a new threshold");

            while (true) {
                try {
                    float newThreshold = Float.parseFloat(dio.readText());
                    if (!(newThreshold >= 0 && newThreshold <= 1))
                        throw new NumberFormatException();
                    sharedState.setCurrentThreshold(newThreshold);
                    break;
                } catch (NumberFormatException ex) {
                    dio.write("please choose a value between 0 and 1.\n");
                }
            }
        }
    }

    public class detect_anomalies extends Command{

        public detect_anomalies() {
            super("Detect anomalies");
        }

        @Override
        public void execute() {
            sharedState.getAnomalyDetector().learnNormal(sharedState.getTrainTimeSeries());
            sharedState.setCurrentDetections(sharedState.getAnomalyDetector().detect(sharedState.getTestTimeSeries()));
            dio.write("anomaly detection complete.\n");
        }
    }

    public class display_results extends Command{

        public display_results() {
            super("Display results");
        }

        @Override
        public void execute() {
            for(AnomalyReport anomalyReport : sharedState.currentDetections)
                dio.write(anomalyReport.timeStep + "\t" + " " + anomalyReport.description + "\n");
            dio.write("Done.\n");
        }
    }

    public class upload_anomalies_and_analyze_results extends Command{

        public upload_anomalies_and_analyze_results() {
            super("Upload anomalies and analyze results");
        }

        private List<Long[]> saveAllRanges() {
            List<Long[]> pairs = new ArrayList<>();
            String range = dio.readText();

            while (!range.equals("done")) {
                String[] split = range.split(",");
                try {
                    pairs.add(new Long[]{Long.parseLong(split[0]), Long.parseLong(split[1])});
                    range = dio.readText();
                } catch (NullPointerException ex) {
                    dio.write("Invalid range format was given\n");
                }
            }
            return pairs;
        }
        private int calNegativeRange(int originalRowNum, List<Long[]> analyzeLineRangePairs) {
            int negativeCounter = originalRowNum;
            for (Long[] range : analyzeLineRangePairs)
                negativeCounter -= (range[1] - range[0] + 1);
            return negativeCounter;
        }

        //union Ranges By Description And Time step
        private List<Long[]> unionRanges(Map<String, List<AnomalyReport>> detections)
        {
            List<Long[]> rowRanges = new ArrayList<>();
            for (Map.Entry<String, List<AnomalyReport>> description : detections.entrySet()) {
                List<AnomalyReport> currentReports = description.getValue();
                rowRanges.add(new Long[]{currentReports.get(0).timeStep, currentReports.get(currentReports.size() - 1).timeStep});
            }
            return rowRanges;
        }

        private float parseDoubleValue(float value) {
            String format = String.format("%f", value);
            String decimal = format.substring(format.lastIndexOf("."), format.lastIndexOf(".") + 4);
            String EndNum = format.substring(0, format.indexOf('.')) + decimal;
            // Important to parse to float since it rounds up the decimal points with zeros
            return Float.parseFloat(EndNum);
        }

        private void WriteTP(float truePositive, int positiveCounter) {
            dio.write("True Positive Rate: " + parseDoubleValue((truePositive / positiveCounter)) + '\n');
        }

        private void WriteFN(float falsePositive, int negativeCounter) {
            dio.write("False Positive Rate: " + parseDoubleValue((falsePositive / negativeCounter)) + '\n');
        }

        private int getAllTruePositives(List<Long[]> analyzeLineRangePairs, List<Long[]> groupedRanges) {
            // Iterate over the given ranges and check if they were detected
            int truePositive = 0;
            for (Long[] requiredRange : analyzeLineRangePairs) {
                ValueRange range = ValueRange.of(requiredRange[0], requiredRange[1]);
                for (Long[] detectedRange : groupedRanges) {
                    // Validate that there were true positives in the range
                    if ((int) LongStream.range(detectedRange[0], detectedRange[1]).filter(range::isValidValue).count() > 0)
                        truePositive++;
                }
            }
            return truePositive;
        }

        @Override
        public void execute() throws IOException {
            dio.write("Please upload your local anomalies file");
            float truePositive = 0 , falsePositive = 0;

            List<Long[]>analyzeRanges = saveAllRanges();
            dio.write("Upload Comlete.\n");

            Map<String, List<AnomalyReport>> detections = sharedState.getCurrentDetections().stream().collect(Collectors.groupingBy(w ->w.description));
            List<Long[]> groupedRanges = unionRanges(detections);
            truePositive = getAllTruePositives(analyzeRanges, groupedRanges);
            falsePositive = groupedRanges.size()- truePositive;
            int dataRowNumber = sharedState.getTestTimeSeries().map.entrySet().iterator().next().getValue().size() - 1;
            WriteTP(truePositive, analyzeRanges.size());
            WriteFN(falsePositive, calNegativeRange(dataRowNumber, analyzeRanges));
        }
    }

    public class exit extends Command{

        public exit() {
            super("Exit");
        }

        @Override
        public void execute() {
        }
    }

}
