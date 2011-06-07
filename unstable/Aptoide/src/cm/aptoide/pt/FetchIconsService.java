package cm.aptoide.pt;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class FetchIconsService extends Service{
	

	private Vector<ServiceIcon> parsedList = null;
	private Thread workingPool = null;
	
	private Context mctx = null;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		mctx = this;
		
		parsedList = new Vector<ServiceIcon>();
		workingPool = new Thread(new WorkThread(), "T1");
		
		Log.d("Aptoide","......................... onCreate FetchIcons Service");
	}
	

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Bundle intent_info = intent.getExtras();
		ServiceIcon tmp = new ServiceIcon(intent_info.getString("srv"), intent_info.getStringArray("login"), (ArrayList<IconNode>)intent_info.getSerializable("icons"));
		parsedList.add(tmp);
		if(!workingPool.isAlive())
			workingPool.start();
		Log.d("Aptoide","......................... onStart FetchIcons Service");
	}

	
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("Aptoide","......................... onDestroy FetchIcons Service");
	}

	
	private class WorkThread implements Runnable {
		public WorkThread() {	}
		
		public void run() {
			try{
				int i = 5;
				while(i > 0){
					Log.d("Aptoide","....... try: " + i);
					if(parsedList.size() > 0){
						i = 5;
						new Thread(new FetchIcons(parsedList.remove(0))).start();
					}else{
						i--;
						Thread.sleep(5000);
					}
				}
			}catch (Exception e){ 	}
		}
	}
	
	private class FetchIcons implements Runnable {
		private String server;
		private String user;
		private String pswd;
		private List<IconNode> lst;
		
		public FetchIcons(ServiceIcon node) {	
			server = node.mserver;
			user = node.usern;
			pswd = node.passwd;
			lst = node.iconsLst;
		}
		
		public void run() {
			try{
				for(IconNode node : lst){
					String test_file = mctx.getString(R.string.icons_path) + node.name;
					
					File exists = new File(test_file);
					if(!exists.exists())
						getIcon(node.url, node.name, server, user, pswd);
				}
			}catch (Exception e){ 
				Log.d("Aptoide", "Wash exception? " + e.toString());
			}
		}
	}
	
	private void getIcon(String uri, String name, String mserver, String usern, String passwd){
		String url = mserver + "/" + uri;
		String file = mctx.getString(R.string.icons_path) + name;
		
		
		try {
			FileOutputStream saveit = new FileOutputStream(file);
			
			HttpResponse mHttpResponse = NetworkApis.getHttpResponse(url, usern, passwd, mctx);
			
			if(mHttpResponse.getStatusLine().getStatusCode() == 401){
				return;
			}else if(mHttpResponse.getStatusLine().getStatusCode() == 403){
				return;
			}else{
				byte[] buffer = EntityUtils.toByteArray(mHttpResponse.getEntity());
				saveit.write(buffer);
			}
			
			Log.d("Aptoide","getIcon done: " + uri + "/" + name + " - " + mserver);

			
		}catch (Exception e){
			Log.d("Aptoide","Error fetching icon.");
		}
	}
	
	private class ServiceIcon extends Object{
		
		public String mserver;
		public String usern;
		public String passwd;
		public List<IconNode> iconsLst;
		
		public ServiceIcon(String srv, String[] login, ArrayList<IconNode> lst){
			this.mserver = srv;
			this.usern = login[0];
			this.passwd = login[1];
			this.iconsLst = lst;
		}
	}
	
	
}
