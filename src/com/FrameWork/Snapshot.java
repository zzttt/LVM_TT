package com.FrameWork;

import java.io.File;
import java.io.Serializable;

/*
 * �ۼ��� : 14.06.24 
 * �ۼ��� : ������
 * 
 */

public class Snapshot extends UserData implements Serializable{
	
	private String id; // Snapshot Identifier
	private int ssTotalCnt;  // Snapshot �� ���� ����� ���ҵ� ����
	private String status; // Snapshot status
	private String date;
	private String snapshotName;
	private String lv_size;
	private String cow_table_size;
	private String path; // Snapshot�� �����ϴ� ���
	
	private String appChanged; // ���� ���泻��
	private String userDataChanged; // ����� ������ ���泻�� 
	private String settingValChanged; // ���� �� ���泻��

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
	 * @param date ������ ���� ��¥
	 * @param path �������� �����ϴ� ���丮 ���
	 */
	public Snapshot(String userCode,  String id, String status, String date, String path){
		super(userCode);
		
		this.id = id;
		this.status = status;
		this.date = date;
		this.path = path;

	}
	
	/**
	 * 
	 * @param changedItem : ����� �׸��� ������Ʈ
	 */
	public void setAppChanged(String changedItem){ 
		this.appChanged = changedItem;
	}
	
	public void setUserDataChanged(String changedItem){ 
		this.userDataChanged = changedItem;
	}
	
	public void setSettingValChanged(String changedItem){ 
		this.settingValChanged = changedItem;
	}
	
	public void setName(String name){
		this.snapshotName = name;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public void setStatus(String status){
		this.status = status;
	}
	
	public void setDate(String date){
		this.date = date;
	}
	
	public void setPath(String path){
		this.path = path;
	}
	
	public void setLvSize(String size){
		this.lv_size = size;
	}

	public void setCowTableSize(String size){
		this.cow_table_size = size;
	}
	
	public void setInfoLists(SnapshotInfoLists sInfoLists){
		this.cow_table_size = sInfoLists.getCowTableSize();
		this.date = sInfoLists.getDate();
		this.id = sInfoLists.getSnapshotId();
		this.lv_size = sInfoLists.getLvSize();
		this.snapshotName = sInfoLists.getSnapshotName();
		this.path = sInfoLists.getPath();
		this.status = sInfoLists.getStatus();
		
	}

	// -- get methodes
	
	
	public String getLVsize(){
		return this.lv_size;
	}
	
	public String getCowTableSize(){
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

	public String getName(){
		return this.snapshotName;
	}
	
	public String getAppAlteration(){
		return this.appChanged;
	}
	
	public String getUserDataAlteration(){
		return this.userDataChanged;
	}
	
	public String getSettingValAlteration(){
		return this.settingValChanged;
	}
			
}		
