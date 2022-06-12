package test;

import java.io.File;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;

public class TimeSeries {
	public HashMap<String, ArrayList<Float>> map = new HashMap<>();
	public String[] keys;

	public TimeSeries(String csvFileName)
	{
		String line;
		String splitBy = ",";
		try {
			BufferedReader in = new BufferedReader(new FileReader(csvFileName));
			Scanner myScanner = new Scanner(new File(csvFileName));
			line = in.readLine();
			keys = line.split(splitBy); //saves the keys in array of strings
			for(String k:keys)
				map.put(k, new ArrayList<Float>());

			while ((line = in.readLine()) != null)//insert values to keys
			{
				float value = 0;
				String[] row = line.split(splitBy);
				for (int i = 0; i < keys.length; i++) {
					value = Float.parseFloat(row[i]);
					map.get(keys[i]).add(value);
				}
			}
			in.close();
		}
		catch(Exception e){
			System.out.println(e);
		}
	}

	public float[] getValues(String key)
	{
		ArrayList<Float> column = this.map.get(key);
		float[] arr = new float[column.size()];
		for(int i=0; i<arr.length; i++)
			arr[i] = column.get(i);
		return arr;
	}

	public HashMap<String, ArrayList<Float>> getMap() {
		return map;
	}

	public String[] getKeys() {
		return keys;
	}
}

