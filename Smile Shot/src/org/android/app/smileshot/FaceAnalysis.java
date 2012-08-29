package org.android.app.smileshot;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector.Face;
import android.util.Log;

public class FaceAnalysis {
	/** Face rectangle */
	int left,right,top,bottom;
	
	/** Eyes distance */
	float eyesDistance;
	/** Middle point between Eyes */
	int X,Y;
	/** Y pos for mouth area */
	int mouthY;
	
	/** lips' left bound and right bound */
	int leftBoundX,leftBoundY,rightBoundX,rightBoundY;
	/** Middle point of mouth */
	int middleX,middleY;
	
	boolean openMouth = false;
	boolean isSmile = false;
	
	int [][]mouthArea;
	int mouthWidth ;
	int mouthHeight;

	public FaceAnalysis() {
		// TODO Auto-generated constructor stub
		left = 0;
		right = 0;
		top = 0;
		bottom = 0;
		eyesDistance = 0;
		X = 0;
		Y = 0;
		mouthY = 0;
		leftBoundX = 0;
		leftBoundY = 0;
		rightBoundX = 0;
		rightBoundY = 0;
		middleX = 0;
		middleY = 0;
	}

	public void process(Face face, Bitmap image) {
		// TODO Auto-generated method stub
		eyesDistance = face.eyesDistance();
		PointF pos = new PointF();
		face.getMidPoint(pos);
		
		X= (int) pos.x;
		Y = (int) pos.y;
		middleX = X;
		left = (int) (middleX-0.8*eyesDistance);
		right = (int) (middleX+0.8*eyesDistance);
		top = (int) (pos.y-0.5*eyesDistance);
		bottom = (int) (pos.y+1.55*eyesDistance);
		
		mouthY = findMouth(image);

		captureMouth(image);
		if (mouthArea==null)
			Log.e("Faceanalysis", "mouth area is null!");
		analysisMouth();
		isSmile = isSmile();
	}
	/**
	 * Find key points of mouth
	 */
	private void analysisMouth() {
		// TODO Auto-generated method stub
		if (openMouth) {
			
		}
		else {
			
			// Find middle position of mouth
			int middle = mouthWidth/2;
			int cnt =0;
			
			for (int i=1;i<mouthHeight-1;i++) {
				if (mouthArea [i][middle]==0) {
					if (mouthArea [i][middle-1]==1 &&
					    mouthArea [i][middle+1]==1 &&
					    mouthArea [i-1][middle-1]==1 &&
					    mouthArea [i-1][middle]==1 &&
					    mouthArea [i-1][middle+1]==1 &&
					    mouthArea [i+1][middle-1]==1 &&
					    mouthArea [i+1][middle]==1 &&
					    mouthArea [i+1][middle+1]==1 )
					continue;
					
					middleY += i;
					cnt ++;
				}
			}
			
			if (cnt==0) 
				middleY = mouthHeight/2;
			else
				middleY /= cnt;
			
			//Find left edge of mouth
			int cnt1 = 0;
		
			int begin = mouthHeight/4;
			int end = 3*mouthHeight/4;
			
			for (int j=1;j<middle;j++) {
				for (int i=begin;i<end;i++) {
					if (mouthArea [i][j]==0) {
						if (mouthArea [i][j-1]==1 &&
						    mouthArea [i][j+1]==1 &&
						    mouthArea [i-1][j-1]==1 &&
						    mouthArea [i-1][j]==1 &&
						    mouthArea [i-1][j+1]==1 &&
						    mouthArea [i+1][j-1]==1 &&
						    mouthArea [i+1][j]==1 &&
						    mouthArea [i+1][j+1]==1 )
						continue;
						
						leftBoundX += j;
						leftBoundY += i;
						cnt1 ++;
					}					
				}
				
				if (cnt1<5) { 
					cnt1 =0 ;
					leftBoundX = 0;
					leftBoundY = 0;
				}
				else
					break;
			}
			
			if (cnt1==0) {
				leftBoundX = 0;
				leftBoundY = mouthHeight/2;
			}
			else {
				leftBoundX /= cnt1;
				leftBoundY /= cnt1;
			}
			
			// Find right edge of mouth
			int cnt2 = 0;
			
			for (int j=mouthWidth-2;j>=middle;j--) {
				for (int i=begin;i<end;i++) {
					if (mouthArea [i][j]==0) {
						if (mouthArea [i][j-1]==1 &&
						    mouthArea [i][j+1]==1 &&
						    mouthArea [i-1][j-1]==1 &&
						    mouthArea [i-1][j]==1 &&
						    mouthArea [i-1][j+1]==1 &&
						    mouthArea [i+1][j-1]==1 &&
						    mouthArea [i+1][j]==1 &&
						    mouthArea [i+1][j+1]==1 )
						continue;
						
						rightBoundX += j;
						rightBoundY += i;
						cnt2 ++;
					}					
				}
				
				if (cnt2<5) { 
					cnt2 =0 ;
					rightBoundX = 0;
					rightBoundY = 0;
				}
				else
					break;
			}
			
			if (cnt2==0) {
				rightBoundX = mouthWidth-1;
				rightBoundY = mouthHeight/2;
			}
			else {
				rightBoundX /= cnt2;
				rightBoundY /= cnt2;
			}
			
			
			int x = middleX - mouthWidth/2;
			int y = mouthY - mouthHeight/2;
			
			leftBoundX += x;
			leftBoundY += y;
			rightBoundX += x;
			rightBoundY += y;
			//middleX += x;
			middleY += y;
		}
			
	}

	/**
	 * Find mouth rectangle
	 * @param image
	 */
	private void captureMouth(Bitmap image) {
		// TODO Auto-generated method stub
		mouthWidth = (int) (1.2*eyesDistance);
		mouthHeight = (int) (0.5*eyesDistance);
		
		/** Allocate mouth rectangle */
		mouthArea = new int[mouthHeight][];
		for (int i=0;i<mouthHeight;i++)
			mouthArea[i] = new int[mouthWidth];
		
		float color = 0;
	
		for (int i=0;i<mouthHeight/2;i++) 
			color +=  image.getPixel(middleX, mouthY-mouthHeight/4+i);
	
		color /= 0.5*mouthHeight;
		
		int x = middleX - mouthWidth/2;
		int y = mouthY - mouthHeight/2;
		
		for (int i=0; i<mouthHeight;i++) 
			for(int j = 0; j<mouthWidth ; j++) {
				if (x+j > image.getWidth() || y+i > image.getHeight()) continue;
				
				if (image.getPixel(x+j,y+i)>color) {
					mouthArea [i][j] = 1;
				}
				else {					
					mouthArea [i][j] = 0;
				}
			}
		/** Noise filter */
		for (int i=0; i<mouthHeight;i++) 
			for(int j = 0; j<mouthWidth ; j++) {
				
				if (mouthArea[i][j]==1 && roundBlack(i,j)) {
					mouthArea[i][j] = 0; 					
				}
				else if (mouthArea[i][j]==0 && roundWhite(i,j)) {
					mouthArea[i][j] = 1;
				}
			}
		
		/** 
		 * Dectection mouth: open or close 
		 * 		[s,e)		
		 */
		openMouth = false;
		
		int s=0,e=0;
		boolean s1=false,s2=false;
		if (mouthArea[0][mouthWidth/2]==1)
			s1 = true;
		
		for (int i=1;i<mouthHeight;i++) {
			if (mouthArea[i-1][mouthWidth/2]==0 && mouthArea[i][mouthWidth/2]==1){
				s = i;
				s1 = true;
			}
			if (mouthArea[i-1][mouthWidth/2]==1 && mouthArea[i][mouthWidth/2]==0) {
				e = i;
				s2 = true;
			}
			
			if (mouthArea[mouthHeight-1][mouthWidth/2]==1) {
				e = mouthHeight;
				s2 = true;
			}
				
			if (s1 && s2) {
				s1 = false;
				s2 = false;
	
				if (e-s>5) {
					boolean isClosed = true;
					
					for (int index=s;index<e;index++)
						if (!isClosed(mouthWidth/2,index)) {
							isClosed = false;
							break;
						}
					
					if (isClosed) {
						Paint t = new Paint();
						t.setColor(Color.BLUE);
						openMouth = true;
						//break;
					}
				}
			}
		}
		
	}

	/**
	 * Point(x,y) is around by other color points ?
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isClosed(int x, int y) {
		// TODO Auto-generated method stub
		if (isLeftClosed(x-1,y) && isRightClosed(x+1,y) && isTopClosed(x,y-1) && isBottomClosed(x,y+1)
			&& isLTClosed(x-1,y-1) && isRTClosed(x+1,y-1) && isLBClosed(x-1,y+1) && isRBClosed(x+1,y+1))
			return true;
		else
			return false;
	}

	private boolean isRTClosed(int x, int y) {
		// TODO Auto-generated method stub
		if (y<0 || x>=mouthWidth)
			return false;
		
		if (mouthArea[y][x]==0)
			return true;
		else
			return isRTClosed(x+1,y-1);
	}

	private boolean isLBClosed(int x, int y) {
		// TODO Auto-generated method stub
		if (x<0 || y>=mouthHeight)
			return false;
		
		if (mouthArea[y][x]==0)
			return true;
		else
			return isLBClosed(x-1,y+1);
	}

	private boolean isRBClosed(int x, int y) {
		// TODO Auto-generated method stub
		if (y>=mouthHeight || x>=mouthWidth)
			return false;
		
		if (mouthArea[y][x]==0)
			return true;
		else
			return isRBClosed(x+1,y+1);
	}

	private boolean isLTClosed(int x, int y) {
		// TODO Auto-generated method stub
		if (y<0 || x<0)
			return false;
		
		if (mouthArea[y][x]==0)
			return true;
		else
			return isLTClosed(x-1,y-1);
	}

	private boolean isTopClosed(int x, int y) {
		// TODO Auto-generated method stub
		if (y<0 )
			return false;
		
		if (mouthArea[y][x]==0)
			return true;
		else
			return isTopClosed(x,y-1);
	}

	private boolean isRightClosed(int x, int y) {
		// TODO Auto-generated method stub
		if (x>=mouthWidth )
			return false;
		
		if (mouthArea[y][x]==0)
			return true;
		else
			return isRightClosed(x+1,y);
	}

	private boolean isBottomClosed(int x, int y) {
		// TODO Auto-generated method stub
		if (y>=mouthHeight )
			return false;
		
		if (mouthArea[y][x]==0)
			return true;
		else
			return isBottomClosed(x,y+1);
	}

	private boolean isLeftClosed(int x, int y) {
		// TODO Auto-generated method stub
		if (x<0 )
			return false;
		
		if (mouthArea[y][x]==0)
			return true;
		else
			return isLeftClosed(x-1,y);
	}

	private boolean roundBlack(int i, int j) {
		// TODO Auto-generated method stub
		int left = j-1<0? 0:j-1;
		int right = j+1<mouthWidth ?j+1:mouthWidth-1;
		int top = i-1<0?0:i-1;
		int bottom = i+1<mouthHeight?i+1:mouthHeight-1;
		
		for (int w = left;w<=right;w++)
			for (int h = top;h<=bottom;h++) {
				if (w==j && h==i) continue;
				
				if ( mouthArea[h][w]==1)
					return false;
			}
		return true;
	}

	private boolean roundWhite(int i, int j) {
		// TODO Auto-generated method stub
	
		int left = j-1<0? 0:j-1;
		int right = j+1<mouthWidth ?j+1:mouthWidth-1;
		int top = i-1<0?0:i-1;
		int bottom = i+1<mouthHeight?i+1:mouthHeight-1;
		
		for (int w = left;w<=right;w++)
			for (int h = top;h<=bottom;h++) {
				if (w==j && h==i) continue;
				
				if ( mouthArea[h][w]==0)
					return false;
			}
		return true;
	}
	/**
	 * find y position of mouth area for future analysis
	 * @param image
	 * @return
	 */
	private int findMouth(Bitmap image) {
		// TODO Auto-generated method stub
		float min = Float.MAX_VALUE;
		int mouthY = (int) (Y+eyesDistance); 
		for (int j=(int) (Y+eyesDistance); j<bottom;j++) {
			float aver = 0;
			for (int i=(int) (X-0.5*eyesDistance); i <(X+0.5*eyesDistance); i++) {
				aver += image.getPixel(i, j);
			}
			aver /= eyesDistance;
			
			if (min>aver) {
				min = aver;
				mouthY = j;
			}
		}
		
		return mouthY;
	}

	/**
	 * Are you smiling ?
	 * @return
	 */
	public boolean isSmile() {
		// TODO Auto-generated method stub
		if (openMouth)
			return true;
		else {
			if (leftBoundX>=middleX || rightBoundX<=middleX)
				return false;
			
		    if (middleY <= rightBoundY && middleY<= leftBoundY) {
		    	return false;
		    }
		    else {
		    	int val = (rightBoundY-middleY)*(leftBoundX-middleX) + 
		    			   (middleY-leftBoundY)*(rightBoundX-middleX);
		    	//int a = rightBoundY-middleY;
		    	if (val<=0)
		    		return false;
		    }
		    
		    double x1 = leftBoundX-middleX;
		    double y1 = leftBoundY-middleY;
		    double x2 = rightBoundX-middleX;
		    double y2 = rightBoundY-middleY;
		    
		    double angle = Math.acos((x1*x2+y1*y2)/Math.sqrt(x1*x1+y1*y1)/Math.sqrt(x2*x2+y2*y2));
		    double level = (Math.PI - angle)/(0.25*Math.PI);
		    
		    Log.e("angle", ""+angle*180/Math.PI+" smileParam="+MyCamera.config.smileParam);
		    if (100*level >MyCamera.config.smileParam)
		    	return true;
		}
		return false;
	}

}
