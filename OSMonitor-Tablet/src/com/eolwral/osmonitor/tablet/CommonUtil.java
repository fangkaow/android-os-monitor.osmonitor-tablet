package com.eolwral.osmonitor.tablet;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Random;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

public class CommonUtil
{

	public final static String NiceCMD = " /data/data/com.eolwral.osmonitor.tablet/nice";
	public static Random RandomGen = new Random();
	
	public static boolean checkExtraStore(Activity activity)
	{  
		boolean flag = false;
    	if(Integer.parseInt(Build.VERSION.SDK) >= 8)
    	{
    		// use Reflection to avoid errors (for cupcake 1.5)
    		Method MethodList[] = activity.getClass().getMethods();
    		for(int checkMethod = 0; checkMethod < MethodList.length; checkMethod++)
    		{
    			if(MethodList[checkMethod].getName().indexOf("ApplicationInfo") != -1)
    			{ 
    				try{
    					if((((ApplicationInfo) MethodList[checkMethod].invoke(activity , new Object[]{})).flags & 0x40000 /* ApplicationInfo.FLAG_EXTERNAL_STORAGE*/ ) != 0 )
    						flag = true;
    				}
    				catch(Exception e) {}
    			}
    		}
    	}
    	return flag;
	}
	
	public static int getSDKVersion()
	{
		return Integer.parseInt(Build.VERSION.SDK);
	}
	
	public static void execCommand(String command) {
		try {
			Process shProc = Runtime.getRuntime().exec("su");
			DataOutputStream InputCmd = new DataOutputStream(shProc.getOutputStream());

			InputCmd.writeBytes(command);

			// Close the terminal
			InputCmd.writeBytes("exit\n");
			InputCmd.flush();
			InputCmd.close();
	    	
			try {
				shProc.waitFor();
			} catch (InterruptedException e) { };
		} catch (IOException e)	{}
	}    

	private static Handler EndHelper = new Handler()
	{
		public void handleMessage(Message msg)
		{
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}; 
    
	public static void killSelf(Context target)
	{
		if(CommonUtil.getSDKVersion() <= 7)
		{
			((ActivityManager) target.getSystemService(Context.ACTIVITY_SERVICE))
	           									.restartPackage("com.eolwral.osmonitor");
	    }
		else
		{ 
			EndHelper.sendEmptyMessageDelayed(0, 500);
		}
	}
	 
	public static void CheckNice(AssetManager Asset)
	{
		try {
			InputStream bNiceIn = Asset.open("nice");
			OutputStream bNiceOut = new FileOutputStream(NiceCMD);
			

			// Transfer bytes from in to out  
            byte[] bTransfer = new byte[1024];   
            int bTransferLen = 0;   
            while ((bTransferLen = bNiceIn.read(bTransfer)) > 0)   
            {   
            	bNiceOut.write(bTransfer, 0, bTransferLen);   
            }   

            bNiceIn.close();   
            bNiceOut.close();    
            
            CommonUtil.execCommand("chmod 755 "+NiceCMD+"\n");
			
		} catch (IOException e) { }
	}
	
	// return value, 0 == none, 1 == support, 2 == rooted
	public static int IsSupportKill() 
	{
		JNIInterface JNILibrary = JNIInterface.getInstance();
		if( JNILibrary.GetRooted() == 1)
			return 2;
		
		if (CommonUtil.getSDKVersion() < 8)
			return 1;
		
		return 0;
	}	
}