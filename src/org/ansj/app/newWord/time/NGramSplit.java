package org.ansj.app.newWord.time;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;

/**
 * 对某个字符串进行n元切分
 * @author qiuqiang
 *
 */
public class NGramSplit {
	
	public String dirPath;
	public String outPath;
	
	/**
	 * @param dirPath
	 * @param outPath
	 */
	public NGramSplit(String dirPath, String outPath){
		this.dirPath = dirPath;
		this.outPath = outPath;
	}
	
	public static Set<String> ngram(int n, List<Term> terms){
		HashSet<String> result = new HashSet<String>();
		
		for(int i=0;i<=terms.size()-n;i++){
			StringBuilder sb = new StringBuilder();
			for(int j=0;j<n;j++){
				sb.append(terms.get(i+j).getName());
			}
			result.add(sb.toString());
		}
		
		return result;
	}
	
	public void split() throws IOException{
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(this.outPath));
		
		File dir = new File(dirPath);
		File[] files = null;
		if(dir.isFile()){
			files = new File[]{dir};
		}else{
			files = dir.listFiles();
		}
		
		BufferedReader reader = null;
		String line = null;
		List<Term> terms = null;
		
		for(File file: files){
			System.out.println(file.getName());
			try{
				reader = new BufferedReader(new FileReader(file));
				while((line = reader.readLine())!=null){
					 terms = BaseAnalysis.parse(line);
					 if(terms!=null){
						 Set<String> sets = ngram(2, terms);
						 for(String str: sets){
							 writer.write(str+"\n");
						 }
						 sets = ngram(3, terms);
						 for(String str: sets){
							 writer.write(str+"\n");
						 }
						 sets = ngram(4, terms);
						 for(String str: sets){
							 writer.write(str+"\n");
						 }
					 }
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					reader.close();
				}catch(Exception e1){
					//ignore this exception
				}
				
			}
		}
		
		try{
			writer.close();
		}finally{
			//ignore
		}
	}
	
	public static void main(String[] args) throws IOException{
//		NGramSplit split = new NGramSplit("/home/qiuqiang/weiboData/trainData","/home/qiuqiang/weiboData/train.ngram");
//		split.split();
//		NGramSplit split = new NGramSplit("/home/qiuqiang/weiboData/testData/20120206","/home/qiuqiang/weiboData/20120206.ngram");
//		split.split();
		String content = "是的，中午去腐败了一盘~！  // 你们都鸣了啊？？？ // 我们的战利品 // 牛了 幸福的花朵：今儿中午春茗的团年午餐~ 9 1 9 1 9 9 9 1 9 0 #美图秀秀iPhone版#";
		List<Term> terms = NlpAnalysis.parse(content);
		System.out.println(terms);
//		Set<String> sets = ngram(2, terms);
//		System.out.println(sets);
	}

}
