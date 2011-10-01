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

package com.eolwral.osmonitor.tablet.network;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.eolwral.osmonitor.tablet.R;
import com.eolwral.osmonitor.tablet.*;
import com.eolwral.osmonitor.tablet.preferences.Preferences;

public class NetworkList extends ListActivity 
{
	private static NetworkList Self = null;
    private static NetworkListAdapter UpdateInterface = null;
	private static JNIInterface JNILibrary = JNIInterface.getInstance();;

		
	// Refresh
	private Runnable InterfcaeRunnable = new Runnable() {
		public void run() {

			if(JNILibrary.doDataLoad() == 1) 
				Self.onRefresh();
			
	        InterfaceHandler.postDelayed(this, 1000);
		}
	};   
	
	Handler InterfaceHandler = new Handler();
	

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // Use a custom layout file
        setContentView(R.layout.networklayout);

        // Setup our adapter
        Self = this;
        setListAdapter(new NetworkListAdapter(this));
        UpdateInterface = (NetworkListAdapter) getListAdapter();
        
    }
    
    public void onRefresh()
    {
    	JNILibrary.doDataSwap();
    	UpdateInterface.notifyDataSetChanged();
    }
        
    @Override
    public void onPause() 
    {
    	InterfaceHandler.removeCallbacks(InterfcaeRunnable);
    	JNILibrary.doTaskStop();
    	super.onPause();
    }

    @Override
    protected void onResume() 
    {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		JNILibrary.doDataTime(settings.getInt(Preferences.PREF_UPDATE, 2));
    	JNILibrary.doTaskStart(JNILibrary.doTaskInterface);
    	InterfaceHandler.post(InterfcaeRunnable);
    	super.onResume();
    }
    
    @Override
    protected void onListItemClick(ListView mListView, View mView, int position, long id)
    {
    	OSMonitor Main = (OSMonitor) Self.getParent();
    	Intent newIntent = new Intent(Main, NetworkDetail.class);
    	newIntent.putExtra("targetInterface", position);
    	Main.startChildActivity("NetworkDetail", newIntent, 3);
    }    
    
    public class NetworkListAdapter extends BaseAdapter {
 
        private Context mContext = null;
        private LayoutInflater mInflater = null;

    	public NetworkListAdapter(Context context)
        {
            this.mContext = context;
        	mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
        }
        
		@Override
		public int getCount() {
			return JNILibrary.GetInterfaceCounts();
		}

		@Override
		public Object getItem(int position) {
			  return position;
		}

		@Override
		public long getItemId(int position) {
			  return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
	        View sv = null;

    	    if (convertView == null) {
    	    	sv = (View) mInflater.inflate(R.layout.networkitem, parent, false);
            } 
            else {
                sv = (View) convertView;
        	}
    	    
			((TextView) sv.findViewById(R.id.networkname)).setText(JNILibrary.GetInterfaceName(position));
			
			if(JNILibrary.GetInterfaceFlags(position).contains("$up$"))
			{
				((TextView) sv.findViewById(R.id.networkvalue)).setText(mContext.getResources().getString(R.string.network_show_up));
				((TextView) sv.findViewById(R.id.networkvalue)).setTextColor(android.graphics.Color.GREEN);
				((ImageView) sv.findViewById(R.id.networkicon)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.network_online));
			}
			else if(!JNILibrary.GetInterfaceAddr(position).equals("0.0.0.0"))
			{
				((TextView) sv.findViewById(R.id.networkvalue)).setText(mContext.getResources().getString(R.string.network_show_down));
				((TextView) sv.findViewById(R.id.networkvalue)).setTextColor(android.graphics.Color.RED);
				((ImageView) sv.findViewById(R.id.networkicon)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.network_idle));
			}
			else
			{
				((TextView) sv.findViewById(R.id.networkvalue)).setText(mContext.getResources().getString(R.string.network_show_dis));
				((TextView) sv.findViewById(R.id.networkvalue)).setTextColor(android.graphics.Color.GRAY);
				((ImageView) sv.findViewById(R.id.networkicon)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.network_offline));
			}
			
			if(position % 2 == 0)
				sv.setBackgroundColor(0x80444444);
			else
				sv.setBackgroundColor(0x80000000);
			
           	return sv;		}

    }
}
