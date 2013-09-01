package org.ansj.app.newWord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import love.cq.util.CollectionUtil;
import love.cq.util.StringUtil;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;

public class nextParser {
	
	public static void main(String[] args) throws IOException{
		
		BufferedReader reader = new BufferedReader(new FileReader("/home/qiuqiang/weibo.txt"));
//		BufferedWriter writer = new BufferedWriter(new FileWriter("/home/qiuqiang/word_freq.txt"));
		
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		
		String content = "《我和你》这一手歌";
		
//		List<Term> parser = NlpAnalysis.parse(StringUtil.rmHtmlTag(content));
//		System.out.println(parser);
//		System.exit(0);
		while((content = reader.readLine())!=null){
			List<Term> parser = BaseAnalysis.parse(StringUtil.rmHtmlTag(content));
			
			for(Term term: parser){
				if(!result.containsKey(term.getName())){
					result.put(term.getName(), 1);
				}else{
					result.put(term.getName(), 1+result.get(term.getName()));
				}
			}
		}
		
//		List<Entry<String, Integer>> sortResult = CollectionUtil.sortMapByValue(result, -1);
//		
//		for(Entry<String, Integer> entry: sortResult){
//			writer.write(entry+"\n");
//		}
//		
//		writer.close();
		
		
		reader.close();
		
	}

}
