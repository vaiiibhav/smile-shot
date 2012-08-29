package org.android.app.smileshot;

public class FaceResult {

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
	//
	int[][] mouth;
	int mouthWidth ;
	int mouthHeight;
	
	public FaceResult(FaceAnalysis analyser) {
		// TODO Auto-generated constructor stub
		left = analyser.left;
		right = analyser.right;
		top = analyser.top;
		bottom = analyser.bottom;
		eyesDistance = analyser.eyesDistance;
		X = analyser.X;
		Y = analyser.Y;
		mouthY = analyser.mouthY;
		leftBoundX = analyser.leftBoundX;
		leftBoundY = analyser.leftBoundY;
		rightBoundX = analyser.rightBoundX;
		rightBoundY = analyser.rightBoundY;
		middleX = analyser.middleX;
		middleY = analyser.middleY;
		openMouth = analyser.openMouth;
		isSmile = analyser.isSmile;
		//
		mouthWidth = analyser.mouthWidth;
		mouthHeight = analyser.mouthHeight;
		
		mouth = new int[mouthHeight][];
		for (int i=0;i<mouthHeight;i++) {
			mouth[i] = new int[mouthWidth];
			
			for (int j=0;j<mouthWidth;j++){
				mouth[i][j] = analyser.mouthArea[i][j];
			}
		}
			
	}

}
