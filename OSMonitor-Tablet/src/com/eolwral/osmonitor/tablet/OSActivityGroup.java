/*
 * It is a little hacking to disable singleActivityMode.
 * by eolwral
 */

package com.eolwral.osmonitor.tablet;

import android.app.ActivityGroup;

public class OSActivityGroup extends ActivityGroup {
	
    public OSActivityGroup() {
        this(false);
    }
    
    public OSActivityGroup(boolean singleActivityMode) {
    	super(false);
    }
}

