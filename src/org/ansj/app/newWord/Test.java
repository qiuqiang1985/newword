package org.ansj.app.newWord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Comparator;

class V{
	String key;
	int value;
	
	public V(){}
	
	public V(String key, int value){
		this.key = key;
		this.value = value;
	}
	
	public String toString(){
		return this.key+"\t"+this.value;
	}
}

public class Test {
	
	
	
	public static void main(String[] args) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader("/home/qiuqiang/newword.txt"));
		HashMap<String, Integer> results = new HashMap<String, Integer>();
		String line=null;
		int count = 0;
		while((line=reader.readLine())!=null){
			count++;
			String[] splits = line.split("\t");
			if(!results.containsKey(splits[0])){
				results.put(splits[0], 1);
			}else{
				results.put(splits[0], 1+results.get(splits[0]));
			}
		}
		reader.close();
		System.out.println("count:"+count+", result size:"+results.size());
		List<V> tmpList = new ArrayList<V>();
		for(Entry<String, Integer> entry: results.entrySet()){
			tmpList.add(new V(entry.getKey(), entry.getValue()));
		}
		
		Collections.sort(tmpList, new Comparator<V>(){

			@Override
			public int compare(V o1, V o2) {
				return o1.value-o2.value>0?-1:1;
			}
			
		});
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("/home/qiuqiang/resultword.txt"));
		for(V v: tmpList){
			writer.write(v.toString()+"\n");
		}
		writer.flush();
		writer.close();
	}

}
