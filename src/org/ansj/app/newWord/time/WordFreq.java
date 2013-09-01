package org.ansj.app.newWord.time;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import love.cq.util.CollectionUtil;

/**
 * 计算每个字的出现频率，通过排序，可以找到stop words
 * @author qiuqiang
 *
 */
public class WordFreq {
	
	public String sourcePath;
	public String destPath;
	
	public WordFreq(String sourcePath, String destPath){
		this.sourcePath = sourcePath;
		this.destPath = destPath;
	}
	
	public void compute() throws IOException{
		HashMap<Character, Integer> wordFreqs = new HashMap<Character, Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(this.sourcePath));
		String line = null;
		while((line=reader.readLine())!=null){
			for(int i=0;i<line.length();i++){
				char c = line.charAt(i);
				if(wordFreqs.containsKey(c)){
					wordFreqs.put(c, 1+wordFreqs.get(c));
				}else{
					wordFreqs.put(c, 1);
				}
			}
		}
		
		List<Entry<Character, Integer>> result = CollectionUtil.sortMapByValue(wordFreqs, 1);
		BufferedWriter writer = new BufferedWriter(new FileWriter(this.destPath));
		
		for(Entry<Character, Integer> entry: result){
			writer.write(entry.getKey()+"\t"+entry.getValue()+"\n");
		}
		writer.close();
		reader.close();
		
	}
	
	public static void main(String[] args) throws IOException{
		WordFreq freq = new WordFreq("/home/qiuqiang/weiboData/20120206.minus","/home/qiuqiang/weiboData/word.freq");
		freq.compute();
		
//		File dir = new File("/home/qiuqiang/weiboData/trainData");
//		File[] files = dir.listFiles();
//		for(File file: files){
//			System.out.println(file.getName());
//		}
	}

}
