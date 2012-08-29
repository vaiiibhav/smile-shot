package org.android.app.smileshot;

import java.io.IOException;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Preview extends SurfaceView implements SurfaceHolder.Callback,PreviewCallback{
	
	public SurfaceHolder mHolder;
	public static Camera mCamera;
	/** Picture data which is ready to save */
	public static byte[] snapShot = null;
 
    /** For smile face detection */
    public static boolean isDetection = false;
    public static boolean isSmileShot = false;
	public static boolean isThreadWorking = false;
	public FaceDetectThread detectThread = null;
	public static FaceResult[] result = new FaceResult[4];
    
	/** Overlay View */
	public static OverlayView overlayer;
	
	/** For auto shutter */
	public static boolean isAutoShot = false;
	/** Shutter sound */
	public static MediaPlayer sound;
	/** Front or Back camera */
	public static boolean frontCamera = false;
	
    Preview(Context context) {
        super(context);

        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // Configure Preview View
        isDetection = MyCamera.config.smileEnable;
        overlayer = new OverlayView(context);
        
        sound = MediaPlayer.create(context, R.raw.sound); 
        Log.e("Preview", "Construct");
        //Log.e("Preview", "sound "+String.valueOf(sound!=null));
    	
    }

    public void surfaceCreated(SurfaceHolder holder) {
    	Log.e("surfaceCreated", "Here");
    	
    	int cameranum =  Camera.getNumberOfCameras();
    	if (cameranum > 1)	{	
    		if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
	     		mCamera = null;
			}    		
	    	    	
	    	for (int i = 0; i <cameranum; i++)	{
		    	CameraInfo info = new CameraInfo();
		    	Camera.getCameraInfo(i, info);
		
	    		if (frontCamera && info.facing == CameraInfo.CAMERA_FACING_FRONT) {

	    			mCamera = Camera.open(i);
//		    			Size size = mCamera.getParameters().getPreviewSize();
//		    			Log.i("camera width",String.valueOf(size.width));
//		    			Log.i("camera height",String.valueOf(size.height));
//		    			//Display display = getWindowManager().getDefaultDisplay(); 
//		    			holder.setFixedSize(dis.getWidth()*size.height/size.width, dis.getHeight());
	    			
	    			Log.e("camera","front");
	    			break;
    			}

	    		if (!frontCamera && info.facing == CameraInfo.CAMERA_FACING_BACK) {

	    			mCamera = Camera.open(i);
	    			Log.e("camera","behind");
	    			break;
	    		}
	    	}
	    	
    	}
    	else
    	{
    		if (mCamera != null)
        		return;
        	//Log.e("Camera Created", "Here");
        	
            mCamera = Camera.open();
    	}
    	
    	Log.e("Camera Created", "Here");
        try {
           mCamera.setPreviewDisplay(holder);
           
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    	Log.e("surfaceDestroyed", "Here");
    	// Release the camera.
    	Log.e("Destroy Camera", "Here");
    	if (isDetection)
    		mCamera.setPreviewCallback(null);
    	waitForThreadComplete();
    	
    	if (isDetection)
    		mCamera.setPreviewCallback(null);
    	
    	mCamera.release();
    	mCamera = null;
        
    	result = null;
	    isAutoShot = false;
	         
	    //sound.release();    	
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	Log.e("surfaceChanged", "Here");
//------为支持ICS4.0而注释掉的代码---------------------------->
//      Camera.Parameters parameters = mCamera.getParameters();
//      /** Set Picture Format */
//      //parameters.setPictureFormat(PixelFormat.JPEG);        
//     // parameters.setPreviewSize(w, h);
//      Log.i("w",String.valueOf(w));
//      mCamera.setParameters(parameters);
//-----------------------------------------------> 
        
        if (isDetection) {
        	//snapShot = null;    	
        	mCamera.setPreviewCallback(this);
        }
        mCamera.startPreview();
    }
    
	public void takePicture() {
		// TODO Auto-generated method stub
		if (mCamera != null) {
			Log.e("takePicture", String.valueOf(MyCamera.config.soundEnable));
			
			AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
			
			if (MyCamera.config.soundEnable) {
				int max = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
				audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, max, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
				mCamera.takePicture(shutterCallBack, null, jpegCallBack);
			}
			else {
				audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
				mCamera.takePicture(null, null, jpegCallBack);
			}
		}
	}
	
	private ShutterCallback shutterCallBack = new ShutterCallback() {

		public void onShutter() {
			// TODO Auto-generated method stub

			sound.start();
		}
		
	};
	private PictureCallback jpegCallBack = new PictureCallback() {

		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			snapShot = data;
		}
		
	};

	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub		
		if(!isThreadWorking){
			
			if (snapShot!=null && isAutoShot) {
				Log.i("onPreview", "Ready to AutoShot,wait...");
				return;
			}
			
			isThreadWorking = true;
						
			waitForThreadComplete();
			Log.i("onPreviewFrame", "Begin to detect");
			
			Size size = mCamera.getParameters().getPreviewSize();
			detectThread = new FaceDetectThread(this,data,size.width, size.height);
			detectThread.start();
		}
	}
	/** Kill detection thread */
	private void waitForThreadComplete() {
		// TODO Auto-generated method stub
		if (detectThread == null)
			return;
		
		if (detectThread.isAlive()){
			try {
				detectThread.join();
				detectThread = null;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void SwitchCamera()
	{
		frontCamera = !frontCamera;

	}
	
}
