package org.ansj.app.newWord.dataSplit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 简单划分的group组
 * @author qiuqiang
 *
 */
public class Canopy {
	
	private UserProfile center;
	private int canopyId;
	
	private List<UserProfile> points = new ArrayList<UserProfile>();
	
	public Canopy(){
		
	}
	
	public Canopy(UserProfile center, int canopyId){
		this.center = center;
		this.canopyId = canopyId;
	}
	
	public void addPoints(UserProfile point){
		points.add(point);
	}

	public UserProfile getCenter() {
		return center;
	}

	public void setCenter(UserProfile center) {
		this.center = center;
	}

	public int getCanopyId() {
		return canopyId;
	}

	public void setCanopyId(int canopyId) {
		this.canopyId = canopyId;
	}

	public List<UserProfile> getPoints() {
		return points;
	}

	public void setPoints(List<UserProfile> points) {
		this.points = points;
	}

	public void sortPointsByDistance() {
		Collections.sort(points);
	}
	
}
