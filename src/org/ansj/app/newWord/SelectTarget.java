package org.ansj.app.newWord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 选择最好的多个字
 * @author qiuqiang
 *
 */

class TT{
	String name;
	int freq;
	double score;
	
	public TT(String name,int freq, double score){
		this.name = name;
		this.freq = freq;
		this.score = score;
	}
	
	public String toString(){
		return this.name+"\t"+this.freq+"\t"+this.score;
	}
	
}

public class SelectTarget {
	
	public static void main(String[] args) throws IOException{
		
		BufferedReader reader = new BufferedReader(new FileReader("/home/qiuqiang/weibo_nw.txt"));
		HashMap<String, ArrayList<Double>> results = new HashMap<String, ArrayList<Double>>();
		String line=null;
		int count = 0;
		while((line=reader.readLine())!=null){
			count++;
			String[] splits = line.split("=");
			double tmpDouble = Double.parseDouble(splits[1]);
			String key = splits[0].trim();
			if(!results.containsKey(key)){
				results.put(key, new ArrayList<Double>());
			}
			results.get(key).add(tmpDouble);
		}
		reader.close();
		
		System.out.println("All Count:"+count);
		
		List<TT> distribution = new ArrayList<TT>();
		ArrayList<Double> tmpList = null;
		for(Entry<String, ArrayList<Double>> entry: results.entrySet()){
			tmpList = entry.getValue();
			int freq = tmpList.size();
			double sum = 0;
			for(Double d: tmpList){
				sum += d;
			}
			double score = sum/freq;
			
			distribution.add(new TT(entry.getKey(),freq, score));
		}
		
		System.out.println("unique keys:"+distribution.size());
		Collections.sort(distribution, new Comparator<TT>(){

			@Override
			public int compare(TT arg0, TT arg1) {
				return arg0.score-arg1.score>0?1:-1;
			}
		});
		
		BufferedWriter out1 = new BufferedWriter(new FileWriter("/home/qiuqiang/word_result/wordresult.txt"));
		BufferedWriter out2 = new BufferedWriter(new FileWriter("/home/qiuqiang/word_result/wordresult_2.txt"));
		BufferedWriter out3 = new BufferedWriter(new FileWriter("/home/qiuqiang/word_result/wordresult_3.txt"));
		BufferedWriter out4 = new BufferedWriter(new FileWriter("/home/qiuqiang/word_result/wordresult_4.txt"));
		BufferedWriter out5 = new BufferedWriter(new FileWriter("/home/qiuqiang/word_result/wordresult_5.txt"));
		
//		List<TT> word2 = new ArrayList<TT>(); 
//		List<TT> word3 = new ArrayList<TT>();
//		List<TT> word4 = new ArrayList<TT>();
//		List<TT> others = new ArrayList<TT>();
		
		Pattern pattern= Pattern.compile("｜|＞|／|←|→|［|］|％|～|＊|〇|｛|｝|点|＝|＋|＆|·|中|正|访|全|才|右|头|加|年|小|时|外|老|求|太|取|某|的|少|了|一|上|下|子|总|先|便|南|西|东|北|转|刚|越|第|处|非|位|低|式|倒|常|阿|含|极|试|欲|般|不|与|且|个|为|乃|么|之|乎|也|于|些|亦|仍|其|都|够|里|似|仅|新|旧|末|先|没|敢|前|后|种|快|失|遇|渐|未|竟|皆|罚|验|怕|撤|暂|扣|队|甚|稍|突|共|略|系|产|需|须|必|专|名|急|赴|办|所|站|姓|喝|人|一|二|三|四|五|男|女|假|真|掉");
		
		HashMap<Character, Integer> charFreq = new HashMap<Character, Integer>();
		
		for(TT tt: distribution){
			if(tt.name.length()==2){
				
				if(true || tt.freq>5 && tt.score<0){
					out2.write(tt.toString()+"\n");
					
				}
				
				
//				char c1 = tt.name.charAt(0);
//				char c2 = tt.name.charAt(1);
//				
//				if(!charFreq.containsKey(c1)){
//					charFreq.put(c1, 0);
//				}
//				
//				if(!charFreq.containsKey(c2)){
//					charFreq.put(c2, 0);
//				}
//				
//				charFreq.put(c1, 1+charFreq.get(c1));
//				charFreq.put(c2, 1+charFreq.get(c2));
//				
//				//对于2个字的进行过滤 stop words过滤
//				Matcher matcher = pattern.matcher(tt.name);
//				if(matcher.find()){
//					out2.write(tt.toString()+"\n");
//				}else{
//					out1.write(tt.toString()+"\n");
//				}
				
			}else if(tt.name.length()==3){
				if(true || tt.freq>5 && tt.score<0){
					out3.write(tt.toString()+"\n");
				}
				
			}else if(tt.name.length()==4){
				
				if(true || tt.freq>5 && tt.score<0){
					out4.write(tt.toString()+"\n");
				}
				
			}else{
				out5.write(tt.toString()+"\n");
			}
		}
		out1.close();
		out2.close();
		out3.close();
		out4.close();
		out5.close();
		
//		for(Entry<Character, Integer> entry: charFreq.entrySet()){
//			System.out.println(entry);
//		}
		
		
	}

}
