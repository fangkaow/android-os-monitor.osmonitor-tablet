package com.eolwral.osmonitor.tablet.log;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.eolwral.osmonitor.tablet.R;
import com.eolwral.osmonitor.tablet.*;
import com.eolwral.osmonitor.tablet.preferences.Preferences;

public class LogList extends Activity 
{
	private JNIInterface JNILibrary = JNIInterface.getInstance();
	
	private LogList Self = null;
	private BaseAdapter UpdateInterface = null;	
	private ListView InternalList = null;
	
	private QuickAction mQuickAction = null;
	private Button mLogType = null;
	private Button mExpertMode = null;
	
	private TextView EmptyMsg = null;
	private TextView MsgCountText = null;
	private String Mode = "dmesg";
	
	private boolean FirstView = false;
	
	// watch log
	private int targetPID = 0; 
    
	// Refresh
    private Runnable runnable = new Runnable() {
		public void run() {

			if(JNILibrary.doDataLoad() == 1)
			{
 			    if(Mode.equals("dmesg"))
 				  	MsgCountText.setText(""+JNILibrary.GetDebugMessageCounts());
 				else
 				   	MsgCountText.setText(""+JNILibrary.GetLogcatCounts());
 					
				if(EmptyMsg != null)
					EmptyMsg.setText("");
				
 				Self.onRefresh();
			}
	        handler.postDelayed(this, 1000);
		}
	};   
	Handler handler = new Handler();
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Request progress bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.loglayout);
        
        // action bar
        ActionItem LogLinux = new ActionItem();
        LogLinux.setTitle(getResources().getString(R.string.logcat_type_linux_title));
        LogLinux.setIcon(getResources().getDrawable(R.drawable.linux));
 
        ActionItem LogAndroid = new ActionItem();
        LogAndroid.setTitle(getResources().getString(R.string.logcat_type_android_title));
        LogAndroid.setIcon(getResources().getDrawable(R.drawable.android));
        
        mQuickAction = new QuickAction(this);
        mQuickAction.addActionItem(LogLinux);
        mQuickAction.addActionItem(LogAndroid);
 
        mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
      		@Override
            public void onItemClick(int pos) {
        		if (pos == 0) {
        			JNILibrary.doTaskStart(JNILibrary.doTaskDMesg);
        			mLogType.setText(getResources().getString(R.string.logcat_type_linux_title));
        			Mode = "dmesg";
        		} else if (pos == 1) { 
        			JNILibrary.doTaskStart(JNILibrary.doTaskLogcat);
        			mLogType.setText(getResources().getString(R.string.logcat_type_android_title));
        			Mode = "logcat";
        		}
        		
                if(Mode.equals("dmesg"))
                {
                    InternalList = (ListView) findViewById(R.id.loglist);
                    InternalList.setEmptyView(findViewById(R.id.debugempty));
                    InternalList.setAdapter(new DMesgListAdapter(Self));
                    InternalList.setOnItemClickListener(NullListener);
                    UpdateInterface = (DMesgListAdapter) InternalList.getAdapter();
 				  	MsgCountText.setText(""+JNILibrary.GetDebugMessageCounts());
                }
                else
                {
                	InternalList = (ListView) findViewById(R.id.loglist);
                    InternalList.setEmptyView(findViewById(R.id.debugempty));
                    InternalList.setAdapter(new LogcatListAdapter(Self));
                    InternalList.setOnItemClickListener(InternalListListener);
                    UpdateInterface = (LogcatListAdapter) InternalList.getAdapter();
 				   	MsgCountText.setText(""+JNILibrary.GetLogcatCounts());
                }
        	}

        });
        
        mLogType = (Button) findViewById(R.id.logtypebutton); 
        mLogType.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		mQuickAction.show(v);
        	}
        });
        
        
        mExpertMode = (Button) findViewById(R.id.filterbybutton);
        mExpertMode.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		if(Mode.equals("dmesg"))
        		{
        	    	OSMonitor Main = (OSMonitor) Self.getParent();
        	    	Intent newIntent = new Intent(Main, DmesgFilter.class);
        	    	Main.startChildActivity("DmesgFilter", newIntent, 3);
        		}
        		else
        		{
        	    	OSMonitor Main = (OSMonitor) Self.getParent();
        	    	Intent newIntent = new Intent(Main, LogcatFilter.class);
        	    	Main.startChildActivity("LogcatFilter", newIntent, 3);
        		}
        	}
        });

		Self = this;
        EmptyMsg = (TextView) findViewById(R.id.debugempty);
        MsgCountText = (TextView) findViewById(R.id.debugmsgcounts);
    }
    
    public void onRefresh()
    { 
		JNILibrary.doDataSwap();
		UpdateInterface.notifyDataSetChanged();

		if(FirstView)
    	{
    		InternalList.setSelection(UpdateInterface.getCount());
			FirstView = false;
    	}

    }
    
    public boolean onCreateOptionsMenu(Menu optionMenu) 
    {
     	super.onCreateOptionsMenu(optionMenu);
    	return true;
    }



	private void restorePrefs()
    {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		JNILibrary.doDataTime(settings.getInt(Preferences.PREF_UPDATE, 2));

        if(Mode.equals("dmesg"))
        {
            InternalList = (ListView) findViewById(R.id.loglist);
            InternalList.setEmptyView(findViewById(R.id.debugempty));
            InternalList.setAdapter(new DMesgListAdapter(this));
            InternalList.setOnItemClickListener(NullListener);
            UpdateInterface = (DMesgListAdapter) InternalList.getAdapter();
        }
        else
        {
        	InternalList = (ListView) findViewById(R.id.loglist);
            InternalList.setEmptyView(findViewById(R.id.debugempty));
            InternalList.setAdapter(new LogcatListAdapter(this));
            InternalList.setOnItemClickListener(InternalListListener);
            UpdateInterface = (LogcatListAdapter) InternalList.getAdapter();
        }
        
    }
	
	private void setTarget()
	{
		
		Mode = "logcat";
    	InternalList = (ListView) findViewById(R.id.loglist);
        InternalList.setEmptyView(findViewById(R.id.debugempty));
        InternalList.setAdapter(new LogcatListAdapter(this));
        InternalList.setOnItemClickListener(InternalListListener);
        UpdateInterface = (LogcatListAdapter) InternalList.getAdapter();
        
		JNILibrary.SetLogcatFilter(1);
		JNILibrary.SetLogcatLevel(JNILibrary.doLogcatNONE);
		JNILibrary.SetLogcatMessage("");
		JNILibrary.SetLogcatSource(0);
		JNILibrary.SetLogcatPID(targetPID);
		
	}
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	restorePrefs();
    }
    
  
    @Override
    public void onPause() 
    {
    	handler.removeCallbacks(runnable);
    	JNILibrary.doTaskStop();

    	super.onPause();
    }

    @Override
    protected void onResume() 
    {    
    	if (getIntent().getExtras() != null)
    		targetPID = this.getIntent().getExtras().getInt("targetPID", 0);
    	else
    		targetPID = 0;

		if(targetPID == 0)
    	{
    		restorePrefs();
        
    		if(Mode.equals("dmesg"))
    			JNILibrary.doTaskStart(JNILibrary.doTaskDMesg);
    		else
    			JNILibrary.doTaskStart(JNILibrary.doTaskLogcat);
    	}
    	else
    	{
    		setTarget();
			JNILibrary.doTaskStart(JNILibrary.doTaskLogcat);
    	}

        if(targetPID != 0)
        	mLogType.setEnabled(false);
        else
        	mLogType.setEnabled(true);

        if(targetPID != 0)
        	mExpertMode.setEnabled(false);
        else
        	mExpertMode.setEnabled(true);


		FirstView = true;
    	
    	handler.post(runnable);
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
    
    private OnItemClickListener InternalListListener = new OnItemClickListener()
    {
    	@Override
    	public void onItemClick(AdapterView<?> l, View v, int position, long id) 
    	{  
    		if(position > JNILibrary.GetLogcatCounts())
    			position = JNILibrary.GetLogcatCounts();
    		
    		AlertDialog.Builder LogcatInfo = new AlertDialog.Builder(l.getContext());
    		LogcatInfo.setTitle("Message");
    		LogcatInfo.setMessage(JNILibrary.GetLogcatMessage(position));
    		LogcatInfo.show();    		
    	}
    };
    
    private OnItemClickListener NullListener = new OnItemClickListener()
    {
    	@Override
    	public void onItemClick(AdapterView<?> l, View v, int position, long id) 
    	{  
    	}
    };
    
    private class DMesgListAdapter extends BaseAdapter {
        public DMesgListAdapter(Context context)
        {
            mContext = context;
        }

        public int getCount() {
           return JNILibrary.GetDebugMessageCounts();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
            DMesgDetailView sv;
            
            if (convertView == null) {
                sv = new DMesgDetailView(mContext, JNILibrary.GetDebugMessageTime(position),
                								   JNILibrary.GetDebugMessageLevel(position),
                								   JNILibrary.GetDebugMessage(position),
                								   position);
            } else {
                sv = (DMesgDetailView)convertView;
                sv.setContext( JNILibrary.GetDebugMessageTime(position),
                			   JNILibrary.GetDebugMessageLevel(position),
						   	   JNILibrary.GetDebugMessage(position),
						 	  position);
            }
            
            return sv;
        }

        private Context mContext;
    }
    
    private class DMesgDetailView extends TableLayout {

    	private TextView DMesgLevel;
    	private TextView DMesgMsg;
    	
        public DMesgDetailView(Context context,String Time, String Level, String Msg, int position) 
        {
            super(context);
            
            DMesgMsg = new TextView(context);
            DMesgLevel = new TextView(context);

            DMesgLevel.setGravity(Gravity.LEFT);
            DMesgLevel.setPadding(3, 3, 3, 3);
            DMesgLevel.setWidth(60);

            DMesgMsg.setGravity(Gravity.LEFT);
            DMesgMsg.setPadding(3, 3, 3, 3);
            DMesgMsg.setWidth(getWidth()-60);
            
            DMesgLevel.setText(Time+" ["+Level+"]");
            
            if(Level.endsWith("EMERGENCY"))
                DMesgLevel.setTextColor(Color.RED);
            else if (Level.endsWith("ALERT"))
                DMesgLevel.setTextColor(Color.RED);
            else if (Level.endsWith("CRITICAL"))
                DMesgLevel.setTextColor(Color.RED);
            else if (Level.endsWith("ERROR"))
                DMesgLevel.setTextColor(Color.RED);
            else if (Level.endsWith("WARNING"))
                DMesgLevel.setTextColor(Color.YELLOW);
            else if (Level.endsWith("NOTICE"))
                DMesgLevel.setTextColor(Color.MAGENTA);
            else if (Level.endsWith("INFORMATION"))
                DMesgLevel.setTextColor(Color.GREEN);
            else if (Level.endsWith("DEBUG"))
                DMesgLevel.setTextColor(Color.BLUE);

            DMesgMsg.setText(Msg);
            
            addView(DMesgLevel);
            addView(DMesgMsg);
            
	     	if(position % 2 == 0)
	     		setBackgroundColor(0x80444444);
	     	else
	     		setBackgroundColor(0x80000000);

        }
        
		public void setContext(String Time, String Level, String Msg, int position) 
		{
            DMesgLevel.setText(Time+" ["+Level+"]");

            if(Level.endsWith("EMERGENCY"))
                DMesgLevel.setTextColor(Color.RED);
            else if (Level.endsWith("ALERT"))
                DMesgLevel.setTextColor(Color.RED);
            else if (Level.endsWith("CRITICAL"))
                DMesgLevel.setTextColor(Color.RED);
            else if (Level.endsWith("ERROR"))
                DMesgLevel.setTextColor(Color.RED);
            else if (Level.endsWith("WARNING"))
                DMesgLevel.setTextColor(Color.YELLOW);
            else if (Level.endsWith("NOTICE"))
                DMesgLevel.setTextColor(Color.MAGENTA);
            else if (Level.endsWith("INFORMATION"))
                DMesgLevel.setTextColor(Color.GREEN);
            else if (Level.endsWith("DEBUG"))
                DMesgLevel.setTextColor(Color.BLUE);
            
            DMesgMsg.setText(Msg);

	     	if(position % 2 == 0)
	     		setBackgroundColor(0x80444444);
	     	else
	     		setBackgroundColor(0x80000000);
	     	
		}
		
		
    }
    
    private class LogcatListAdapter extends BaseAdapter {
        public LogcatListAdapter(Context context)
        {
            mContext = context;
        }

        public int getCount() {
           return JNILibrary.GetLogcatCounts();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
        	LogcatDetailView sv;
            
            if (convertView == null) {
                sv = new LogcatDetailView(mContext, 
                		JNILibrary.GetLogcatTime(position),
                		JNILibrary.GetLogcatLevel(position),
                		JNILibrary.GetLogcatPID(position),
                		JNILibrary.GetLogcatTag(position),
                		/*JNILibrary.GetLogcatMessage(position)*/ "",
                								   position);
            } else {
                sv = (LogcatDetailView)convertView;
                sv.setContext( JNILibrary.GetLogcatTime(position),
                			   JNILibrary.GetLogcatLevel(position),
               				   JNILibrary.GetLogcatPID(position),
               				   JNILibrary.GetLogcatTag(position),
               				/*JNILibrary.GetLogcatMessage(position)*/ "",
               				   position);
            }
            
            return sv;
        }

        /**
         * Remember our context so we can use it when constructing views.
         */
        private Context mContext;
    }
    
    /**
     * We will use a SpeechView to display each speech. It's just a LinearLayout
     * with two text fields.
     *
     */
    private class LogcatDetailView extends TableLayout {

    	private TextView LogcatTitle;
    	private TextView LogcatMsg;
    	
        public LogcatDetailView(Context context,String Time, String Level, int PID,
        						String Tag, String Msg, int position) 
        {
            super(context);
            
            LogcatMsg = new TextView(context);
            LogcatTitle = new TextView(context);

            LogcatTitle.setGravity(Gravity.LEFT);

            LogcatMsg.setGravity(Gravity.LEFT);
            
            LogcatTitle.setText(Time+"  ["+Level+"]\n"+Tag+"("+PID+")");
            
            if(Level.endsWith("ERROR"))
                LogcatTitle.setTextColor(Color.RED);
            else if (Level.endsWith("DEBUG"))
            	LogcatTitle.setTextColor(Color.BLUE);
            else if (Level.endsWith("INFORMATION"))
            	LogcatTitle.setTextColor(Color.GREEN);
            else if (Level.endsWith("WARNING"))
            	LogcatTitle.setTextColor(Color.YELLOW);
            else if (Level.endsWith("VERBOSE"))
            	LogcatTitle.setTextColor(Color.WHITE);

            String temp = JNILibrary.GetLogcatMessage(position);
            if(temp.length() > 200)
            	LogcatMsg.setText(Self.getResources().getString(R.string.logcat_longmsg_title));
            else
            	LogcatMsg.setText(temp.trim());
             
            addView(LogcatTitle);
            addView(LogcatMsg);
            
	     	if(position % 2 == 0)
	     		setBackgroundColor(0x80444444);
	     	else
	     		setBackgroundColor(0x80000000);

        }
        
		public void setContext(String Time, String Level, int PID,
									String Tag, String Msg, int position) 
		{
            
            LogcatTitle.setText(Time+"  ["+Level+"]\n"+Tag+"("+PID+")");
            
            if(Level.endsWith("ERROR"))
                LogcatTitle.setTextColor(Color.RED);
            else if (Level.endsWith("DEBUG"))
            	LogcatTitle.setTextColor(Color.BLUE);
            else if (Level.endsWith("INFORMATION"))
            	LogcatTitle.setTextColor(Color.GREEN);
            else if (Level.endsWith("WARNING"))
            	LogcatTitle.setTextColor(Color.YELLOW);
            else if (Level.endsWith("VERBOSE"))
            	LogcatTitle.setTextColor(Color.WHITE);

            String temp = JNILibrary.GetLogcatMessage(position);
            if(temp.length() > 150)
            	LogcatMsg.setText(Self.getResources().getString(R.string.logcat_longmsg_title));
            else
            	LogcatMsg.setText(temp.trim());
            
            
	     	if(position % 2 == 0)
	     		setBackgroundColor(0x80444444);
	     	else
	     		setBackgroundColor(0x80000000);
	     	
		}
    }
}
