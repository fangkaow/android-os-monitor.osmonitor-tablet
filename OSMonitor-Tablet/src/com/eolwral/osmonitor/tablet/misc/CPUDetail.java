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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.eolwral.osmonitor.tablet.R;
import com.eolwral.osmonitor.tablet.*;


public class CPUDetail extends Activity
{
	private CPUDetail Self = null;
	private JNIInterface JNILibrary = JNIInterface.getInstance();
	
	private String [] CPUGovList = null;
	private String [] CPUFreqList = null;

    private ListView InternalList = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // Use a custom layout file
        setContentView(R.layout.cpudetail);

    	Self = this;
    	InternalList = (ListView) findViewById(R.id.cpulist);
        InternalList.setEmptyView(findViewById(R.id.empty));
        InternalList.setAdapter(new CPUListAdapter(this));
        
		try {
			byte[] RawData = new byte[256];
			
			File CPUFreq = new File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies");
			BufferedInputStream bInputStream = 
				new BufferedInputStream(new FileInputStream(CPUFreq));

	    	bInputStream.read(RawData);
	    	String CPUFreqListString = (new String(RawData)).trim();
	    	bInputStream.close();
	    		
	    	CPUFreqList = CPUFreqListString.split(" ");
		} catch (Exception e) {}

		try {
			byte[] RawData = new byte[256];

			File CPUGov = new File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors");
			BufferedInputStream bInputStream = 
				new BufferedInputStream(new FileInputStream(CPUGov));

			bInputStream.read(RawData);
			String CPUGovListString = (new String(RawData)).trim();
			bInputStream.close();
	    		
			CPUGovList = CPUGovListString.split(" ");
		} catch (Exception e) {}
		
	}
    
	private class CPUListAdapter extends BaseAdapter {
    	
    	private LayoutInflater mInflater = null;
        private Context mContext = null;
        
        public CPUListAdapter(Context context)
        {
            mContext = context;
        	mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  

        }

        public int getCount() {
            return JNILibrary.GetProcessorNum();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
        	View sv = null;

            if (convertView == null && CPUFreqList != null && CPUGovList != null) {
            	sv = (View) mInflater.inflate(R.layout.cpuitem, parent, false);

        		final SeekBar MaxSeekBar = (SeekBar)sv.findViewById(R.id.cpu_detail_max_value);
        		final SeekBar MinSeekBar = (SeekBar)sv.findViewById(R.id.cpu_detail_min_value);
                final TextView MaxSeekBarValue = (TextView)sv.findViewById(R.id.cpu_processor_freq_max_title);
                final TextView MinSeekBarValue = (TextView)sv.findViewById(R.id.cpu_processor_freq_min_title);

            	MaxSeekBar.setMax(CPUFreqList.length-1);
        		MinSeekBar.setMax(CPUFreqList.length-1);
        		
        		ArrayAdapter<String> GovAdapter = new ArrayAdapter<String>(Self, android.R.layout.simple_spinner_item, CPUGovList);
        		GovAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        		((Spinner)sv.findViewById(R.id.cpu_detail_gov_value)).setAdapter(GovAdapter);

        		for(int i = 0; i < CPUGovList.length;i++)
        			if(JNILibrary.GetProcessorScalGov(position).equals(CPUGovList[i]))
        				((Spinner)sv.findViewById(R.id.cpu_detail_gov_value)).setSelection(i);
        		((TextView)sv.findViewById(R.id.cpu_processor_title)).setText(
        				Self.getResources().getString(R.string.misc_processor_num_title)+" "+position);
        		
        		MaxSeekBarValue.setText(Self.getResources().getString(R.string.misc_processor_freq_max_title)+" "+JNILibrary.GetProcessorScalMax(position));            
        		for(int i = 0; i < CPUFreqList.length;i++)
        			if(JNILibrary.GetProcessorScalMax(position) == Integer.parseInt(CPUFreqList[i]))
        				MaxSeekBar.setProgress(i);
        		
        		MinSeekBarValue.setText(Self.getResources().getString(R.string.misc_processor_freq_min_title)+" "+JNILibrary.GetProcessorScalMin(position));            
        		for(int i = 0; i < CPUFreqList.length;i++)
        			if(JNILibrary.GetProcessorScalMin(position) == Integer.parseInt(CPUFreqList[i]))
        				MinSeekBar.setProgress(i);
        		
        		if(JNILibrary.GetRooted() == 1)
        		{
        			((SeekBar)sv.findViewById(R.id.cpu_detail_max_value)).setEnabled(true);
        			((SeekBar)sv.findViewById(R.id.cpu_detail_min_value)).setEnabled(true);
        			((Spinner)sv.findViewById(R.id.cpu_detail_gov_value)).setClickable(true);
        		}
        		else
        		{
        			((SeekBar)sv.findViewById(R.id.cpu_detail_max_value)).setEnabled(false);
        			((SeekBar)sv.findViewById(R.id.cpu_detail_min_value)).setEnabled(false);
        			((Spinner)sv.findViewById(R.id.cpu_detail_gov_value)).setClickable(false);
        		}
     
                if(position % 2 == 0)
    	     		sv.setBackgroundColor(0x80444444);
    	     	else
    	     		sv.setBackgroundColor(0x80000000);          
                
        		((Spinner)sv.findViewById(R.id.cpu_detail_gov_value)).setOnItemSelectedListener(new OnItemSelectedListener() {
        			@Override
        			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        				String SetCPUCmd = "";
        				int CPUNum = Integer.parseInt((String) ((View)parentView.getParent()).getTag());

        				if(CPUGovList == null)
        		    		return;

        				
        				if(JNILibrary.GetRooted() == 1)
        				{
        					SetCPUCmd = "echo "+CPUGovList[position]+
        			        			" > /sys/devices/system/cpu/cpu"+CPUNum+"/cpufreq/scaling_governor"+"\n";
        					CommonUtil.execCommand(SetCPUCmd);
        				}
        			}

        			@Override
        			public void onNothingSelected(AdapterView<?> parentView) {
        			}

				});
        		
                MaxSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
                	@Override
        			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        				if(MaxSeekBar.getProgress() < MinSeekBar.getProgress()) {
        					MaxSeekBar.setProgress(MinSeekBar.getProgress());
        					progress = MaxSeekBar.getProgress();
        				}
        				MaxSeekBarValue.setText(Self.getResources().getString(R.string.misc_processor_freq_max_title)
        						   						+" "+CPUFreqList[progress]);
     				    String SetCPUCmd = "";
     				    int CPUNum = Integer.parseInt((String) ((View) MaxSeekBar.getParent()).getTag());

     				    if(CPUFreqList == null)
     				    	return;
       				
     				    if(JNILibrary.GetRooted() == 1)
     				    {
     				    	SetCPUCmd = "echo "+CPUFreqList[progress]+
     				    				" > /sys/devices/system/cpu/cpu"+CPUNum+"/cpufreq/scaling_max_freq\n";
     				    	CommonUtil.execCommand(SetCPUCmd);
     				    }
                	}

        			@Override
        			public void onStartTrackingTouch(SeekBar seekBar) {}

        			@Override
        			public void onStopTrackingTouch(SeekBar seekBar) {}
        		});

                MinSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

                	@Override
     			   	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                		if(MaxSeekBar.getProgress() < MinSeekBar.getProgress()) {
                			MinSeekBar.setProgress(MaxSeekBar.getProgress());
                			progress = MinSeekBar.getProgress();
                		}

                		MinSeekBarValue.setText(Self.getResources().getString(R.string.misc_processor_freq_min_title)
     						   						+" "+CPUFreqList[progress]);
     				    
     				    String SetCPUCmd = "";
     				    int CPUNum = Integer.parseInt((String) ((View) MinSeekBar.getParent()).getTag());

     				    if(CPUFreqList == null)
     				    	return;
       				
     				    if(JNILibrary.GetRooted() == 1)
     				    {
     				    	SetCPUCmd = "echo "+CPUFreqList[progress]+
     				    				" > /sys/devices/system/cpu/cpu"+CPUNum+"/cpufreq/scaling_min_freq\n";
     				    	CommonUtil.execCommand(SetCPUCmd);
     				    }
     			   }

     			   @Override
     			   public void onStartTrackingTouch(SeekBar seekBar) {}

     			   @Override
     			   public void onStopTrackingTouch(SeekBar seekBar) {}
                });

        		sv.setTag(""+position);                
            } else {
            	sv = convertView;
            }
            return sv;
        }

    }
  }
