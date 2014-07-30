package com.FrameWork;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class InstalledAppInfo {
	 
	private Context context;
	private String appname = "";
    private String pname = "";
    private String versionName = "";
    private int versionCode = 0;
    private Drawable icon;
    
    public InstalledAppInfo() {
    	
    }
    
    public InstalledAppInfo(Context context) {
    	super();
    	this.context = context;
    }
    

     private void prettyPrint() {
         //출력
    	 Log.v("LOG", appname + "   " + pname + "   " + versionName + "   " + versionCode);
     }
     
     private ArrayList<InstalledAppInfo> getPackages() {
         ArrayList<InstalledAppInfo> apps = getInstalledApps(false); /* false = no system packages */
         final int max = apps.size();
         for (int i=0; i<max; i++) {
             apps.get(i).prettyPrint();
         }
         return apps;
     }
  
     private ArrayList<InstalledAppInfo> getInstalledApps(boolean getSysPackages) {
     	
         ArrayList<InstalledAppInfo> res = new ArrayList<InstalledAppInfo>(); 
         
         /* 여기 ArrayList에 Packagename, appname 등의 데이터를 담는다.*/
         List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);

         for(int i=0;i<packs.size();i++) {
             PackageInfo p = packs.get(i);
             if ((!getSysPackages) && (p.versionName == null)) {
                 continue ;
             }
            
             InstalledAppInfo newInfo = new InstalledAppInfo();
             newInfo.appname = p.applicationInfo.loadLabel(context.getPackageManager()).toString();
             newInfo.pname = p.packageName;
             newInfo.versionName = p.versionName;
             newInfo.versionCode = p.versionCode;
             newInfo.icon = p.applicationInfo.loadIcon(context.getPackageManager());
             res.add(newInfo);
         }
         return res; 
     }
}
