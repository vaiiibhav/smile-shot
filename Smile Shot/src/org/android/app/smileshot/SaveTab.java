package org.android.app.smileshot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class SaveTab extends Activity {
	EditText name;
	RadioGroup mRadioGroup;
	CheckBox  upload;
	Button saveB,cancelB;
	
	String location= "SmileShot";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.savetab);
		
		name = (EditText) findViewById(R.id.Name);
		mRadioGroup = (RadioGroup) findViewById(R.id.Location);
		//toSystem = (RadioButton) findViewById(R.id.CheckBox01);
		//toSdcard = (RadioButton) findViewById(R.id.CheckBox02);
		upload = (CheckBox ) findViewById(R.id.CheckBox03);
		saveB = (Button) findViewById(R.id.Confirm);
		cancelB = (Button) findViewById(R.id.Cancel);
		
		mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				DisplayToast("The image will be saved in '/sdcard/SmileShot'.");
				
			}
		});
		
		saveB.setOnClickListener(new Button.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (name.getText().toString().length()==0) {
					DisplayToast("Please enter a name for this picture!");
					return;
				}
				
				boolean isSDCardPresent = android.os.Environment.getExternalStorageState()
												  .equals(android.os.Environment.MEDIA_MOUNTED);
				
				if (!isSDCardPresent) {
					DisplayToast("Please check your sdcard whether mounted!");
					return;
				}
//				
				File file = new File(Environment.getExternalStorageDirectory(), location);
			    if (!file.exists()) {
			        if (!file.mkdirs()) {
			            Log.e("TravellerLog :: ", "Problem creating Image folder");
			            return ; //dir create failed!
			        }
			    }
				File myPic = new File (file.getAbsolutePath()+"/"+name.getText().toString()+".jpg");
				try {

					if (!myPic.exists()) {
						Log.e("Smile Shot", "Create new image file");
						myPic.createNewFile();
					}
					else if (!upload.isChecked()){
						DisplayToast("Image "+name.getText().toString()+".jpg  is existed,choose other name.");
						return;
					}
						
		  
					FileOutputStream fos = new FileOutputStream(myPic);
										
					if (Preview.snapShot!=null) {		
						fos.write(Preview.snapShot, 0, Preview.snapShot.length);
						
						Preview.snapShot = null;
						System.gc();
					}
					else {
						DisplayToast("Something wrong with bitmap data,sorry!");
						return ;
					}
									
					fos.flush();
					fos.close();
					DisplayToast("Saved in '/sdcard/SmileShot/' successfully.");
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Intent intent = new Intent();
				intent.setClass(SaveTab.this, MyCamera.class);
				startActivity(intent);
				SaveTab.this.finish();
				
				if (upload.isChecked()) {
				
					Uri attach=Uri.parse("file://"+myPic.getAbsolutePath()); 
					Intent intent1 = new Intent(Intent.ACTION_SEND); 
					intent1.setType("image/jpeg"); 
					intent1.putExtra(Intent.EXTRA_STREAM,attach); 
					startActivity(Intent.createChooser(intent1, "Share options"));
					saveB.setEnabled(false);

				}
				
			}
		});

		cancelB.setOnClickListener(new Button.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(SaveTab.this, MyCamera.class);
	    		startActivity(intent);
	    		SaveTab.this.finish();
	    		// Reset flags
	    		Preview.isAutoShot = false;
				
				Preview.snapShot = null;
				System.gc();
			}
		});
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			Intent intent = new Intent();
			intent.setClass(SaveTab.this, MyCamera.class);
    		startActivity(intent);
    		SaveTab.this.finish();
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	protected void DisplayToast(String string) {
		// TODO Auto-generated method stub
		Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
	}
	@Override
	protected void onResume() {
	 /**
	  * …Ë÷√Œ™∫·∆¡
	  */
	 if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
	  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	 }
	 super.onResume();
	}
}
