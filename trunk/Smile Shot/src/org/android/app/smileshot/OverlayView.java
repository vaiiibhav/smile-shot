package org.android.app.smileshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.View;
/**
 * Draw face detection result on Preview 
 * 
 * @author wangzm
 *
 */
public class OverlayView extends View {

	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG); 
	private Bitmap normLogo,faceDLogo,timerLogo;
	
	public OverlayView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		paint.setStyle(Paint.Style.STROKE); 
		paint.setColor(0xFF33FF33);
		paint.setStrokeWidth(3);
		
		faceDLogo = BitmapFactory.decodeResource(context.getResources(), R.drawable.smile);
		normLogo = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_camera);
		timerLogo = BitmapFactory.decodeResource(context.getResources(), R.drawable.time);
	}
	
	@Override 
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int x = canvas.getWidth() - 100;
		int y = 10;
		
		if (MyCamera.isCountDown) {
			Paint tPaint = new Paint(Paint.ANTI_ALIAS_FLAG); 
			tPaint.setColor(0xFFFFFFFF);
			float fontSize = 256;
			tPaint.setTextSize(fontSize);
			tPaint.setTypeface(Typeface.SANS_SERIF);
			tPaint.setTextAlign(Align.CENTER);
			
			canvas.drawText(MyCamera.secsToFinished+"", canvas.getWidth()/2, (canvas.getHeight()+fontSize)/2, tPaint);
			canvas.drawBitmap(timerLogo, null , new Rect(x,y,x+timerLogo.getWidth(),y+timerLogo.getHeight()),paint);
			return ;
		}
		
		
		if (faceDLogo==null || normLogo==null || timerLogo==null) {
			Log.e("OverLay View", "Logo is null");
			return;
		}
		
		if (!MyCamera.config.smileEnable)
			canvas.drawBitmap(normLogo, null , new Rect(x,y,x+normLogo.getWidth(),y+normLogo.getHeight()),paint);
		else
			canvas.drawBitmap(faceDLogo, null , new Rect(x,y,x+faceDLogo.getWidth(),y+faceDLogo.getHeight()),paint);
		
		if (Preview.isDetection) {
			if (Preview.result==null)
				return;
			
			Size size = Preview.mCamera.getParameters().getPreviewSize();
			float xRatio = (float) (canvas.getWidth()*1.0/size.width);
			float yRatio = (float) (canvas.getHeight()*1.0/size.height);
			
			for (int i=0;i<Preview.result.length;i++) {
				if (Preview.result[i]==null) continue;
				
				if (Preview.frontCamera)
					canvas.drawRect(xRatio*(size.width-Preview.result[i].left), yRatio*Preview.result[i].top, 
									xRatio*(size.width-Preview.result[i].right), yRatio*Preview.result[i].bottom, paint);
				else
					canvas.drawRect(xRatio*Preview.result[i].left, yRatio*Preview.result[i].top, 
									xRatio*Preview.result[i].right, yRatio*Preview.result[i].bottom, paint);
			
				Paint p1 = new Paint();
				Paint p2 = new Paint();
				Paint p3 = new Paint();
				p1.setColor(Color.RED);
				p2.setColor(Color.YELLOW);
				p3.setColor(Color.BLUE);
				
				//
				Paint p4 = new Paint();
				Paint p5 = new Paint();
				p4.setColor(Color.WHITE);
				p5.setColor(Color.BLACK);
				
				//int width = Preview.result[i].mouthWidth;
				//int height = Preview.result[i].mouthHeight;
				
				/*for (int h = 0; h<height;h++)
					for (int w =0; w<width;w++)
					{
						float xx = (float) (Preview.result[i].X- width/2 +w);
						float yy = (float) (Preview.result[i].mouthY - height/2 +h);
						if (Preview.result[i].mouth[h][w]==0)
							canvas.drawPoint(xx, yy, p5);
						else
							canvas.drawPoint(xx, yy, p5);
					}
					*/
				//
				if (Preview.frontCamera) {
					canvas.drawCircle(xRatio*(size.width-Preview.result[i].middleX), yRatio*Preview.result[i].middleY, 4, p1);
					canvas.drawCircle(xRatio*(size.width-Preview.result[i].leftBoundX), yRatio*Preview.result[i].leftBoundY, 4, p2);
					canvas.drawCircle(xRatio*(size.width-Preview.result[i].rightBoundX), yRatio*Preview.result[i].rightBoundY, 4, p3);
					canvas.drawLine(xRatio*(size.width-Preview.result[i].left), yRatio*Preview.result[i].mouthY, xRatio*(size.width-Preview.result[i].right), yRatio*Preview.result[i].mouthY, paint);
				}
				else {
					canvas.drawCircle(xRatio*Preview.result[i].middleX, yRatio*Preview.result[i].middleY, 4, p1);
					canvas.drawCircle(xRatio*Preview.result[i].leftBoundX, yRatio*Preview.result[i].leftBoundY, 4, p2);
					canvas.drawCircle(xRatio*Preview.result[i].rightBoundX, yRatio*Preview.result[i].rightBoundY, 4, p3);
					canvas.drawLine(xRatio*Preview.result[i].left, yRatio*Preview.result[i].mouthY, xRatio*Preview.result[i].right, yRatio*Preview.result[i].mouthY, paint);
				}
			}
			
		}
	}

}
