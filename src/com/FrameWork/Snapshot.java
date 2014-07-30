package com.FrameWork;

import java.io.File;
import java.io.Serializable;

/*
 * 작성일 : 14.06.24 
 * 작성자 : 조영민
 * 
 */

public class Snapshot extends UserData implements Serializable{
	private String id; // Snapshot Identifier
	private int ssTotalCnt;  // Snapshot 이 분할 압축시 분할된 개수
	private int status; // Snapshot status
	private String date;
	private double lv_size;
	private double cow_table_size;
	private String path; // Snapshot이 존재하는 경로

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
	 * @param id // snapshot Id
	 * @param ssNumber
	 * @param status 
	 * @param date 스냅샷 생성 날짜
	 * @param path 스냅샷이 존재하는 디렉토리 경로
	 */
	public Snapshot(String userCode,  String id, int status, String date, String path){
		super(userCode);
		
		this.id = id;
		this.status = status;
		this.date = date;
		this.path = path;

	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public void setStatus(int status){
		this.status = status;
	}
	
	public void setDate(String date){
		this.date = date;
	}
	
	public void setPath(String path){
		this.path = path;
	}
	
	public void setLvSize(double size){
		this.lv_size = size;
	}

	public void setCowTableSize(double size){
		this.cow_table_size = size;
	}

	// -- get methodes
	
	
	public double getLVsize(){
		return this.lv_size;
	}
	
	public double getCowTableSize(){
		return this.cow_table_size;
	}

	public String getPath(){
		return this.path;
	}
	
	public String getDate(){
		return this.date;
	}
	
	public String getSId(){
		return this.id;
	}
	
			
}		
