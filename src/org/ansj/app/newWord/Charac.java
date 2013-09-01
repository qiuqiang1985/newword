package org.ansj.app.newWord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Charac {
	
	/**
	 * 判断所有的字符是否一致
	 * @param str
	 * @return
	 */
	public static boolean allTheSame(String str){
		int length = str.length();
		for(int i=0;i<length-1;i++){
			if(str.charAt(i)!=str.charAt(i+1)){
				return false;
			}
		}
		return true;
	}
	
	public static void main(String[] args) throws IOException{
		Pattern pattern= Pattern.compile("｜|＞|／|←|→|［|］|％|～|＊|〇|｛|｝|＝|＋|＆|·");
		BufferedReader reader = new BufferedReader(new FileReader("/home/qiuqiang/resultword.txt"));
		BufferedWriter writer = new BufferedWriter(new FileWriter("/home/qiuqiang/cleanword.txt"));
		
		String line=null;
		int count = 0;
		while((line=reader.readLine())!=null){
			String[] test = line.split("\t");
			String splits = test[0].trim();
			int freq = Integer.parseInt(test[1].trim());
			if(allTheSame(splits)){ //去除所有字符相同的字符串郑洁
				System.out.println(line);
				continue;
			}
			if(splits.length()==2){ //如果是两个的字符串，判断是否里面含有过滤字符
				 Matcher matcher = pattern.matcher(splits);
				 if(matcher.find()){
					 System.out.println(line);
					 count++;
				 }else{
					 writer.write(line+"\n");
				 }
			}else{
				writer.write(line+"\n");
			}
		}
		
		reader.close();
		writer.close();
		System.out.println(count);
	}

}
