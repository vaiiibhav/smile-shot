package org.android.app.smileshot;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

public class MyCamera extends Activity {
	
	private Preview mPreview;
	public static CameraConfig config = new CameraConfig();
	public String location= "SmileShot";
	//public static int DisplayWidth;
	//public static int DisplayHeight;
	
	
	/** AutoShot on Smile/Timer mode */
	public boolean isListening = true;	
	public static Thread autoShotListener = null;
	public static Thread smileShotListener = null;
	
	/** 
	 * Timer mode: 
	 * 		timer:= 5s 15s 30s 
	 */
	public static int secsToFinished = 0;
	public static boolean isCountDown = false;
	public MyCount timer;
	
	/**
	 *  For Setting Dialog: 
	 */
	public static CheckBox soundEnable,smileEnable;
	public static SeekBar smileParam;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the window title.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Read Preference information
        getConfig(config);
        
        mPreview = new Preview(this);
        setContentView(mPreview);
        
		addContentView(Preview.overlayer, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		// Create Work Directory: in /sdcard/SmileShot
		File file = new File(Environment.getExternalStorageDirectory(), location);
	    if (!file.exists()) {
	        if (!file.mkdirs()) {
	            Log.e("TravellerLog :: ", "Problem creating Image folder");
	            return ; //dir create failed!
	        }
	    }
	    
		if (Preview.isDetection && smileShotListener == null) {
			smileShotListener = new Thread(){
				@Override
				public void run() {
					//if (config.smileEnable || isCountDown) {
						Log.e("Mycamera", "smileShotListener");
						while(Preview.isDetection) {
							//if (Preview.isAutoShot && Preview.prefetchData!=null){
							if (Preview.isSmileShot && Preview.snapShot!=null){
								Preview.isSmileShot = false;
								handler1.sendEmptyMessage(0);
							}
						}
						Log.e("Mycamera", "SmileShot finished");
					//}
				}
			};
			smileShotListener.start();
		}
		Log.i("MyCamera", "Create");
		
		/*DisplayMetrics dm = new DisplayMetrics(); 
		getWindowManager().getDefaultDisplay().getMetrics(dm); 
		DisplayWidth = dm.widthPixels;
		DisplayHeight = dm.heightPixels;
		Log.i("MyCamera", "DisplayDensity: "+dm.density);
		Log.i("MyCamera", "DisplayMetrics: "+dm.widthPixels+"* "+dm.heightPixels);*/
    }
    
	private Handler  handler1 = new Handler (){
		@Override
        public void handleMessage(Message msg) {
			Log.e("MyCamera", "Smile Shot");
			
			showSaveDialog();
		}
	};
    	
	@Override
	public void finish() {
		Log.e("finish", "finish MyCamera");
		
		/*if (timer!=null) {
			timer.delete();
			timer = null;
		}
		*/
		waitForFinished();	
		super.finish();

	}
	/**
	 * Kill auto-shot listener thread
	 */
    private void waitForFinished() {
		// TODO Auto-generated method stub
    	Log.e("waitForFinished", "Here");
    	
    	if (timer!=null) {
			timer.delete();
			timer = null;
		}
		
		if (autoShotListener != null && autoShotListener.isAlive()){
			try {
				Log.e("waitForFinished", "Kill auto-shot listener thread.");
				isListening = false;
				
				autoShotListener.join();
				autoShotListener = null;
				
				isListening = true;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (smileShotListener != null){
			try {
				Log.e("waitForFinished", "Kill smile-shot listener thread.");
				boolean isDetection = Preview.isDetection;
				Preview.isDetection = false;
				
				smileShotListener.join();
				smileShotListener = null;
				
				Preview.isDetection = isDetection;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected void autoShot() {
		// TODO Auto-generated method stub   
    	
    	Log.e("autoShot",autoShotListener.isAlive()+"");
    	
		Dialog dialog = new AlertDialog.Builder(MyCamera.this)
							.setTitle("Hello").setMessage("Enjoy your picture?")
							.setPositiveButton("Save", 
									new DialogInterface.OnClickListener() {
										
										public void onClick(DialogInterface dialog, int which) {
											// TODO Auto-generated method stub
											Intent intent = new Intent();
											intent.setClass(MyCamera.this, SaveTab.class);
								    		startActivity(intent);
								    		MyCamera.this.finish();
								    		dialog.dismiss();
										}
									})
							.setNegativeButton("Discard", 
									new DialogInterface.OnClickListener() {
												
										public void onClick(DialogInterface dialog, int which) {
											// TODO Auto-generated method stub
											dialog.dismiss();	
											Preview.isAutoShot = false;
											Preview.snapShot = null;
											System.gc();
											
											Preview.mCamera.startPreview();
										}
									})
							.setCancelable(false).create();
		try {
		//	Stop autoShotListener thread.
		//waitForFinished();
		
		dialog.show();
		
		}catch (Exception e){
			Log.e("Dialog", "Failed to show autoShot dialog");
			//autoShot();
			DisplayToast("Sorry,something wrong with the dialog.");
			Preview.mCamera.startPreview();
		}
	}

	private void getConfig(CameraConfig conf) {
		// TODO Auto-generated method stub
		SharedPreferences settings = getSharedPreferences("Preference",Activity.MODE_PRIVATE);
		if (settings==null)
			Log.e("getConfig", "Setting is null");
		
		conf.soundEnable = settings.getBoolean("soundEnable", false);
		conf.smileEnable = settings.getBoolean("smileEnable", false);
		conf.uploadEnable = settings.getBoolean("uploadEnable", false);
		conf.frontCamera = settings.getBoolean("frontCamera", false);
		
		conf.smileParam = settings.getInt("smileParam", -1);
		conf.userName = settings.getString("userName", "");
		conf.passWord = settings.getString("passWord", "");		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	//TODO
    	switch (keyCode) {
    	case KeyEvent.KEYCODE_CAMERA:
    	case KeyEvent.KEYCODE_DPAD_CENTER:
    		/*if (timer!=null) {
    			timer.delete();
    			timer = null;
    		}*/
    		//waitForFinished();
    		
    		takePicture();  
    		break;
    	}
		return super.onKeyDown(keyCode, event);
    	
    }
    
    private void takePicture() {
		// TODO Auto-generated method stub
    	if (Preview.mCamera != null) {
    		AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
			
    		if (MyCamera.config.soundEnable) {
    			int max = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
				audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, max, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
				Preview.mCamera.takePicture(shutterCallBack, null, jpegCallBack);
    		}
			else {
				audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
				Preview.mCamera.takePicture(null, null, jpegCallBack);
				Log.e("takePicture","No sound");
			}
		}
	}

    private ShutterCallback shutterCallBack = new ShutterCallback() {

		public void onShutter() {
			// TODO Auto-generated method stub
			Preview.sound.start();
		}
		
	};
	
	private PictureCallback jpegCallBack = new PictureCallback() {

		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Preview.snapShot = data;
			showSaveDialog();
		}
		
	};
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.d("Menu", "Create!!!");
    	
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	menu.getItem(0).setIcon(android.R.drawable.ic_menu_camera);
    	menu.getItem(1).setIcon(android.R.drawable.ic_menu_preferences);
    	menu.getItem(2).setIcon(android.R.drawable.ic_media_play);
    	menu.getItem(3).setIcon(android.R.drawable.ic_menu_recent_history);
    	menu.getItem(4).setIcon(R.drawable.camera_switch);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		/*if (timer!=null) {
			timer.delete();
			timer = null;
		}*/
    	//waitForFinished();
    	
    	int item_id = item.getItemId();
    	
    	Intent intent;
    	switch (item_id) {
    	
    	case R.id.shutter:
    		takePicture(); 
    		break;
    		
    	case R.id.setting:
    		//waitForFinished();
    		
    		showSettingDialog();
    		break;
    		
    	case R.id.view:
    		waitForFinished();
    		
    		intent = new Intent();
    		intent.setClass(MyCamera.this, QuickView.class);
    		startActivity(intent);
    		MyCamera.this.finish();
    		break;
    		
    	case R.id.timer:
    		//waitForFinished();
    		
    		showTimerDialog();
    		break;
    		
    	case R.id.Switch:
    		if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.GINGERBREAD)
    		{
    			Preview.SwitchCamera();  
	            mPreview = new Preview(this);
	            setContentView(mPreview);
	            addContentView(Preview.overlayer, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	            
	            // Save Camera Selection.
	            SharedPreferences newSettings = getSharedPreferences("Preference",Activity.MODE_PRIVATE);
	    		SharedPreferences.Editor editor = newSettings.edit();
	    		
	    		config.frontCamera = Preview.frontCamera;

	    		editor.putBoolean("frontCamera", Preview.frontCamera);
	    		editor.commit();
    		}
    		else
    			DisplayToast("Sorry! This function is just supported above Android 2.3");
    		break;
    	}
    	
    	return true;
    }

    private void showSettingDialog()
	{

		Log.e("Dialog", "showSetting");
			
		LayoutInflater inflater = LayoutInflater.from(MyCamera.this);
    	final View view = inflater.inflate(R.layout.settingtab, null);
//    	view.getBackground().setAlpha(100);
		soundEnable = (CheckBox) view.findViewById(R.id.ensound);
		smileEnable = (CheckBox) view.findViewById(R.id.ensmile);
		smileParam = (SeekBar) view.findViewById(R.id.barsmile);
		soundEnable.setChecked(config.soundEnable);
		smileEnable.setChecked(config.smileEnable) ;
		if (smileEnable.isChecked())
			smileParam.setProgress(config.smileParam );
    	
    	smileEnable.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

    		public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				boolean enable = smileEnable.isChecked();
				smileParam.setClickable(enable);
				Log.e("set", String.valueOf(smileEnable.isChecked()));
			}
			
		});   	

    	new AlertDialog.Builder(MyCamera.this).setTitle("Setting").setView(view)
    	.setPositiveButton("Confirm",new DialogInterface.OnClickListener() {
    		 public void onClick(DialogInterface arg0, int arg1) {

				//Log.e("set", String.valueOf(smileEnable.isChecked()));
				//Log.e("set", String.valueOf(smileParam.getProgress()));
    			SaveConfig();
    			
    			if (Preview.isDetection != smileEnable.isChecked()) {
    				Log.e("Setting", "Have changed smile detection state.");
	 	            mPreview = new Preview(MyCamera.this);
	 	            setContentView(mPreview);
	  	            addContentView(Preview.overlayer, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	  	            
	  	            if (smileEnable.isChecked()) {
	  	            	if (smileShotListener == null) {
			  				  smileShotListener = new Thread(){
			  					  @Override
			  					  public void run() {
			  						  //if (config.smileEnable || isCountDown) {
			  							  Log.e("Mycamera", "smileShotListener");
			  							  while(Preview.isDetection) {
			  							  	  //if (Preview.isAutoShot && Preview.prefetchData!=null){
			  								  if (Preview.isSmileShot && Preview.snapShot!=null){
			  									  Preview.isSmileShot = false;
			  									  handler1.sendEmptyMessage(0);
			  								  }
			  							  }
			  							  Log.e("Mycamera", "SmileShot finished");
			  						//}
			  					  }
			  				  };
			  				  smileShotListener.start();
	  	            	}	  	          
	  				  
	  	            }
	  	            else {
	  	            	waitForFinished();
	  	            }
    			}
    			else {
    				Log.e("Setting", "No change smile detection state.");
    				if (smileShotListener != null ) {
    					Log.e("Setting", smileShotListener.getState().toString());
    				}
    			}
  	            Toast.makeText(MyCamera.this, "Setting success!", Toast.LENGTH_SHORT).show();
    		 }
    	}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {              
         
          public void onClick(DialogInterface arg0, int arg1) {  
              Toast.makeText(MyCamera.this, "Cancel setting", Toast.LENGTH_SHORT).show();  
          }  
      }).show(); 
    	
    	

	}
    
	protected void SaveConfig() {
		// TODO Auto-generated method stub
		SharedPreferences newSettings = getSharedPreferences("Preference",Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = newSettings.edit();
		
		config.soundEnable = soundEnable.isChecked();
		config.smileEnable = smileEnable.isChecked();

		
		editor.putBoolean("soundEnable", soundEnable.isChecked());
		editor.putBoolean("smileEnable", smileEnable.isChecked());
				
		if (smileEnable.isChecked()) {
			config.smileParam = smileParam.getProgress();
			editor.putInt("smileParam", smileParam.getProgress());
		}
		else {
			config.smileParam = -1;
			editor.putInt("smileParam", -1);
		}

		editor.commit();
	}

	private Handler  handler = new Handler (){
		@Override
        public void handleMessage(Message msg) {
			Log.e("MyCamera", "Auto Shot");
			if (timer!=null)
				timer = null;
			
			autoShot();
		}
	};
	
	private void showTimerDialog() {
		// TODO Auto-generated method stub
		LayoutInflater factory = LayoutInflater.from(this);
		View DialogView = factory.inflate(R.layout.timer, null);
		
		RadioGroup mRadioGroup = (RadioGroup) DialogView.findViewById(R.id.timeselector);
		final RadioButton fivesec = (RadioButton) DialogView.findViewById(R.id.fivesec);
		final RadioButton fifteensec = (RadioButton) DialogView.findViewById(R.id.fifteensec);
		final RadioButton thirtysec = (RadioButton) DialogView.findViewById(R.id.thirtysec);
		
		mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId==fivesec.getId())
					secsToFinished = 5;
				else if (checkedId==fifteensec.getId())
					secsToFinished = 15;
				else if (checkedId==thirtysec.getId()) 
					secsToFinished = 30;
				else
					secsToFinished = 0;
			}
		});
		
		Dialog dlg = new AlertDialog.Builder(MyCamera.this)
								.setTitle("Set Timer")
								.setView(DialogView)
								.setPositiveButton("Start", new DialogInterface.OnClickListener() {
									
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										if (secsToFinished==0)
											DisplayToast("0s cannot be counted down!");
										else {
											autoShotListener = new Thread(){
												@Override
												public void run() {
													//if (config.smileEnable || isCountDown) {
														Log.e("Mycamera", "AutoShotListener");
														while(isListening) {
															//if (Preview.isAutoShot && Preview.prefetchData!=null){
															if (Preview.isAutoShot && Preview.snapShot!=null){
																Preview.isAutoShot = false;
																handler.sendEmptyMessage(0);
															}
														}
														Log.e("Mycamera", "AutoShot finished");
													//}
												}
											};
											autoShotListener.start();
											
											if (Preview.isDetection) {
												timer = new MyCount(mPreview,secsToFinished*1000,1000);
												timer.start();
											}
											else {
												timer = new MyCount(null,secsToFinished*1000,1000);
												timer.start();
											}
											
											dialog.dismiss();
										}
										
										
									}
								})
								.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
									
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										dialog.dismiss();
									}
								}).create();
		
		try {
			dlg.show();
			
			}catch (Exception e){
				Log.e("Dialog", "Failed to show timer dialog");
				DisplayToast("Sorry,something wrong with the dialog.");
			}
	}

	private void showSaveDialog() {
		// TODO Auto-generated method stub
	
		Dialog dialog = new AlertDialog.Builder(MyCamera.this)
						.setTitle("Hello").setMessage("Enjoy your picture?")
						.setPositiveButton("Save", 
								new DialogInterface.OnClickListener() {
									
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										Intent intent = new Intent();
										intent.setClass(MyCamera.this, SaveTab.class);
							    		startActivity(intent);
							    		MyCamera.this.finish();
							    		dialog.dismiss();
									}
								})
						.setNegativeButton("Discard", 
								new DialogInterface.OnClickListener() {
											
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										Preview.snapShot = null;
										System.gc();
										
										dialog.dismiss();	
										Preview.mCamera.startPreview();
									}
								})
						.setCancelable(false).create();
		
		try {
			// Stop autoShotListener thread.
			//waitForFinished();
			
			dialog.show();
			
			}catch (Exception e){
				Log.e("Dialog", "Failed to show shotsave dialog");
				DisplayToast("Sorry,something wrong with the dialog.");
			}
		
	}
	
	protected void DisplayToast(String string) {
		// TODO Auto-generated method stub
		Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
	}

}
/*
public class MyCamera extends Activity {
	private WebCamera mWebView;
	static public CameraConfig config;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    
        config = new CameraConfig("/data/data/org.android.app.smileshot/Config");      
        
        mWebView = new WebCamera(this);
        setContentView(mWebView);   
                
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	//TODO
    	switch (keyCode) {
    	case KeyEvent.KEYCODE_CAMERA:
    	case KeyEvent.KEYCODE_DPAD_CENTER:
    		showSaveDialog();  
    		break;
    	}
		return super.onKeyDown(keyCode, event);
    	
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.d("Menu", "Create!!!");
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int item_id = item.getItemId();
    	
    	MyCamera.this.mWebView.loop = false;
    	
    	switch (item_id) {
    	case R.id.shutter:
    		showSaveDialog();  		
    		break;
    	case R.id.setting:
    		Intent intent = new Intent();
    		intent.setClass(MyCamera.this, SettingTab.class);
    		startActivity(intent);
    		MyCamera.this.finish();
    		break;
    	}
    	
    	return true;
    }

	private void showSaveDialog() {
		// TODO Auto-generated method stub
		mWebView.takePicture();
		
		Dialog dialog = new AlertDialog.Builder(MyCamera.this)
						.setTitle("To Do").setMessage("Save this picture?")
						.setPositiveButton("Save", 
								new DialogInterface.OnClickListener() {
									
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub										
										Intent intent = new Intent();
										intent.setClass(MyCamera.this, SaveTab.class);
							    		startActivity(intent);							    		
							    		MyCamera.this.finish();
									}
								})
						.setNegativeButton("Discard", 
								new DialogInterface.OnClickListener() {
											
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										MyCamera.this.mWebView.loop = true;
										MyCamera.this.mWebView.thread = new Thread(MyCamera.this.mWebView);
										MyCamera.this.mWebView.thread.start();
										dialog.dismiss();											
									}
								}).create();
		dialog.show();
	}
}*/
