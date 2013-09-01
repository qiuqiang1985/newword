package org.ansj.app.newWord.dataSplit;

import java.util.HashMap;

/**
 * 每组的新词识别结果
 * @author qiuqiang
 *
 */
public class GroupResult implements Comparable<GroupResult> {
	
	public int docNum; //该组的文档总数
	public HashMap<String, FreqScore> twoWords; //该组识别的新词
	public HashMap<String, FreqScore> threeWords;
	public HashMap<String, FreqScore> fourWords;
	
	public GroupResult(){}

	public GroupResult(int docNum, HashMap<String, FreqScore> twoWords, HashMap<String, FreqScore> threeWords, HashMap<String, FreqScore> fourWords){
		this.docNum = docNum;
		this.twoWords = twoWords;
		this.threeWords = threeWords;
		this.fourWords = fourWords;
	}
	
	public int getDocNum() {
		return docNum;
	}

	public void setDocNum(int docNum) {
		this.docNum = docNum;
	}

	public HashMap<String, FreqScore> getTwoWords() {
		return twoWords;
	}

	public void setTwoWords(HashMap<String, FreqScore> twoWords) {
		this.twoWords = twoWords;
	}

	public HashMap<String, FreqScore> getThreeWords() {
		return threeWords;
	}

	public void setThreeWords(HashMap<String, FreqScore> threeWords) {
		this.threeWords = threeWords;
	}

	public HashMap<String, FreqScore> getFourWords() {
		return fourWords;
	}

	public void setFourWords(HashMap<String, FreqScore> fourWords) {
		this.fourWords = fourWords;
	}
	
	public int getSize(){
		return this.twoWords.size()+this.threeWords.size()+this.fourWords.size();
	}

	public static void main(String[] args){
		
	}

	@Override
	public int compareTo(GroupResult o) {
		if(this.docNum<o.docNum){
			return 1;
		}else if(this.docNum>o.docNum){
			return -1;
		}
		return 0;
	}
}
