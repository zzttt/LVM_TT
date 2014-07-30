package com.FrameWork;

import java.io.File;
import java.io.Serializable;

public class Snapshot extends UserData implements Serializable{
	private String id; // Snapshot Identifier
	private int ssTotal;  // Snapshot 
	private int state; // Snapshot state
	private int date;
	private int type;
	private String path; // Snapshot

	public Snapshot(String userCode){
		super(userCode);
	}
	
	/**
	 * 
	 * @param id snapshot ID(user ID)
	 * @param ssNumber 
	 */
	public Snapshot(String userCode, String id){
		super(userCode);
		this.id = id;
	}
	
	/**
	 * 
	 * @param id
	 * @param ssNumber
	 * @param state 
	 * @param date 
	 * @param type 
	 * @param path 
	 */
	public Snapshot(String userCode,  String id, int state, int date, int type, String path){
		super(userCode);
		
		this.id = id;
		this.state = state;
		this.date = date;
		this.type = type;
		this.path = path;
		
		File f = new File(path);
		this.ssTotal =  f.list().length; // 
		
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public void setState(int state){
		this.state = state;
	}
	
	public void setDate(int date){
		this.date = date;
	}
	
	public void setType(int type){
		this.type = type;
	}
	
	public void setPath(String path){
		this.path = path;
	}
	
	public String SnapshotInfo(){
		String result = null;
		result = "sId = "+this.id+"\n state = "+this.state;
		return result;
	}
	
	// -- get methodss
	
	
	public String getSId(){
		return this.id;
	}
	
			
}		
