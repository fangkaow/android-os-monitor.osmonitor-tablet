/*
 * Main Window for OS Monitor 2.0.0 
 * Author: eolwral@gmail.com
 */
package com.eolwral.osmonitor.tablet;

import java.util.Stack;

import com.eolwral.osmonitor.tablet.R;
import com.eolwral.osmonitor.tablet.log.DmesgFilter;
import com.eolwral.osmonitor.tablet.log.LogcatFilter;
import com.eolwral.osmonitor.tablet.main.MainActivity;
import com.eolwral.osmonitor.tablet.preferences.Preferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class OSMonitor extends OSActivityGroup
{
	private LocalActivityManager mLam = getLocalActivityManager();
	private Stack<String> mActivity = new Stack<String>();
	private JNIInterface JNILibrary = JNIInterface.getInstance();
	private LinearLayout mainView;
	private LinearLayout detailView;
	private LinearLayout sideView;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	// turn title bar off
   	    requestWindowFeature(Window.FEATURE_NO_TITLE);

        // create view
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.osmonitor);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        
        if(settings.getBoolean(Preferences.PREF_CPUUSAGE, false))
        {
        	if(OSMonitorService.getInstance() == null)
        		startService(new Intent(this, OSMonitorService.class));
        }
        else
        	stopService(new Intent(this, OSMonitorService.class));
        
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

     	
		// reset settings
		Editor settingsEditor = settings.edit();
		settingsEditor.putBoolean(DmesgFilter.PREF_DMESGUSEFILTER, false);
		settingsEditor.putString(DmesgFilter.PREF_DMESGFILTERSTR, "");
		settingsEditor.putString(DmesgFilter.PREF_DMESGFILTERLV, "");
		settingsEditor.putString(LogcatFilter.PREF_LOGCATSOURCE, "");
		settingsEditor.putBoolean(LogcatFilter.PREF_LOGCATUSEFILTER, false);
		settingsEditor.putString(LogcatFilter.PREF_LOGCATFILTERLV, "");
		settingsEditor.putString(LogcatFilter.PREF_LOGCATFILTERPID, "");
		settingsEditor.putString(LogcatFilter.PREF_LOGCATFILTERSTR, "");
		settingsEditor.commit();
    	
    	// detect root and nice 
    	if(JNILibrary.GetRooted() == 1)
    		CommonUtil.CheckNice(getAssets());

        mainView = (LinearLayout) findViewById(R.id.mainscreen);
       	sideView = (LinearLayout) findViewById(R.id.sidescreen);
       	detailView = (LinearLayout) findViewById(R.id.detailscreen);
    	
    	// start main activity
		Intent newActivity = new Intent(this, MainActivity.class);
		Window newLayout = mLam.startActivity("MainActivity", newActivity);
		mActivity.push("MainActivity");
		
		sideView.addView(newLayout.getDecorView());
		
        // set title
        this.setTitle(R.string.app_title);
    }
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	String curActitivy = mActivity.peek();
	    	Activity targetActivity = mLam.getActivity(curActitivy);
	    	targetActivity.finish();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}		

    public void startChildActivity(String newActivity, Intent newIntent, int Level)
    {
    	if( mActivity.peek().split("-")[0] == newActivity && Level == 2)
    		return;
    	
    	// only for Preferences
    	if(newIntent.getComponent().getClassName().contains("Preferences")) 
    	{
    		startActivity(newIntent);
    		return;
    	}

    	// fix activity group bug!
    	newActivity = newActivity + "-" + CommonUtil.RandomGen.nextInt();
    	
    	// remove other level activity
    	while(mActivity.size() >= Level)
    	{
    		String oldActivityID = mActivity.peek();
    		Activity oldActivity = mLam.getActivity(oldActivityID);
    		finishFromChild(oldActivity);
    	}
    	
		mActivity.push(newActivity);
		Window newWindow = mLam.startActivity(newActivity, newIntent);
		
		newWindow.getDecorView().setLayoutParams(
				new LayoutParams(LayoutParams.FILL_PARENT,
						         LayoutParams.FILL_PARENT));
		
		if(mActivity.size() > 2)
		{
			detailView.removeAllViews();
			detailView.addView(newWindow.getDecorView());
		}
		else {
			mainView.removeAllViews();
			mainView.addView(newWindow.getDecorView());
		}
		
		return;
	}

	
    public void finishFromChild(Activity child) {

    	String oldActivity = mActivity.pop();
    	mLam.destroyActivity(oldActivity, true);
    	
    	// exit from program
    	if(mActivity.size() == 0) {
    		this.finish();
    		return;
    	}

    	// large layout
    	if(mActivity.size() > 1)
    		detailView.removeAllViews();
    	else
    		mainView.removeAllViews();
    	
    	return;
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
