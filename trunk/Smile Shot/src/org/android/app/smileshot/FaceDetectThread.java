package org.android.app.smileshot;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.util.Log;
/**
 * Asynchronous detection for face
 * 
 * @author wangzm
 *
 */
public class FaceDetectThread extends Thread {
	
	final int MAX_FACES = 4;
	byte[] grayScale;
	int previewWidth;
	int previewHeight;
	
	/** Hold the old previewCallBack */
	private PreviewCallback previewCallback;
	
	private PictureCallback jpegReady = new PictureCallback(){

		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Preview.snapShot = data;
			//Preview.isAutoShot = true;
		}
		
	};
	private ShutterCallback shutterReady = new ShutterCallback(){

		public void onShutter() {
			// TODO Auto-generated method stub
			Preview.sound.start();
		}
		
	};

	public FaceDetectThread(PreviewCallback previewCB, byte[] data, int width, int height) {
		// TODO Auto-generated constructor stub
		grayScale = data;
		previewWidth = width;
		previewHeight = height;
		previewCallback = previewCB;
	}
	
	@Override
	public void run() {
		Preview.mCamera.setPreviewCallback(null);
		
		int [] colors = new int[previewWidth* previewHeight];
		decodeYUV420SP(grayScale,colors, previewWidth, previewHeight);
		
		Bitmap image = Bitmap.createBitmap(colors, previewWidth, previewHeight, Bitmap.Config.RGB_565);		
		
		Log.d("FaceDetection","Image size: "+image.getWidth()+"*"+image.getHeight());
		Log.d("FaceDetection","Preview size: "+previewWidth+"*"+previewHeight);
		
		FaceDetector detector = new FaceDetector(image.getWidth(),image.getHeight(),MAX_FACES);
		Face[] faces = new Face[MAX_FACES];
		
		long begin = System.currentTimeMillis();
		detector.findFaces(image,faces);
		long end = System.currentTimeMillis();
		
		FaceAnalysis analyser = new FaceAnalysis();

		int faceNum=0,smileNum=0;
		
		if (Preview.result==null)
			Preview.result = new FaceResult[4];
		
		for (int i=0; i<MAX_FACES; i++) {
			if (faces[i] == null){
				Preview.result[i] = null;
				continue;
			}
			faceNum++;
			
			analyser.process(faces[i],image);
			
			if (analyser.mouthY+analyser.mouthHeight/2>analyser.bottom) {
				Preview.result[i] = null;
				continue;
			}
			
			Preview.result[i] = new FaceResult(analyser);
			
			Log.e("FaceDetection","Face at ("+analyser.X+","+analyser.Y+") , cost " +(end-begin)+" ms");
			
			if (analyser.isSmile) {
				Log.e("FaceDetection","You are smiling!");
				if (analyser.openMouth)
					Log.e("FaceDetection", "Open Mouth");
				
				smileNum ++;
			}
		}
		
		if (smileNum>0 && smileNum==faceNum) {
			Preview.isSmileShot = true;
			// To send message to open a dialog. 
			if (MyCamera.config.soundEnable)
				Preview.mCamera.takePicture(shutterReady , null, jpegReady );
			else
				Preview.mCamera.takePicture(null , null, jpegReady );
			
		}
		
		Preview.overlayer.postInvalidate();
		Preview.isThreadWorking = false;
		
		colors = null;
		grayScale = null;
		System.gc();
		
		if (!MyCamera.isCountDown)
			Preview.mCamera.setPreviewCallback(previewCallback);
		
		Log.e("FaceDetection", "End detection");
	}
	/**
	 * GrayScale to RGB
	 * @param yData
	 * @param rgb
	 * @param width
	 * @param height
	 */
	private void decodeYUV420SP(byte[] yData, int[] rgb, int width, int height) {
		// TODO Auto-generated method stub
		int len = width * height;
		int i = 0;
		while (true) {
			if (i == len)
				break;
			int Y = yData[i] & 0xff; 
			rgb[i] = 0xff000000 + (Y << 16) + (Y << 8) + Y;
			i ++;
		}
	}

}
