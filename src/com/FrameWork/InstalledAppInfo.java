package com.FrameWork;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class InstalledAppInfo implements Serializable {
	 
	private static final String LOGTAG = "InsAppInfo";
	private static final String SavedFileName = "AppListFile";
	private Context context;
	private String appname = "";
    private String pname = "";
    private String versionName = "";
    private int versionCode = 0;
    private long installDate = 0;
    private String installDateStr = "";
    //private Drawable icon;
    
    public InstalledAppInfo() {
    	
    }
    
    public InstalledAppInfo(Context context) {
    	super();
    	this.context = context;
    }
    
    public synchronized ArrayList<InstalledAppInfo> ReadAppInfo(String SnapshotDate) {
    	HashMap<String, ArrayList> readAppMap = null;
    	Log.d(LOGTAG, "DeserialiZ AppInfo");
    	try {
    		File AppListFile = new File(context.getDir("data", context.MODE_PRIVATE), SavedFileName);
    		FileInputStream fis = new FileInputStream(AppListFile);
    		ObjectInputStream ois = new ObjectInputStream(fis);
    			
    		try {
				readAppMap = (HashMap<String, ArrayList>) ois.readObject();
    			ois.close();
    			fis.close();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	} catch (FileNotFoundException e) {
    		
    	} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	Set set = readAppMap.entrySet();
    	Iterator ite = set.iterator();
    	ArrayList<InstalledAppInfo> readAppInfoList = new ArrayList<InstalledAppInfo>();
    	
    	while(ite.hasNext()) {
    		Map.Entry mapE = (Map.Entry)ite.next();
    		//Log.d(LOGTAG, "read result : "+mapE.getKey()+" & V : "+mapE.getValue());
    		if(mapE.getKey().equals(SnapshotDate))
    			readAppInfoList = (ArrayList<InstalledAppInfo>)mapE.getValue();
    		break;
    	}
    	Log.d(LOGTAG, "read al size : "+readAppInfoList.size());
    	/*for(int i=0;i<readAppInfoList.size();i++)
    		readAppInfoList.get(i).resultPrint();*/
    	
    	return readAppInfoList;
    }

    public synchronized void resultToSaveFile(String todayIsName) {
         //Hashmap에 app list (getPackages())저장
    	 HashMap<String, ArrayList> AppMap = new HashMap<String, ArrayList>();
    	 AppMap.put(todayIsName, getPackages());
    	 if(AppMap.isEmpty()) {
    		Log.d(LOGTAG, "AppMap is empty");
    		 return;
    	 }
    	 else 
    		Log.d(LOGTAG, "APpMap size : "+AppMap.size());

    	 File AppListFile = new File(context.getDir("data", context.MODE_PRIVATE), SavedFileName);    
    	 ObjectOutputStream outputStream = null;
    	 
    	 try {
    		FileOutputStream fos = new FileOutputStream(AppListFile);
    		outputStream = new ObjectOutputStream(fos);
			
    		outputStream.writeObject(AppMap);
	    	outputStream.flush();
	    	outputStream.close();
	    	fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(LOGTAG, e.getMessage());
		}
    	
    	 Log.d(LOGTAG, "Saved AppListFile.");
    	
    	 
     }
     
     public void resultPrint() {
    	 Log.v(LOGTAG, "App :: "+appname + "   " + pname + "   " + versionName + "   " + versionCode+"   "+installDate+"STR:"+installDateStr);
     }
     
     public String resultOfAppNamePrint() {
    	 return appname;
     }
     
     public String resultOfPackagesNamePrint() {
    	 return pname;
     }
     
     public String getInstallTimePrint() {
    	 return installDateStr;
     }
     
     public String getAppNameFromPName(String pname) {
    	 if(pname == this.pname)
    		 return appname;
		return "None";
     }
     
     private ArrayList<InstalledAppInfo> getPackages() {
         ArrayList<InstalledAppInfo> apps = getInstalledApps(false); /* false = no system packages */
         final int max = apps.size();
         for (int i=0; i<max; i++) {
             apps.get(i).resultPrint();
         }
         
         //결과 리턴 of ArrayList
         return apps;
     }
  
     private ArrayList<InstalledAppInfo> getInstalledApps(boolean getSysPackages) {
     	
         ArrayList<InstalledAppInfo> res = new ArrayList<InstalledAppInfo>(); 
         
         /* 여기 ArrayList에 Packagename, appname 등의 데이터를 담는다.*/
         List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);
         for(int i=0;i<packs.size();i++) {
             PackageInfo p = packs.get(i);
             /*if ((!getSysPackages) && (p.versionName == null)) {
                 continue ;
             }*/
             if((packs.get(i).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1)
            	 continue;
            
             InstalledAppInfo newInfo = new InstalledAppInfo();
             newInfo.appname = p.applicationInfo.loadLabel(context.getPackageManager()).toString();
             newInfo.pname = p.packageName;
             newInfo.versionName = p.versionName;
             newInfo.versionCode = p.versionCode;
      
             try {
				newInfo.installDate = context.getPackageManager().getPackageInfo(newInfo.pname, 0).firstInstallTime;
				Log.d("TTT", String.valueOf(newInfo.installDate));
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
             
             newInfo.installDateStr = getDate(newInfo.installDate);
         
   
             //newInfo.icon = p.applicationInfo.loadIcon(context.getPackageManager());
             res.add(newInfo);
         }
         return res; 
     }
     
     private static String getDate(long datetime) {
    	    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 
    	 	//DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
    	    Calendar calendar = Calendar.getInstance();    
    	    calendar.setTimeInMillis(datetime); 
    	    String strDate = formatter.format(calendar.getTime());
    	 
    	    Log.d("ASD", strDate+"TIME:"+datetime);
    	    
    	    return strDate;
    	 }
}
