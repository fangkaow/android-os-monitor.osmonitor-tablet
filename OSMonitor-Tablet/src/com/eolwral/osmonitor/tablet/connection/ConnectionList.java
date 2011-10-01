/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eolwral.osmonitor.tablet.connection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.eolwral.osmonitor.tablet.R;
import com.eolwral.osmonitor.tablet.*;
import com.eolwral.osmonitor.tablet.preferences.Preferences;


public class ConnectionList extends Activity
{
	private static ConnectionList Self = null;
	private static NetworkListAdapter UpdateInterface = null;
	private static JNIInterface JNILibrary = JNIInterface.getInstance();
	private static TextView EmptyMsg = null;
	private static PackageManager AppInfo = null;

	private boolean RDNS = false;
	
    private ListView InternalList = null;
    
    private ConnectionInfoQuery NetworkInfo = null;
    
	private Runnable runnable = new Runnable() {
		public void run() {
	        
			if(JNILibrary.doDataLoad() == 1)
			{
				Self.onRefresh();

				if(EmptyMsg != null)
					EmptyMsg.setText("");
			}
	        
	        handler.postDelayed(this, 1000);
		}
	};   
	
	Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // Use a custom layout file
        setContentView(R.layout.connectionlayout);

    	AppInfo = this.getPackageManager();
    	NetworkInfo = ConnectionInfoQuery.getInstance();
        
    	Self = this;
    	InternalList = (ListView) findViewById(R.id.connectionlist);
        InternalList.setEmptyView(findViewById(R.id.empty));
        InternalList.setAdapter(new NetworkListAdapter(this));
        InternalList.setOnItemClickListener(ConnectionListener);
        
        UpdateInterface = (NetworkListAdapter) InternalList.getAdapter();
        
        EmptyMsg = (TextView) findViewById(R.id.empty);
 
    }
    
    public void onRefresh()
    {
    	JNILibrary.doDataSwap();
    	UpdateInterface.notifyDataSetChanged();
    }
    
	private void restorePrefs()
    {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		JNILibrary.doDataTime(settings.getInt(Preferences.PREF_UPDATE, 2));
		
		if(settings.getBoolean(Preferences.PREF_IP6to4, true))
			JNILibrary.SetNetworkIP6To4(1);
		else
			JNILibrary.SetNetworkIP6To4(0);
		
		if(settings.getBoolean(Preferences.PREF_RDNS, false))
			RDNS = true;
		else
			RDNS = false;
	
    }
	
    private String GetAppInfo(String AppName, int UID)
    {
		PackageInfo appPackageInfo = null;
		String PackageName = null;
		if(AppName.contains(":"))
			PackageName = AppName.substring(0, AppName.indexOf(":"));
		else
			PackageName = AppName;
		
		// for system user
		try {  
			appPackageInfo = AppInfo.getPackageInfo(PackageName, 0);
		} catch (NameNotFoundException e) {}
		
		if(appPackageInfo == null && UID >0)
		{
			String[] subPackageName = AppInfo.getPackagesForUid(UID);
				
			if(subPackageName != null)
			{
				for(int PackagePtr = 0; PackagePtr < subPackageName.length; PackagePtr++)
				{
					if (subPackageName[PackagePtr] == null)
						continue;
					
					try {  
						appPackageInfo = AppInfo.getPackageInfo(subPackageName[PackagePtr], 0);
						PackagePtr = subPackageName.length;
					} catch (NameNotFoundException e) {}						
				}
			}
		}    	
		if(appPackageInfo != null)
			return appPackageInfo.applicationInfo.loadLabel(AppInfo).toString();
		else if (UID == 0)
			return "System";
		else
			return PackageName;
    }
    private OnItemClickListener ConnectionListener = new OnItemClickListener()
    {
    	@Override
    	public void onItemClick(AdapterView<?> l, View v, int position, long id) 
    	{  
    		if(JNILibrary.GetNetworkRemoteIP(position).equals("0.0.0.0"))
    			return;
    		
    		String QueryIP = "";
       		if(JNILibrary.GetNetworkProtocol(position).equals("TCP6") ||
        			JNILibrary.GetNetworkProtocol(position).equals("UDP6"))
        		{
        			QueryIP = JNILibrary.GetNetworkRemoteIP(position);
        			QueryIP = QueryIP.replaceFirst("ffff:", "");
        			QueryIP = QueryIP.replaceFirst("::", "");
        		}
        		else
        			QueryIP = JNILibrary.GetNetworkRemoteIP(position);
       		
       		if(QueryIP.equals("0.0.0.0"))
       			return;
       		
		    OSMonitor Main = (OSMonitor) Self.getParent();
   		    Intent newIntent = new Intent(Main, ConnectionDetail.class);
   		    newIntent.putExtra("targetIP", QueryIP);
   		    Main.startChildActivity("ConnectionDetail", newIntent, 3);
    	}
    };
    
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
        restorePrefs();
    	JNILibrary.doTaskStart(JNILibrary.doTaskNetwork);
    	handler.post(runnable);
    	super.onResume();
    }
    
    private class NetworkListAdapter extends BaseAdapter {
        public NetworkListAdapter(Context context)
        {
            mContext = context;
        }

        /**
         * The number of items in the list is determined by the number of speeches
         * in our array.
         * 
         * @see android.widget.ListAdapter#getCount()
         */
        public int getCount() {
            return JNILibrary.GetNetworkCounts();
        }

        /**
         * Since the data comes from an array, just returning
         * the index is sufficient to get at the data. If we
         * were using a more complex data structure, we
         * would return whatever object represents one 
         * row in the list.
         * 
         * @see android.widget.ListAdapter#getItem(int)
         */
        public Object getItem(int position) {
            return position;
        }

        /**
         * Use the array index as a unique id.
         * @see android.widget.ListAdapter#getItemId(int)
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * Make a SpeechView to hold each row.
         * @see android.widget.ListAdapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
        	NetworkDetailView sv;

        	String LocalDNS = "";
    		String RemoteDNS = "";
    		
           	if(JNILibrary.GetNetworkProtocol(position).equals("TCP6") ||
           		JNILibrary.GetNetworkProtocol(position).equals("UDP6"))
           	{
           		String QueryIPv4 = JNILibrary.GetNetworkLocalIP(position);
       			QueryIPv4 = QueryIPv4.replaceFirst("ffff:", "");
       			QueryIPv4 = QueryIPv4.replaceFirst("::", "");
           		NetworkInfo.doCacheInfo(QueryIPv4);
       			LocalDNS = NetworkInfo.GetDNS(QueryIPv4);

       			QueryIPv4 = JNILibrary.GetNetworkRemoteIP(position);
       			QueryIPv4 = QueryIPv4.replaceFirst("ffff:", "");
       			QueryIPv4 = QueryIPv4.replaceFirst("::", "");
       			NetworkInfo.doCacheInfo(QueryIPv4);
           		RemoteDNS = NetworkInfo.GetDNS(QueryIPv4);
           	}
           	else
           	{
           		NetworkInfo.doCacheInfo(JNILibrary.GetNetworkRemoteIP(position));
           		RemoteDNS = NetworkInfo.GetDNS(JNILibrary.GetNetworkRemoteIP(position));
           		NetworkInfo.doCacheInfo(JNILibrary.GetNetworkLocalIP(position));
           		LocalDNS = NetworkInfo.GetDNS(JNILibrary.GetNetworkLocalIP(position));
           	}
            

            String AppName = GetAppInfo(JNILibrary.GetProcessNamebyUID(JNILibrary.GetNetworkUID(position)),
            							JNILibrary.GetNetworkUID(position));

            if (convertView == null) {
            	if(RDNS)
            	{
            		sv = new NetworkDetailView(mContext, JNILibrary.GetNetworkProtocol(position),
            										 LocalDNS,
                									 JNILibrary.GetNetworkLocalPort(position),
                				                     RemoteDNS,
                									 JNILibrary.GetNetworkRemotePort(position),
                									 JNILibrary.GetNetworkStatus(position),
                									 AppName,
                									 position);
            	}
            	else
                    sv = new NetworkDetailView(mContext, JNILibrary.GetNetworkProtocol(position),
		                     JNILibrary.GetNetworkLocalIP(position),
							 JNILibrary.GetNetworkLocalPort(position),
		                     JNILibrary.GetNetworkRemoteIP(position),
							 JNILibrary.GetNetworkRemotePort(position),
							 JNILibrary.GetNetworkStatus(position),
							 AppName,
							 position);

            } else {
                sv = (NetworkDetailView)convertView;
                if(RDNS)
                	sv.setContext(JNILibrary.GetNetworkProtocol(position),
                			  LocalDNS,
							  JNILibrary.GetNetworkLocalPort(position),
					          RemoteDNS,
 							  JNILibrary.GetNetworkRemotePort(position),
						 	  JNILibrary.GetNetworkStatus(position),
							  AppName,
						 	  position);
                else
                	sv.setContext(JNILibrary.GetNetworkProtocol(position),
              			  	  JNILibrary.GetNetworkLocalIP(position),
							  JNILibrary.GetNetworkLocalPort(position),
					          JNILibrary.GetNetworkRemoteIP(position),
							  JNILibrary.GetNetworkRemotePort(position),
						 	  JNILibrary.GetNetworkStatus(position),
						 	  AppName,
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
    private class NetworkDetailView extends TableLayout {

    	private TableRow ConnectionRow;
    	private TextView ProtocolField;
    	private TextView IPField;
    	private TextView StatusField;
    	private TableRow ExtendRow;
    	private TextView APPField;
    	
        public NetworkDetailView(Context context,String Protocol, String LocalIP, int LocalPort,
        							String RemoteIP, int RemotePort, String Status, String AppName, int position) 
        {
            super(context);
            this.setColumnStretchable(1, true);
            this.setOrientation(VERTICAL);
            
            ProtocolField = new TextView(context);
            IPField = new TextView(context);
            StatusField = new TextView(context);
            APPField = new TextView(context);

            ProtocolField.setGravity(Gravity.LEFT);
            ProtocolField.setPadding(3, 3, 3, 3);
            ProtocolField.setWidth(70);

            IPField.setGravity(Gravity.LEFT);
            IPField.setPadding(3, 3, 3, 3);
            IPField.setWidth(getWidth()-160);

            StatusField.setGravity(Gravity.LEFT);
            StatusField.setPadding(3, 3, 3, 3);
            StatusField.setWidth(140);
            
            ProtocolField.setText(Protocol);
            StatusField.setText(Status);

            if(RemotePort == 0)
           		IPField.setText(LocalIP+":"+LocalPort+"\n"+RemoteIP+":*");
            else
           		IPField.setText(LocalIP+":"+LocalPort+"\n"+RemoteIP+":"+RemotePort);
            	 
            ConnectionRow = new TableRow(context);
            ConnectionRow.addView(ProtocolField);
            ConnectionRow.addView(IPField);
            ConnectionRow.addView(StatusField);
            addView(ConnectionRow);
            
            ExtendRow = new TableRow(context);
            APPField.setText(AppName);
            
            ExtendRow.addView(new TextView(context));
            ExtendRow.addView(APPField);
            
            TableRow.LayoutParams RowParams = (TableRow.LayoutParams)APPField.getLayoutParams();
            RowParams.span = 2;
            APPField.setLayoutParams(RowParams);
            
            addView(ExtendRow);

            if(position % 2 == 0)
	     		setBackgroundColor(0x80444444);
	     	else
	     		setBackgroundColor(0x80000000);
 
        }
        
		public void setContext(String Protocol, String LocalIP, int LocalPort,
								String RemoteIP, int RemotePort, String Status, String AppName, int position) 
		{
			
            ProtocolField.setText(Protocol);
            StatusField.setText(Status);

            if(RemotePort == 0)
           		IPField.setText(LocalIP+":"+LocalPort+"\n"+RemoteIP+":*");
            else
           		IPField.setText(LocalIP+":"+LocalPort+"\n"+RemoteIP+":"+RemotePort);
            
            APPField.setText(AppName);
                      
	     	if(position % 2 == 0)
	     		setBackgroundColor(0x80444444);
	     	else
	     		setBackgroundColor(0x80000000);
		}
    }
}
