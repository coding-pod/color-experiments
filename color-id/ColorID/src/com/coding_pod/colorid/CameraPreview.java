package com.coding_pod.colorid;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {
	private static final String TAG = "CameraPreview";
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private List<Camera.Size> mSupportedPreviewSizes;
	private Camera.Size mPreviewSize;
//	private int[] mCameraData;
//	private Paint mPaint;
	
	private enum PreviewState {
		STOPPED, RUNNING
	};
	
	private PreviewState mPreviewState = PreviewState.STOPPED;
	
	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		// initialize
		init();
	}

	public CameraPreview(Context context) {
		super(context);
		
		// initialize
		init();
	}

	private void init() {
		// initialize surface holder
		mHolder = getHolder();
		mHolder.addCallback(this);
		
		// open camera
		safeCameraOpen();
		
		// get supported preview sizes
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        for(Camera.Size str: mSupportedPreviewSizes) {
        	Log.i(TAG, str.width + "/" + str.height);
        }
        
//        setWillNotDraw(false);
//        mPaint = new Paint();
	}
	
    // safe camera open
	// http://developer.android.com/training/camera/cameradirect.html
    private boolean safeCameraOpen() {
        boolean qOpened = false;
      
        // try to release camera and open new instance
        try {
            releaseCamera();
            mCamera = Camera.open(0);
            qOpened = (mCamera != null);
        } catch (Exception e) {
            Log.e(TAG, "failed to open camera");
            e.printStackTrace();
        }
        
        return qOpened;    
    }
    
    // release camera
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
    
    // start camera preview
    private void startPreview() {
    	if ((mCamera != null) && (mPreviewState == PreviewState.STOPPED)) {
			mCamera.startPreview();
			mPreviewState = PreviewState.RUNNING;
    	}
    }
    
    // stop camera preview
    private void stopPreview() {
    	if ((mCamera != null) && (mPreviewState == PreviewState.RUNNING)) {
			mCamera.stopPreview();
			mPreviewState = PreviewState.STOPPED;
    	}
    }
    
	// find best size for preview
	private Camera.Size findBestSize(List<Camera.Size> sizes, int width, int height) {
		// get camera size list
		List<Camera.Size> supportedSizes = sizes;

		// iterate through sizes, find best
		Camera.Size bestSize = supportedSizes.get(0);
		for (Camera.Size size : supportedSizes) {
			// find camera size with an area slightly larger than the screen, i guess?
			if ((size.width*size.height) >= (width*height)) {
				bestSize = size;
			}
		}
		
		Log.i(TAG, "bestSize.width = " + bestSize.width + ", bestSize.height = " + bestSize.height);
		return bestSize;
	}
	
	// set display orientation
	// http://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
	 private void setCameraDisplayOrientation() {
	     Camera.CameraInfo info = new Camera.CameraInfo();
	     Camera.getCameraInfo(0, info);
	     int rotation = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
	     int degrees = 0;
	     switch (rotation) {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	     int result;
	     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } else {  // back-facing
	         result = (info.orientation - degrees + 360) % 360;
	     }
	     mCamera.setDisplayOrientation(result);
	 }

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// stop preview
		try {
			stopPreview();
		} catch (Exception e) {
			Log.e(TAG, "failed to stop preview");
		}
		
		// set display orientation
		setCameraDisplayOrientation();
		
		// get camera parameters
		Camera.Parameters parameters = mCamera.getParameters();
        
        // set parameters
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        parameters.setPreviewFormat(ImageFormat.NV21);
		mCamera.setParameters(parameters);

		// restart preview
		try {
			startPreview();
		} catch (RuntimeException e) {
			Log.e(TAG, "failed to start preview");
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        // open camera
 		if(mCamera == null) {
 			safeCameraOpen();
 		}

 		// set preview holder and callback
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.setPreviewCallback(this);
			startPreview();
		} catch (IOException e) {
			Log.e(TAG, "failed to start preview");
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// stop preview
		try {
			stopPreview();
		} catch (Exception e) {
			Log.e(TAG, "failed to stop preview");
		}
		
		// release camera
        try {
            releaseCamera();
        } catch (Exception e) {
            Log.e(TAG, "failed to release Camera");
            e.printStackTrace();
        }
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// resolve width and height
		final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
		
		// get camera preview size
		mPreviewSize = findBestSize(mSupportedPreviewSizes, width, height);
		
		// get camera aspect ratio, assuming width is greater than height
		float aspectRatio = (float)mPreviewSize.width/(float)mPreviewSize.height;
		// adjust width or height to match aspect ratio, depending on orientation
		float newWidth, newHeight;
		if(width >= height) {
			newHeight = (float)height;
			newWidth = newHeight*aspectRatio;
		}
		else {
			newWidth = (float)width;
			newHeight = newWidth*aspectRatio;
		}
		
		Log.i(TAG, "newWidth = " + newWidth + ", newHeight = " + newHeight);
		// set measured dimension
		setMeasuredDimension((int)newWidth, (int)newHeight);
//		setMeasuredDimension(width, height);
	}
	
//	@Override
//	protected void onDraw(Canvas canvas) {
////		super.onDraw(canvas);
//		
//		// paint background
//		if(mCameraData != null) {
//	        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//	        mPaint.setStrokeWidth(3);
//	        mPaint.setAntiAlias(true);
//	        mPaint.setColor(mCameraData[0]);
//	        canvas.drawRect(0, 0, this.getWidth(), this.getHeight(), mPaint);
//		}	
//	}
	
	// https://en.wikipedia.org/wiki/YUV#Y.27UV420sp_.28NV21.29_to_ARGB8888_conversion
	/**
	 * Converts YUV420 NV21 to ARGB8888
	 * 
	 * @param data byte array on YUV420 NV21 format.
	 * @param width pixels width
	 * @param height pixels height
	 * @return a ARGB8888 pixels int array. Where each int is a pixels ARGB. 
	 */
	 public static int[] convertYUV420_NV21toARGB8888(byte [] data, int width, int height) {
	    int size = width*height;
	    int offset = size;
	    int[] pixels = new int[size];
	    int u, v, y1, y2, y3, y4;
	 
	    // i along Y and the final pixels
	    // k along pixels U and V
	    for(int i=0, k=0; i < size; i+=2, k+=2) {
	    	y1 = data[i]&0xff;
	    	y2 = data[i+1]&0xff;
	    	y3 = data[width+i]&0xff;
	    	y4 = data[width+i+1]&0xff;
	 
	    	v = data[offset+k]&0xff;
	    	u = data[offset+k+1]&0xff;
	    	v = v-128;
	    	u = u-128;
	 
	    	pixels[i] = convertYUVtoARGB(y1, u, v);
	    	pixels[i+1] = convertYUVtoARGB(y2, u, v);
	    	pixels[width+i] = convertYUVtoARGB(y3, u, v);
	    	pixels[width+i+1] = convertYUVtoARGB(y4, u, v);
	 
	    	if (i!=0 && (i+2)%width==0)
				i += width;
	    }
	 
	    return pixels;
	}
		
	 //// https://en.wikipedia.org/wiki/YUV#Y.27UV420sp_.28NV21.29_to_ARGB8888_conversion
	private static int convertYUVtoARGB(int y, int u, int v) {
		int r = y + (int)(1.772f*v);
		int g = y - (int)(0.344f*v + 0.714f*u);
		int b = y + (int)(1.402f*u);
		r = r>255? 255 : r<0 ? 0 : r;
		g = g>255? 255 : g<0 ? 0 : g;
		b = b>255? 255 : b<0 ? 0 : b;
		return 0xff000000 | (r<<16) | (g<<8) | b;
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
//		// store camera data
//		mCameraData = convertYUV420_NV21toARGB8888(data, mPreviewSize.width, mPreviewSize.height);
	}
}
