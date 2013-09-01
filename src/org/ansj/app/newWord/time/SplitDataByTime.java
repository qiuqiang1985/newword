package org.ansj.app.newWord.time;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * 根据时间进行新词发现，将数据集按照时间进行划分
 * @author qiuqiang
 *
 */
public class SplitDataByTime {
	
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
	public static SimpleDateFormat toSdf = new SimpleDateFormat("yyyyMMdd");
	
	public String filePath;  //文件路径
	public String outDir;    //结果保存路径
	
	
	/**
	 * @param filePath
	 * @param outDir
	 */
	public SplitDataByTime(String filePath, String outDir){
		this.filePath = filePath;
		this.outDir = outDir;
	}
	
	/**
	 * 抽取人名
	 * @param content
	 * @return
	 */
	public String removePeople(String content){
	
		StringBuilder sb = new StringBuilder();
		boolean isPeople = false;
		for(int i=0;i<content.length();i++){
			if(content.charAt(i)=='@' || content.substring(i).startsWith("http://")){ //表示人名的开始或者http
				isPeople = true;
			}
			if(content.charAt(i)==' '){ //表示人名的结束
				if(isPeople==true){
					isPeople = false;
				}
			}
			
			if(isPeople==false){
				sb.append(content.charAt(i));
			}
			
		}
		
		return sb.toString();
	}
	
	/**
	 * 将时间字符串转换成日期字符串。例如：
	 * 2012/2/9 17:44:58---> 20120209
	 * @param time
	 * @return
	 * @throws ParseException 
	 */
	private String timeToDate(String time) throws ParseException{
		Date tmpDate = sdf.parse(time);
		return toSdf.format(tmpDate);
	}
	
	public void split() throws IOException, ParseException{
		
		BufferedReader reader = new BufferedReader(new FileReader(this.filePath));
		
		HashMap<String, BufferedWriter> writers = new HashMap<String, BufferedWriter>();
		
		String line = null;
		boolean start = false;
		String timeString = null;
		String content = null;
		
		StringBuilder sb = new StringBuilder();
		
		while((line=reader.readLine())!=null){
			line = line.trim();
			if(line.startsWith("<article>")){
				start = true; //start
				sb.append(line+" ");
			}else if(line.startsWith("<discuss>")){
				start = false; //end
			}else if(line.startsWith("<insertTime>")){
				//对sb中的字符串进行处理
				timeString = line.substring("<insertTime>".length(), line.length() - "</insertTime>".length());
				timeString = timeToDate(timeString);
				
				content = sb.toString().trim();
				content = content.substring("<article>".length(), content.length() - "</article>".length());
				content = removePeople(content); //根据weibo过滤人名
				
				if(!writers.containsKey(timeString)){
					writers.put(timeString, new BufferedWriter(new FileWriter(this.outDir+"/"+timeString)));
				}
				
				writers.get(timeString).write(content.trim()+"\n");
				
				sb = new StringBuilder();
				
			}else{
				if(start){
					sb.append(line);
				}
			}
		}
		
		reader.close();
		for(Entry<String, BufferedWriter> entry: writers.entrySet()){
			try{
				entry.getValue().close();
			}catch(Exception e){
				//ignore
			}
		}
		
	}
	
	
	public static void main(String[] args) throws ParseException, IOException{
		
		//System.out.println(toSdf.format(sdf.parse("2012/2/9 17:44:58")));
		
//		SplitDataByTime splitData = new SplitDataByTime("/home/qiuqiang/more.xml","/home/qiuqiang/weiboData/");
//		splitData.split();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/qiuqiang/Downloads/搜狗标准词库.txt"),"gbk"));
		String line = null;
		while((line= reader.readLine())!=null){
			System.out.println(line);
		}
	}
	
	
	

}
