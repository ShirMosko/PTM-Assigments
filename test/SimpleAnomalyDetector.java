package test;

import java.util.ArrayList;
import java.util.List;
import  java.util.ArrayList;
public class SimpleAnomalyDetector implements TimeSeriesAnomalyDetector {
	private final Float corDetection = 0.9F;
	List<CorrelatedFeatures> correlativeArray = new ArrayList<>();
	ArrayList<AnomalyReport> reportArray = new ArrayList<>();

	@Override
	public void learnNormal(TimeSeries ts) {
		float threshold;
		String[] keys = ts.getKeys();
		int numOfRows = ts.getMap().size();

		for (int i = 0; i < numOfRows; i++) {
			int col = -1;
			float maxCorrelation = corDetection;
			Boolean isCorrelated = false;

			for (int j = i + 1; j < numOfRows; j++) {
				float[]x = ts.getValues(keys[i]);
				float[]y = ts.getValues(keys[j]);
				float p = Math.abs(StatLib.pearson(x, y));
				if (p > maxCorrelation) {
					maxCorrelation = p;
					col = j;
					isCorrelated = true;
				}
				else
					isCorrelated = false;

				if (isCorrelated) {
					Point[] points = buildPoints(x,y);
					Line linear = StatLib.linear_reg(points);
					threshold = ThreshMax(linear , points);
					CorrelatedFeatures cor = new CorrelatedFeatures(keys[i], keys[col] ,maxCorrelation , linear , threshold);
					correlativeArray.add(cor);
				}
			}
		}
	}


	@Override
	public List<AnomalyReport> detect(TimeSeries ts)
	{
		for (CorrelatedFeatures corFi : correlativeArray){
			float[]f1 = ts.getValues(corFi.feature1);
			float[]f2 = ts.getValues(corFi.feature2);
			Point[] cor_Points = buildPoints(f1, f2);
			int row = 1;

			for (Point p:cor_Points){
				if (StatLib.dev(p, corFi.lin_reg) > corFi.threshold)
					reportArray.add(new AnomalyReport(corFi.feature1 + "-" + corFi.feature2, row));

				row++;
			}
		}
		return reportArray;
	}

	public List<CorrelatedFeatures> getNormalModel() {
		return correlativeArray;
	}

	public Point[] buildPoints(float[] x, float[] y) {
		Point[] pointsArr = new Point[x.length];
		for (int i = 0; i < x.length; i++) {
			pointsArr[i] = new Point(x[i], y[i]);
		}
		return pointsArr;

	}

	public float ThreshMax(Line line, Point[] p)
	{
		float maxThresh = 0;
		for (Point point:p)
		{
			float deviation = StatLib.dev(point, line);
			if (deviation > maxThresh)
				maxThresh = deviation;
		}
		maxThresh *= 1.1F;
		return maxThresh;
	}
}