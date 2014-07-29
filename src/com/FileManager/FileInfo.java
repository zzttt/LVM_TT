package com.FileManager;

public class FileInfo {

	private String fType, permission, fSize, mDate, mTime, fName;
	
	public FileInfo(){
		
	}
	
	public FileInfo(String fType, String fName){
		this.fType = fType;
		this.fName = fName;
	}

	public FileInfo(String fType, String permission,
			String mDate, String mTime, String fName) { // �Ϲ� ������ �ƴѰ�� Size �� ������ ����
		this.fType = fType;
		this.permission = permission;
		this.mDate = mDate;
		this.mTime = mTime;
		this.fName = fName;
	}
	
	public FileInfo(String fType, String permission, String fSize,
			String mDate, String mTime, String fName) {
		this.fType = fType;
		this.permission = permission;
		this.fSize = fSize;
		this.mDate = mDate;
		this.mTime = mTime;
		this.fName = fName;
	}

	public String getDate(){
		return this.mDate;
	}
	
	public String getTime(){
		return this.mTime;
	}
	
	public String getType(){
		if( this.fType.equals("."))
			return "sub";
		else
			return this.fType;
	}
	
	public String getPerm(){
		return this.permission;
	}
	
	public String getSize(){
		return this.fSize;
	}
	
	public String getName(){
		return this.fName;
	}
	
	
}
