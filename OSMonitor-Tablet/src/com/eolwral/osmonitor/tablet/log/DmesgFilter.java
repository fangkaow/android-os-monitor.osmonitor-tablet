package com.eolwral.osmonitor.tablet.log;

import com.eolwral.osmonitor.tablet.R;
import com.eolwral.osmonitor.tablet.*;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.KeyEvent;

public class DmesgFilter extends PreferenceActivity {
	
	private JNIInterface JNILibrary = JNIInterface.getInstance();

	public static final String PREF_DMESGUSEFILTER = "DMESGEnable_Preference";
	public static final String PREF_DMESGFILTERSTR = "DMESGStr_Preference";
	public static final String PREF_DMESGFILTERLV = "DMESGLevel_Preference";
	public static final String PREF_DMESGEXPORT = "DMESGExport_Preference";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.dmesgfilter);
        
    	Preference dmesgEnabled = (Preference)findPreference(PREF_DMESGUSEFILTER);
        dmesgEnabled.setOnPreferenceChangeListener( new OnPreferenceChangeListener ()
        {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				boolean value = (Boolean) newValue;
				if(value)
					JNILibrary.SetDebugMessageFilter(1);
				else
					JNILibrary.SetDebugMessageFilter(0);
				JNILibrary.TruncateDebugMessage();
				return true;
			}
        });
        
    	Preference dmesgFilter = (Preference)findPreference(PREF_DMESGFILTERSTR);
    	dmesgFilter.setOnPreferenceChangeListener( new OnPreferenceChangeListener ()
        { 
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				String value = (String) newValue;
				if(!value.equals(""))
					JNILibrary.SetDebugMessage(value);
				JNILibrary.TruncateDebugMessage();
				return true;
			}
        });

    	Preference dmesgFilterLV = (Preference)findPreference(PREF_DMESGFILTERLV);
    	dmesgFilterLV.setOnPreferenceChangeListener( new OnPreferenceChangeListener ()
        { 
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				int value = Integer.parseInt((String) newValue);
				JNILibrary.SetDebugMessageLevel(value);
				JNILibrary.TruncateDebugMessage();
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
