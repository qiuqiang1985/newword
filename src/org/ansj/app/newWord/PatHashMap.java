package org.ansj.app.newWord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.ansj.domain.Term;
import org.ansj.util.MathUtil;

/**
 * 改成hash了
 * 
 * @author ansj
 */
public class PatHashMap {

	private HashMap<String, Node> hm = new HashMap<String, Node>();
	private int allFreq;
	

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
					// 计算总数
					allFreq++;

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
		
//		System.out.println("=======郑洁===");
//		for(Entry<String,Node> entry: hm.entrySet()){
//			System.out.println(entry.getKey()+":"+entry.getValue().toString());
//		}
		
		Collection<Node> values = hm.values();
		 		 
		return values;
	}

	/**
	 * 验证一个词是否是新词
	 * 
	 * @param node
	 * @param validate
	 * @return false 不合格.true 合格
	 */
	private boolean filter(Node node, double validate) {
		// TODO Auto-generated method stub
		if (node.freq < validate && node.score < 1) {
			return false;
		} else {
			return true;
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
}
