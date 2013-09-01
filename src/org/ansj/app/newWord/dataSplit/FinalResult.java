package org.ansj.app.newWord.dataSplit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import love.cq.util.CollectionUtil;

/**
 * 最终的新词结果
 * @author qiuqiang
 *
 */
class GroupSort implements Comparable<GroupSort>{
	double freq;
	double score;
	
	public GroupSort(double freq, double score){
		this.freq = freq;
		this.score = score;
	}

	@Override
	public int compareTo(GroupSort o) {
		if(this.freq<o.freq){
			return -1;
		}else if(this.freq>o.score){
			return 1;
		}
		return 0;
	}
	
	public String toString(){
		return this.freq+":"+this.score;
	}
	
}

public class FinalResult {

	public List<GroupResult> groupResults; //所有组的新词结果
	
	public FinalResult(){
		this.groupResults = new ArrayList<GroupResult>();
	}
	
	public FinalResult(List<GroupResult> groupResults){
		this.groupResults = groupResults;
	}
	
	public void addGroupResult(GroupResult groupResult){
		this.groupResults.add(groupResult);
	}
	
	/**
	 * 对新words进行排序
	 * @throws IOException 
	 */
	public HashMap<String, FreqScore> rankTwoWords() throws IOException{
		
		HashMap<String, FreqScore> totalWords = new HashMap<String, FreqScore>();
		HashMap<String, Integer> wordGroup = new HashMap<String, Integer>();
		int allGroups = 0;
		
		BufferedWriter out = new BufferedWriter(new FileWriter("/home/qiuqiang/weiboData/groupResult.freq"));
		Collections.sort(groupResults);
		
		for(GroupResult groupResult: groupResults){
			
			if(allGroups>100){
				break;
			}
			
			if(groupResult.getSize()>0){
				System.out.println(groupResult.docNum);
				allGroups ++;
			}
			//组间排序
			int docNum = groupResult.docNum;
			
			
			HashMap<String, FreqScore> groupSort = new HashMap<String, FreqScore>();
			HashMap<String, FreqScore> groupScore = groupResult.getTwoWords();
			int allCount = 0;
			for(Entry<String, FreqScore> entry: groupScore.entrySet()){
				
				if(entry.getValue().freq>2){
					allCount += entry.getValue().freq;
					groupSort.put(entry.getKey(), new FreqScore( entry.getValue().score, entry.getValue().freq*1.0/docNum, entry.getValue().mutual, entry.getValue().size));
				
				}
				
					
					
			}
			out.write(groupResult.docNum+"--------------\n");
			List<Entry<String, FreqScore>> entrys = CollectionUtil.sortMapByValue(groupSort, -1);
			
			
			for(Entry<String, FreqScore> entry: entrys){
				
				out.write(entry.getKey()+"\t"+entry.getValue()+"\n");
				
				if(!totalWords.containsKey(entry.getKey())){
					wordGroup.put(entry.getKey(), 1);
					totalWords.put(entry.getKey(), new FreqScore(entry.getValue().score, entry.getValue().freq, entry.getValue().mutual, entry.getValue().size));
				}else{
					totalWords.get(entry.getKey()).maxFreq(entry.getValue().freq);
//					totalWords.get(entry.getKey()).maxScore(totalWords.get(entry.getKey()).score);
					wordGroup.put(entry.getKey(), 1+wordGroup.get(entry.getKey()));
				}
			}
		}
		
		
		HashMap<String, FreqScore> testWords = new HashMap<String, FreqScore>();
		HashMap<String, Double> freqs = new HashMap<String, Double>();
		HashMap<String, Double> scores = new HashMap<String, Double>();
		HashMap<String, Double> mutuals = new HashMap<String, Double>();
		
		for(Entry<String, FreqScore> entry: totalWords.entrySet()){
			
			String key = entry.getKey();
			//entry.getValue().setFreq(entry.getValue().getFreq()*Math.log10(allGroups*1.0/wordGroup.get(key)));
//			testWords.put(entry.getKey(), new FreqScore(entry.getValue().score,entry.getValue().getFreq()*Math.log10(allGroups*1.0/wordGroup.get(key)), entry.getValue().mutual, entry.getValue().size));
			if(entry.getValue().score>=0 && entry.getValue().mutual>0){
				testWords.put(key, new FreqScore(entry.getValue().score, entry.getValue().getFreq(), entry.getValue().mutual, entry.getValue().size));
				freqs.put(key, entry.getValue().freq);
				scores.put(key, entry.getValue().score);
				mutuals.put(key, entry.getValue().mutual);
			}
			
		}
		
		BufferedWriter out1 = new BufferedWriter(new FileWriter("/home/qiuqiang/weiboData/scores/freq.sort"));
		List<Entry<String, Double>> entrys= CollectionUtil.sortMapByValue(freqs, 1);
		for(Entry<String, Double> entry: entrys){
			out1.write(entry.getKey()+"\t"+entry.getValue()+"\n");
		}
		out1.close();
		out1 = new BufferedWriter(new FileWriter("/home/qiuqiang/weiboData/scores/score.sort"));
		entrys= CollectionUtil.sortMapByValue(scores, 1);
		for(Entry<String, Double> entry: entrys){
			out1.write(entry.getKey()+"\t"+entry.getValue()+"\n");
		}
		out1.close();
		out1 = new BufferedWriter(new FileWriter("/home/qiuqiang/weiboData/scores/mutual.sort"));
		entrys= CollectionUtil.sortMapByValue(mutuals, 1);
		for(Entry<String, Double> entry: entrys){
			out1.write(entry.getKey()+"\t"+entry.getValue()+"\n");
		}
		out1.close();
		
		int i=0;
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("/home/qiuqiang/weiboData/finalResult.freq.score"));
		
		List<Entry<String, FreqScore>> entrystest = CollectionUtil.sortMapByValue(testWords, -1);
		
		for(Entry<String, FreqScore> entry: entrystest){
			
			writer.write(entry.getKey()+"\t"+entry.getValue()+"\n");
			
		}
		out.close();
		writer.close();
		
		return null;
	}
	
	public HashMap<String, FreqScore> rankThreeWords() throws IOException{
		
		HashMap<String, FreqScore> totalWords = new HashMap<String, FreqScore>();
		HashMap<String, Integer> wordGroup = new HashMap<String, Integer>();
		int allGroups = 0;
		Collections.sort(groupResults);
		BufferedWriter out = new BufferedWriter(new FileWriter("/home/qiuqiang/weiboData/groupResult.freq.3"));
		for(GroupResult groupResult: groupResults){
			
			if(allGroups>200){
				break;
			}
			
			if(groupResult.getSize()>0){
				System.out.println(groupResult.docNum);
				allGroups ++;
			}
			//组间排序
			int docNum = groupResult.docNum;
			
			
			HashMap<String, FreqScore> groupSort = new HashMap<String, FreqScore>();
			HashMap<String, FreqScore> groupScore = groupResult.getThreeWords();
			int allCount = 0;
			for(Entry<String, FreqScore> entry: groupScore.entrySet()){
				
					if(entry.getValue().freq>2){
						allCount += entry.getValue().freq;
						groupSort.put(entry.getKey(), new FreqScore( entry.getValue().score, entry.getValue().freq*1.0, entry.getValue().mutual, entry.getValue().size));
					
					}
					
					
			}
			out.write(groupResult.docNum+"--------------\n");
			List<Entry<String, FreqScore>> entrys = CollectionUtil.sortMapByValue(groupSort, -1);
			
			
			for(Entry<String, FreqScore> entry: entrys){
				
				out.write(entry.getKey()+"\t"+entry.getValue()+"\n");
				
				if(!totalWords.containsKey(entry.getKey())){
					wordGroup.put(entry.getKey(), 1);
					totalWords.put(entry.getKey(), new FreqScore(entry.getValue().score, entry.getValue().freq, entry.getValue().mutual, entry.getValue().size));
				}else{
					totalWords.get(entry.getKey()).maxFreq(entry.getValue().freq);
//					totalWords.get(entry.getKey()).maxScore(totalWords.get(entry.getKey()).score);
					wordGroup.put(entry.getKey(), 1+wordGroup.get(entry.getKey()));
				}
			}
		}
		
		
		HashMap<String, FreqScore> testWords = new HashMap<String, FreqScore>();
		HashMap<String, Double> freqs = new HashMap<String, Double>();
		HashMap<String, Double> scores = new HashMap<String, Double>();
		HashMap<String, Double> mutuals = new HashMap<String, Double>();
		
		for(Entry<String, FreqScore> entry: totalWords.entrySet()){
			
			String key = entry.getKey();
			//entry.getValue().setFreq(entry.getValue().getFreq()*Math.log10(allGroups*1.0/wordGroup.get(key)));
//			testWords.put(entry.getKey(), new FreqScore(entry.getValue().score,entry.getValue().getFreq()*Math.log10(allGroups*1.0/wordGroup.get(key)), entry.getValue().mutual, entry.getValue().size));
			if(entry.getValue().score>0){
				testWords.put(key, new FreqScore(entry.getValue().score, entry.getValue().getFreq(), entry.getValue().mutual, entry.getValue().size));
				freqs.put(key, entry.getValue().freq);
				scores.put(key, entry.getValue().score);
				mutuals.put(key, entry.getValue().mutual);
			}
			
		}
		
		BufferedWriter out1 = new BufferedWriter(new FileWriter("/home/qiuqiang/weiboData/scores/freq.sort"));
		List<Entry<String, Double>> entrys= CollectionUtil.sortMapByValue(freqs, 1);
		for(Entry<String, Double> entry: entrys){
			out1.write(entry.getKey()+"\t"+entry.getValue()+"\n");
		}
		out1.close();
		out1 = new BufferedWriter(new FileWriter("/home/qiuqiang/weiboData/scores/score.sort"));
		entrys= CollectionUtil.sortMapByValue(scores, 1);
		for(Entry<String, Double> entry: entrys){
			out1.write(entry.getKey()+"\t"+entry.getValue()+"\n");
		}
		out1.close();
		out1 = new BufferedWriter(new FileWriter("/home/qiuqiang/weiboData/scores/mutual.sort"));
		entrys= CollectionUtil.sortMapByValue(mutuals, 1);
		for(Entry<String, Double> entry: entrys){
			out1.write(entry.getKey()+"\t"+entry.getValue()+"\n");
		}
		out1.close();
		
		int i=0;
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("/home/qiuqiang/weiboData/finalResult.freq.score.3"));
		
		List<Entry<String, FreqScore>> entrystest = CollectionUtil.sortMapByValue(testWords, -1);
		
		for(Entry<String, FreqScore> entry: entrystest){
			
				writer.write(entry.getKey()+"\t"+entry.getValue()+"\n");
		}
		out.close();
		writer.close();
		
		return null;
	}

	
//		HashMap<String, FreqScore> testWords = new HashMap<String, FreqScore>();
//		for(Entry<String, FreqScore> entry: totalWords.entrySet()){
//			
//			String key = entry.getKey();
//			//entry.getValue().setFreq(entry.getValue().getFreq()*Math.log10(allGroups*1.0/wordGroup.get(key)));
//			//testWords.put(entry.getKey(), new FreqScore(entry.getValue().score,entry.getValue().getFreq()*Math.log10(allGroups*1.0/wordGroup.get(key))));
//			testWords.put(entry.getKey(), new FreqScore(entry.getValue().score,entry.getValue().getFreq()));
//		}
//		
//		int i=0;
//		List<Entry<String, FreqScore>> entrys = CollectionUtil.sortMapByValue(testWords, 1);
//		
//		BufferedWriter writer = new BufferedWriter(new FileWriter("/home/qiuqiang/weiboData/finalResult.freq.score"));
//		
//		for(Entry<String, FreqScore> entry: entrys){
//			
//			if(entry.getValue().getScore()>0.9){
//				writer.write(entry.getKey()+"\t"+entry.getValue()+"\n");
//			}
//		
//		}
//		out.close();
//		writer.close();
//		
//		return null;
//	}

//	public HashMap<String, FreqScore> rankFourWords() throws IOException{
//		
//		HashMap<String, FreqScore> totalWords = new HashMap<String, FreqScore>();
//		HashMap<String, Integer> wordGroup = new HashMap<String, Integer>();
//		int allGroups = groupResults.size();
//		
//		BufferedWriter out = new BufferedWriter(new FileWriter("/home/qiuqiang/weiboData/groupResult.freq"));
//		for(GroupResult groupResult: groupResults){
//			//组间排序
//			HashMap<String, GroupSort> groupSort = new HashMap<String, GroupSort>();
//			HashMap<String, FreqScore> groupScore = groupResult.getNewWords();
//			int allCount = 0;
//			for(Entry<String, FreqScore> entry: groupScore.entrySet()){
//				
//					allCount += entry.getValue().freq;
//					groupSort.put(entry.getKey(), new GroupSort(entry.getValue().freq, entry.getValue().score));
//				
//				
//			}
//			out.write(groupResult.docNum+"--------------\n");
//			List<Entry<String, FreqScore>> entrys = CollectionUtil.sortMapByValue(groupSort, 1);
//			for(Entry<String, FreqScore> entry: entrys){
//				
//				out.write(entry.getKey()+"\t"+entry.getValue()+"\n");
//				
//				if(!totalWords.containsKey(entry.getKey())){
//					wordGroup.put(entry.getKey(), 1);
//					totalWords.put(entry.getKey(), new FreqScore(entry.getValue().score, entry.getValue().freq*1.0, entry.getValue()));
//				}else{
//					totalWords.get(entry.getKey()).setFreq(entry.getValue().freq*1.0+totalWords.get(entry.getKey()).freq);
//					totalWords.get(entry.getKey()).maxScore(totalWords.get(entry.getKey()).score);
//					wordGroup.put(entry.getKey(), 1+wordGroup.get(entry.getKey()));
//				}
//			}
//		}
//		
//		
//		HashMap<String, FreqScore> testWords = new HashMap<String, FreqScore>();
//		for(Entry<String, FreqScore> entry: totalWords.entrySet()){
//			
//			String key = entry.getKey();
//			//entry.getValue().setFreq(entry.getValue().getFreq()*Math.log10(allGroups*1.0/wordGroup.get(key)));
//			//testWords.put(entry.getKey(), new FreqScore(entry.getValue().score,entry.getValue().getFreq()*Math.log10(allGroups*1.0/wordGroup.get(key))));
//			testWords.put(entry.getKey(), new FreqScore(entry.getValue().score,entry.getValue().getFreq()));
//		}
//		
//		int i=0;
//		List<Entry<String, FreqScore>> entrys = CollectionUtil.sortMapByValue(testWords, 1);
//		
//		BufferedWriter writer = new BufferedWriter(new FileWriter("/home/qiuqiang/weiboData/finalResult.freq.score"));
//		
//		for(Entry<String, FreqScore> entry: entrys){
//			
//			if(entry.getValue().getScore()>0.9){
//				writer.write(entry.getKey()+"\t"+entry.getValue()+"\n");
//			}
//		
//		}
//		out.close();
//		writer.close();
//		
//		return null;
//	}
	
	
	
	public static void main(String[] args) throws IOException{
		
		FinalResult finalResult = new FinalResult();
		
		//init data
		File dir = new File("/home/qiuqiang/weiboData/result");
		File[] files = dir.listFiles();
		String line = null;
		String[] splits = null;
		String[] freqScore = null;
		
		for(File file: files){
			GroupResult groupResult = new GroupResult();
			HashMap<String, FreqScore> twoWords = new HashMap<String, FreqScore>();
			HashMap<String, FreqScore> threeWords = new HashMap<String, FreqScore>();
			HashMap<String, FreqScore> fourWords = new HashMap<String, FreqScore>();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			int count = 0;
			boolean flag = true;
			while((line=reader.readLine())!=null){
				
				if(flag==false){
					splits = line.split("\t",2);
					freqScore = splits[1].split(":");
					int size = Integer.parseInt(freqScore[3]);
					double mutual = Math.log(Double.parseDouble(freqScore[0])) + Double.parseDouble(freqScore[2]);
					if(size==2){
						twoWords.put(splits[0], new FreqScore(Double.parseDouble(freqScore[1]), Double.parseDouble(freqScore[0]),mutual, size));
					}else if(size ==3){
						threeWords.put(splits[0], new FreqScore(Double.parseDouble(freqScore[1]), Double.parseDouble(freqScore[0]),0, size));
					}else{
						fourWords.put(splits[0], new FreqScore(Double.parseDouble(freqScore[1]), Double.parseDouble(freqScore[0]),0, size));
					}
					
				}
				
				if(line.startsWith("-------word")){
					flag = false;
				}
				
				if(flag){
					count++;
				}
			}
			reader.close();
			groupResult.setDocNum(count);
			groupResult.setTwoWords(twoWords);
			groupResult.setThreeWords(threeWords);
			groupResult.setFourWords(fourWords);
			finalResult.addGroupResult(groupResult);
		}
		
		//rank the new word
		HashMap<String, FreqScore> rankTwoWords = finalResult.rankTwoWords();
		HashMap<String, FreqScore> rankThreeWords = finalResult.rankThreeWords();
//		HashMap<String, FreqScore> rankFourWords = finalResult.rankFourWords();
		
//		for(Entry<String, FreqScore> entry: rankWords.entrySet()){
//			System.out.println(entry.getKey()+"\t"+entry.getValue());
//		}
		
	}
	
}
