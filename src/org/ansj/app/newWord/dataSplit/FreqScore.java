package org.ansj.app.newWord.dataSplit;


/**
 * 词频和成词score
 * @author qiuqiang
 *
 */
public class FreqScore implements Comparable<FreqScore>{
	
	public double score; //成词能力
	public double freq; //词频
	public double mutual; //互信息
	
	public double finalMutual; //最终互信息
	
	public int size; //该词由几部分组成
	
	public FreqScore(double score, double freq, double mutual, int size){
		this.score = score;
		this.freq = freq;
		this.mutual = mutual;
		this.size = size;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getFreq() {
		return freq;
	}

	public void setFreq(double freq) {
		this.freq = freq;
	}
	
	public void maxFreq(double freq) {
		this.freq = Math.max(this.freq, freq);
	}
	
	public void addFreq(double freq){
		this.freq = this.freq+freq;
	}
	
	public void maxScore(double score){
		this.score = Math.max(this.score, score);
	}
	
	public String toString(){
		return this.freq+":"+this.score+":"+this.mutual+":"+this.size;
	}
	
	public double getMutual() {
		return mutual;
	}

	public void setMutual(double mutual) {
		this.mutual = mutual;
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
	
	public double getFinalMutual(){
//		if(this.mutual>0){
//			return Math.log(this.freq)+this.mutual;
//		}else{
//			return 0;
//		}
		return Math.log(this.freq)+this.mutual;
		
	}

	@Override
	public int compareTo(FreqScore o) {
		
		double a =  this.freq*(5*this.score+this.mutual);
		double b = o.freq*(5*o.score+o.mutual);
		
		if(a<b){
			return 1;
		}else if(a>b){
			return -1;
		}
		
		if(this.score<o.score){
			return 1;
		}else if(this.score>o.score){
			return -1;
		}
		

		return 0;
	}
	
}
