package com.eolwral.osmonitor.tablet.log;

import java.io.File;
import java.io.FileWriter;

import com.eolwral.osmonitor.tablet.R;
import com.eolwral.osmonitor.tablet.*;

import android.app.AlertDialog;
import android.content.Context; 
import android.content.DialogInterface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ExportPreference extends Preference {
	private JNIInterface JNILibrary = JNIInterface.getInstance();
	private String Mode = "dmesg";

    public ExportPreference(Context context) {
     super(context);
    }
    
    public ExportPreference(Context context, AttributeSet attrs) {
     super(context, attrs);
    }
    
    public ExportPreference(Context context, AttributeSet attrs, int defStyle) {
     super(context, attrs, defStyle);
     Mode = attrs.getAttributeValue("", "logType");
    }
      
    @Override
    protected View onCreateView(ViewGroup parent){
     
      LinearLayout PreferenceLayout = new LinearLayout(getContext());
      
      LinearLayout.LayoutParams ExportLayout = new LinearLayout.LayoutParams(
                                          LinearLayout.LayoutParams.FILL_PARENT,
                                          LinearLayout.LayoutParams.FILL_PARENT);
      ExportLayout.gravity = Gravity.CENTER;
      ExportLayout.weight  = 1.0f;
	   	
      PreferenceLayout.setPadding(15, 5, 10, 5);
      PreferenceLayout.setOrientation(LinearLayout.HORIZONTAL);
      
      Button ExportButton = new Button(getContext());
      ExportButton.setText(getTitle());
      ExportButton.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium_Inverse);
      ExportButton.setOnClickListener( new OnClickListener() {
		@Override
		public void onClick(View v) {
			FileNameDialog();
		}
      });
      ExportButton.setLayoutParams(ExportLayout);
      
      PreferenceLayout.addView(ExportButton);
      PreferenceLayout.setId(android.R.id.widget_frame);
      
      return PreferenceLayout; 
    }
    
    private void SaveLog(String FileName, Boolean useHTML)
    {
    	if(FileName.trim().equals(""))
    		return;
    	
    	try {
        	File LogFile = new File("/sdcard/" + FileName);
    		
        	if (LogFile.exists())
        	{
        		new AlertDialog.Builder(getContext())
   		   		.setTitle(R.string.app_name)
    		   		.setMessage(R.string.log_exportexist_title)
    		   		.setPositiveButton(R.string.common_button_ok,
    		   				new DialogInterface.OnClickListener() {
    		   			public void onClick(DialogInterface dialog, int whichButton) { } })
    		   		.create()
    		   		.show();
        		return;
        	}

        	LogFile.createNewFile();
        	
        	int LogCount = 0;
        	if(Mode.equals("dmesg"))
        		LogCount = JNILibrary.GetDebugMessageCounts();
        	else
        		LogCount = JNILibrary.GetLogcatCounts();
        	
        	FileWriter TextFile = new FileWriter(LogFile);
        	
        	if(useHTML)
        		TextFile.write("<html>\n<body>\n<table>\n");
	        	
        	for(int i = 0; i < LogCount;i++)
        	{
        		String TextLine = "";
        		if(Mode.equals("dmesg"))
        		{
        			if(useHTML)
       					TextLine = "<tr><td>"+JNILibrary.GetDebugMessageTime(i)+ "</td><td> ["
   								+ JNILibrary.GetDebugMessageLevel(i) + "] </td><td>"
   								+ JNILibrary.GetDebugMessage(i) + "</td></tr>\n";
       				else
       					TextLine = JNILibrary.GetDebugMessageTime(i)+ " ["
       						+ JNILibrary.GetDebugMessageLevel(i) + "] "
       						+ JNILibrary.GetDebugMessage(i) + "\n";
        		}
        		else
        		{
        			if(useHTML)
        				TextLine = "<tr><td>"+JNILibrary.GetLogcatTime(i) + "</td><td> ["
        						+ JNILibrary.GetLogcatLevel(i)+ "] </td><td>"
        						+ JNILibrary.GetLogcatTag(i) + "("
								+ JNILibrary.GetLogcatPID(i) + ") </td><td>"
								+ JNILibrary.GetLogcatMessage(i) + "</td></tr>\n";
        			else
        				TextLine = JNILibrary.GetLogcatTime(i) + " ["
        						+ JNILibrary.GetLogcatLevel(i)+ "] "
        						+ JNILibrary.GetLogcatTag(i) + "("
        						+ JNILibrary.GetLogcatPID(i) + ") "
        						+ JNILibrary.GetLogcatMessage(i) + "\n";
        		}
        		TextFile.write(TextLine);
        	}

        	if(useHTML)
        		TextFile.write("</table>\n</body>\n</html>\n");

        	TextFile.close();

    	} catch (Exception e) {
	    	new AlertDialog.Builder(getContext())
		  		.setTitle(R.string.app_name)
		  		.setMessage(e.getMessage())
		  		.setPositiveButton(R.string.common_button_ok,
		  				new DialogInterface.OnClickListener() {
		  			public void onClick(DialogInterface dialog, int whichButton) { } })
		  		.create()
		  		.show();

	    	return;
	    }
	    	
	  	new AlertDialog.Builder(getContext())
	   		.setTitle(R.string.app_name)
	   		.setMessage(R.string.log_exportdone_title)
	   		.setPositiveButton(R.string.common_button_ok,
	   			new DialogInterface.OnClickListener() {
	   		public void onClick(DialogInterface dialog, int whichButton) { } })
	   	.create()
	   	.show();

	  	return;
	}

	private void FileNameDialog() {
		LinearLayout Layout = new LinearLayout(getContext());
		Layout.setOrientation(LinearLayout.VERTICAL);
		Layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
									            LayoutParams.FILL_PARENT));
		
		EditText FileName = new EditText(getContext());
		FileName.setId(100);

		CheckBox UseHTML = new CheckBox(getContext());
		UseHTML.setId(200);
		UseHTML.setText(R.string.log_format_title);
		
		Layout.addView(FileName, 0);
		Layout.addView(UseHTML, 1);
		
		AlertDialog Prompt = new AlertDialog.Builder(getContext())
        .setTitle(R.string.log_exportfile_title)
        .setView(Layout)
        .setPositiveButton(R.string.common_button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	String FileName = ((EditText)((AlertDialog)dialog).findViewById(100)).getText().toString();
            	Boolean useHTML = ((CheckBox)((AlertDialog)dialog).findViewById(200)).isChecked();
            	SaveLog(FileName, useHTML);
            }
        })
        .setNegativeButton(R.string.common_button_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                /* User clicked cancel so do some stuff */
            }
        })
        .create();
		
		Prompt.show();
		
		return;		
	}
	    
 
}
