package com.FileManager;

import java.io.File;
import java.util.ArrayList;

import com.example.timetraveler.MainActivity;

import android.os.Handler;
import android.util.Log;

public class SnapshotDiskManager {
	
	private String ssHome = null;
	private Handler uiHandler = null;
	/**
	 * 
	 * @param ssHome HomeDirectory
	 */
	public SnapshotDiskManager(String ssHome){
		this.ssHome = ssHome;
	}
	
	/**
	 * �����Ϳ� ���� UI ������ ������ ��� 
	 * @param ssHome
	 * @param uiHandler
	 */
	public SnapshotDiskManager(String ssHome, Handler uiHandler){
		this.ssHome = ssHome;
		this.uiHandler = uiHandler;
	}
	
	/**
	 * �������� �����ϴ� ���丮�� ��¥���� ��ȯ�Ѵ�
	 * @return
	 */
	public File[] getSnapshotList(){
		File f = new File(ssHome);
		File[] ssDirList = f.listFiles();
		ArrayList<File> list = new ArrayList<File>();
		
		return ssDirList;
		
	}
	
	/**
	 * �ش� ��¥�� �ش��ϴ� Snapshot ���ϵ��� �����Ѵ�.
	 * @param date
	 * @return
	 */
	public File[] getSnapshotFiles(String date){
		File f = new File(ssHome+date+"/");
		File[] ssList = f.listFiles();
		
		return ssList;
	}
	
	
	public File[] getSnapshotFiles(){
		File f = new File(ssHome);
		File[] ssList = f.listFiles();
		return ssList;
	}
	
	
	// Snapshot input lists
	public String[] getSnapshotInfoList(){
		String[] s = new String[100];
		
		return s; 
	}
	
	
	// get All depth of Snapshot

	/**
	 * ArrayList �� ssHome ���� ������ Directory ���� ��� ���ϵ��� ��´�.
	 * 
	 */
	public ArrayList<File> getAllFilesInDepth(){
		ArrayList<File> FileLists = new ArrayList<File>();
		
		//Snapshot Home Dir
		File f = new File(this.ssHome);
		File[] fListInDir = f.listFiles();
		
		for(File tmpF : fListInDir){
			
			if(tmpF.isDirectory()){ // ������ ���丮�϶� > �� depth �� ����.
				getAllFilesInDepth(tmpF.getPath(), FileLists); // 
			}else{ // �Ϲ� ������ ���
				FileLists.add(tmpF);
			}
		}
		
		for(File tmpF : fListInDir)
			FileLists.add(tmpF);
		
		return FileLists;
	}

	/**
	 * �Ķ���ͷ� �޴� FileLists ArrayList �� ssHome ���� ���� ����Ʈ�� ��Ͻ�Ų��.
	 * 
	 * @param ssHome
	 * @param FileLists
	 */
	public void getAllFilesInDepth(String ssHome, ArrayList<File> FileLists){
		//Snapshot Home Dir
		File f = new File(ssHome);
		File[] fListInDir = f.listFiles();

		for(File tmpF : fListInDir){
			
			if(tmpF.isDirectory()){ // ������ ���丮�϶� > �� depth �� ����.
				getAllFilesInDepth(tmpF.getPath(), FileLists );
			}else{ // �Ϲ� ������ ���
				FileLists.add(tmpF);
			}
		}
		
		return;
	}
	
	
	/**
	 * fileList���� �ֱ� ������ ���� ����Ʈ 3���� ArrayList�� ����
	 * @param fileList 
	 * @return
	 */
	public ArrayList<File> getLatModified(ArrayList<File> fileList){
		ArrayList<File> fL = new ArrayList<File>();
		
		for(File f : fileList){
			if(fL.size() <= 3){ // 3�������� �����ǵ��
				fL.add(f);
			}else{
				long thirdOrder = Long.MAX_VALUE;
				long lm = 0;
				File tFile = null; // �ӽ� ���Ϻ���
				
				for(File ff : fL){
					// lm : 3�� �׸��� ���� ������ ������ �׸� 
					lm = ff.lastModified();
					if(thirdOrder > lm ){
						thirdOrder = lm;
						tFile = ff;
					}
				}
				if(f.lastModified() > thirdOrder){ // 3 �׸� �� ���� �����׸񺸴� �Ŀ� ������ ������ ���
					fL.remove(tFile); // tFile ��ü�� ����� ���ο� f �� �־� size 3 �� ����
					fL.add(f);
				}
				
			}
			
		}
		
		return fL;
	}
	
	
	
}
