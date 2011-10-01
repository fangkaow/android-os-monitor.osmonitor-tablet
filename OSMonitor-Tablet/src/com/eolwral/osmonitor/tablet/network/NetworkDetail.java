package com.eolwral.osmonitor.tablet.network;

import java.text.DecimalFormat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.eolwral.osmonitor.tablet.R;
import com.eolwral.osmonitor.tablet.*;

public class NetworkDetail extends Activity
{
	private int targetInterface = 0;
	private JNIInterface JNILibrary = JNIInterface.getInstance();
	private NetworkDetail Self = null;
	private DecimalFormat SpeedFormat = new DecimalFormat(",000");

	private Runnable uiRunnable = new Runnable() {
		public void run() {
			doRefreshData();
			uiHandler.postDelayed(this, 1000);
		}
	};   
	
	private Handler uiHandler = new Handler();
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // Use a custom layout file
        setContentView(R.layout.networkdetail);
        Bundle Extras = getIntent().getExtras();
        targetInterface = Extras.getInt("targetInterface");
        Self = this;
         
        doRefreshData();
    }

    @Override
    public void onDestroy() 
    {
    	uiHandler.removeCallbacks(uiRunnable);
    	super.onDestroy();
    }

    @Override
    protected void onResume() 
    {    
    	uiHandler.post(uiRunnable);
    	super.onResume();
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

	private void doRefreshData() {
		
		
		if(JNILibrary.GetInterfaceFlags(targetInterface).contains("$up$"))
			((ImageView) findViewById(R.id.network_detail_image)).setImageDrawable(Self.getResources().getDrawable(R.drawable.network_online));
		else if(!JNILibrary.GetInterfaceAddr(targetInterface).equals("0.0.0.0"))
			((ImageView) findViewById(R.id.network_detail_image)).setImageDrawable(Self.getResources().getDrawable(R.drawable.network_idle));
		else
			((ImageView) findViewById(R.id.network_detail_image)).setImageDrawable(Self.getResources().getDrawable(R.drawable.network_offline));
		
		((TextView) findViewById(R.id.network_name_value)).setText(JNILibrary.GetInterfaceName(targetInterface));
		((TextView) findViewById(R.id.network_mac_value)).setText(JNILibrary.GetInterfaceMac(targetInterface));
		((TextView) findViewById(R.id.network_ip_value)).setText(JNILibrary.GetInterfaceAddr(targetInterface)+"\n"+JNILibrary.GetInterfaceNetMask(targetInterface));
		
		if(!JNILibrary.GetInterfaceNetMask6(targetInterface).equals("0"))
			((TextView) findViewById(R.id.network_ip6_value)).setText(JNILibrary.GetInterfaceAddr6(targetInterface)+"/"+JNILibrary.GetInterfaceNetMask6(targetInterface));
		
		long RxSize = JNILibrary.GetInterfaceInSize(targetInterface);
    	if(RxSize >= 1024*1024*1024)
    		((TextView) findViewById(R.id.network_rx_value)).setText((RxSize/(1024*1024*1024))+"G ("+SpeedFormat.format(RxSize).toString()+")");
    	else if(RxSize >= 1024*1024)  
    		((TextView) findViewById(R.id.network_rx_value)).setText((RxSize/(1024*1024))+"M ("+SpeedFormat.format(RxSize).toString()+")");
    	else if(RxSize >= 1024)
    		((TextView) findViewById(R.id.network_rx_value)).setText((RxSize/1024)+"K ("+SpeedFormat.format(RxSize).toString()+")");
    	else 
    		((TextView) findViewById(R.id.network_rx_value)).setText(""+RxSize);	
    	
    	long TxSize = JNILibrary.GetInterfaceOutSize(targetInterface);
    	if(TxSize >= 1024*1024*1024)
    		((TextView) findViewById(R.id.network_tx_value)).setText((TxSize/(1024*1024*1024))+"G ("+SpeedFormat.format(TxSize).toString()+")");
    	else if(TxSize >= 1024*1024)  
    		((TextView) findViewById(R.id.network_tx_value)).setText((TxSize/(1024*1024))+"M ("+SpeedFormat.format(TxSize).toString()+")");
    	else if(TxSize >= 1024)
    		((TextView) findViewById(R.id.network_tx_value)).setText((TxSize/1024)+"K ("+SpeedFormat.format(TxSize).toString()+")");
    	else 
    		((TextView) findViewById(R.id.network_tx_value)).setText(""+TxSize);
    	
    	String Flags = JNILibrary.GetInterfaceFlags(targetInterface);
    	
    	Flags = Flags.replace("$up$", Self.getResources().getText(R.string.network_status_up)+"\n");
    	Flags = Flags.replace("$down$", Self.getResources().getText(R.string.network_status_down)+"\n");
    	Flags = Flags.replace(" $broadcast$", Self.getResources().getText(R.string.network_status_broadcast)+"\n");
    	Flags = Flags.replace(" $loopback$", Self.getResources().getText(R.string.network_status_loopback)+"\n");
    	Flags = Flags.replace(" $point-to-point$", Self.getResources().getText(R.string.network_status_p2p)+"\n");
    	Flags = Flags.replace(" $running$", Self.getResources().getText(R.string.network_status_running)+"\n");
    	Flags = Flags.replace(" $multicast$", Self.getResources().getText(R.string.network_status_multicast));
    	
		((TextView) findViewById(R.id.network_status_value)).setText(Flags);
	}		
    
}
