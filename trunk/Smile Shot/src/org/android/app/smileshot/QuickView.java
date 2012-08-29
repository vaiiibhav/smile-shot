package org.android.app.smileshot;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
/**
 * Show pictures which were shot just now.
 * @author wangzm
 *
 */
public class QuickView extends Activity {
	
	ImageAdapter adapter;
	String curImagePathName;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
	       super.onCreate(savedInstanceState);
	        
	       getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	       requestWindowFeature(Window.FEATURE_NO_TITLE);
	       
	       setContentView(R.layout.quickview);
	       
	       Gallery g = (Gallery)findViewById(R.id.Gallery01);
	       adapter = new ImageAdapter(this);
	       g.setAdapter(adapter);
	       g.setSpacing(50);
	       g.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> parent, View v, int pos,
					long id) {
				// TODO Auto-generated method stub
				DisplayToast(adapter.images[pos].getAbsoluteFile().toString());
				curImagePathName = adapter.images[pos].getAbsoluteFile().toString();
			}
	    	   
	       });

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			Intent intent = new Intent();
			intent.setClass(QuickView.this, MyCamera.class);
    		startActivity(intent);
    		QuickView.this.finish();
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.quickviewmenu, menu);
    	menu.getItem(0).setIcon(android.R.drawable.ic_menu_share);
    	menu.getItem(1).setIcon(android.R.drawable.ic_menu_set_as);
    	//menu.getItem(2).setIcon(android.R.drawable.ic_menu_edit);
		return true;
		
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	int item_id = item.getItemId();
    	
    	Intent intent;
    	Uri attach=Uri.parse("file://"+curImagePathName); 
    	
    	switch (item_id) {
    	case R.id.share:
    		if (curImagePathName==null || curImagePathName.length() ==0) {
    			DisplayToast("No image be selected!");
    			return true;
    		}
    		
			intent = new Intent(Intent.ACTION_SEND); 
			intent.setType("image/jpeg"); 
			intent.putExtra(Intent.EXTRA_STREAM,attach); 
			startActivity(Intent.createChooser(intent, "Share to"));
			
    		break;
    	case R.id.setas:
       		if (curImagePathName==null || curImagePathName.length() ==0) {
    			DisplayToast("No image be selected!");
    			return true;
    		}
    			 
			intent = new Intent(Intent.ACTION_ATTACH_DATA); 
			intent.setType("image/jpeg"); 
			intent.putExtra(Intent.EXTRA_STREAM,attach); 
			startActivity(Intent.createChooser(intent, "Set as"));
			
    		break;
    	/*case R.id.edit:
    		if (curImagePathName==null || curImagePathName.length() ==0) {
    			DisplayToast("No image be selected!");
    			return true;
    		}
    			 
			intent = new Intent(Intent.ACTION_VIEW); 
			intent.setType("image/jpeg"); 
			intent.putExtra(Intent.EXTRA_STREAM,attach); 
			startActivity(intent);
			
			break;*/
    	}
		return true;
	}
	
	private void DisplayToast(String string) {
		// TODO Auto-generated method stub
		Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
	}
	private void DisplayToastLong(String string) {
		// TODO Auto-generated method stub
		Toast.makeText(this, string, Toast.LENGTH_LONG).show();
	}
	
	public class ImageAdapter extends BaseAdapter {

		private Context mContext;
		File[] images;
		ImageView v;
		
		public ImageAdapter(Context context) {
			// TODO Auto-generated constructor stub
			mContext = context;
			
			boolean isSDCardPresent = android.os.Environment.getExternalStorageState()
			  						         .equals(android.os.Environment.MEDIA_MOUNTED);

			if (!isSDCardPresent)
				return;
			File root = new File(Environment.getExternalStorageDirectory(), "SmileShot");
			Log.e("Adapter", root.getAbsolutePath());
			FilenameFilter filter = new FilenameFilter(){

				public boolean accept(File dir, String filename) {
					// TODO Auto-generated method stub
					if (filename.endsWith(".jpg"))
						return true;
					else
						return false;
				}
				
			};
			
			images = root.listFiles(filter);

			Log.e("Adapter", images.length+" images");
			
			if (images==null || images.length==0) {
				DisplayToastLong("No image to view!");
				Log.e("Adapter", "No image to view!");
			}
			
			for (int i=0;i<images.length/2;i++) {
				File tmp = images[i];
				images[i] = images[images.length-i-1];
				images[images.length-i-1] = tmp;
			}
		}

		public int getCount() {
			// TODO Auto-generated method stub
			return images.length;
		}

		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		public View getView(int position, View view, ViewGroup parent) {
			// TODO Auto-generated method stub
			v = new ImageView(mContext);
			
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			Bitmap bmp = BitmapFactory.decodeFile(images[position].getAbsolutePath(), opts);
			int w = opts.outWidth;
			opts.inJustDecodeBounds = false;
			opts.inSampleSize = w/360;
			bmp = BitmapFactory.decodeFile(images[position].getAbsolutePath(), opts);
			
			v.setImageBitmap(bmp);
			v.setLayoutParams(new Gallery.LayoutParams(360,240));
			v.setScaleType(ImageView.ScaleType.FIT_CENTER);
			return v;
		}
		
	}
}
