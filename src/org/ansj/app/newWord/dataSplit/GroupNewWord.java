package org.ansj.app.newWord.dataSplit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

import org.ansj.app.newWord.dataSplit.VectorSpaceModel.Node;
import org.ansj.dic.DicReader;
import org.ansj.domain.Term;
import org.ansj.domain.TermNatures;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.util.MathUtil;

/**
 * 对每组进行新词识别
 * @author qiuqiang
 *
 */
public class GroupNewWord {
	
	public List<Canopy> canopies;
	public HashMap<Integer, String> docs;
	public String outputDir;
	public int maxLong = 5; //如果该字符串含有5个以上的汉字，则去除
	
	private HashMap<String, Node> hm = null;
	
	public GroupNewWord(){
		this.hm = new HashMap<String, Node>();
	}
	
	public GroupNewWord(String outputDir, List<Canopy> canopies, HashMap<Integer, String> docs){
		this.outputDir = outputDir;
		this.canopies = canopies;
		this.docs = docs;
		this.hm = new HashMap<String, Node>();
	}
	
	private boolean filter(Term term) {
		int length = term.getName().length();
		if((length>2 && !term.getNatrue().natureStr.equalsIgnoreCase("en"))){
			return false;
		}
			
		// 用停用词过滤.
		if (hs.contains(term.getName()) || term.getName().trim().length() == 0) {
			return false;
		}
		
		String natureStr = term.getNatrue().natureStr;
		if (("v".equals(natureStr) && term.getTermNatures().allFreq > 100*length)
				|| (("d".equals(natureStr)) && term.getTermNatures().allFreq > 1000) || "t".equals(natureStr) || "z".equals(natureStr)) {
			return false;
		}

		return true;
	}
	
	/**
	 * 发现可能的新词
	 * @param terms
	 * @return
	 */
	private Collection<Node> newWords(List<Term> terms,int allCount, HashMap<String, Integer> wordCounts){
		 List<Term> tempList = new ArrayList<Term>();
		 hm.clear();
		 
		if (terms != null) {
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
						addList(tempList,allCount, wordCounts);
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
				addList(tempList, allCount, wordCounts);
			}
		}
		 
		return getWords();
		 
	}
		
	/**
	 * 增加term链到树中
	 * 
	 * @param terms
	 */
	public void addList(List<Term> terms, int allCount, HashMap<String, Integer>wordCounts) {
		int length = terms.size();
		// 长度太短直接忽略
		if (length < 2) {
			return;
		}
		
		for (int i = 0; i < length; i++) {
			
			List<Term> all = new ArrayList<Term>(length - i);
			
			for (int j = i; j < length; j++) {
				all.add(terms.get(j));
				if (all.size() > 1 && all.size()<5) { //字数>1并且字数<5
					
					double leftRightEntropy = MathUtil.leftRightEntropy(all); //成词能力，互信息
					
					double mutual = MathUtil.mutualInformation(all, allCount, wordCounts);
					
					StringBuilder sb = new StringBuilder();
					
					//根据字数过滤字符串
					int flag = 0;
					boolean isWord = true;
					int len=0;
		
					for (Term term : all) {
						if(term.getNatrue().natureStr.equalsIgnoreCase("en")){
							flag = 0;
							len += 1;
						}else if(term.getNatrue().natureStr.equalsIgnoreCase("m")){
							flag = 0;
							len+=1;
						}else{
							
							len += term.getName().length();
							
							if(term.getName().length()>1){
								flag += 1;
							}else if(term.getName().length()>2){
								isWord = false; //含有2个字以上的则直接去掉
								break;
							}
							if(flag>=2){ //出现联系两个已经划分好的词，则退出
								isWord = false;
								break;
							}
							
						}
						
						sb.append(term.getName());
					}
					
					if(isWord && len<=5){
						
						String name = sb.toString();
						Node node = hm.get(name);
						if (node == null) {
							node = new Node(name);
							node.score = leftRightEntropy;
							node.freq = 1;
							node.mutual = mutual;
							node.size = all.size();
							hm.put(name, node);
						} else {
							//node.score += leftRightEntropy;
							node.freq += 1;
							node.maxMutual(mutual); //设置最大的互信息
						}
					}
					
				}
			}

		}
	}
	
	public Collection<Node> getWords() {
		
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
				if(str.indexOf(tmp)!=-1){
					if(freq>tmpfreq){ //如果长串频率大于子串频率，则删除子串
						iter.remove();
					}else if(freq<tmpfreq){ //如果长串频率小于子串频率，则长串需要删除
						removeself = true;
					}else{ //相等
						if(str.length()!=tmp.length()){ //不是自己就删除，肯定是比自己长度小的串
							iter.remove();
						}
					}
				}
			}
			
			//删除长串
			if(removeself){
				hm.remove(str);
			}
		}

		Collection<Node> values = hm.values();
		 		 
		return values;
	}
		
	/**
	 * 新词识别
	 * @throws IOException 
	 */
	public void newWord() throws IOException{
		
		String line = null;
		List<Term> terms = null;
		Collection<Node> nodes = null;
		
		HashMap<String, FreqScore> twoMaps = new HashMap<String, FreqScore>(); //2
		HashMap<String, FreqScore> threeMaps = new HashMap<String, FreqScore>(); //3
		HashMap<String, FreqScore> moreMaps = new HashMap<String, FreqScore>(); //above3
		
		for(Canopy canopy: canopies){
			
			List<UserProfile> points = canopy.getPoints();
			
			int allDocSize = points.size();
			
			if(points.size()<5){
				continue;
			}
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(this.outputDir+"/"+canopy.getCanopyId()));
			HashMap<String, FreqScore> allWords = new HashMap<String, FreqScore>();
			HashMap<String, Integer> wordCounts = new HashMap<String, Integer>();
			int allCount = 0;
			
			for(UserProfile point: points){
				line = docs.get(point.docNum);
				terms = BaseAnalysis.parse(line);
				for(Term term: terms){
					if (hs.contains(term.getName()) || term.getName().trim().length() == 0) {
						continue;
					}
					
					allCount++;
					
					if(!wordCounts.containsKey(term.getName())){
						wordCounts.put(term.getName(), 1);
					}else{
						wordCounts.put(term.getName(), 1+wordCounts.get(term.getName()));
					}
				}
				
			}
			
			//对每篇文章进行新词识别
			for(UserProfile point: points){
				line = docs.get(point.docNum);
				writer.write(line+"\n");
				terms = BaseAnalysis.parse(line);
				nodes = newWords(terms, allCount, wordCounts);
				if(nodes!=null){
					for(Node node: nodes){
							if(!allWords.containsKey(node.getName())){
								 allWords.put(node.getName(), new FreqScore(node.score, node.freq, node.mutual, node.size));
							 }else{
								 allWords.get(node.getName()).addFreq(node.freq);
								 allWords.get(node.getName()).maxScore(node.score);
								 allWords.get(node.getName()).maxMutual(node.mutual);
							 }
					 }
				}
			}
			
			writer.write("-------word------------\n");
						
			
			//根据score进行排序，垃圾词过滤
			List<Entry<String,FreqScore>> list = CollectionUtil.sortMapByValue(allWords, 1);
			for(Entry<String,FreqScore> entry: list){
									
					if(entry.getKey().length()==2){
						if(!twoMaps.containsKey(entry.getKey())){
							twoMaps.put(entry.getKey(), entry.getValue());
						}else{
							twoMaps.get(entry.getKey()).addFreq(entry.getValue().freq);
							twoMaps.get(entry.getKey()).maxScore(entry.getValue().score);
						}
					}else if(entry.getKey().length()==3){
						if(!threeMaps.containsKey(entry.getKey())){
							threeMaps.put(entry.getKey(), entry.getValue());
						}else{
							threeMaps.get(entry.getKey()).addFreq(entry.getValue().freq);
							threeMaps.get(entry.getKey()).maxScore(entry.getValue().score);
						}
					}else{
						if(!moreMaps.containsKey(entry.getKey())){
							moreMaps.put(entry.getKey(), entry.getValue());
						}else{
							moreMaps.get(entry.getKey()).addFreq(entry.getValue().freq);
							moreMaps.get(entry.getKey()).maxScore(entry.getValue().score);
						}
					}
					
					
					writer.write(entry.getKey()+"\t"+entry.getValue()+":"+entry.getValue().getFinalMutual()+"\n");
				 
			}
			
			try{
				writer.close();
			}catch(Exception e){
				//ignore
			}
			
		}
		
		BufferedWriter out = new BufferedWriter(new FileWriter("/home/qiuqiang/weiboData/word.result"));
				
		
		List<Entry<String,FreqScore>> list = CollectionUtil.sortMapByValue(twoMaps, 1);
		for(Entry<String, FreqScore> entry: list){
			//将词语中含有的过滤词去掉
			out.write(entry.getKey()+"\t"+entry.getValue()+"\n");
			
		}
		
		out.write("-------------three word----------------\n");
		
		list = CollectionUtil.sortMapByValue(threeMaps, 1);
		for(Entry<String, FreqScore> entry: list){
			//将词语中含有的过滤词去掉
			out.write(entry.getKey()+"\t"+entry.getValue()+"\n");
			
		}
		
		out.write("-------------four word----------------\n");
		
		list = CollectionUtil.sortMapByValue(moreMaps, 1);
		for(Entry<String, FreqScore> entry: list){
			//将词语中含有的过滤词去掉
			out.write(entry.getKey()+"\t"+entry.getValue()+"\n");
			
			
		}
		
		out.close();
		
		
		
	}
	
	private static final HashSet<String> hs = new HashSet<String>();

	/**
	 * 加载停用词典
	 */
	static {
		
		BufferedReader filter = null;
		try {
			filter = DicReader.getReader("stopWord.dic");
			//filter = DicReader.getReader("newWord/newWordFilter.dic");
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
		
		//互信息
		private double mutual;
		
		private int size; //由几部分组成
		
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
		
		public double getMutual(){
			return this.mutual;
		}
		
		public void maxMutual(double mutual){
			this.mutual = Math.max(this.mutual, mutual);
		}
		
		public void setSize(int size){
			this.size = size;
		}
		
		public int getSize(){
			return this.size;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return this.name + ":" + this.score + ":" + this.freq + ":" + this.mutual+":"+this.size;
		}

	}
	
	public static void main(String[] args){
		GroupNewWord discoverNewWord = new GroupNewWord();
		String content = "美的牌空调，美的是什么，打车遇上超霸气的司机阿姨，路边耀武扬威的一群城管在得瑟，被她开车窗直接一句“还执法，tmd有在路中间执法的么”瞬间全部秒了……我在后座一脸仰慕啊啊……！！";
		//content = "【金正日去世】据韩联社报道，金正日去世。（新华社）";
		List<Term> terms = BaseAnalysis.parse(content);
		System.out.println(terms);
		
		HashMap<String, Integer> allCounts = new HashMap<String, Integer>();
		Collection<Node> nodes = discoverNewWord.newWords(terms, terms.size(), allCounts);
		for(Node node: nodes){

			System.out.println(node);
			
		}
	}

}
