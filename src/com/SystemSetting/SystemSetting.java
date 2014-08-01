package com.SystemSetting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.acl.LastOwnerException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;

import android.R.integer;
import android.R.string;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.provider.MediaStore.Files;
import android.text.TextUtils.StringSplitter;
import android.util.Log;
import android.widget.Toast;

public class SystemSetting {

	public static int readcount = 0; // password length

	public static final String TAG = "File_I/O";

	public static final String appdb_path = Environment.getDataDirectory() + "/data/com.example.timetraveler/database/";
	public static final String appdb_password_name = "password.key";
	public static final String appdb_gesture_name = "gesture.key";
	public static final String appdb_wifi_name = "wpa_supplicant.conf";

	public static final String system_path = Environment.getDataDirectory() + "/system/";
	public static final String password_name = "password.key";
	public static final String gesture_name = "gesture.key";

	public static final String wifi_path = Environment.getDataDirectory() + "/misc/wifi/";
	public static final String wifi_name = "wpa_supplicant.conf";

	public static final String snapshot_path = "/sdcard/ssdir";
	// snapshot_path + snapshot_name(ex:20140802030) + usersystem / userdata / usersd//
	public static final String snapshot_password_name = "password.key";
	public static final String snapshot_gesture_name = "gesture.key";
	
	public static File appdb_dir = makeDirectory(appdb_path); // ���� �����ǰ�
	public static File appdb_password_file = makeFile(appdb_dir, appdb_path + appdb_password_name); // ���� ���� �ǰ�
	public static File appdb_gesture_file = makeFile(appdb_dir, appdb_path
			+ appdb_gesture_name); // ���� ���� �ǰ�
	public static File appdb_wifi_file = makeFile(appdb_dir, appdb_path + appdb_wifi_name); // ����
																				// ����
																				// �ǰ�

	public static File system_dir = makeDirectory(system_path); // ���� �����ǰ�
	public static File passwordkey_file = makeFile(system_dir, system_path + password_name); // ����
																				// ����
																				// �ǰ�
	public static File gesturekey_file = makeFile(system_dir, system_path + gesture_name); // ����
																				// ����
																				// �ǰ�

	public static File wifi_dir = makeDirectory(wifi_path); // ���� �����ǰ�
	public static File wifi_file = makeFile(wifi_dir, wifi_path + wifi_name); // ���� ���� �ǰ�

	Process process;

	public void save_file() {		// ����ٰ� �׽�Ʈ
		
// 		testing....   //		
//		wifi_checking();
//		save_wifi();
//		wifi_checking();
//		restore_wifi();
		
//		restore_password();
//		password_checking();	
		save_password();			
		password_checking();	
		
//		save_gesture();
//		gesture_checking();
//		restore_gesture();
//		gesture_checking();

	}

	public void set_permission(){					// ���� ���� / ���� �Ҷ� set_permission ����ߵ� //
		
		Process chperm;
		    try {
		        chperm=Runtime.getRuntime().exec("su");
		          DataOutputStream os = 
		              new DataOutputStream(chperm.getOutputStream());
		            os.writeBytes("chmod 777 /data/system\n");
		            os.flush();
		            os.writeBytes("chmod 777 /data/misc\n");
		            os.flush();
		            os.writeBytes("chmod 777 /data/misc/wifi\n");
		            os.flush();
		            os.writeBytes("chmod 777 /data/system/gesture.key\n");
		            os.flush();
		            os.writeBytes("chmod 777 /data/system/password.key\n");
		            os.flush();
		            os.writeBytes("chmod 777 /data/misc/wifi/wpa_supplicant.conf\n");
		            os.flush();
		            os.writeBytes("chmod 777 /data/data/com.example.timetraveler/database/password.key\n");
		            os.flush();
		            os.writeBytes("chmod 777 /data/data/com.example.timetraveler/database/gesture.key\n");
		            os.flush();
		            os.writeBytes("chmod 777 /data/data/com.example.timetraveler/database/wpa_supplicant.conf\n");
		            os.flush();

		              os.writeBytes("exit\n");
		              os.flush();
	
		              chperm.waitFor();
	
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    } catch (InterruptedException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
	}
	/* ------------------------------------------------------------------ */
	public boolean wifi_checking() { // /system/gesture.key ��
		// app/database/gesture.key ��
		boolean result = compare(wifi_file, appdb_wifi_file);

		if (result == true) {
			// �н����尡 �Ȱ��� //
			Log.i("test", "true");
			return result;
		} else {
			// �н����尡 �ٸ� //
			Log.i("test", "false");
			return result;
		}
	}
	
	public boolean gesture_checking() { // /system/gesture.key ��
										// app/database/gesture.key ��
		boolean result = compare(gesturekey_file, appdb_gesture_file);

		if (result == true) {
			// �н����尡 �Ȱ��� //
			Log.i("test", "true");
			return result;
		} else {
			// �н����尡 �ٸ� //
			Log.i("test", "false");
			return result;
		}
	}

	public boolean password_checking() { // /system/password.key ��
											// app/database/password.key ��
		boolean result = compare(passwordkey_file, appdb_password_file);
		if (result == true) {
			// �н����尡 �Ȱ��� //
			Log.i("test", "true");
			return result;
		} else {
			// �н����尡 �ٸ� //
			Log.i("test", "false");
			return result;
		}
	}
	
	/* ------------------------------------------------------------------ */
	public void save_wifi() { // wifi file app_database�� ���� // 
		get_root();
		int file_length = (int) wifi_file.length();
		Log.i("length", "password_length : " + file_length);
		byte[] password_buffer = new byte[file_length];
		password_buffer = readFile(wifi_file, file_length);
		writeFile(appdb_wifi_file, password_buffer);
	}
	

	
	public void restore_wifi(){	// database�� �ִ� wifi���� ���� //
		get_root();
		int file_length = (int) appdb_wifi_file.length();
		Log.i("length", "password_length : " + file_length);
		byte[] password_buffer = new byte[file_length];
		password_buffer = readFile(appdb_wifi_file, file_length);
		writeFile(wifi_file, password_buffer);
	}
	
	/* ------------------------------------------------------------------ */
	public void save_password() { // �н��������� app database�� ���� ok //
		get_root();
		int file_length = (int) passwordkey_file.length();
		Log.i("length", "password_length : " + file_length);
		byte[] password_buffer = new byte[file_length];
		password_buffer = readFile(passwordkey_file, file_length);
		writeFile(appdb_password_file, password_buffer);
	}

	public void restore_password(){
		get_root();
		int file_length = (int) appdb_password_file.length();
		Log.i("length", "password_length : " + file_length);
		byte[] password_buffer = new byte[file_length];
		password_buffer = readFile(appdb_password_file, file_length);
		writeFile(passwordkey_file, password_buffer);
	}

	/* ------------------------------------------------------------------ */
	public void save_gesture() { // ���������� app database�� ���� //
		get_root();
		int file_length = (int) gesturekey_file.length();
		Log.i("length", "gesture_length : " + file_length);
		byte[] gesture_buffer = new byte[file_length];
		gesture_buffer = readFile(gesturekey_file, file_length);
		writeFile(appdb_gesture_file, gesture_buffer);
	}
	
	public void restore_gesture(){ // ���������� ���� //
		get_root();
		int file_length = (int) appdb_gesture_file.length();
		Log.i("length", "gesture_length : " + file_length);
		byte[] gesture_buffer = new byte[file_length];
		gesture_buffer = readFile(appdb_gesture_file, file_length);
		writeFile(gesturekey_file, gesture_buffer);
	}
	/* ------------------------------------------------------------------ */
	
	private boolean writeFile(File file, byte[] file_content) {
		boolean result;
		FileOutputStream fos;
		if (file != null && file.exists() && file_content != null) {
			try {
				fos = new FileOutputStream(file);
				try {
					fos.write(file_content);
					fos.flush();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	private byte[] readFile(File file, int length) { // ������ �о byte[]�� ��ȯ //
		byte[] buffer = new byte[length];
		if (file != null && file.exists()) {
			Log.i(TAG, getAbsolutePath(file));
			try {
				FileInputStream fis = new FileInputStream(file);
				fis.read(buffer);
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String s1 = new String(buffer, length);
		Log.i("buffer", s1); // ���� ���� ��� �����
		return buffer;
	}
	/* ------------------------------------------------------------------ */
	
	// /data/system/*.key vs ../app/database/*.key ��
	public boolean compare(File f1_file, File f2_file) {
		// compare_password(appdb_file,passwordkey_file); // ���� ���� �ΰ� �־ �����
		FileInputStream f1 = null;
		FileInputStream f2 = null;
		try {
			f1 = new FileInputStream(f1_file);
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			f2 = new FileInputStream(f2_file);
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		int f1_length = (int) f1_file.length();
		int f2_length = (int) f2_file.length();

		byte[] f1_buf = new byte[f1_length];
		byte[] f2_buf = new byte[f2_length];

		try {
			f1.read(f1_buf);
			// String s1 = new String(f1_buf);
			// Log.i("f1", s1); // f1�� �ִ� ������ Ȯ��
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			f2.read(f2_buf);
			// String s2 = new String(f2_buf);
			// Log.i("f2", s2); // f2�� �ִ� ������ Ȯ��
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (Arrays.equals(f1_buf, f2_buf)) {
			return true;
		} else {
			return false;
		}

	}
	/* ------------------------------------------------------------------ */
	// ���� ����
	private static File makeDirectory(String dir_path) {
		File direction = new File(dir_path); // ���� ��θ� �Ѱ��ְ�
		if (!direction.exists()) // ������ �������� ������
		{
			direction.mkdirs(); // ���� ������ְ�
			Log.i(TAG, "!dir.exists");
		} else { // ������ �׳�
			Log.i(TAG, "dir.exists");
		}
		return direction; // ��θ� �Ѱ��ش�.
	}

	// ���� ���� //
	private static File makeFile(File dir, String file_path) {
		File file = null;
		boolean isSuccess = false;
		Log.i(TAG, "file_path"+file_path);
		if (dir.isDirectory()) { // ���� ���
			file = new File(file_path);
			if (file != null && !file.exists()) {
				Log.i(TAG, "!file.exists");
				try {
					isSuccess = file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					Log.i(TAG, "���ϻ��� ���� = " + isSuccess);
				}
			} else {
				Log.i(TAG, "file.exists");
			}
		}
		return file;
	}
	/* ------------------------------------------------------------------ */
	public void get_root() { // ���Ͽ� �����Ϸ��� ��Ʈ���� �ʿ� ���� ��������
		try {
			Log.i(TAG, "susu");
			process = Runtime.getRuntime().exec("su"); // root ���� �ο�
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// ������ �������� //
	private String getAbsolutePath(File file) {
		return "" + file.getAbsolutePath();
	}

}
