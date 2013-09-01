package org.ansj.app.newWord.dataSplit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import love.cq.util.CollectionUtil;

import org.ansj.dic.DicReader;
import org.ansj.domain.Term;
import org.ansj.domain.TermNatures;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.util.MathUtil;

/**
 * 将文档表示成向量空间模型
 * 预分词，去除标点等符号
 * @author qiuqiang
 *
 */
public class VectorSpaceModel {
	
	public String dirPath;
	public String outPath;
	
	private HashMap<String, Node> hm = null;
	
	public VectorSpaceModel(){
		this.hm = new HashMap<String, Node>();
	}
	
	public VectorSpaceModel(String dirPath, String outPath){
		this.dirPath = dirPath;
		this.outPath = outPath;
		this.hm = new HashMap<String, Node>();
	}
	

	private boolean filter(Term term) {
		int length = term.getName().length()  ;
		//只对单字新词发现
//		if(length>1){
//			return false ;
//		}
		// 用停用词过滤.
		if (hs.contains(term.getName()) || term.getName().trim().length() == 0) {
			return false;
		}

		// 词性过滤
//		String natureStr = term.getNatrue().natureStr;
//		if (natureStr.contains("m") || ("v".equals(natureStr) && term.getTermNatures().allFreq > 100*length)
//				|| (("d".equals(natureStr)) && term.getTermNatures().allFreq > 1000) || "z".equals(natureStr) || term.getTermNatures() == TermNatures.NB
//				//|| term.getTermNatures() == TermNatures.EN
//				) {
//			return false;
//		}

		return true;
	}
	
	/**
	 * 增加term链到树中
	 * 
	 * @param terms
	 */
	public void addList(List<Term> terms) {
		int length = terms.size();
		// 长度太短直接忽略
		if (length < 2) {
			return;
		}
		List<Term> all = null;
		for (int i = 0; i < length; i++) {
			all = new ArrayList<Term>(length - i);
			for (int j = i; j < length; j++) {
				all.add(terms.get(j));
				if (all.size() > 1) {
					double leftRightEntropy = MathUtil.leftRightEntropy(all);
					
					StringBuilder sb = new StringBuilder();
					for (Term term : all) {
						sb.append(term.getName());
					}
					String name = sb.toString();
					Node node = hm.get(name);
					if (node == null) {
						node = new Node(name);
						node.score = leftRightEntropy;
						node.freq = 1;
						hm.put(name, node);
					} else {
						node.score += leftRightEntropy;
						node.freq += 1;
					}
				}
			}

		}
	}
	
	/**
	 * 发现可能的新词
	 * @param terms
	 * @return
	 */
	public Collection<Node> newWords(List<Term> terms){
		 List<Term> tempList = new ArrayList<Term>();
		 hm.clear();
		 
		 if(terms!=null){
			 for (Term term : terms) {
					if (term == null) {
						continue;
					}
					if (filter(term)) {
						tempList.add(term);
					} else {
						// 如果大于,则放到树中
						if (tempList.size() > 1) {
							// 计算分数.并且增加到pattree中
							addList(tempList);
						}
						if (tempList.size() > 0) {
							if (tempList.size() < 10) {
								tempList.clear();
							} else {
								tempList = new ArrayList<Term>();
							}
						}
					}
				}
			 if (tempList.size() > 1) {
					// 计算分数.并且增加到pattree中
					addList(tempList);
			 }
		 }
		 
		return getWords();
		 
	}
	
	
	
	
	public void split() throws IOException{
		
		HashMap<String, Integer> allWords = new HashMap<String, Integer>();
		
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
		Collection<Node> nodes = null;
		
		for(File file: files){
			System.out.println(file.getName());
			try{
				reader = new BufferedReader(new FileReader(file));
				while((line = reader.readLine())!=null){
					
					terms = BaseAnalysis.parse(line);
					
					nodes = newWords(terms);
					 
					 for(Node node: nodes){
						
						 if(!allWords.containsKey(node.getName())){
							 allWords.put(node.getName(), node.getFreq());
						 }else{
							 allWords.put(node.getName(), node.getFreq()+allWords.get(node.getName()));
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
			
			List<Entry<String,Integer>> list = CollectionUtil.sortMapByValue(allWords, 1);
			for(Entry<String,Integer> entry: list){
				 writer.write(entry.getKey()+"\t"+entry.getValue()+"\n");
			}
			
			
			writer.close();
		}finally{
			//ignore
		}
	}
	
	
	/**
	 * 得道所有的词，根据公共串进行删除
	 * 
	 * @return
	 */
	public Collection<Node> getWords() {
//		for(Entry<String,Node> entry: hm.entrySet()){
//			System.out.println(entry.getKey()+":"+entry.getValue().toString());
//		}
		
		List<String> keyList = new ArrayList<String>(hm.keySet());
		Collections.sort(keyList, new Comparator<String>(){

			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub
				return o1.length()-o2.length()>=0?-1:1;
			}
		});
		
		for(String str: keyList){
			boolean removeself = false;
			if(hm.get(str)==null){
				continue;
			}
			int freq = hm.get(str).freq;
			Iterator<String> iter = hm.keySet().iterator();
			while(iter.hasNext()){
				String tmp = iter.next();
				int tmpfreq = hm.get(tmp).freq;
				if(str.indexOf(tmp)!=-1){ //找到了
					if(freq==tmpfreq && str.length()!=tmp.length()){
						iter.remove();
					}else if(freq<tmpfreq && freq<2){
						removeself = true;
					}
				}
			}
//			if(removeself){
//				hm.remove(str);
//			}
			
		}
		
//		System.out.println("==========");
//		for(Entry<String,Node> entry: hm.entrySet()){
//			System.out.println(entry.getKey()+":"+entry.getValue().toString());
//		}
		
		Collection<Node> values = hm.values();
		 		 
		return values;
	}
	
	private static final HashSet<String> hs = new HashSet<String>();

	/**
	 * 加载停用词典
	 */
	static {
		
		BufferedReader filter = null;
		try {
			filter = DicReader.getReader("newWord/newWordFilter.dic");
			String temp = null;
			while ((temp = filter.readLine()) != null) {
				hs.add(temp.toLowerCase());
			}
			hs.add("－");
			hs.add("　");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (filter != null)
				try {
					filter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	/**
	 * 树中的节点:ps一定切记要不对外开放
	 * 
	 * @author ansj
	 * 
	 */
	class Node implements Comparable<Node> {
		// 此节点的所有偏移量
		private double score;
		// 此节点的term值
		private String name;
		// 出现次数
		private int freq;

		public Node(String name) {
			this.name = name;
		}

		@Override
		public int hashCode() {
			// TODO Auto-generated method stub
			return this.name.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			// TODO Auto-generated method stub
			return this.name.equals(((Node) obj).name);
		}

		@Override
		public int compareTo(Node o) {
			// TODO Auto-generated method stub
			// 先比数字
			if (this.score < o.score) {
				return 1;
			} else if (this.score > o.score) {
				return -1;
			}
			// 再比长度
			if (this.name.length() < o.name.length()) {
				return 1;
			} else {
				return -1;
			}
		}

		public int getFreq() {
			return freq;
		}

		public double getScore() {
			return score;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return this.name + ":" + this.score + ":" + this.freq;
		}

	}
	
	public static void main(String[] args) throws IOException{
//		VectorSpaceModel split = new VectorSpaceModel("/home/qiuqiang/weiboData/trainData","/home/qiuqiang/weiboData/train.ngram");
//		split.split();
		VectorSpaceModel split = new VectorSpaceModel();
		String content = "很多年以后，iPad阅读应用 的创始人麦克麦丘(Mike McCue)回顾自己人生最美好的时候，准会想起那个遥远的初夏午后乔布斯来自己办公室的情形：乔布斯直接从麦克麦丘手中拿过iPad，试>用二十多分钟后，脸上绽放出了笑容，说“恩，做的很不错”。(郑峻)";
		List<Term> terms = BaseAnalysis.parse(content);
		System.out.println(terms);
		Collection<Node> nodes = split.newWords(terms);
		for(Node node: nodes){
			System.out.println(node);
		}
		
//		BufferedReader reader = new BufferedReader(new FileReader("/home/qiuqiang/workspace/anjs/src/org/ansj/dic/newWord/newWordFilter.dic"));
//		String line = null;
//		while((line=reader.readLine())!=null){
//			if(line.trim().length()>=2){
//				System.out.println(line);
//			}
//		}
		
		
	}

}
