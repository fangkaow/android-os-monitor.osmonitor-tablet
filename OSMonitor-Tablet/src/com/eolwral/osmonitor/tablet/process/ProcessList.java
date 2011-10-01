package com.eolwral.osmonitor.tablet.process;

import java.text.DecimalFormat;
import java.util.List;

import net.londatiga.android.*;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.PopupWindow.OnDismissListener;

import com.eolwral.osmonitor.tablet.*;
import com.eolwral.osmonitor.tablet.log.LogList;
import com.eolwral.osmonitor.tablet.preferences.Preferences;

public class ProcessList extends ListActivity
{
	private ProcessListAdapter UpdateInterface = null;
	private ProcessList Self = null;
	private JNIInterface JNILibrary = JNIInterface.getInstance();
	private int OrderBy = JNILibrary.doSortLoad;
	
	private Button SortByButton = null; 
	private QuickAction mQuickAction = null;
	private QuickAction mProcessAction = null;
	private int mProcessTarget = 0;
	
	private boolean mSuspend = false;
	private boolean mFreeze = false;

	
	// TextView
	private TextView CPUUsage = null;
	private TextView RunProcess = null;
	private TextView MemTotal = null;
	private TextView MemFree = null;

	private DecimalFormat MemoryFormat = new DecimalFormat(",000");

	private Runnable uiRunnable = new Runnable() {
		public void run() {

			if(JNILibrary.doDataLoad() == 1 && mFreeze == false) {
     			CPUUsage.setText(JNILibrary.GetCPUUsage());
    	     	RunProcess.setText(JNILibrary.GetProcessCounts()+"");
    	     	MemTotal.setText(MemoryFormat.format(JNILibrary.GetMemTotal())+ "K");
    	     	MemFree.setText(MemoryFormat.format(JNILibrary.GetMemBuffer()
    	     					+JNILibrary.GetMemCached()+JNILibrary.GetMemFree())+ "K");
    	     	
    	     	Self.onRefresh();
   	     	
     		}
	        uiHandler.postDelayed(this, 500);
		}
	};   
	
	
	private Handler uiHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // Use a custom layout file
        setContentView(R.layout.processlayout);
        
        CPUUsage = (TextView) findViewById(R.id.process_usage_value);
        RunProcess = (TextView) findViewById(R.id.process_count_value);
        MemTotal = (TextView) findViewById(R.id.process_memory_total);
        MemFree = (TextView) findViewById(R.id.process_memory_free);

        // Tell the list view which view to display when the list is empty
        getListView().setEmptyView(findViewById(R.id.empty));

        // action bar
        ActionItem SortbyCPUAction = new ActionItem();
        SortbyCPUAction.setTitle(getResources().getString(R.string.prcoess_sortby_cpu));
        SortbyCPUAction.setIcon(getResources().getDrawable(R.drawable.sortbycpu));

        ActionItem SortbyMemAction = new ActionItem();
        SortbyMemAction.setTitle(getResources().getString(R.string.prcoess_sortby_mem));
        SortbyMemAction.setIcon(getResources().getDrawable(R.drawable.sortbymem));

        mQuickAction = new QuickAction(this);
        mQuickAction.addActionItem(SortbyCPUAction);
        mQuickAction.addActionItem(SortbyMemAction);

        mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
      		@Override
            public void onItemClick(int pos) {
        		if (pos == 0) {
        			JNILibrary.SetProcessSort(JNILibrary.doSortLoad);
        			OrderBy = JNILibrary.doSortLoad;
        			SortByButton.setText(getResources().getString(R.string.prcoess_sortby_cpu));
        			
        		} else if (pos == 1) { 
        			JNILibrary.SetProcessSort(JNILibrary.doSortMem);
        			OrderBy = JNILibrary.doSortMem;
        			SortByButton.setText(getResources().getString(R.string.prcoess_sortby_mem));
        		}
    			UpdateInterface.OrderBy = OrderBy;
        	}

        });

        
        
        // action bar
        ActionItem DetailInfoAction = new ActionItem();
        DetailInfoAction.setTitle(getResources().getString(R.string.process_detail_title));
        DetailInfoAction.setIcon(getResources().getDrawable(R.drawable.process_action_detail));

        ActionItem KillProcAction = new ActionItem();
        KillProcAction.setTitle(getResources().getString(R.string.process_kill_title));
        KillProcAction.setIcon(getResources().getDrawable(R.drawable.process_action_kill));

        ActionItem NiceProcAction = new ActionItem();
        NiceProcAction.setTitle(getResources().getString(R.string.process_nice_title));
        NiceProcAction.setIcon(getResources().getDrawable(R.drawable.process_action_nice));


        ActionItem SwtichProcAction = new ActionItem();
        SwtichProcAction.setTitle(getResources().getString(R.string.process_switch_title));
        SwtichProcAction.setIcon(getResources().getDrawable(R.drawable.process_action_switch));

        ActionItem WatchLogAction = new ActionItem();
        WatchLogAction.setTitle(getResources().getString(R.string.process_log_title));
        WatchLogAction.setIcon(getResources().getDrawable(R.drawable.process_action_log));

        mProcessAction = new QuickAction(this);
        mProcessAction.addActionItem(DetailInfoAction);
        mProcessAction.addActionItem(SwtichProcAction);
        mProcessAction.addActionItem(WatchLogAction);

        //if(CommonUtil.IsSupportKill() != 0)
        mProcessAction.addActionItem(KillProcAction);
        
        if(JNILibrary.GetRooted() == 1)
        	mProcessAction.addActionItem(NiceProcAction);
        
        mProcessAction.setOnDismissListener(new OnDismissListener()
        {
			@Override
			public void onDismiss() {
				mSuspend = false;
			}
        });
        
        mProcessAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
        	@Override
            public void onItemClick(int pos) {
     			switch(pos)
       			{
       			case 0:
       		    	OSMonitor Main = (OSMonitor) Self.getParent();
       		    	Intent newIntent = new Intent(Main, ProcessDetail.class);
       		    	newIntent.putExtra("targetPID", mProcessTarget);
       		    	Main.startChildActivity("ProcessDetail", newIntent, 3);
       				break;
       			case 1:
       				String ClassName = null;
       				String selectedPackageName =  JNILibrary.GetProcessName(mProcessTarget);
       				
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
       				break;
       			case 2:
       			   	OSMonitor LogMain = (OSMonitor) Self.getParent();
       		    	Intent LogIntent = new Intent(LogMain, LogList.class);
       		    	LogIntent.putExtra("targetPID", mProcessTarget);
       		    	LogMain.startChildActivity("LogList", LogIntent, 2);
       				break;
       			case 3:
       				switch(CommonUtil.IsSupportKill())
       				{
       				case 0:
       				case 1:
       	   	        	android.os.Process.killProcess(mProcessTarget);
       					((ActivityManager)getSystemService(ACTIVITY_SERVICE)).restartPackage(JNILibrary.GetProcessName(mProcessTarget));
           			 	JNILibrary.doDataRefresh();
           			 	UpdateInterface.notifyDataSetChanged();
       					break;
       				case 2:
       					CommonUtil.execCommand("kill -9 "+mProcessTarget+"\n");
           			 	JNILibrary.doDataRefresh();
           			 	UpdateInterface.notifyDataSetChanged();
       					break;
       				}
       				break;
       			case 4: 
       				/* -20 to +19 */
       				CharSequence[] NiceValue = {"-20", "-19", "-18", "-17", "-16", "-15", "-14", "-13", "-12", "-11",
       									        "-10", "-9", "-8", "-7", "-6", "-5", "-4", "-3", "-2", "-1",
       									        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
       									        "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"};

       				AlertDialog.Builder builder = new AlertDialog.Builder(Self);
       				builder.setTitle(Self.getResources().getString(R.string.process_nice_title));
       				
       				builder.setSingleChoiceItems(NiceValue, (int) (JNILibrary.GetProcessNice(mProcessTarget)+20),
       					new OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog, int which) {
								CommonUtil.execCommand(CommonUtil.NiceCMD+" "+mProcessTarget+" "+(which-20));
								dialog.dismiss();
							}
      						
     				});
       				builder.show();
       			
       				break;
       			}
        	}
        	
        });
        
        SortByButton = (Button) findViewById(R.id.sortbybutton); 
        SortByButton.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		mQuickAction.show(v);
        	}
        });
        
        // Freeze
        CheckBox Freeze = (CheckBox) findViewById(R.id.process_list_freeze);
        Freeze.setOnCheckedChangeListener(
        	new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					mFreeze = isChecked;
				}
        	}
        );
        
        // Use our own list adapter
        Self = this;
        setListAdapter(new ProcessListAdapter(this));
        UpdateInterface = (ProcessListAdapter) getListAdapter();
    	
    }
    
	public void onRefresh()
	{ 
		if(mSuspend)
			return;
		
		JNILibrary.doDataSwap(); 
		UpdateInterface.notifyDataSetChanged();
		
	}
	
	private void restorePrefs()
    {
		boolean ExcludeSystem = true;

		// load settings
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		JNILibrary.doDataTime(settings.getInt(Preferences.PREF_UPDATE, 2));
		ExcludeSystem = settings.getBoolean(Preferences.PREF_EXCLUDE, true);
		
	    // change options
   		JNILibrary.SetProcessSort(OrderBy);
   		JNILibrary.SetProcessOrder(JNILibrary.doOrderASC);
   		
        if(ExcludeSystem)
    		JNILibrary.SetProcessFilter(1);
        else
        	JNILibrary.SetProcessFilter(0);
        
       
        UpdateInterface.OrderBy = OrderBy;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	restorePrefs();
    }
 
    @Override
    public void onPause() 
    {
    	uiHandler.removeCallbacks(uiRunnable);
    	JNILibrary.doTaskStop();

    	super.onPause();
    }

    @Override
    protected void onResume() 
    {    
        restorePrefs();

        JNILibrary.doTaskStart(JNILibrary.doTaskProcess);
    	uiHandler.post(uiRunnable);
    	super.onResume();
    }

    @Override
    protected void onListItemClick(ListView mListView, View mView, int position, long id)
    {
    	OSMonitor Main = (OSMonitor) Self.getParent();
    	Intent newIntent = new Intent(Main, ProcessDetail.class);
    	newIntent.putExtra("targetPID", JNILibrary.GetProcessPID(position));
    	Main.startChildActivity("ProcessDetail", newIntent, 3);
    }    
   
    private class ProcessListAdapter extends BaseAdapter {
    	
    	private int LastCount = 0;
    	private ProcessInfoQuery ProcessInfo = null;
    	private LayoutInflater mInflater = null;
    	public int OrderBy = JNILibrary.doSortLoad;
    	
        public ProcessListAdapter(Context mContext)
        {
        	mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
            ProcessInfo = ProcessInfoQuery.getInstance(mContext);
        }

        public int getCount() {
            return JNILibrary.GetProcessCounts(); 
        }

        public Object getItem(int position) {
            return position;
        }
     
        public long getItemId(int position) {
            return position;
        }
 
        public View getView(int position, View convertView, ViewGroup parent) {
        	
            View sv = null;

            String OrderValue = "";
            int targetPID = ProcessInfo.getProcessPID(position);

            ProcessInfo.doCacheInfo(targetPID);
        	 
        	switch(OrderBy)
        	{
        	case 1:
        	case 2:
        	case 5:
        		OrderValue = ProcessInfo.getProcessLoad(targetPID);
        		break;
        	case 3:
        		OrderValue = ProcessInfo.getProcessMem(targetPID);
        		break;
        	case 4:
        		OrderValue = ProcessInfo.getProcessThreads(targetPID);
        		break;
        	}
        	
    		
    	    if (convertView == null) {
    	    	sv = (View) mInflater.inflate(R.layout.processitem, parent, false);
    	    	((ImageView) sv.findViewById(R.id.processicon)).setOnClickListener(new View.OnClickListener() {
    	        	@Override
    	        	public void onClick(View v) {
    	        		mSuspend = true;
    	        		mProcessTarget = (Integer) ((View)v.getParent()).getTag();
    	        		mProcessAction.show(v);
    	        	}
    	        	
    	        });
            } 
            else {
                sv = (View) convertView;
        	}

    	    ((ImageView) sv.findViewById(R.id.processicon)).setImageDrawable(ProcessInfo.getAppIcon(targetPID));
			((TextView) sv.findViewById(R.id.processname)).setText(ProcessInfo.getPackageName(targetPID));
			((TextView) sv.findViewById(R.id.processvalue)).setText(OrderValue);
			sv.setTag(targetPID);
			
			if(position % 2 == 0)
				sv.setBackgroundColor(0x80444444);
			else
				sv.setBackgroundColor(0x80000000);
			
           	return sv;
        }
        
        @Override
        public void notifyDataSetChanged () {
        	
        	if(LastCount != JNILibrary.GetProcessCounts()) {
        		super.notifyDataSetChanged();
        		LastCount = JNILibrary.GetProcessCounts();
        		return;
        	}
        		

			int FirstPosition = Self.getListView().getFirstVisiblePosition();
        	
        	for (int i = 0; i <= Self.getListView().getChildCount() ;i++ ) {
    			View sv = Self.getListView().getChildAt(i);
    			int position = FirstPosition+i;
    			int targetPID = ProcessInfo.getProcessPID(position);
       			String OrderValue = "";

       			if(sv == null)
        			continue;

       			
               	switch(OrderBy)
               	{
               	case 1:
               	case 2:
               	case 5:
               		OrderValue = ProcessInfo.getProcessLoad(targetPID);
               		break;
               	case 3:
               		OrderValue = ProcessInfo.getProcessMem(targetPID);
               		break;
               	case 4:
               		OrderValue = ProcessInfo.getProcessThreads(targetPID);
               		break;
               	}

               	if(((TextView) sv.findViewById(R.id.processname)).getText() != ProcessInfo.getPackageName(targetPID))
               	{
               		((ImageView) sv.findViewById(R.id.processicon)).setImageDrawable(ProcessInfo.getAppIcon(targetPID));
           			((TextView) sv.findViewById(R.id.processname)).setText(ProcessInfo.getPackageName(targetPID));
           			sv.setTag(targetPID);
               	}

               	((TextView) sv.findViewById(R.id.processvalue)).setText(OrderValue);
        	}
        }
        
    }

}
