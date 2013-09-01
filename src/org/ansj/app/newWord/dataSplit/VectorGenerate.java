package org.ansj.app.newWord.dataSplit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.ansj.dic.DicReader;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * 生成词向量
 * @author qiuqiang
 *
 */
class TermVector{
	int index;
	double weight;
	
	public TermVector(int index, double weight){
		this.index = index;
		this.weight = weight;
	}
	
	public String toString(){
		return this.index+":"+this.weight;
	}
}
public class VectorGenerate {
	
	public String dirPath;
	public String outPath;
	public String wordIndexPath;
	public int count;
	public HashMap<String, Integer> wordIndex;
	
	public HashMap<Integer, Integer> wordCount;
	
	public HashMap<String, Integer> wordStrCount;

	
	private static final HashSet<String> stopWords = new HashSet<String>();
	
	/**
	 * 加载停用词典
	 */
	static {
		
		BufferedReader filter = null;
		try {
//			filter = DicReader.getReader("stopWord.dic");
			filter = DicReader.getReader("newWord/newWordFilter.dic");
			String temp = null;
			while ((temp = filter.readLine()) != null) {
				stopWords.add(temp.toLowerCase());
			}
			stopWords.add("－");
			stopWords.add("　");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (filter != null)
				try {
					filter.close();
				} catch (IOException e) {
				}
		}
	}
	
	public VectorGenerate(){
		this.count = 1;
		this.wordIndex = new HashMap<String, Integer>();
		this.wordCount = new HashMap<Integer, Integer>();
		this.wordStrCount = new HashMap<String, Integer>();
	}
	
	public VectorGenerate(String dirPath, String outPath, String wordIndexPath){
		this.dirPath = dirPath;
		this.outPath = outPath;
		this.wordIndexPath = wordIndexPath;
		this.count = 1;
		this.wordIndex = new HashMap<String, Integer>();
		this.wordCount = new HashMap<Integer, Integer>();
		this.wordStrCount = new HashMap<String, Integer>();
	}
		
	/**
	 * 获取词向量，并且按照word index排序
	 * @param terms
	 * @return
	 */
	public List<TermVector> vector(List<Term> terms, boolean isReg){
		List<TermVector> vectors = new ArrayList<TermVector>();
		
		HashMap<Integer, Integer> wordCnts = new HashMap<Integer, Integer>();
		
		int cnt = 0;
		if(terms!=null){
			
			for(Term term: terms){
				
				if(!stopWords.contains(term.getName())){
					
					//if(term.getNatrue().natureStr.startsWith("an") || term.getNatrue().natureStr.startsWith("n") || term.getNatrue().natureStr.startsWith("v") || term.getNatrue().natureStr.equalsIgnoreCase("en")){ //根据词性过滤，目前只保留名词和动词
						cnt++;
						
						if(!wordIndex.containsKey(term.getName())){ //判断该词是否在词库中
							count ++;
							wordIndex.put(term.getName(), count);
						}
						
						int index = wordIndex.get(term.getName()); //将词加入到该wordCnts中
						if(!wordCnts.containsKey(index)){
							wordCnts.put(index, 1);
						}else{
							wordCnts.put(index, 1+wordCnts.get(index));
						}
						
						if(!wordStrCount.containsKey(term.getName())){
							wordStrCount.put(term.getName(), 1);
						}else{
							wordStrCount.put(term.getName(), 1 + wordStrCount.get(term.getName()));
						}
					//}
				}
			}
			
			for(Entry<Integer, Integer> entry: wordCnts.entrySet()){
				if(isReg){ //归一化
					vectors.add(new TermVector(entry.getKey(), entry.getValue()*1.0/cnt));
				}else{
					vectors.add(new TermVector(entry.getKey(), entry.getValue()));
				}
				if(!wordCount.containsKey(entry.getKey())){
					wordCount.put(entry.getKey(), 1);
					
				}else{
					wordCount.put(entry.getKey(), 1+wordCount.get(entry.getKey()));
				}
			}
			
			Collections.sort(vectors, new Comparator<TermVector>(){

				@Override
				public int compare(TermVector arg0, TermVector arg1) {
					return arg0.index-arg1.index;
				}
				
			});
			
		}
		
		return vectors;
	}
	
	/**
	 * 将文本转换成词向量
	 * @throws IOException
	 */
	public void split(boolean isReg) throws IOException{
		
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
					line =StringEscapeUtils.unescapeHtml(StringEscapeUtils.unescapeHtml(line));
					terms = NlpAnalysis.parse(line);
					List<TermVector> vectors = vector(terms, isReg);
					for(TermVector entry: vectors){
						 writer.write(entry.index+":"+entry.weight+" ");
					}
					writer.write("\t"+line+"\n");
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
		
		BufferedWriter wordWriter = new BufferedWriter(new FileWriter(this.wordIndexPath));
		for(Entry<Integer, Integer> entry: wordCount.entrySet()){
			wordWriter.write(entry.getKey()+"\t"+entry.getValue()+"\n");
		}
		
		BufferedWriter wordStrWriter = new BufferedWriter(new FileWriter("/home/qiuqiang/weiboData/wordStrCount"));
		for(Entry<String, Integer> entry: wordStrCount.entrySet()){
			wordStrWriter.write(entry.getKey()+"\t"+entry.getValue()+"\n");
		}
		
		wordStrWriter.flush();
		
		wordWriter.flush();
		
		try{
			
			writer.close();
			wordWriter.close();
			wordStrWriter.close();
		}finally{
			//ignore
		}
	}
	
	public static void main(String[] args) throws IOException{
		VectorGenerate split = new VectorGenerate("/home/qiuqiang/weiboData/trainData","/home/qiuqiang/weiboData/train.ngram", "/home/qiuqiang/weiboData/wordCount");
		split.split(true);
		
//		VectorGenerate vectorGenerate = new VectorGenerate();
//		String line = "微软周三宣布推出iPhone版SkyDrive服务。微软通过该服务向用户提供了25GB的免费存储空间，用户可以上传、存储及分享视频、图片和文档。此前，用户只能通过skydrive.live.com网站，>或Windows Phone手机内建的应用来使用SkyDrive服务";
//		line = "美的牌空调，美的是什么，打车遇上超霸气的司机阿姨，路边耀武扬威的一群城管在得瑟，被她开车窗直接一句“还执法，tmd有在路中间执法的么”瞬间全部秒了……我在后座一脸仰慕啊啊……！！";
		//[2:0.14285714285714285, 3:0.14285714285714285, 4:0.14285714285714285, 5:0.14285714285714285, 6:0.14285714285714285, 7:0.14285714285714285, 8:0.14285714285714285]
//		List<Term> terms = NlpAnalysis.parse(line);
//		System.out.println(terms);
//		List<TermVector> vector = vectorGenerate.vector(terms);
//		System.out.println(vector);
//		double sum = 0;
//		for(TermVector tt: vector){
//			sum += tt.weight;
//		}
//	    System.out.println(sum);
		
	}

}
