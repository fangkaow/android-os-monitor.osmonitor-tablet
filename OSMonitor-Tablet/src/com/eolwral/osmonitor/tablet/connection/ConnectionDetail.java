package com.eolwral.osmonitor.tablet.connection;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.LinearLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eolwral.osmonitor.tablet.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class ConnectionDetail extends MapActivity
{
	private String targetIP = "";
	private ProgressDialog ProcDialog = null;
    private EventHandler ProcHandler = null;
    private QueryWhois ProcThread = null;
    private ConnectionInfoQuery NetworkInfo = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // Use a custom layout file
        setContentView(R.layout.connectiondetail);
        Bundle Extras = getIntent().getExtras();
        targetIP = Extras.getString("targetIP");
        
        // Release Key
		// "0N4HYg91PN1-cGgp3exBmvC1AdzeiGYzp7C3V7g";
		
		// Debug Key
		// "0N4HYg91PN19P2R67mtD2NGBl3ce5DxqXlmH6TA";

        LinearLayout MapSize = (LinearLayout) findViewById(R.id.connection_mapwindow);
       	MapSize.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, 300));
        	
       	NetworkInfo = ConnectionInfoQuery.getInstance();
    	ProcHandler = new EventHandler();
    	
    	// predefine utrace ip
    	if(CacheWhois.get("88.198.156.18") == null)
    	{
    		CacheQuery WhoisQuery = new CacheQuery();
    		WhoisQuery.Longtiude = "10";
    		WhoisQuery.Latitude = "53.5499992371";
    		WhoisQuery.IP = "88.198.156.18";
    		WhoisQuery.Country = "DE";
    		WhoisQuery.Region = "Hamburg";
    		WhoisQuery.ISP = "Hetzner Online AG";
    		WhoisQuery.Organization = "Pagedesign GmbH";
    		CacheWhois.put("88.198.156.18", WhoisQuery);
    	}

    	if(CacheWhois.get("0.0.0.0") == null)
    	{
    		CacheQuery WhoisQuery = new CacheQuery();
    		WhoisQuery.Longtiude = "0";
    		WhoisQuery.Latitude = "0";
    		WhoisQuery.IP = "0.0.0.0";
    		WhoisQuery.Country = "";
    		WhoisQuery.Region = "";
    		WhoisQuery.ISP = "";
    		WhoisQuery.Organization = "";
    		CacheWhois.put("0.0.0.0", WhoisQuery);
    	}
         
        doRefreshData();
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
		// show progress dialog
		ProcDialog = ProgressDialog.show(this, getResources().getText(R.string.connection_whois_title),
					getResources().getText(R.string.process_loading), true);
		
		ProcDialog.setOnCancelListener(new DialogInterface.OnCancelListener() 
		{
			public void onCancel(DialogInterface dialog) 
			{
				ProcThread.ForceStop = true;
				ProcThread.stop();
			}
		});
		ProcDialog.setCancelable(true);

		ProcThread = new QueryWhois();
		ProcThread.QueryIP = targetIP;

		ProcThread.start();
	}
	
	public void showWhois()
	{
		if(CacheWhois.get(targetIP) == null)
			return;
		
		CacheQuery WhoisQuery = CacheWhois.get(targetIP);
		
		try {
			MapView GeoIPMap = (MapView) findViewById(R.id.connection_mapview);
		    GeoPoint MapPoint = new GeoPoint((int) (Double.parseDouble(WhoisQuery.Latitude) * 1E6), (int) (Double.parseDouble(WhoisQuery.Longtiude) * 1E6));
			MapController GeoIPControl = GeoIPMap.getController();
			GeoIPControl.setZoom(8);
		    GeoIPControl.animateTo(MapPoint);
		    GeoIPControl.setCenter(MapPoint);
		    GeoIPMap.getOverlays().add(new MapOverlay(this, MapPoint, R.drawable.connection_point));
		} catch(NumberFormatException e) {} 
		
		try
        {
			if(targetIP.equals("88.198.156.18"))
				((TextView) findViewById(R.id.connection_dns_value)).setText("utrace.de");
			else if(targetIP.equals("88.198.156.18"))
				((TextView) findViewById(R.id.connection_dns_value)).setText("");
			else
				((TextView) findViewById(R.id.connection_dns_value)).setText(NetworkInfo.GetDNS(targetIP));
        } catch (Exception e) {}

		((TextView) findViewById(R.id.connection_ip_value)).setText(targetIP);
		((TextView) findViewById(R.id.connection_country_value)).setText(WhoisQuery.Country);
		((TextView) findViewById(R.id.connection_region_value)).setText(WhoisQuery.Region);
		((TextView) findViewById(R.id.connection_isp_value)).setText(WhoisQuery.ISP);
		((TextView) findViewById(R.id.connection_org_value)).setText(WhoisQuery.Organization);
		((TextView) findViewById(R.id.connection_lat_value)).setText(""+WhoisQuery.Latitude);
		((TextView) findViewById(R.id.connection_long_value)).setText(""+WhoisQuery.Longtiude);
	}
	
	Handler handler = new Handler();
	
    public class EventHandler extends Handler 
    {
        public void handleMessage(Message msg) 
        {
        	showWhois();
        	if(ProcDialog != null)
        	{
            	ProcDialog.dismiss();
            	ProcDialog = null;
        	}
        }
    }
    
    class CacheQuery 
    {
        public String IP = "NA";
        public String Country = "NA";
        public String Region = "NA";
        public String ISP = "NA";
        public String Organization = "NA";
    	public String Longtiude;
        public String Latitude; 
    }
    
    private final static HashMap<String, CacheQuery> CacheWhois = new HashMap<String, CacheQuery>();
	class QueryWhois extends Thread 
	{
		public String QueryIP = ""; 
		public Boolean ForceStop = false;
		@Override
        public void run() 
		{
			if(CacheWhois.get(QueryIP) != null)
			{
				if(!ForceStop)
					ProcHandler.sendEmptyMessage(0);
				return;
			}
			
			StringBuilder whoisInfo = new StringBuilder();
			try {
				/* Create a URL we want to load some xml-data from. */
	            URL url = new URL("http://xml.utrace.de/?query="+QueryIP);
 
	            /* Get a SAXParser from the SAXPArserFactory. */
	            SAXParserFactory spf = SAXParserFactory.newInstance();
	            SAXParser sp = spf.newSAXParser();

	            /* Get the XMLReader of the SAXParser we created. */
	            XMLReader xr = sp.getXMLReader();
	            
	            /* Create a new ContentHandler and apply it to the XML-Reader*/
	            WhoisSAX SAXHandler = new WhoisSAX();
	            xr.setContentHandler(SAXHandler);
	               
	            InputStream urlData = url.openStream();
	            /* Parse the xml-data from our URL. */
	            xr.parse(new InputSource(urlData));
	            /* Parsing has finished. */ 
	            urlData.close();

	            /* Our ExampleHandler now provides the parsed data to us. */
	            WhoisSAXDataSet parsedDataSet = SAXHandler.getParsedData();

	            /* Set the result to be displayed in our GUI. */
	            whoisInfo.append(parsedDataSet.toString());

		        CacheQuery WhoisQuery = new CacheQuery();
				WhoisQuery.Longtiude = parsedDataSet.getMapLongtiude();
				WhoisQuery.Latitude = parsedDataSet.getMapLatitude();
				WhoisQuery.IP = parsedDataSet.getip();
				WhoisQuery.Country = parsedDataSet.getcountry();
				WhoisQuery.Region = parsedDataSet.getregion();
				WhoisQuery.ISP = parsedDataSet.getisp();
				WhoisQuery.Organization = parsedDataSet.getorg();
		        CacheWhois.put(QueryIP, WhoisQuery);
	        } 
			catch (Exception e) 
	        {
	        }  
			
			if(!ForceStop)
				ProcHandler.sendEmptyMessage(0);

			return;
		}
    }
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
    
}
