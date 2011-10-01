package com.eolwral.osmonitor.tablet;

import com.eolwral.osmonitor.tablet.preferences.Preferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootUpReceiver extends BroadcastReceiver{

	JNIInterface JNILibrary = JNIInterface.getInstance();
	 
	@Override
	public void onReceive(Context context, Intent intent) {

		// load settings
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		
        if(settings.getBoolean(Preferences.PREF_AUTOSTART, false) && 
        		settings.getBoolean(Preferences.PREF_CPUUSAGE, false))
        {
        	context.startService(new Intent(context, OSMonitorService.class));
        }
	}
}
