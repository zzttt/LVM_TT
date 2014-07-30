package net.kkangsworld.lvmexec;

public class NativePipe {

	//test init
	public native String getMsg();
	public native String getPipe();
	public native String test_getPipe();
	public native int createPipe();
	public native int writePipe(String command);
	public native int copystartPipe();

	static
	{
		System.loadLibrary("LVMExec");

	}
}