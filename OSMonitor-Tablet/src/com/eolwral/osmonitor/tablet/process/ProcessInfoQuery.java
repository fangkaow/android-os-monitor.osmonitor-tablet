package com.eolwral.osmonitor.tablet.process;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.eolwral.osmonitor.tablet.R;
import com.eolwral.osmonitor.tablet.*;


public class ProcessInfoQuery extends Thread
{
	private static JNIInterface JNILibrary = JNIInterface.getInstance();
	
	private static ProcessInfoQuery singletone = null;
	private static PackageManager AppInfo = null;
	private static Resources  ResInfo = null;
	private static Drawable DefaultIcon = null; 
	private static Drawable SystemIcon = null;
	
	public static ProcessInfoQuery getInstance(Context context)
	{
		if(singletone == null)
		{
			singletone = new ProcessInfoQuery();
            AppInfo = context.getPackageManager();
            ResInfo = context.getResources();
            DefaultIcon = singletone.resizeImage(ResInfo.getDrawable(R.drawable.appdefault)); 
            SystemIcon = singletone.resizeImage(ResInfo.getDrawable(R.drawable.binary)); 
            singletone.start();
		}
		
		return singletone;
	}
	
	class ProcessInstance
	{
		public String Name;
		public Drawable Icon;
		public String Package;
	}
	
 	private final HashMap<String, ProcessInstance> ProcessCache = new HashMap<String, ProcessInstance>();
    
	public void doCacheInfo(int pid)
	{
		ProcessInstance CacheInstance = ProcessCache.get(JNILibrary.GetProcessName(pid));
		if(CacheInstance != null)
			return;
		
		try {
			QueryQueueLock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		QueryQueue.add(new WaitCache(JNILibrary.GetProcessName(pid),
				JNILibrary.GetProcessOwner(pid), JNILibrary.GetProcessUID(pid)));
		QueryQueueLock.release();
		
		CacheInstance = new ProcessInstance();
		CacheInstance.Name = JNILibrary.GetProcessName(pid);
		ProcessCache.put(JNILibrary.GetProcessName(pid),
					      CacheInstance);
		
		return;
	}

	private class WaitCache
	{
		private final String ItemName;
		private final String ItemOwner;
		private final int ItemUID;
		public WaitCache(String Name, String Owner, int UID)
		{
			ItemName = Name;
			ItemOwner = Owner;
			ItemUID = UID;
		}
		
		public String getName()
		{
			return ItemName;
		}

		public String getOwner()
		{
			return ItemOwner;
		}
		
		public int getUID()
		{
			return ItemUID;
		}
	}
    private static LinkedList<WaitCache> QueryQueue = new LinkedList<WaitCache>();
	private final Semaphore QueryQueueLock = new Semaphore(1, true);
    
	
	@Override 
	public void run()
	{
 
		while(true)
		{
			if(!getCacheInfo())
			{
				try {
					sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean getCacheInfo()
	{
		if(QueryQueue.isEmpty())
			return false;
		
		try {
			QueryQueueLock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		WaitCache SearchObj = QueryQueue.remove();

		QueryQueueLock.release();
		
		PackageInfo appPackageInfo = null;
		String PackageName = null;
/*		if(SearchObj.getName().contains(":"))
			PackageName = SearchObj.getName().substring(0,
								SearchObj.getName().indexOf(":"));
		else*/
		PackageName = SearchObj.getName();
		
		// for system user
		if(SearchObj.getOwner().contains("system") &&
						SearchObj.getName().contains("system") &&
						!SearchObj.getName().contains(".") )
			PackageName = "android";
		
		try {  
			appPackageInfo = AppInfo.getPackageInfo(PackageName, 0);
		} catch (NameNotFoundException e) {}
		
		if(appPackageInfo == null && SearchObj.getUID() >0)
		{
			String[] subPackageName = AppInfo.getPackagesForUid(SearchObj.getUID());
				
			if(subPackageName != null)
			{
				for(int PackagePtr = 0; PackagePtr < subPackageName.length; PackagePtr++)
				{
					if (subPackageName[PackagePtr] == null)
						continue;
					
					try {  
						appPackageInfo = AppInfo.getPackageInfo(subPackageName[PackagePtr], 0);
						PackagePtr = subPackageName.length;
					} catch (NameNotFoundException e) {}						
				}
			}
		}
		
		ProcessInstance CacheInstance = new ProcessInstance();
		
		CacheInstance.Package = PackageName;
	
		if(appPackageInfo != null)
		{  
			CacheInstance.Name = appPackageInfo.applicationInfo.loadLabel(AppInfo).toString();
			CacheInstance.Icon = resizeImage(appPackageInfo.applicationInfo.loadIcon(AppInfo));
		}
		else if(PackageName.equals("System"))
		{ 
			CacheInstance.Name = PackageName;
			CacheInstance.Icon = resizeImage(ResInfo.getDrawable(R.drawable.sysproc));
		}
		else  if(SearchObj.getOwner().contains("root"))
		{
			CacheInstance.Name = PackageName;
			CacheInstance.Icon = SystemIcon;
		}
		else
		{
			CacheInstance.Name = PackageName;
			CacheInstance.Icon = DefaultIcon;
		}
		
		ProcessCache.put(SearchObj.getName(), CacheInstance);
		
		return true;
	}
		
	public String getPackageName(int pid) 
	{
		ProcessInstance CacheInstance = ProcessCache.get(JNILibrary.GetProcessName(pid));
		if( CacheInstance != null)
			return CacheInstance.Name;
		return "Loading";

	}

	public String getPacakage(int pid)
	{
		ProcessInstance CacheInstance = ProcessCache.get(JNILibrary.GetProcessName(pid));
		if( CacheInstance != null)
			return CacheInstance.Package;
		return "Loading";

	}
	
	public int getProcessPID(int position)
	{
		return JNILibrary.GetProcessPID(position);
	}
	
	public String getProcessThreads(int pid)
	{
		return JNILibrary.GetProcessThreads(pid)+"";
	}

	public String getProcessLoad(int pid)
	{
		return JNILibrary.GetProcessLoad(pid)+"%";
	}

	public String getProcessMem(int pid)
	{
		if(JNILibrary.GetProcessRSS(pid) > 1024) 
			return (JNILibrary.GetProcessRSS(pid)/1024)+"M";
		return JNILibrary.GetProcessRSS(pid)+"K";
	}
	
	public Drawable getAppIcon(int pid) 
	{
		ProcessInstance CacheInstance = ProcessCache.get(JNILibrary.GetProcessName(pid));
		if( CacheInstance != null)
			return CacheInstance.Icon;
		return DefaultIcon;
	}
	
	public Drawable resizeImage(Drawable Icon) {

		Bitmap BitmapOrg = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888); 
		Canvas BitmapCanvas = new Canvas(BitmapOrg);
		Icon.setBounds(0, 0, 48, 48);
		Icon.draw(BitmapCanvas); 
        return new BitmapDrawable(BitmapOrg);
    }
	
}
