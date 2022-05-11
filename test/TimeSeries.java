package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TimeSeries {
	public HashMap<String , Float> map = new HashMap<String, Float>;
	private int numOfLines;
	public StringBuilder builder = new StringBuilder();

	public TimeSeries(String csvFileName) {

		try (BufferedReader buffer = new BufferedReader(new FileReader(csvFileName)))
		{
			String str;
			while ((str = buffer.readLine()) != null)
			{
				if(numOfLines == 0)
				{
					builder.append(str).append("\n");

					map.put(str);
				}

			}
		}

		// Catch block to handle the exceptions
		catch (IOException e) {
			e.printStackTrace();// Print the line number here exception occured
								// using printStackTrace() method
		}

	}


	}
	
}
