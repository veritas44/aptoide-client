package cm.aptoide.pt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeoutException;


import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.ServiceState;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;


public class ScheduledDownload extends ListActivity {
	
	private DownloadQueueService downloadQueueService;
	Context ctx = this;
	private boolean serviceConnected = false;
	
	
	private ServiceConnection serviceConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	        downloadQueueService = ((DownloadQueueService.DownloadQueueBinder)serviceBinder).getService();
	        if(serviceConnected){
	        	downloadAll();
	        }
	        Log.d("Aptoide-BaseManagement", "DownloadQueueService bound to a ScheduledDOwnload");
	    }
	    
	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
	        downloadQueueService = null;
	        serviceConnected = false;
	        Log.d("Aptoide-BaseManagement","DownloadQueueService unbound from a Tab");
	    }

		

	};	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	private MyCustomAdapter mAdapter;
	private DbHandler db = new DbHandler(this);
	private Vector<ApkNode> sch_list;
	private DownloadNode downloadNode;
	Boolean noneChecked = true;
	
	
	
	 
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	

    	
        super.onCreate(savedInstanceState);
        
        
//        HWSpecifications specs = new HWSpecifications(this);
//        Toast.makeText(this, 
//        		new String(new Integer(specs.getScreenSize()).toString()), Toast.LENGTH_LONG).show();
//        Toast.makeText(this, 
//        		specs.getEsglVer(), Toast.LENGTH_LONG).show();
        
        
        
//        registerForContextMenu(this.getListView());
        getApplicationContext().bindService(new Intent(this, DownloadQueueService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        
        
        setContentView(R.layout.sch_downloadempty);
        mAdapter = new MyCustomAdapter();
        
        sch_list=db.getScheduledListNames();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Map<String, Object> server_line;
        for(int i =0;i!=sch_list.size();i++){
//        	mAdapter.addName(sch_list.get(i).name);
//        	mAdapter.addIconpath(getString(R.string.icons_path)+sch_list.get(i).apkid);
//        	mAdapter.addVersion(sch_list.get(i).ver);
        	server_line = new HashMap<String, Object>();
        	server_line.put("name", sch_list.get(i).name);
        	server_line.put("iconpath", getString(R.string.icons_path)+sch_list.get(i).apkid);
        	server_line.put("version", "Version: "+sch_list.get(i).ver);
        	result.add(server_line);
        	
        }
        SimpleAdapter show_out = new SimpleAdapter(this, result, R.layout.sch_download, 
        		new String[] {"name", "iconpath", "version"}, new int[] {R.id.name, R.id.appicon, R.id.isinst});
        
        setListAdapter(show_out);
        if(sch_list.isEmpty()){
			Toast.makeText(this, R.string.no_sch_downloads, Toast.LENGTH_LONG).show();
			setContentView(R.layout.sch_downloadempty);
			
        }
        Intent intent = getIntent();
    	if(intent.hasExtra("downloadAll")){
//    		AlertDialog alrt = new AlertDialog.Builder(this).create();
//    		alrt.setMessage(getText(R.string.schDown_wireless_detected));
//			alrt.setButton(getText(R.string.btn_yes), new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int which) {
//					downloadAll();
//					return;
//				} }); 
//			alrt.setButton2(getText(R.string.btn_no), new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int which) {
//					finish();
//					return;
//				}});
//			alrt.show();
    		
    		serviceConnected=true;
    		finish();
    	}
        
        
        
    }
    
    

    

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		
		menu.add(Menu.NONE,2, 2, R.string.schDown_downselected).setIcon(R.drawable.ic_menu_save);
		menu.add(Menu.NONE,3, 3, R.string.schDown_invertselection).setIcon(R.drawable.ic_menu_invert);
		menu.add(Menu.NONE,4, 3, R.string.schDown_removeselected).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		
		
		
		
		return super.onCreateOptionsMenu(menu);
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		AlertDialog alrt = new AlertDialog.Builder(this).create();
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case 4:
			if(isAllChecked()){
				alrt.setMessage(getText(R.string.schDown_sureremove));
				alrt.setButton(getText(R.string.btn_yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						for(int i=0; i < getListAdapter().getCount(); i++){
							LinearLayout itemLayout = (LinearLayout)getListView().getChildAt(i);
							CheckBox cb = (CheckBox)itemLayout.findViewById(R.id.schDwnChkBox);
							if(cb.isChecked()){
								db.deleteScheduledDownload(sch_list.get(i).apkid,sch_list.get(i).ver);
							}
						}
						redraw();
						return;
					} }); 
				alrt.setButton2(getText(R.string.btn_no), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						return;
					}});
				alrt.show();
			} else {
				Toast.makeText(this, R.string.schDown_nodownloadselect,
						Toast.LENGTH_LONG).show();
			}

			break;

		case 2:
			
//			String programs = "";
//			boolean existsDownloads=false;
			if(isAllChecked()){
				for(int i=0; i < this.getListAdapter().getCount(); i++){
					LinearLayout itemLayout = (LinearLayout)getListView().getChildAt(i);
					CheckBox cb = (CheckBox)itemLayout.findViewById(R.id.schDwnChkBox);

					if (cb.isChecked()){
						DownloadNode downloadnode = doDownloadNode(i);
						if(downloadnode!=null){
							downloadQueueService.startDownload(downloadnode);
						}
						else{
							Toast.makeText(ctx, getString(R.string.schDown_downerror, new Object[]{db.getScheduledDwnServer(sch_list.get(i).apkid)}), Toast.LENGTH_LONG).show();

						}
					}
				}

				//					programs=programs.concat("\n"+getListAdapter().getItem(i).toString());
				//					schDownToDelete.add(sch_list.get(i).apkid);
				//					existsDownloads = true;
				//				}


				//			}
				//			if(existsDownloads){
				//				alrt.setMessage("Delete Scheduled Downloads? "+programs);
				//				alrt.setButton(getText(R.string.btn_yes), new DialogInterface.OnClickListener() {
				//					public void onClick(DialogInterface dialog, int which) {
				//						for (String apkid : schDownToDelete){
				//							db.deleteScheduledDownload(apkid);
				//						}
				//						redraw();
				//						return;
				//					} }); 
				//				alrt.setButton2(getText(R.string.btn_no), new DialogInterface.OnClickListener() {
				//					public void onClick(DialogInterface dialog, int which) {
				//						return;
				//					}});
				//				alrt.show();


			} else {
				Toast.makeText(this, R.string.schDown_nodownloadselect,
						Toast.LENGTH_LONG).show();
			}
				
			break;
		case 3:
			for(int i=0; i < this.getListAdapter().getCount(); i++){
				LinearLayout itemLayout = (LinearLayout)getListView().getChildAt(i);
				CheckBox cb = (CheckBox)itemLayout.findViewById(R.id.schDwnChkBox);
					cb.toggle();
				}
			break;
		default:
			break;
		}
		
		return super.onMenuItemSelected(featureId, item);
	}


	private boolean isAllChecked(){
		if(this.getListAdapter().isEmpty()){
			return false;
		}
		for(int i=0; i < this.getListAdapter().getCount(); i++){
			LinearLayout itemLayout = (LinearLayout)getListView().getChildAt(i);
			CheckBox cb = (CheckBox)itemLayout.findViewById(R.id.schDwnChkBox);
				if(cb.isChecked())
					return true;
			}
		return false;
	}
	
	private void downloadAll() {
		// TODO Auto-generated method stub
		for (int i =0; i!=sch_list.size();i++){
			DownloadNode downloadnode = doDownloadNode(i);
			if(downloadnode!=null){
				downloadQueueService.startDownload(downloadnode);
			}
			else{
				Toast.makeText(getApplicationContext(), getString(R.string.schDown_downerror, new Object[]{db.getScheduledDwnServer(sch_list.get(i).apkid)}), Toast.LENGTH_LONG).show();

			}
		}
		
	}

	


	
	


	/* (non-Javadoc)
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterContextMenuInfo itemInfo = (AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId()) {
		case 4:
			db.deleteScheduledDownload(sch_list.get(itemInfo.position).apkid,sch_list.get(itemInfo.position).ver);
			sch_list.remove(itemInfo.position);
			redraw();
			break;

		default:
			break;
		}
		return super.onContextItemSelected(item);
		
		
	}


	private void redraw() {
		// TODO Auto-generated method stub
		sch_list=db.getScheduledListNames();
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Map<String, Object> server_line;
        for(int i =0;i!=sch_list.size();i++){
//        	mAdapter.addName(sch_list.get(i).name);
//        	mAdapter.addIconpath(getString(R.string.icons_path)+sch_list.get(i).apkid);
//        	mAdapter.addVersion(sch_list.get(i).ver);
        	server_line = new HashMap<String, Object>();
        	server_line.put("name", sch_list.get(i).name);
        	server_line.put("iconpath", getString(R.string.icons_path)+sch_list.get(i).apkid);
        	server_line.put("version", sch_list.get(i).ver);
        	result.add(server_line);
        	
        }
        SimpleAdapter show_out = new SimpleAdapter(this, result, R.layout.sch_download, 
        		new String[] {"name", "iconpath", "version"}, new int[] {R.id.name, R.id.appicon, R.id.isinst});
        
        setListAdapter(show_out);
		
		
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		downloadQueueService.startDownload(doDownloadNode(position));
		super.onListItemClick(l, v, position, id);
	}


	private DownloadNode doDownloadNode(int position) {
		Vector<DownloadNode> tmp_serv = new Vector<DownloadNode>();	
		try{
			String packageName=sch_list.get(position).apkid;
			String ver = sch_list.get(position).ver;
//			Vector<ApkNode> apk_lst = db.getAll("abc");
			Vector<ApkNode> apk_lst = BaseManagement.apk_lst;
			String LOCAL_APK_PATH = Environment.getExternalStorageDirectory().getPath()+"/.aptoide/";
			tmp_serv = db.getPathHash(packageName, ver);
			
			String localPath = new String(LOCAL_APK_PATH+packageName+".apk");
			String appName = packageName;
			for(ApkNode node: apk_lst){
				if(node.apkid.equals(packageName)){
					appName = node.name;
					break;
				}
			}
			downloadNode = tmp_serv.firstElement();
			downloadNode.setPackageName(packageName);
			downloadNode.setAppName(appName);
			downloadNode.setLocalPath(localPath);
			downloadNode.setUpdate(false);
			String remotePath = downloadNode.getRemotePath();
			if(remotePath.length() == 0)
				throw new TimeoutException();
			String[] logins = null; 
			logins = db.getLogin(downloadNode.getRepo());
			downloadNode.setLogins(logins);
		} catch(Exception e){	
			e.printStackTrace();
		}
		return downloadNode;
	}


	private class MyCustomAdapter extends BaseAdapter {
 
        private ArrayList<String> name = new ArrayList<String>();
        private ArrayList<String> iconpath = new ArrayList<String>();
        private ArrayList<String> version = new ArrayList<String>();
        
        private LayoutInflater mInflater;
 
        public MyCustomAdapter() {
            mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
 
        public void addName(final String item) {
            name.add(item);
            notifyDataSetChanged();
        }
        
        public void addIconpath(final String item) {
            iconpath.add(item);
            notifyDataSetChanged();
        }
        
        public void addVersion(final String item) {
            version.add("Version: "+item);
            notifyDataSetChanged();
        }
 
        public int getCount() {
            return name.size();
        }
 
        public String getItem(int position) {
            return name.get(position);
        }
 
        public long getItemId(int position) {
            return position;
        }
 
        public View getView(int position, View convertView, ViewGroup parent) {
            System.out.println("getView " + position + " " + convertView);
            ViewHolder holder = null;
          
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.sch_download, null);
                holder = new ViewHolder();
                holder.name = (TextView)convertView.findViewById(R.id.name);
                holder.iconpath = (ImageView)convertView.findViewById(R.id.appicon);
                holder.version=(TextView)convertView.findViewById(R.id.isinst);
                
                
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.version.setText(version.get(position));
            
            
            new Uri.Builder().build();
            holder.name.setText(name.get(position));
            holder.iconpath.setImageURI(Uri.parse(iconpath.get(position)));
            return convertView;
        }
 
    }
 
    public static class ViewHolder {
        public TextView name;
        public ImageView iconpath;
        public TextView version;
    }

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("ScheduledResult",requestCode + " "+ resultCode);

	}

    
	

}


