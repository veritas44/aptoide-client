/**
 * StaticInstalledAppsListAdapter,		part of Aptoide's data model
 * Copyright (C) 2011  Duarte Silveira
 * duarte.silveira@caixamagica.pt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package cm.aptoide.pt.ifaceutil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cm.aptoide.pt.EnumAptoideInterfaceTasks;
import cm.aptoide.pt.R;
import cm.aptoide.pt.data.AIDLAptoideServiceData;
import cm.aptoide.pt.data.display.ViewDisplayApplication;
import cm.aptoide.pt.data.display.ViewDisplayApplicationInstalled;
import cm.aptoide.pt.data.display.ViewDisplayListApps;

 /**
 * StaticInstalledAppsListAdapter, models a static loading, installed apps list adapter
 * 									extends baseAdapter
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class StaticInstalledAppsListAdapter extends BaseAdapter{

	private ListView listView;
	private LayoutInflater layoutInflater;

	private ViewDisplayListApps apps = null;
	private ViewDisplayListApps freshApps = null;
	
	private InstalledAppsManager appsManager;
	
	private AIDLAptoideServiceData serviceDataCaller = null;
	
	private Handler aptoideTasksHandler;

	
	private Handler interfaceTasksHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	EnumAptoideInterfaceTasks task = EnumAptoideInterfaceTasks.reverseOrdinal(msg.what);
        	switch (task) {
				
				case RESET_INSTALLED_LIST_DISPLAY:
					resetDisplay();
					break;
	
				default:
					break;
			}
        }
    };
    
    

    private class InstalledAppsManager{
    	private ExecutorService installedColectorsPool;
    	
    	public InstalledAppsManager(){
    		installedColectorsPool = Executors.newSingleThreadExecutor();
    	}
    	
    	public void reset(){
        	try {
				installedColectorsPool.execute(new GetInstalledApps());
			} catch (Exception e) { }
        }
    	
    	private class GetInstalledApps implements Runnable{

			@Override
			public void run() {
				aptoideTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_INSTALLED_TO_PROGRESSBAR.ordinal());
				try {
					setFreshInstalledApps(serviceDataCaller.callGetInstalledApps());
					interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.RESET_INSTALLED_LIST_DISPLAY.ordinal());
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
    		
    	}
    }
	
	
	
	public static class InstalledRowViewHolder{
		ImageView app_icon;
		
		TextView app_name;
		TextView installed_versionname;
		
		ImageView app_downgradable;
		ImageView app_upgradable;
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		InstalledRowViewHolder rowViewHolder;
		
		if(convertView == null){
			convertView = layoutInflater.inflate(R.layout.row_app_installed, null);
			
			rowViewHolder = new InstalledRowViewHolder();
			rowViewHolder.app_icon = (ImageView) convertView.findViewById(R.id.app_icon);
			rowViewHolder.app_name = (TextView) convertView.findViewById(R.id.app_name);
			rowViewHolder.installed_versionname = (TextView) convertView.findViewById(R.id.installed_versionname);
			rowViewHolder.app_downgradable = (ImageView) convertView.findViewById(R.id.app_downgradable);
			rowViewHolder.app_upgradable = (ImageView) convertView.findViewById(R.id.app_upgradable);
			
			convertView.setTag(rowViewHolder);
		}else{
			rowViewHolder = (InstalledRowViewHolder) convertView.getTag();
		}
		
		File iconCache = new File(apps.get(position).getIconCachePath());
		if(iconCache.exists() && iconCache.length() > 0){
			rowViewHolder.app_icon.setImageURI(Uri.parse(apps.get(position).getIconCachePath()));
		}else{
			rowViewHolder.app_icon.setImageResource(android.R.drawable.sym_def_app_icon);
		}
		
		rowViewHolder.app_name.setText(apps.get(position).getAppName());
		rowViewHolder.installed_versionname.setText(" "+apps.get(position).getVersionName());
		
		if(((ViewDisplayApplicationInstalled) apps.get(position)).isDowngradable()){
			rowViewHolder.app_downgradable.setVisibility(View.VISIBLE);
		}else{
			rowViewHolder.app_downgradable.setVisibility(View.INVISIBLE);
		}

		if(((ViewDisplayApplicationInstalled) apps.get(position)).isUpdatable()){
			rowViewHolder.app_upgradable.setVisibility(View.VISIBLE);
		}else{
			rowViewHolder.app_upgradable.setVisibility(View.INVISIBLE);
		}
		
		
		return convertView;
	}
	
	@Override
	public int getCount() {
		return apps.size();
	}

	@Override
	public ViewDisplayApplication getItem(int position) {
		return apps.get(position);
	}

	@Override
	public long getItemId(int position) {
		return apps.get(position).getAppHashid();
	}
	
	
	/**
	 * StaticInstalledAppsListAdapter Constructor
	 *
	 * @param context
	 * @param textViewResourceId
	 */
	public StaticInstalledAppsListAdapter(Context context, ListView listView, AIDLAptoideServiceData serviceDataCaller, Handler aptoideTasksHandler) {
		
		this.serviceDataCaller = serviceDataCaller;
		this.aptoideTasksHandler = aptoideTasksHandler;
		
		apps = new ViewDisplayListApps();

		appsManager = new InstalledAppsManager();


		this.listView = listView;
		layoutInflater = LayoutInflater.from(context);
	} 
	
	
	
	public void resetDisplayInstalled(){
		appsManager.reset();
	}
	
	public void refreshDisplayInstalled(){
		notifyDataSetChanged();
	}
	
	
	
    private void initDisplay(){
		listView.setAdapter(this);    	
    }
	
	private synchronized void setFreshInstalledApps(ViewDisplayListApps freshInstalledApps){
		this.freshApps = freshInstalledApps;
	}
	
	private void resetDisplay(){
		if(freshApps == null || freshApps.isEmpty()){
			aptoideTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_INSTALLED_TO_NO_APPS.ordinal());
		}else{
			aptoideTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_INSTALLED_TO_LIST.ordinal());
		
	    	this.apps = freshApps;
			Log.d("Aptoide-StaticInstalledAppsListAdapter", "new InstalledList: "+getCount());
	   		initDisplay();
	    	refreshDisplayInstalled();
	    	
	    	aptoideTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.RESET_UPDATABLE_LIST_DISPLAY.ordinal());
		}
	}
	
	public void shutdownNow(){
		appsManager.installedColectorsPool.shutdownNow();
	}
	
}
