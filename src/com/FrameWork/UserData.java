package com.FrameWork;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;


public class UserData implements Serializable{

	private String userCode; // authCode �� ����
	private ArrayList<Snapshot> sList ; // Snapshot ����Ʈ�� �����Ѵ�.
	
	public UserData(String userCode){
		this.userCode = userCode;
		this.sList = new ArrayList<Snapshot>();
		
	}
	
	public void initUser(){
		
	}	
	
	public void setUserOnDisk(String userCode){
		// User�����͸� ���� ����Ʈ�� ����
	}
	
	
	public void addSnapShot(Snapshot s){
		sList.add(s);
	}
	

	public String getUserCode(){
		return this.userCode;
	}
	
	public int getSnapshotCnt(){
		return this.sList.size();
	}
	
	public ArrayList<Snapshot> getAllSnapshots(){
		return this.sList;
	}
	
}
