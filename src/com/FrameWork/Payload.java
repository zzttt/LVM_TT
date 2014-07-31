package com.FrameWork;

import java.io.Serializable;

/*
 * �ۼ��� : 14.06.24 
 * �ۼ��� : ������
 * 
 */

public class Payload implements Serializable {

	private int opCode = -1;
	private int infoCode = -1;
	private String authCode = null;

	private Snapshot snapshot;
	
	// constuctor
	/**
	 * ������ ��ſ� �̿�Ǵ� payload �� �ʿ��� ������ ����.
	 * @param opCode : �����ڵ� ( 1 : �������� / 2 : ���� �ٿ�ε� / 3 : �������� �б� / 
	 */
	public Payload(){
		
	}
	
	/**
	 * ������ ��ſ� �̿�Ǵ� payload �� �ʿ��� ������ ����.
	 * @param opCode : �����ڵ� ( 1 : �������� / 2 : ���� �ٿ�ε� / 3 : �������� �б�
	 */
	public Payload(int opCode){
		this.opCode = opCode;
	}
	
	/**
	 * ������ ��ſ� �̿�Ǵ� payload �� �ʿ��� ������ ����.
	 * @param opCode : �����ڵ� ( 1 : �������� / 2 : ���� �ٿ�ε� / 3 : �������� �б�
	 * @param authCode : �����ڵ� ( mobile ���� ���� )
	 */
	public Payload(int opCode, String authCode){
		this.opCode = opCode;
		this.authCode = authCode;
	}
	
	public Snapshot getSnapshot(){
		return this.snapshot;
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
