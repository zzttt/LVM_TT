package com.FrameWork;

public class SnapshotInfoLists {
	private String id; // Snapshot Identifier
	private String status; // Snapshot status
	private String date;
	private String snapshotName;
	private double lv_size;
	private double cow_table_size;
	private String path; // Snapshot이 존재하는 경로
	
	public SnapshotInfoLists(String uid, String status, String data, String snapshotName , double lv_size , double cow_table_size , String path){
		this.id = uid;
		this.status = status;
		this.date = data;
		this.snapshotName = snapshotName;
		this.lv_size = lv_size;
		this.cow_table_size = cow_table_size;
		this.path = path;
	}
	

	public String getSnapshotId(){
		return this.id;
	}
	
	
	public String getStatus(){
		return this.getStatus();
	}
	
	public String getData(){
		return this.date;
	}
	
	public String getSnapshotName(){
		return this.snapshotName;
	}
	
	public double getLvSize(){
		return this.lv_size;
	}
	
	public double getCowTableSize(){
		return this.cow_table_size;
	}
	
	public String getPath(){
		return this.path;
	}
	
	
}
