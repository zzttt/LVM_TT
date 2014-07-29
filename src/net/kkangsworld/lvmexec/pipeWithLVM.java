package net.kkangsworld.lvmexec;

import java.io.BufferedReader;
import java.io.FileReader;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/* 이쪽 pipeWithLVM modifying */

public class pipeWithLVM {

	/* Use for timetraveler package */
	public static final String RESULTFIFO = "/data/data/com.example.timetraveler/result_pipe";
	public static final String CMDFIFO = "/data/data/com.example.timetraveler/cmd_pipe";
	public static final String COPYFIFO = "/data/data/com.example.timetraveler/copy_pipe";

	private NativePipe nativepipe;
	private ResultReader resultReader;
	private Handler rh;
	private int noneReader = 1;
	public pipeWithLVM() {
		//constructor
		nativepipe = new NativePipe(); //Native comm init
		nativepipe.createPipe();
		resultReader = new ResultReader(); //ResultReader thread init;
		resultReader.start();
		//readFromPipe(); //read도 동시에 실행
	}

	public pipeWithLVM(int noneReader) {
		//constructor
		nativepipe = new NativePipe(); //Native comm init
		nativepipe.createPipe();
	}

	public pipeWithLVM(Handler rh) {
		nativepipe = new NativePipe();
		nativepipe.createPipe();
		this.rh = rh;
		resultReader = new ResultReader(rh); //ResultReader thread init;
		resultReader.start();
		//readFromPipe(); //read도 동시에 실행
	}

	public void ActionWritePipe(String command) {

		if(!resultReader.isAlive()) {
			//resultReader = new ResultReader(rh); //ResultReader thread init;
			//resultReader.start();
		}
		nativepipe.writePipe(command);

	}

	public void ActionCopystartPipe() {
		nativepipe.copystartPipe();
	}

	public void ActionGetPipe() {
		//readFromPipe();
		//String temp = nativepipe.getPipe();
		//Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_SHORT).show();
	}

}