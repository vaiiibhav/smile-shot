package org.android.app.smileshot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.util.Log;
/**
 * Configure Information:
 * 		Sound Enable
 * 		Smile Enable
 * 		Upload Enable
 *      front Camera 
 * 		Smile paramater
 * 		user name
 * 		password
 * 
 * @author wangzm
 *
 */
public class CameraConfig {
	public boolean soundEnable = false;
	public boolean smileEnable = false;
	public boolean uploadEnable = false;
	public boolean frontCamera = false;
	public int smileParam = -1;
	public String userName = new String();
	public String passWord = new String();

	
	CameraConfig() {
		soundEnable = false;
		smileEnable = false;
		uploadEnable = false;
		frontCamera = false;
	}
	CameraConfig(String file) {
		
		File config = new File (file);
		try {		
			if (!config.exists()) {
				Log.e("Smile Shot", "Not find Config file!");
				return;
			}
			  
			BufferedReader fin = new BufferedReader(new FileReader(config));
			String buf;
			while((buf=fin.readLine())!=null) {
				String[] arr = buf.split(" := ");
				
				if (arr[0].equals("soundEnable")) {
					soundEnable = Boolean.valueOf(arr[1]);
				}
				if (arr[0].equals("smileEnable")) {
					smileEnable = Boolean.valueOf(arr[1]);
				}
				if (arr[0].equals("uploadEnable")) {
					uploadEnable = Boolean.valueOf(arr[1]);
				}
				if (arr[0].equals("frontCamera")) {
					frontCamera = Boolean.valueOf(arr[1]);
				}
				if (arr[0].equals("smileParam") && arr[1]!=null) {
					smileParam = Integer.valueOf(arr[1]);
				}
				if (arr[0].equals("userName") && arr[1]!=null) {
					userName = arr[1];
				}
				if (arr[0].equals("passWord") && arr[1]!=null) {
					passWord = arr[1];
				}
							
			}
			fin.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
