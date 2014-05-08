package com.coding_pod.colorid;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
//import android.support.v7.app.ActionBar;
//import android.support.v4.app.Fragment;
import android.os.Bundle;
//import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
//import android.view.ViewGroup;
import android.widget.FrameLayout;
//import android.widget.LinearLayout;
//import android.os.Build;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	private FrameLayout mFrame;
	private CameraPreview mCameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 		
 		// add touch event to frame
		mFrame = (FrameLayout) findViewById(R.id.container);
		//mFrame.setEnabled(true);
		mFrame.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getActionMasked() == (MotionEvent.ACTION_UP)) {
					int x = (int)event.getX();
					int y = (int)event.getY();
					
					Context context = getApplicationContext();
					CharSequence text = "x = " + x + ", y = " + y;
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, text, duration);
					toast.show();

				}
				return true;
			}
		});
		
		// camera preview for viewing camera
		mCameraPreview = (CameraPreview)findViewById(R.id.cameraPreview);

//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.container, new PlaceholderFragment())
//                    .commit();
//        }
    }
    

	@Override
	protected void onPause() {
		super.onPause();

		// disable frame
		mFrame.setEnabled(false);
	}
	
    
    @Override
	protected void onResume() {
		super.onResume();
		
		// enable frame
		mFrame.setEnabled(true);
		
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    /**
//     * A placeholder fragment containing a simple view.
//     */
//    public static class PlaceholderFragment extends Fragment {
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//            return rootView;
//        }
//    }

}
