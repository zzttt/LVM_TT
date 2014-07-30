package com.FrameWork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ComExec {

	private String command;
	private Process p;
	private InputStream is;
	private OutputStream os;
	private BufferedReader br;
	
	
	public ComExec(){
		try {
			this.p = new ProcessBuilder("su").start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
}
