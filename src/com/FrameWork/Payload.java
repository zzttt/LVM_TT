package com.FrameWork;

import java.io.Serializable;

/*
 * 작성일 : 14.06.24 
 * 작성자 : 조영민
 * 
 */

public class Payload implements Serializable {

	private int opCode = -1;
	private int infoCode = -1;
	private String authCode = null;

	private Snapshot snapshot;
	
	// constuctor
	/**
	 * 서버와 통신에 이용되는 payload 에 필요한 정보를 담음.
	 * @param opCode : 동작코드 ( 1 : 파일전송 / 2 : 파일 다운로드 / 3 : 파일정보 읽기 / 
	 */
	public Payload(){
		
	}
	
	/**
	 * 서버와 통신에 이용되는 payload 에 필요한 정보를 담음.
	 * @param opCode : 동작코드 ( 1 : 파일전송 / 2 : 파일 다운로드 / 3 : 파일정보 읽기
	 */
	public Payload(int opCode){
		this.opCode = opCode;
	}
	
	/**
	 * 서버와 통신에 이용되는 payload 에 필요한 정보를 담음.
	 * @param opCode : 동작코드 ( 1 : 파일전송 / 2 : 파일 다운로드 / 3 : 파일정보 읽기
	 * @param authCode : 인증코드 ( mobile 에서 생성 )
	 */
	public Payload(int opCode, String authCode){
		this.opCode = opCode;
		this.authCode = authCode;
	}
	
	public int getOpCode(){
		return this.opCode;
	}
	
	public String getAuth(){
		return this.authCode;
	}
	
	public int getInfoCode(){
		return this.infoCode;
	}
	
	
	
	//  set operations
	public void setOpCode(int opCode){
		this.opCode = opCode;
	}
	
	public void setAuth(String authCode){
		this.authCode = authCode;
	}
	
	public void setSnapshot(Snapshot snapshot){
		this.snapshot = snapshot;
	}
	
	public void setInfoCode(int infoCode){
		this.infoCode = infoCode;
	}
	
}
