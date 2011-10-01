package com.eolwral.osmonitor.tablet.process;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.eolwral.osmonitor.tablet.*;
import com.eolwral.osmonitor.tablet.log.LogList;

public class ProcessDetail extends Activity
{
	private int targetPID = 0;
	private JNIInterface JNILibrary = JNIInterface.getInstance();
	private ProcessInfoQuery ProcessInfo = ProcessInfoQuery.getInstance(this);
	private ProcessDetail Self = null;

	private Runnable uiRunnable = new Runnable() {
		public void run() {
			doRefreshData();
			uiHandler.postDelayed(this, 1000);
		}
	};   
	
	private Handler uiHandler = new Handler();
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // Use a custom layout file
        setContentView(R.layout.processdetail);
        Bundle Extras = getIntent().getExtras();
        targetPID = Extras.getInt("targetPID");
        Self = this;
        
        //if(CommonUtil.IsSupportKill() == 0)
        {
        	//((Button) findViewById(R.id.process_kill_button)).setVisibility(View.GONE);
        }
        //else
        {
        	(findViewById(R.id.process_kill_button)).setOnClickListener(new View.OnClickListener(){
    			public void onClick(View v) {
    				switch(CommonUtil.IsSupportKill())
    				{
    				case 0:
    				case 1:
    					android.os.Process.killProcess(targetPID);
    					((ActivityManager)getSystemService(ACTIVITY_SERVICE)).restartPackage(JNILibrary.GetProcessName(targetPID));
    					Self.finish();
    					break;
    				case 2:
    					CommonUtil.execCommand("kill -9 "+targetPID+"\n");
    					Self.finish();
    					break;
    				}
    				
    			}
        	});
        }
        
        if(JNILibrary.GetRooted() != 1)
        {
        	((Button) findViewById(R.id.process_nice_button)).setVisibility(View.GONE);
        }
        else
        {
        	(findViewById(R.id.process_nice_button)).setOnClickListener(new View.OnClickListener(){
    			public void onClick(View v) {
    				CharSequence[] NiceValue = {"-20", "-19", "-18", "-17", "-16", "-15", "-14", "-13", "-12", "-11",
    											"-10", "-9", "-8", "-7", "-6", "-5", "-4", "-3", "-2", "-1",
    											"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
    											"10", "11", "12", "13", "14", "15", "16", "17", "18", "19"};

    				AlertDialog.Builder builder = new AlertDialog.Builder(Self);
    				builder.setTitle(Self.getResources().getString(R.string.process_nice_title));

    				builder.setSingleChoiceItems(NiceValue, (int) (JNILibrary.GetProcessNice(targetPID)+20),
    						new DialogInterface.OnClickListener(){
    					
    						@Override
    						public void onClick(DialogInterface dialog, int which) {
    							CommonUtil.execCommand(CommonUtil.NiceCMD+" "+targetPID+" "+(which-20));
    							dialog.dismiss();
    						}
    						
    				});
    				builder.show();
    			}
        	});
        }
        
        (findViewById(R.id.process_log_button)).setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
        
		   	OSMonitor LogMain = (OSMonitor) Self.getParent();
		    	Intent LogIntent = new Intent(LogMain, LogList.class);
		    	LogIntent.putExtra("targetPID", targetPID);
		    	LogMain.startChildActivity("LogList", LogIntent, 2);

			}
        });
        (findViewById(R.id.process_switch_button)).setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				String ClassName = null;
				String selectedPackageName =  JNILibrary.GetProcessName(targetPID);
			
				// find ClassName
				PackageManager QueryPackage = Self.getPackageManager();
				Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
				mainIntent.addCategory(Intent.CATEGORY_LAUNCHER); 
				List<ResolveInfo> appList = QueryPackage.queryIntentActivities(mainIntent, 0);
				for(int i=0; i<appList.size(); i++)
				{
					if(appList.get(i).activityInfo.applicationInfo.packageName.equals(selectedPackageName))
						ClassName = appList.get(i).activityInfo.name;
				}
  	        
				if(ClassName != null)
				{
  	   	        	Intent switchIntent = new Intent();
  	   	        	switchIntent.setAction(Intent.ACTION_MAIN);
  	   	        	switchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
  	   	        	switchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
  	   	        		   			  	  Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |
  	   	        		   			      Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
  	   	        	switchIntent.setComponent(new ComponentName(selectedPackageName, ClassName));
  	   	        	startActivity(switchIntent);
				}
			}
        });
        
        doRefreshData();
    }

    @Override
    public void onDestroy() 
    {
    	uiHandler.removeCallbacks(uiRunnable);
    	super.onDestroy();
    }

    @Override
    protected void onResume() 
    {    
    	uiHandler.post(uiRunnable);
    	super.onResume();
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

	private void doRefreshData() {
		((ImageView) findViewById(R.id.process_detail_image)).setImageDrawable(ProcessInfo.getAppIcon(targetPID));
		((TextView) findViewById(R.id.process_name_value)).setText(ProcessInfo.getPackageName(targetPID));
		((TextView) findViewById(R.id.process_package_value)).setText(ProcessInfo.getPacakage(targetPID));
		((TextView) findViewById(R.id.process_uid_value)).setText(""+JNILibrary.GetProcessUID(targetPID));
		((TextView) findViewById(R.id.process_pid_value)).setText(""+targetPID);
		((TextView) findViewById(R.id.process_nice_value)).setText(""+JNILibrary.GetProcessNice(targetPID));
      
		if(JNILibrary.GetProcessRSS(targetPID) > 1024) 
			((TextView) findViewById(R.id.process_memoryrss_value)).setText((JNILibrary.GetProcessRSS(targetPID)/1024)+"M");
		else 
			((TextView) findViewById(R.id.process_memoryrss_value)).setText((JNILibrary.GetProcessRSS(targetPID))+"K");

		int ProcessPID[] = new int[1];
		ProcessPID[0] = targetPID;
		ActivityManager ActInfo = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

		Debug.MemoryInfo[] pMemoryInfo = ActInfo.getProcessMemoryInfo(ProcessPID);
        
		if(pMemoryInfo != null)
        {
        	int PSSMemory = pMemoryInfo[0].dalvikPss + pMemoryInfo[0].nativePss + pMemoryInfo[0].otherPss;

        	if(PSSMemory > 1024) 
        		((TextView) findViewById(R.id.process_memorypss_value)).setText((PSSMemory/1024)+"M");        	
        	else 
        		((TextView) findViewById(R.id.process_memorypss_value)).setText(PSSMemory+"M");        	
        }
		if(JNILibrary.GetProcessRSS(targetPID) > 1024) 
			
		((TextView) findViewById(R.id.process_threads_value)).setText(""+JNILibrary.GetProcessThreads(targetPID));

		((TextView) findViewById(R.id.process_time_value)).setText(JNILibrary.GetProcessTime(targetPID));

		String STime = String.format("%02d:%02d", 
				JNILibrary.GetProcessUTime(targetPID)/60,
				JNILibrary.GetProcessUTime(targetPID)%60
		);
      
		((TextView) findViewById(R.id.process_stime_value)).setText(STime);

		String UTime = String.format("%02d:%02d", 
				JNILibrary.GetProcessSTime(targetPID)/60,
				JNILibrary.GetProcessSTime(targetPID)%60 
		);

		((TextView) findViewById(R.id.process_utime_value)).setText(UTime);
      
		String Status = JNILibrary.GetProcessStatus(targetPID).trim();
		if(Status.compareTo("Z") == 0)
			((TextView) findViewById(R.id.process_status_value)).setText(getResources().getString(R.string.process_status_zombie));
		else if(Status.compareTo("S") == 0)
			((TextView) findViewById(R.id.process_status_value)).setText(getResources().getString(R.string.process_status_sleep));
		else if(Status.compareTo("R") == 0)
			((TextView) findViewById(R.id.process_status_value)).setText(getResources().getString(R.string.process_status_running));
		else if(Status.compareTo("D") == 0)
			((TextView) findViewById(R.id.process_status_value)).setText(getResources().getString(R.string.process_status_waitio));
		else if(Status.compareTo("T") == 0)
			((TextView) findViewById(R.id.process_status_value)).setText(getResources().getString(R.string.process_status_stop));
		else 
			((TextView) findViewById(R.id.process_status_value)).setText(getResources().getString(R.string.process_status_unknown));
	}		
    
}
