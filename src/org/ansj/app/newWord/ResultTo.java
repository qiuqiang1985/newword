package org.ansj.app.newWord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;

public class ResultTo {
	
	public static void main(String[] args) throws NumberFormatException, IOException{
		BufferedReader reader = new BufferedReader(new FileReader("/home/qiuqiang/cleanword.txt"));
		BufferedWriter writer = new BufferedWriter(new FileWriter("/home/qiuqiang/workspace/anjs/library/default1.dic"));
		
		String line=null;
		int count = 0;
		while((line=reader.readLine())!=null){
			String[] test = line.split("\t");
			String splits = test[0].trim();
			int freq = Integer.parseInt(test[1].trim());
			if(splits.length()<=6 && freq>100){
				writer.write(splits+"\n");
				count++;
			}
		}
		
		reader.close();
		writer.close();
		System.out.println(count);
	}

}
