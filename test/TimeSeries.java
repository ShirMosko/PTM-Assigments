package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TimeSeries {
	public HashMap<String, ArrayList<Float>> map = new HashMap<>();

	public TimeSeries(String csvFileName) throws IOException
	{
		String line;
		String splitBy = ",";
		String[] keys;
		Scanner myScanner = null;

		try {
			BufferedReader in = new BufferedReader(new FileReader("cvsFileName"));
			myScanner = new Scanner((in));
			line = in.readLine();
			keys = line.split(splitBy); //saves the keys in array of strings

			while (myScanner.hasNext()) {
				map.put(myScanner.next(), new ArrayList<Float>()); //insert keys to the hash map
			}

			while ((line = in.readLine()) != null)//insert values to keys
			{
				for (String key: keys) {
					float value = myScanner.nextFloat();
					map.get(key).add(value);
				}
			}
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
}


	

