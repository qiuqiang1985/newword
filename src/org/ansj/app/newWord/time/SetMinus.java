package org.ansj.app.newWord.time;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.BaseAnalysis;

public class SetMinus {
	
	public String destPath;
	public String setPath;
	public String outPath;
	
	public SetMinus(String destPath, String setPath, String outPath){
		this.destPath = destPath;
		this.setPath = setPath;
		this.outPath = outPath;
	}
	
	public Set<String> getSets(String filePath) throws IOException{
		Set<String> sets = new HashSet<String>();
		
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String line = null;
		while((line=reader.readLine())!=null){
			sets.add(line.trim());
		}
		
		reader.close();
		
		return sets;
	}
	
	public void minus() throws IOException{
		Set<String> one = getSets(this.destPath);
		Set<String> two = getSets(this.setPath);
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(this.outPath));
		for(String str: one){
			if(!two.contains(str)){
				if(str.length()<5 && str.indexOf("，")<0 && str.indexOf("。")<0 && str.indexOf("、")<0 && str.indexOf(",")<0 && str.indexOf("：")<0 && str.indexOf("！")<0 && str.indexOf("？")<0 && str.indexOf("的")<0 && str.indexOf("；")<0 && str.indexOf("】")<0 && str.indexOf(" ")<0 && str.indexOf("“")<0 && str.indexOf(".")<0 && str.indexOf("”")<0 && str.indexOf("#")<0 && str.indexOf("[")<0 && str.indexOf("是")<0 && str.indexOf("我")<0 && str.indexOf("可以")<0 && str.indexOf("你")<0 && str.indexOf("吴")<0 && str.indexOf("邓")<0 && str.indexOf("!")<0 && str.indexOf("_")<0 && str.indexOf("~")<0 && str.indexOf("(")<0 && str.indexOf(")")<0 && str.indexOf(";")<0 && str.indexOf("他")<0 && str.indexOf("她")<0 && str.indexOf("你")<0 && str.indexOf("们")<0){
					List<Term> parsers = BaseAnalysis.parse(str);
					//System.out.println(parsers);
					boolean flag = true;
					for(Term term: parsers){
						if(term.getName().length()>=2 || "w".equalsIgnoreCase(term.getNatrue().natureStr) || "p".equalsIgnoreCase(term.getNatrue().natureStr) || "y".equalsIgnoreCase(term.getNatrue().natureStr) || "d".equalsIgnoreCase(term.getNatrue().natureStr) || "e".equalsIgnoreCase(term.getNatrue().natureStr)){
							flag = false;
							//System.out.println(str);
							break;
						}
					}
					if(flag){
						System.out.println(parsers);
						writer.write(str+"\n");
					}
					
				}
			}
		}
		
		writer.close();
		
	}
	
	
	public static void main(String[] args) throws IOException{
		SetMinus minus = new SetMinus("/home/qiuqiang/weiboData/20120206.ngram", "/home/qiuqiang/weiboData/train.ngram", "/home/qiuqiang/weiboData/20120206.minus");
		minus.minus();
	}

}
