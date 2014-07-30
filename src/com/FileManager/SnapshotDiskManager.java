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
	 * 데이터에 따라 UI 수정이 핋요할 경우 
	 * @param ssHome
	 * @param uiHandler
	 */
	public SnapshotDiskManager(String ssHome, Handler uiHandler){
		this.ssHome = ssHome;
		this.uiHandler = uiHandler;
	}
	
	/**
	 * 스냅샷이 존재하는 디렉토리를 날짜별로 반환한다
	 * @return
	 */
	public File[] getSnapshotList(){
		File f = new File(ssHome);
		File[] ssDirList = f.listFiles();
	
		return ssDirList;
		
	}
	
	/**
	 * 해당 날짜에 해당하는 Snapshot 파일들을 리턴한다.
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
	 * ArrayList 에 ssHome 으로 설정된 Directory 내의 모든 파일들을 담는다.
	 * 
	 */
	public ArrayList<File> getAllFilesInDepth(){
		ArrayList<File> FileLists = new ArrayList<File>();
		
		//Snapshot Home Dir
		File f = new File(this.ssHome);
		File[] fListInDir = f.listFiles();
		
		for(File tmpF : fListInDir){
			
			if(tmpF.isDirectory()){ // 파일이 디렉토리일때 > 한 depth 더 들어간다.
				getAllFilesInDepth(tmpF.getPath(), FileLists); // 
			}else{ // 일반 파일일 경우
				FileLists.add(tmpF);
			}
		}
		
		for(File tmpF : fListInDir)
			FileLists.add(tmpF);
		
		return FileLists;
	}

	/**
	 * 파라미터로 받는 FileLists ArrayList 에 ssHome 내의 파일 리스트를 등록시킨다.
	 * 
	 * @param ssHome
	 * @param FileLists
	 */
	public void getAllFilesInDepth(String ssHome, ArrayList<File> FileLists){
		//Snapshot Home Dir
		File f = new File(ssHome);
		File[] fListInDir = f.listFiles();

		for(File tmpF : fListInDir){
			
			if(tmpF.isDirectory()){ // 파일이 디렉토리일때 > 한 depth 더 들어간다.
				getAllFilesInDepth(tmpF.getPath(), FileLists );
			}else{ // 일반 파일일 경우
				FileLists.add(tmpF);
			}
		}
		
		return;
	}
	
	
	/**
	 * fileList에서 최근 수정된 파일 리스트 3개를 ArrayList로 리턴
	 * @param fileList 
	 * @return
	 */
	public ArrayList<File> getLatModified(ArrayList<File> fileList){
		ArrayList<File> fL = new ArrayList<File>();
		
		for(File f : fileList){
			if(fL.size() <= 3){ // 3개까지는 무조건등록
				fL.add(f);
			}else{
				long thirdOrder = Long.MAX_VALUE;
				long lm = 0;
				File tFile = null; // 임시 파일변수
				
				for(File ff : fL){
					// lm : 3개 항목중 가장 이전에 수정된 항목 
					lm = ff.lastModified();
					if(thirdOrder > lm ){
						thirdOrder = lm;
						tFile = ff;
					}
				}
				if(f.lastModified() > thirdOrder){ // 3 항목 중 가장 이전항목보다 후에 수정된 파일일 경우
					fL.remove(tFile); // tFile 객체를 지우고 새로운 f 를 넣어 size 3 을 유지
					fL.add(f);
				}
				
			}
			
		}
		
		return fL;
	}
	
	
	
}
