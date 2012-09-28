/*
 * ViewDownloadManagement, part of Aptoide
 * Copyright (C) 2012 Duarte Silveira
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
package cm.aptoide.pt2.views;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;

import cm.aptoide.pt2.ApplicationServiceManager;

/**
 * ViewDownloadManagement
 *
 * @author dsilveira
 *
 */
public class ViewDownloadManagement {
	ApplicationServiceManager serviceManager;
	ArrayList<Handler> observers;
	
	EnumDownloadStatus status;
	
	ViewApk appInfo;
	
	ViewDownload viewDownload;
	ViewCache cache;
	
	boolean isLoginRequired;
	ViewLogin login;
	
	/**
	 * 
	 * ViewDownloadManagement skeleton Constructor
	 *
	 * @param serviceManager
	 */
	public ViewDownloadManagement(ApplicationServiceManager serviceManager){
		this.serviceManager = serviceManager;
		this.observers = new ArrayList<Handler>();
		this.status = EnumDownloadStatus.SETTING_UP;
	}
	
	/**
	 * 
	 * ViewDownloadManagement Constructor
	 *
	 * @param serviceManager
	 * @param url
	 * @param appInfo
	 * @param cache
	 */
	public ViewDownloadManagement(ApplicationServiceManager serviceManager, String remoteUrl, ViewApk appInfo, ViewCache cache) {
		this(serviceManager);
		this.viewDownload = new ViewDownload(remoteUrl);
		this.cache = cache;
		this.appInfo = appInfo;
	}
	
	/**
	 * 
	 * ViewDownloadManagement Constructor
	 *
	 * @param serviceManager
	 * @param url
	 * @param appInfo
	 * @param cache
	 * @param login
	 */
	public ViewDownloadManagement(ApplicationServiceManager serviceManager, String remoteUrl, ViewApk appInfo, ViewCache cache, ViewLogin login) {
		this(serviceManager,remoteUrl, appInfo, cache);
		if(login != null){
			this.isLoginRequired = true;
			this.login = login;
		}
	}

	public int getProgress() {
		return (viewDownload == null?0:viewDownload.getProgressPercentage());
	}
	
	public void updateProgress(ViewDownload update){
		this.viewDownload.setProgressTarget(update.getProgressTarget());
		this.viewDownload.setProgress(update.getProgress());
		this.viewDownload.setSpeedInKbps(update.getSpeedInKbps());
		if(viewDownload.getProgress() >= viewDownload.getProgressTarget()){
			notifyObservers(EnumDownloadProgressUpdateMessages.COMPLETED);
		}else{
			notifyObservers(EnumDownloadProgressUpdateMessages.UPDATE);
		}
	}
	
	public int getSpeedInKbps(){
		return viewDownload.getSpeedInKbps();
	}
	
	public ViewDownload getDownload(){
		return this.viewDownload;
	}

	public ViewCache getCache() {
		return cache;
	}
	
	public void setCache(ViewCache cache){
		this.cache = cache;
	}

	public String getRemoteUrl() {
		return viewDownload.getRemotePath();
	}

	public ViewApk getAppInfo() {
		return appInfo;
	}
	
	public void setAppInfo(ViewApk appInfo){
		this.appInfo = appInfo;
	}
	
	public boolean isLoginRequired(){
		return isLoginRequired;
	}
	
	public ViewLogin getLogin() {
		return login;
	}

	public void setLogin(ViewLogin login) {
		if(login != null){
			this.isLoginRequired = true;
			this.login = login;
		}else{
			this.isLoginRequired = false;
			this.login = null;
		}
	}

	public void registerObserver(Handler listenerDownloadProgress){
		observers.add(listenerDownloadProgress);
	}
	
	private void notifyObservers(EnumDownloadProgressUpdateMessages progressUpdate){
		for (Handler listenerDownloadProgress : observers) {
			listenerDownloadProgress.sendEmptyMessage(progressUpdate.ordinal());
		}
	}
	
	
	/**
	 * startDownloadManagement, starts the download of the apk for the app described within this view
	 * 
	 * @throws AptoideDownloadException (runtimeException)
	 */
	public void startDownload(){
		serviceManager.startDownload(this);
	}
	
	
	
	/**
	 * ViewDownloadManagement object reuse clean references
	 *
	 */
	public void clean(){
		this.serviceManager = null;
		this.observers = null;
		this.status = null;
		this.appInfo = null;
		this.cache = null;
	}
	
	/**
	 * ViewDownloadManagement object skeleton reuse reConstructor
	 *
	 * @param serviceManager
	 */
	public void reuse(ApplicationServiceManager serviceManager) {
		this.serviceManager = serviceManager;
		this.observers = new ArrayList<Handler>();
		this.status = EnumDownloadStatus.SETTING_UP;
	}
	
	/**
	 * ViewDownloadManagement object reuse reConstructor
	 *
	 * @param serviceManager
	 * @param remoteUrl
	 * @param appInfo
	 * @param cache
	 */
	public void reuse(ApplicationServiceManager serviceManager, String remoteUrl, ViewApk appInfo, ViewCache cache) {
		reuse(serviceManager);
		this.viewDownload = new ViewDownload(remoteUrl);
		this.cache = cache;
		this.appInfo = appInfo;
	}
	
	/**
	 * ViewDownloadManagement object reuse reConstructor
	 *
	 * @param serviceManager
	 * @param remoteUrl
	 * @param appInfo
	 * @param cache
	 * @param login
	 */
	public void reuse(ApplicationServiceManager serviceManager, String remoteUrl, ViewApk appInfo, ViewCache cache, ViewLogin login) {
		reuse(serviceManager);
		this.viewDownload = new ViewDownload(remoteUrl);
		this.cache = cache;
		this.login = login;
		this.appInfo = appInfo;
	}


	@Override
	public int hashCode() {
		return this.appInfo.hashCode();
	}


	@Override
	public boolean equals(Object object) {
		if(object instanceof ViewDownloadManagement){
			ViewDownloadManagement download = (ViewDownloadManagement) object;
			if(download.hashCode() == this.hashCode()){
				return true;
			}
		}
		return false;
	}


	@Override
	public String toString() {
		return appInfo+" downloadStatus: "+status+viewDownload;
	}
	
}