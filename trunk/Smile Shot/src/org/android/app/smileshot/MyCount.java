package org.android.app.smileshot;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.CountDownTimer;
import android.util.Log;
/**
 * Count down timer
 * 
 * @author wangzm
 *
 */
public class MyCount extends CountDownTimer {

	PreviewCallback cb ;
	private PictureCallback jpegReady = new PictureCallback(){

		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Preview.snapShot = data;
			Preview.isAutoShot = true;
		}
		
	};
	private ShutterCallback shutterReady = new ShutterCallback(){

		public void onShutter() {
			// TODO Auto-generated method stub
			Preview.sound.start();
		}
	};
	
	public MyCount(PreviewCallback previewCallBack, long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		// TODO Auto-generated constructor stub
		MyCamera.isCountDown = true;
		MyCamera.secsToFinished = (int)millisInFuture/1000;
		
		cb = previewCallBack;
		Preview.mCamera.setPreviewCallback(null);
	}
	/**
	 *  Delete but not finish
	 */
	public void delete() {
		this.cancel();
		
		MyCamera.isCountDown = false;
		MyCamera.secsToFinished = 0;
		Preview.isAutoShot = false;
		Preview.snapShot = null;
		
		Preview.mCamera.setPreviewCallback(cb);
		Preview.overlayer.postInvalidate();

		Log.e("MyCount", "delete");
	}
	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		MyCamera.isCountDown = false;
		MyCamera.secsToFinished = 0;
		
		//Preview.isAutoShot = true;
		if (MyCamera.config.soundEnable)
			Preview.mCamera.takePicture(shutterReady , null, jpegReady );
		else
			Preview.mCamera.takePicture(null , null, jpegReady );
		
		Preview.mCamera.setPreviewCallback(cb);
		
		Preview.overlayer.postInvalidate();

		Log.e("MyCount", "OnFinish");
		
	}

	@Override
	public void onTick(long millisUntilFinished) {
		// TODO Auto-generated method stub
		MyCamera.secsToFinished = (int)millisUntilFinished/1000;
		Preview.overlayer.postInvalidate();
	}

}
