/*
 * Main Window for OS Monitor 2.0.0 
 * Author: eolwral@gmail.com
 */
package com.eolwral.osmonitor.tablet.main;

import com.eolwral.osmonitor.tablet.R;
import com.eolwral.osmonitor.tablet.*;
import com.eolwral.osmonitor.tablet.connection.ConnectionList;
import com.eolwral.osmonitor.tablet.log.LogList;
import com.eolwral.osmonitor.tablet.misc.MiscList;
import com.eolwral.osmonitor.tablet.network.NetworkList;
import com.eolwral.osmonitor.tablet.preferences.Preferences;
import com.eolwral.osmonitor.tablet.process.ProcessList;

import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


public class MainActivity extends ActivityGroup
{
    private GridView mGrid;
   
    @Override
    public void onCreate(Bundle savedInstanceState) {
   			
        // create view
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlayout);
        
        OnItemClickListener mGridListener = new OnItemClickListener() { 
        	@Override
        	public void onItemClick(AdapterView<?> parent, View subView, int position, long id) {
        		subView.performClick();
       		}
   		};
   		
   		ImageView Logo = (ImageView) findViewById(R.id.main_logo);
   		if(Logo != null) { 
   			Logo.setOnClickListener(new OnClickListener() {
   				@Override
   				public void onClick(View v) {
   					// TODO Auto-generated method stub
   					Help();
   				}
   			
   			});
   		}
   		
        mGrid = (GridView) findViewById(R.id.main_grid);
        mGrid.setAdapter(new MainAdapter(this));
        mGrid.setVerticalSpacing(getVerticalsize());
        mGrid.setOnItemClickListener(mGridListener);

    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	getParent().onKeyDown(keyCode, event);
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}		

    
    public int getVerticalsize()
    {
    	int VerticalSize = 768;
        
        // calculate size
		Bitmap mBitmapLogo = BitmapFactory.decodeResource(getResources(), R.drawable.applogo);
		Bitmap mBitmapIcon = BitmapFactory.decodeResource(getResources(), R.drawable.process);
		VerticalSize -= mBitmapLogo.getHeight();
		VerticalSize -= mBitmapIcon.getHeight()*3;
		
		// reserved size
		VerticalSize = (VerticalSize - 400)/5;
		
		return VerticalSize;
    }
    

    public void startSubActivity(String newActivity, Object intentClass) {
    	OSMonitor Main = (OSMonitor) this.getParent();

    	Intent newIntent = new Intent(this, (Class<?>) intentClass);
    	Main.startChildActivity(newActivity, newIntent, 2);
    }
    
    public class MainAdapter extends BaseAdapter {
    	
    	private Integer[] mIcon = {
    			R.drawable.process,  R.drawable.network, R.drawable.connection,
    			R.drawable.system,   R.drawable.logcat, R.drawable.preferences
        };
        
        private String[] mText = null;
        
        private Object[] mActive = {
        		ProcessList.class,  NetworkList.class,  ConnectionList.class,
        		MiscList.class, LogList.class, Preferences.class
        };
        
        public MainAdapter(Context mContext) {
        	mText = mContext.getResources().getStringArray(R.array.menu_title);
        }

        public int getCount() {
            return mIcon.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }
        
        public void resetView() {
        	for(int viewPtr = 0; viewPtr < mActive.length; viewPtr++)
        		mGrid.getChildAt(viewPtr).setBackgroundColor(android.graphics.Color.BLACK);
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
        	
        	View itemView;
        	
        	if(convertView == null){
        		// layout
				LayoutInflater appLI = getLayoutInflater();
				itemView = appLI.inflate(R.layout.mainicon, null);
				
				// text
				TextView appTV = (TextView) itemView.findViewById(R.id.main_icon_text);
				appTV.setText(mText[position]);
				
				// image
				ImageView appIV = (ImageView) itemView.findViewById(R.id.main_icon_image);
				appIV.setImageResource(mIcon[position]);
				itemView.setTag(""+position);
				itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						int position = Integer.parseInt((String) view.getTag());
						if(position <= 4 ) {
							resetView();
							view.setBackgroundColor(android.graphics.Color.DKGRAY);
						}
						startSubActivity(mText[position], mActive[position]);
						
					}
				});
			}
			else {
				itemView = convertView;
			}
        	
			return itemView;
        }
        
    }
    
    void Help()
    {
    	AlertDialog.Builder HelpWindows = new AlertDialog.Builder(this);
        WebView HelpView = new WebView(this);
        HelpView.loadUrl("http://wiki.android-os-monitor.googlecode.com/hg/onlinehelp.html?r=7c6925d7be72c85ab21a1afa782db04055e5abdd");
        HelpWindows.setView(HelpView);
        HelpWindows.show();    	
    }
}
