package com.eolwral.osmonitor.tablet.log;

import com.eolwral.osmonitor.tablet.R;
import com.eolwral.osmonitor.tablet.*;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.KeyEvent;

public class LogcatFilter extends PreferenceActivity {
	
	private JNIInterface JNILibrary = JNIInterface.getInstance();

	public static final String PREF_LOGCATSOURCE = "LOGCATSource_Preference";
	public static final String PREF_LOGCATUSEFILTER = "LOGCATEnable_Preference";
	public static final String PREF_LOGCATFILTERLV = "LOGCATLevel_Peference";
	public static final String PREF_LOGCATFILTERPID = "LOGCATPID_Preference";
	public static final String PREF_LOGCATFILTERSTR = "LOGCATStr_Peference";
	 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.logcatfilter);

        
    	Preference logcatSource = (Preference)findPreference(PREF_LOGCATSOURCE);
    	logcatSource.setOnPreferenceChangeListener( new OnPreferenceChangeListener ()
        {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				String value = (String) newValue;
				JNILibrary.SetLogcatSource(Integer.parseInt(value));
				JNILibrary.TruncateLogcat();
				return true;
			}
        }); 

        
    	Preference logcatEnabled = (Preference)findPreference(PREF_LOGCATUSEFILTER);
    	logcatEnabled.setOnPreferenceChangeListener( new OnPreferenceChangeListener ()
        {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				boolean value = (Boolean) newValue;
				if(value)
					JNILibrary.SetLogcatFilter(1);
				else
					JNILibrary.SetLogcatFilter(0);
				JNILibrary.TruncateLogcat();
				return true;
			}
        });

        
    	Preference logcatFilterPID = (Preference)findPreference(PREF_LOGCATFILTERPID);
    	logcatFilterPID.setOnPreferenceChangeListener( new OnPreferenceChangeListener ()
        {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				int value = Integer.parseInt((String) newValue);
				JNILibrary.SetLogcatPID(value);
				JNILibrary.TruncateLogcat();
				return true;
			}
        });
        
    	Preference logcatFilterSTR = (Preference)findPreference(PREF_LOGCATFILTERSTR);
    	logcatFilterSTR.setOnPreferenceChangeListener( new OnPreferenceChangeListener ()
        { 
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				String value = (String) newValue;
				if(!value.equals(""))
					JNILibrary.SetLogcatMessage(value);
				JNILibrary.TruncateLogcat();
				return true;
			}
        });

    	Preference logcatFilterLV = (Preference)findPreference(PREF_LOGCATFILTERLV);
    	logcatFilterLV.setOnPreferenceChangeListener( new OnPreferenceChangeListener ()
        { 
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				int value = Integer.parseInt((String) newValue);
				JNILibrary.SetLogcatLevel(value);
				JNILibrary.TruncateLogcat();
				return true;
			}
        });
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
 
}
