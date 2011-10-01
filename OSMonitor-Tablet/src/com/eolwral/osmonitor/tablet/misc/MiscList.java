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

package com.eolwral.osmonitor.tablet.misc;


import android.app.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.eolwral.osmonitor.tablet.R;
import com.eolwral.osmonitor.tablet.*;
import com.eolwral.osmonitor.tablet.preferences.Preferences;

public class MiscList extends Activity
{
	private static JNIInterface JNILibrary = JNIInterface.getInstance();;
	private static MiscList Self = null;
	
	private static long PreCPUFreq = 0;
	
	private Runnable MiscRunnable = new Runnable() {
		public void run() 
		{
		
			if(JNILibrary.doDataLoad() == 1)
			{  
				Self.onRefresh();
			}
           	MiscHandler.postDelayed(this, 1000);
        }
	};
	
	private Runnable doUpdateUI = new Runnable() {
		public void run() {
			((TextView) Self.findViewById(R.id.misc_processor_num_value)).setText(""+JNILibrary.GetProcessorNum());
			
			int ProcessorScalCur = 0;
			int ProcessorMax = 0;
			int ProcessorMin = 0;
			
			if(JNILibrary.GetProcessorNum() != 0) {
				for(int i = 0; i < JNILibrary.GetProcessorNum(); i++)
				{
					ProcessorScalCur += JNILibrary.GetProcessorScalCur(i);
					ProcessorMax += JNILibrary.GetProcessorMax(i);
					ProcessorMin += JNILibrary.GetProcessorMin(i);
				}
				ProcessorScalCur /= JNILibrary.GetProcessorNum();
				ProcessorMax /= JNILibrary.GetProcessorNum();
				ProcessorMin /= JNILibrary.GetProcessorNum();
			}
			
			((TextView) Self.findViewById(R.id.misc_processor_scal_value)).setText(JNILibrary.GetProcessorScalMin(0)+"~"
																		  +JNILibrary.GetProcessorScalMax(0));
			((TextView) Self.findViewById(R.id.misc_processor_freq_value)).setText(ProcessorMin+"~"+ProcessorMax);
			((TextView) Self.findViewById(R.id.misc_processor_gov_value)).setText(""+JNILibrary.GetProcessorScalGov(0));
			((TextView) Self.findViewById(R.id.misc_processor_cur_value)).setText(""+ProcessorScalCur);
			if(JNILibrary.GetProcessorScalCur(0) > PreCPUFreq)
				((TextView) Self.findViewById(R.id.misc_processor_cur_value)).setTextColor(android.graphics.Color.RED);
			else if (JNILibrary.GetProcessorScalCur(0) < PreCPUFreq)
				((TextView) Self.findViewById(R.id.misc_processor_cur_value)).setTextColor(android.graphics.Color.GREEN);
			else
				((TextView) Self.findViewById(R.id.misc_processor_cur_value)).setTextColor(android.graphics.Color.WHITE);
			PreCPUFreq = JNILibrary.GetProcessorScalCur(0);

			java.text.DecimalFormat DiskFormat = new java.text.DecimalFormat(",###");
			java.text.DecimalFormat UsageFormat = new java.text.DecimalFormat("#.#");
			
			((TextView) Self.findViewById(R.id.misc_diskrate_system_value)).setText(UsageFormat.format(JNILibrary.GetSystemMemUsed()/JNILibrary.GetSystemMemTotal()*100)+"%");    		   	
			((TextView) Self.findViewById(R.id.misc_disktotal_system_value)).setText(DiskFormat.format(JNILibrary.GetSystemMemTotal())+"K ");
			((TextView) Self.findViewById(R.id.misc_diskused_system_value)).setText(DiskFormat.format(JNILibrary.GetSystemMemUsed())+"K ");
			((TextView) Self.findViewById(R.id.misc_diskavaiable_system_value)).setText(DiskFormat.format(JNILibrary.GetSystemMemAvail())+"K ");

			((TextView) Self.findViewById(R.id.misc_diskrate_data_value)).setText(UsageFormat.format(JNILibrary.GetDataMemUsed()/JNILibrary.GetDataMemTotal()*100)+"%");    		   	
			((TextView) Self.findViewById(R.id.misc_disktotal_data_value)).setText(DiskFormat.format(JNILibrary.GetDataMemTotal())+"K ");
			((TextView) Self.findViewById(R.id.misc_diskused_data_value)).setText(DiskFormat.format(JNILibrary.GetDataMemUsed())+"K ");
			((TextView) Self.findViewById(R.id.misc_diskavaiable_data_value)).setText(DiskFormat.format(JNILibrary.GetDataMemAvail())+"K ");

			((TextView) Self.findViewById(R.id.misc_diskrate_sdcard_value)).setText(UsageFormat.format(JNILibrary.GetSDCardMemUsed()/JNILibrary.GetSDCardMemTotal()*100)+"%");    		   	
			((TextView) Self.findViewById(R.id.misc_disktotal_sdcard_value)).setText(DiskFormat.format(JNILibrary.GetSDCardMemTotal())+"K ");
			((TextView) Self.findViewById(R.id.misc_diskused_sdcard_value)).setText(DiskFormat.format(JNILibrary.GetSDCardMemUsed())+"K ");
			((TextView) Self.findViewById(R.id.misc_diskavaiable_sdcard_value)).setText(DiskFormat.format(JNILibrary.GetSDCardMemAvail())+"K ");

			((TextView) Self.findViewById(R.id.misc_diskrate_cache_value)).setText(UsageFormat.format(JNILibrary.GetCacheMemUsed()/JNILibrary.GetCacheMemTotal()*100)+"%");    		   	
			((TextView) Self.findViewById(R.id.misc_disktotal_cache_value)).setText(DiskFormat.format(JNILibrary.GetCacheMemTotal())+"K ");
			((TextView) Self.findViewById(R.id.misc_diskused_cache_value)).setText(DiskFormat.format(JNILibrary.GetCacheMemUsed())+"K ");
			((TextView) Self.findViewById(R.id.misc_diskavaiable_cache_value)).setText(DiskFormat.format(JNILibrary.GetCacheMemAvail())+"K ");
		}
	};   
	
	Handler MiscHandler = new Handler();


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
       
        setContentView(R.layout.misclayout);
        Self = this;
        
        ((Button) findViewById(R.id.cpu_detail_button)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
   		    	OSMonitor Main = (OSMonitor) Self.getParent();
   		    	Intent newIntent = new Intent(Main, CPUDetail.class);
   		    	Main.startChildActivity("CPUDetail", newIntent, 3);
			}
        });
    }
    
    public void onRefresh()
    {
    	runOnUiThread(doUpdateUI);
    }
    
    @Override
    public void onPause() 
    {
   		stopBatteryMonitor();

    	MiscHandler.removeCallbacks(MiscRunnable);
    	JNILibrary.doTaskStop();
    	super.onPause();
    }

    @Override
    protected void onResume() 
    {    
   		startBatteryMonitor();
    	
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		JNILibrary.doDataTime(settings.getInt(Preferences.PREF_UPDATE, 2));
    	JNILibrary.doTaskStart(JNILibrary.doTaskMisc);
    	MiscHandler.post(MiscRunnable);
    	super.onResume();
    }
    
    private void startBatteryMonitor()
    {
    	IntentFilter battFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    	registerReceiver(battReceiver, battFilter);		        		
    }
    
    private void stopBatteryMonitor()
    {
    	unregisterReceiver(battReceiver);
    }

	private static BroadcastReceiver battReceiver = new BroadcastReceiver() 
	{
		public void onReceive(Context context, Intent intent) {
			
			int rawlevel = intent.getIntExtra("level", -1);
			int scale = intent.getIntExtra("scale", -1);
			int status = intent.getIntExtra("status", -1);
			int health = intent.getIntExtra("health", -1);
			int plugged = intent.getIntExtra("plugged", -1);
			int temperature = intent.getIntExtra("temperature", -1);
			int voltage = intent.getIntExtra("voltage", -1);
			String technology = intent.getStringExtra("technology");
				
			int level = -1;  // percentage, or -1 for unknown
			if (rawlevel >= 0 && scale > 0) {
				level = (rawlevel * 100) / scale;
			}

			switch(status) 
			{
			case BatteryManager.BATTERY_STATUS_UNKNOWN:
				((TextView) Self.findViewById(R.id.misc_status_value)).setText("Unknown");
				break;
			case BatteryManager.BATTERY_STATUS_CHARGING:
				((TextView) Self.findViewById(R.id.misc_status_value)).setText("Charging");
				break;
			case BatteryManager.BATTERY_STATUS_DISCHARGING:
				((TextView) Self.findViewById(R.id.misc_status_value)).setText("DisCharging");
				break;
			case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
				((TextView) Self.findViewById(R.id.misc_status_value)).setText("Not Charging");
				break;
			case BatteryManager.BATTERY_STATUS_FULL:
				((TextView) Self.findViewById(R.id.misc_status_value)).setText("Full");
				break;
			}
				
			switch(health)
			{
			case BatteryManager.BATTERY_HEALTH_DEAD:
				((TextView) Self.findViewById(R.id.misc_health_value)).setText("Dead");
				break;
				case BatteryManager.BATTERY_HEALTH_GOOD:
				((TextView) Self.findViewById(R.id.misc_health_value)).setText("Good");
				break;
			case BatteryManager.BATTERY_HEALTH_OVERHEAT:
				((TextView) Self.findViewById(R.id.misc_health_value)).setText("Over Heat");
				break;
			case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
				((TextView) Self.findViewById(R.id.misc_health_value)).setText("Over Voltage");
				break;
			case BatteryManager.BATTERY_HEALTH_UNKNOWN:
				((TextView) Self.findViewById(R.id.misc_health_value)).setText("Unknown");
				break;
			case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
				((TextView) Self.findViewById(R.id.misc_health_value)).setText("Unspecified Failure");
				break;
				
			}
			
			java.text.DecimalFormat TempFormat = new java.text.DecimalFormat("#.##");
			((TextView) Self.findViewById(R.id.misc_technology_value)).setText(technology);
			((TextView) Self.findViewById(R.id.misc_capacity_value)).setText(""+level+"%");
			((TextView) Self.findViewById(R.id.misc_voltage_value)).setText(voltage+"mV");
			((TextView) Self.findViewById(R.id.misc_temperature_value)).setText(""+((double) temperature/10)+"¢XC ("
	    		                                   +TempFormat.format(((double)temperature/10*9/5+32))+"¢XF)");
				
			if(plugged == BatteryManager.BATTERY_PLUGGED_AC)
				((TextView) Self.findViewById(R.id.misc_power_value)).setText(Self.getResources().getString(R.string.misc_acpower_text));
			else if(plugged == BatteryManager.BATTERY_PLUGGED_USB)
				((TextView) Self.findViewById(R.id.misc_power_value)).setText(Self.getResources().getString(R.string.misc_usbpower_text));
    	   	else
				((TextView) Self.findViewById(R.id.misc_power_value)).setText(Self.getResources().getString(R.string.misc_nopower_text));
		}
	};
    
}
