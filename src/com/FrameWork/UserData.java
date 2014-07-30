package com.FrameWork;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class UserData {

	private String userCode; // authCode 와 동일
	private ArrayList<Snapshot> sList ; // Snapshot 리스트를 저장한다.
	
	public UserData(String userCode){
		this.userCode = userCode;
		this.sList = new ArrayList<Snapshot>();
		
	}
	
	public void initUser(){
		
	}	
	
	public void setUserOnDisk(String userCode){
		// User데이터를 파일 리스트로 보관
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
	
}
