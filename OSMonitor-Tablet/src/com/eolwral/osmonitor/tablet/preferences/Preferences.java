package com.eolwral.osmonitor.tablet.preferences;

import java.util.List;

import com.eolwral.osmonitor.tablet.R;
import com.eolwral.osmonitor.tablet.CommonUtil;
import com.eolwral.osmonitor.tablet.OSMonitorService;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.Window;


public class Preferences extends PreferenceActivity {

	public static final String PREF_VIEW = "Preference_View";
	public static final String PREF_UPDATE = "GlobalUpdate_Preference";

	public static final String PREF_CPUUSAGE = "CPUUsage_Preference";
	public static final String PREF_STATUSBARCOLOR = "StatusBarColor_Preference";
	public static final String PREF_AUTOSTART = "AutoStart_Preference";
	public static final String PREF_TEMPERATURE = "Temperature_Preference";
	
	public static final String PREF_EXCLUDE = "Exclude_Preference";
	public static final String PREF_IP6to4 = "IP6to4_Preference";
	public static final String PREF_RDNS = "RDNS_Preference";
	
	private static Application target = null;
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// turn title bar off
   	    requestWindowFeature(Window.FEATURE_NO_TITLE);
   	    target = getApplication();
   	    super.onCreate(savedInstanceState);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }
    
    /**
     * This fragment shows the preferences for the first header.
     */
    public static class MainFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
       	    super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.main_preference_inner);
            
            Preference AutoStart = (Preference)findPreference(PREF_AUTOSTART);
            
           	if(CommonUtil.checkExtraStore(this.getActivity()))
       			AutoStart.setEnabled(false);
            
           	Preference StatusBar = (Preference) findPreference(PREF_CPUUSAGE);
           	StatusBar.setOnPreferenceChangeListener( new OnPreferenceChangeListener ()
            {
    			@Override
    			public boolean onPreferenceChange(Preference preference, Object newValue)
    			{
    				boolean value = (Boolean) newValue;
    		        if(value)
   		        		target.startService(new Intent(target, OSMonitorService.class));
    		        else
    		        	target.stopService(new Intent(target, OSMonitorService.class));	
    		        return true;
    			}
            });
        }
    }

    /**
     * This fragment contains a second-level set of preference that you
     * can get to by tapping an item in the first preferences fragment.
     */
    public static class SubFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.sub_preference_inner);
        }
    }


}
