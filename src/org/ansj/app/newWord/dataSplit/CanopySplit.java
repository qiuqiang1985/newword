package org.ansj.app.newWord.dataSplit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import love.cq.util.CollectionUtil;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.BaseAnalysis;


/**
 * 将数据集按照Canopy进行划分
 * @author qiuqiang
 *
 */
class UserProfile implements Comparable<UserProfile>{
	List<TermVector> terms;
	int docNum;
	double distanceToCenter;
	
	public UserProfile(List<TermVector> terms, int docNum){
		this(terms, docNum, 1000);
	}
	
	public UserProfile(List<TermVector> terms, int docNum, double distanceToCenter){
		this.terms = terms;
		this.docNum = docNum;
		this.distanceToCenter = distanceToCenter;
	}

	public List<TermVector> getTerms() {
		return terms;
	}

	public void setTerms(List<TermVector> terms) {
		this.terms = terms;
	}

	public double getDistanceToCenter() {
		return distanceToCenter;
	}

	public void setDistanceToCenter(double distanceToCenter) {
		this.distanceToCenter = distanceToCenter;
	}
	
	public int getDocNum() {
		return docNum;
	}

	public void setDocNum(int docNum) {
		this.docNum = docNum;
	}

	@Override
	public int compareTo(UserProfile arg0) {
		return  (int)(this.distanceToCenter - arg0.distanceToCenter);
	}
}

public class CanopySplit {
	
	private double t1;
	private double t2;
	
	public String inputPath;
	public String wordPath;
	public String outputDir;
	
	public ArrayList<UserProfile> points ;
	public HashMap<Integer, String> docs ;
	public HashMap<Integer, Double> wordCounts;
	
	public CanopySplit(){}
	
	public CanopySplit(double t1, double t2, String inputPath, String wordPath, String outputDir){
		this.t1 = t1;
		this.t2 = t2;
		this.inputPath = inputPath;
		this.wordPath = wordPath;
		this.outputDir = outputDir;
		
		this.points = new ArrayList<UserProfile>();
		this.docs = new HashMap<Integer, String>();
		this.wordCounts = new HashMap<Integer, Double>();
	}
	
	/**
	 * 解析向量
	 * @param vectorStr
	 * @return
	 */
	public ArrayList<TermVector> parserVector(String vectorStr){
		
		String[] splits = vectorStr.split(" ");
		ArrayList<TermVector> vector = new ArrayList<TermVector>();
		
		String line = null;
		String[] tmpSplits = null;
		for(int i=0;i<splits.length;i++){
			line = splits[i];
			tmpSplits = line.split(":");
			try{
				if(tmpSplits.length==2){
					vector.add(new TermVector(Integer.parseInt(tmpSplits[0]), Double.parseDouble(tmpSplits[1])));
				}
				
			}catch(Exception e){
				System.out.println("------"+line);
				throw new RuntimeException(e);
			}
			
		}
		
		return vector;
	}
	
	
	
	/**
	 * 读取原始数据
	 * @throws IOException
	 */
	public void readInput() throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(this.inputPath));
		
		
		String line = null;
		int docNum = 1;
		String[] splits = null;
		
		while((line=reader.readLine())!=null){
			splits = line.split("\t",2);
			
			points.add(new UserProfile(parserVector(splits[0]), docNum));
			
			docs.put(docNum, splits[1]);
			
			docNum++;
		}
		
		reader.close();
		
		reader = new BufferedReader(new FileReader(this.wordPath));
		int allDoc = 220348;//points.size();
		while((line=reader.readLine())!=null){
			splits = line.split("\t",2);
			wordCounts.put(Integer.parseInt(splits[0]), Math.log10(allDoc*1.0/Integer.parseInt(splits[1])));
		}
		
		reader.close();
		
	}
	
	
	/**
	 * 进行划分
	 * @param points
	 * @return
	 */
	public List<Canopy> createCanopies(ArrayList<UserProfile> points, Measure distance) {
		List<Canopy> canopies = new ArrayList<Canopy>();

		int nextCanopyId = 0;
		Canopy greyUsersCanopy = new Canopy();
		while (!points.isEmpty()) {
			Iterator<UserProfile> ptIter = points.iterator();
			UserProfile p1 = ptIter.next();
//			boolean flag = false;
//			while(p1!=null && p1.terms.size()<10){ //挑选优质好的作为起始中心
//				
//				if(p1.getTerms().size()<5){
//					
//					ptIter.remove();
//					
//					if(greyUsersCanopy.getCenter() == null ) {
//						greyUsersCanopy = new Canopy(p1, nextCanopyId++);
//						greyUsersCanopy.addPoints(p1);
//					}
//					else {
//						greyUsersCanopy.addPoints(p1);
//					}
//					
//					flag = true;
//					break;
//					
//					
//				}else{
//					if(ptIter.hasNext()){
//						p1 = ptIter.next();
//					}else{
//						break;
//					}
//				}
//			}
//			
//			if(flag){
//				continue;
//			}
			
			ptIter.remove();
			
			// condition for grey users (users without subjects of interest)
			if (p1.getTerms().size()<5) {
				// if it is the first user in the "grey" canopy, 
				// it is set as the center and it is added to the canopy
				if(greyUsersCanopy.getCenter() == null ) {
					greyUsersCanopy = new Canopy(p1, nextCanopyId++);
					greyUsersCanopy.addPoints(p1);
				}
				else {
					greyUsersCanopy.addPoints(p1);
				}
				continue;
			}

			// a clone of the point picked becomes the centroid of the new canopy 
			Canopy canopy = new Canopy(p1, nextCanopyId++);
			// the point is added to the canopy with distance to the center 0
			p1.setDistanceToCenter(0);
			canopy.addPoints(p1);
			// add the new canopy to the canopy list
			canopies.add(canopy);

			while (ptIter.hasNext()) {
				UserProfile p2 = ptIter.next();
				//double dist = 1 - distance.calcuateDistance(canopy.getCenter(), p2);
				while(p2.terms.size()<5){
					if(ptIter.hasNext()){
						p2 = ptIter.next();
					}else{
						break;
					}
				}
				
				double dist = 1 - calculateDistance(canopy.getCenter(), p2);
				//System.out.println(dist);
		        // Put all points that are within distance threshold T1 into the canopy
		        if (dist < t1 && dist>0) {
		        	p2.setDistanceToCenter(dist);
		        	canopy.addPoints(p2);
		        }
		        // Remove from the list all points that are within distance threshold T2
		        if (dist < t2) {
		        	ptIter.remove();
		        }
			}
			// sort the users into the canopy by their distance to the centroid
			canopy.sortPointsByDistance();
		}
		// sort the grey users canopy and add it to the canopies list
		greyUsersCanopy.sortPointsByDistance();
		System.out.println("not canopy: "+greyUsersCanopy.getCanopyId());
		//canopies.add(greyUsersCanopy);
		
		return canopies;
	}
	
	
	
	
	/**
	 * 将数据集进行分组
	 * @throws IOException 
	 */
	public void groupData() throws IOException{
		readInput();
		List<Canopy> canopies = createCanopies(this.points, new Measure(){

			@Override
			public double calcuateDistance(UserProfile point1,
					UserProfile point2) {
				
				List<TermVector> vector1 = point1.terms;
				List<TermVector> vector2 = point2.terms;
				
				if(vector1.size()==0 || vector2.size()==0){
					return 0;
				}
				
				double dist = 0;
				
				for(int i=0,j=0;i<vector1.size() && j<vector2.size();){
					
					if(vector1.get(i).index==vector2.get(j).index){
						//double idf2 =1; //Math.pow(wordCounts.get(vector1.get(i).index),2);
						dist += vector1.get(i).weight+vector2.get(j).weight;
						
						i++;
						j++;
					}else if(vector1.get(i).index<vector2.get(j).index){
						i++;
					}else{
						j++;
					}
				}
				
				double testTmp = 0;
				for(int i=0;i<vector1.size();i++){
					testTmp += vector1.get(i).weight;
				}
				for(int i=0;i<vector2.size();i++){
					testTmp += vector2.get(i).weight;
				}
				
				return dist*1.0/testTmp;
			}
			
		});
		System.out.println(canopies.size());
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("/home/qiuqiang/weiboData/1g.txt"));
		
		HashMap<Integer, Integer> clusters = new HashMap<Integer, Integer>();
		
		int allDocNums = 0;
		
		for(Canopy canopy: canopies){
			List<UserProfile> points = canopy.getPoints();
			double sum = 0;
			allDocNums += points.size();
			for(UserProfile point: points){
				sum += point.getDistanceToCenter();
			}
			//System.out.println(canopy.getCanopyId()+"-------"+points.size()+"-----------"+sum/points.size());
			clusters.put(canopy.getCanopyId(), points.size());
		}
		System.out.println("all doc num: "+allDocNums);
		
		List<Entry<Integer, Integer>> clusterSort = CollectionUtil.sortMapByValue(clusters, 1);
		
		Set<Integer> goodCluster = new HashSet<Integer>();
		
		for(Entry<Integer, Integer> entry: clusterSort){
			System.out.println(entry.getKey()+"\t"+entry.getValue());
			if(entry.getValue()>5){
				goodCluster.add(entry.getKey());
			}
		}
		
		
		for(Canopy canopy: canopies){
			
			List<UserProfile> points = canopy.getPoints();
			
			if(goodCluster.contains(canopy.getCanopyId())){
				BufferedWriter tmpWriter = new BufferedWriter(new FileWriter("/home/qiuqiang/weiboData/result/"+canopy.getCanopyId()));
				for(UserProfile point : points){
					tmpWriter.write(docs.get(point.docNum)+"\n");
				}
				tmpWriter.close();
			}else{
				writer.write("------------------\n");
				for(UserProfile point: points){
					writer.write(docs.get(point.docNum)+"\n");
				}
			}
		}
		writer.close();
		GroupNewWord newWords = new GroupNewWord(outputDir, canopies,  docs);
		newWords.newWord();
		
	}
	
	
	/**
	 * 计算文档相似性
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	private double calSimilarity(List<TermVector> vector1, List<TermVector> vector2){
		double dist = 0;
		
		for(int i=0,j=0;i<vector1.size() && j<vector2.size();){
			
			if(vector1.get(i).index==vector2.get(j).index){
				double idf2 =1; //Math.pow(wordCounts.get(vector1.get(i).index),2);
				dist += vector1.get(i).weight* vector2.get(j).weight* idf2;
				
				i++;
				j++;
			}else if(vector1.get(i).index<vector2.get(j).index){
				i++;
			}else{
				j++;
			}
		}
		
		return dist;
	}
	
	/**
	 * cos相似度计算
	 * @param center
	 * @param point
	 * @return
	 */
	private double calculateDistance(UserProfile center, UserProfile point){
		double x2 = calSimilarity(center.getTerms(), center.getTerms());
		double y2 = calSimilarity(point.getTerms(), point.getTerms());
		if(x2==0 || y2==0){
			return 0;
		}
		double dist = calSimilarity(center.getTerms(), point.getTerms())/(Math.sqrt(x2)*Math.sqrt(y2));
		return dist;
	}
	
	public double calculateDistance(List<TermVector> vector1, List<TermVector> vector2){
		double x2 = calSimilarity(vector1, vector1);
		double y2 = calSimilarity(vector2, vector2);
		if(x2==0 || y2==0){
			return 0;
		}
		double dist = calSimilarity(vector1, vector2)/(Math.sqrt(x2)*Math.sqrt(y2));
		return dist;
	}
	
	public static void main(String[] args) throws IOException{
		CanopySplit canopy = new CanopySplit(0.5, 0.5, "/home/qiuqiang/weiboData/train.ngram","/home/qiuqiang/weiboData/wordCount","/home/qiuqiang/weiboData/result");
		long startTime = System.currentTimeMillis();
		canopy.groupData();
		
		System.out.println(System.currentTimeMillis()-startTime);
		
//		VectorGenerate vectorGenerate = new VectorGenerate();
//		String line = "微软周三宣布推出iPhone版SkyDrive服务。微软通过该服务向用户提供了25GB的免费存储空间，用户可以上传、存储及分享视频、图片和文档。此前，用户只能通过skydrive.live.com网站，>或Windows Phone手机内建的应用来使用SkyDrive服务";
////		line = "美的牌空调，美的是什么，打车遇上超霸气的司机阿姨，路边耀武扬威的一群城管在得瑟，被她开车窗直接一句“还执法，tmd有在路中间执法的么”瞬间全部秒了……我在后座一脸仰慕啊啊……！！";
//		//[2:0.14285714285714285, 3:0.14285714285714285, 4:0.14285714285714285, 5:0.14285714285714285, 6:0.14285714285714285, 7:0.14285714285714285, 8:0.14285714285714285]
//		List<Term> terms = BaseAnalysis.parse(line);
//		System.out.println(terms);
//		List<TermVector> vector = vectorGenerate.vector(terms);
//		CanopySplit canopy = new CanopySplit();
//		System.out.println(canopy.calculateDistance(vector, vector));
		
	}

}
