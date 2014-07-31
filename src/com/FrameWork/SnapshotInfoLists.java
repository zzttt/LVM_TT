package com.FrameWork;

import java.io.Serializable;

public class SnapshotInfoLists  implements Serializable{
	private String id; // Snapshot Identifier
	private String status; // Snapshot status
	private String date;
	private String snapshotName;
	private String lv_size;
	private String cow_table_size;
	private String path; // Snapshot이 존재하는 경로
	
	public SnapshotInfoLists(String uid, String status, String date, String snapshotName , String lv_size , String cow_table_size , String path){
		this.id = uid;
		this.status = status;
		this.date = date;
		this.snapshotName = snapshotName;
		this.lv_size = lv_size;
		this.cow_table_size = cow_table_size;
		this.path = path;
	}
	

	public String getSnapshotId(){
		return this.id;
	}
	
	
	public String getStatus(){
		return this.status;
	}
	
	public String getDate(){
		return this.date;
	}
	
	public String getSnapshotName(){
		return this.snapshotName;
	}
	
	public String getLvSize(){
		return this.lv_size;
	}
	
	public String getCowTableSize(){
		return this.cow_table_size;
	}
	
	public String getPath(){
		return this.path;
	}
	
	
}
