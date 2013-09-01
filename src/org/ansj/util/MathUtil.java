package org.ansj.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ansj.domain.NewWordNatureAttr;
import org.ansj.domain.Term;
import org.ansj.library.NatureLibrary;
import org.ansj.library.NgramLibrary;
import org.ansj.recognition.NatureRecognition.NatureTerm;
import org.ansj.splitWord.analysis.BaseAnalysis;

public class MathUtil {

	// 平滑参数
	private static final double dSmoothingPara = 0.1;
	// 一个参数
	private static final int MAX_FREQUENCE = 2079997;// 7528283+329805;
	// ﻿Two linked Words frequency
	private static final double dTemp = (double) 1 / MAX_FREQUENCE;
	
//	private static HashMap<String, Integer> wordCounts = new HashMap<String, Integer>();
//	private static int MAX_COUNT = 0;

//	static{
//		try{
//			BufferedReader reader = new BufferedReader(new FileReader("/home/qiuqiang/weiboData/wordStrCount"));
//			String line = null;
//			while((line=reader.readLine())!=null){
//				try{
//					String[] splits = line.split("\t",2);
//					MAX_COUNT += Integer.parseInt(splits[1]);
//					wordCounts.put(splits[0], Integer.parseInt(splits[1]));
//				}catch(Exception e){
//					continue;
//				}
//				
//			}
//			System.out.println(wordCounts.size());
//			reader.close();
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * 从一个词的词性到另一个词的词的分数
	 * 
	 * @param form
	 *            前面的词
	 * @param to
	 *            后面的词
	 * @return 分数
	 */
	public static double compuScore(Term from, Term to) {
		double frequency = from.getTermNatures().allFreq + 1;

		if (frequency < 0) {
			return from.getScore() + MAX_FREQUENCE;
		}

		int nTwoWordsFreq = NgramLibrary.getTwoWordFreq(from, to);
		double value = -Math.log(dSmoothingPara * frequency / (MAX_FREQUENCE + 80000) + (1 - dSmoothingPara)
				* ((1 - dTemp) * nTwoWordsFreq / frequency + dTemp));

		if (value < 0)
			value += frequency;

		if (value < 0) {
			value += frequency;
		}
		return from.getScore() + value;
	}

	/**
	 * 词性词频词长.计算出来一个分数
	 * 
	 * @param from
	 * @param term
	 * @return
	 */
	public static double compuScoreFreq(Term from, Term term) {
		// TODO Auto-generated method stub
		return from.getTermNatures().allFreq + term.getTermNatures().allFreq;
	}

	/**
	 * 两个词性之间的分数计算
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public static double compuNatureFreq(NatureTerm from, NatureTerm to) {
		double twoWordFreq = NatureLibrary.getTwoNatureFreq(from.termNature.nature, to.termNature.nature);
		if (twoWordFreq == 0) {
			twoWordFreq = Math.log(from.selfScore + to.selfScore);
		}
		double score = from.score + Math.log((from.selfScore + to.selfScore) * twoWordFreq) + to.selfScore;
		return score;
	}


	public static void main(String[] args) {
		MathUtil math = new MathUtil();
		//System.out.println(Math.log(dTemp * 2));
		List<Term> terms = BaseAnalysis.parse("美的中国");
		System.out.println(terms);
		//terms.add(new Term("牌", 0, null));
		System.out.println(math.testScore(terms));
//		System.out.println(math.mutualInformation(terms));
	}
	
	static{
		
	}
	
	/**
	 * 互信息
	 * @param all
	 * @return
	 */
	public static double mutualInformation(List<Term> all, int MAX_COUNT, HashMap<String, Integer> wordCounts){
		double score = Math.log(1.0/MAX_COUNT);
		//System.out.println(score);
		if(all.size()==2){
			for(Term term : all){
//				System.out.println(term.getName()+"\t"+wordCounts.get(term.getName()));
//				System.out.println(Math.log(wordCounts.get(term.getName())*1.0/MAX_COUNT));
				score -= Math.log(wordCounts.get(term.getName())*1.0/MAX_COUNT);
			}
		}else{
			return 0;
		}
		
		
//		System.out.println(score);
		
		return score;
	}
	
	public static double testScore(List<Term> all){
		double score = 0;
		NewWordNatureAttr newWordAttr = null;
		Term first = all.get(0);
		

		// 查看左右链接
//		int twoWordFreq = NgramLibrary.getTwoWordFreq(first.getFrom(), first);
//		score -= twoWordFreq;
		

		// 查看右连接
		int length = all.size() - 1;
//		Term end = all.get(all.size() - 1);
//		twoWordFreq = NgramLibrary.getTwoWordFreq(end, end.getTo());
//		score -= twoWordFreq;
		

		// 查看内部链接
		for (int i = 0; i < length; i++) {
			score -= NgramLibrary.getTwoWordFreq(all.get(i), all.get(i + 1));
		}
				
		return score;
	}
	
	/**
	 * 新词熵及其左右熵
	 * 
	 * @param all
	 */	
	public static double leftRightEntropy(List<Term> all) {
		// TODO Auto-generated method stub
		double score = 0;
		NewWordNatureAttr newWordAttr = null;
		Term first = all.get(0);
		

		// 查看左右链接
		int twoWordFreq = NgramLibrary.getTwoWordFreq(first.getFrom(), first);
		score -= twoWordFreq;
		

		// 查看右连接
		int length = all.size() - 1;
		Term end = all.get(all.size() - 1);
		twoWordFreq = NgramLibrary.getTwoWordFreq(end, end.getTo());
		score -= twoWordFreq;
		

		// 查看内部链接
		for (int i = 0; i < length; i++) {
			score -= NgramLibrary.getTwoWordFreq(all.get(i), all.get(i + 1));
		}
		if (score < -3) {
			return 0;
		}
		
		//score = 0;
		
		// 首字分数
		newWordAttr = first.getTermNatures().newWordAttr;
		score += getTermScore(newWordAttr, newWordAttr.getB());
		// 末字分数
		newWordAttr = end.getTermNatures().newWordAttr;
		score += getTermScore(newWordAttr, newWordAttr.getE());
		// 中词分数
		double midelScore = 0 ;
		Term term = null ;
		for (int i = 1; i < length ; i++) {
			term = all.get(i) ;
			newWordAttr = term.getTermNatures().newWordAttr;
			midelScore += getTermScore(newWordAttr, newWordAttr.getM());
		}
		score +=  midelScore/(length) ;
		
		return score;
	}

	private static double getTermScore(NewWordNatureAttr newWordAttr, int freq) {
		if(newWordAttr==NewWordNatureAttr.NULL){
			return 3 ;
		}
		return (freq / (double) (newWordAttr.getAll() + 1)) * Math.log(500000 / (double) (newWordAttr.getAll() + 1));
	}
	

}
