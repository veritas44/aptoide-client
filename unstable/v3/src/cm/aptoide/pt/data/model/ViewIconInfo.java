/**
 * ViewIconInfo,		part of Aptoide's data model
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

package cm.aptoide.pt.data.model;

import android.content.ContentValues;
import cm.aptoide.pt.data.util.Constants;

 /**
 * ViewIconInfo, models an icon's download info
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class ViewIconInfo {

	private ContentValues values;

	
	/**
	 * ViewIcon Constructor
	 * 
	 * @param String iconRemotePathTail, icon's remote path tail (what comes after repository's icons path)
	 * @param int applicationFullHashid, (applicationPackageName+'|'+applicationVersionCode+'|'+repositoryHashid).hashCode()
	 */
	public ViewIconInfo(String iconRemotePathTail, int applicationFullHashid) {
		this.values = new ContentValues(Constants.NUMBER_OF_COLUMNS_ICON_INFO);
		setIconRemotePathTail(iconRemotePathTail);
		setAppFullHashid(applicationFullHashid);
	}
	
	
	private void setIconRemotePathTail(String iconRemotePathTail){
		values.put(Constants.KEY_ICON_REMOTE_PATH_TAIL, iconRemotePathTail);		
	}
	
	public String getIconRemotePathTail(){
		return values.getAsString(Constants.KEY_ICON_REMOTE_PATH_TAIL);
	}
	
	private void setAppFullHashid(int appFullHashid){
		values.put(Constants.KEY_ICON_APP_FULL_HASHID, appFullHashid);
	}
	
	public int getAppFullHashid() {
		return values.getAsInteger(Constants.KEY_ICON_APP_FULL_HASHID);
	}
	
	
	public ContentValues getValues(){
		return this.values;
	}

	

	/**
	 * ViewIcon object reuse, clean references
	 */
	public void clean(){
		this.values = null;
	}

	/**
	 * ViewIcon object reuse, reConstructor
	 * 
	 * @param String iconRemotePathTail, icon's remote path tail (what comes after repository's base path)
	 * @param int applicationFullHashid, (applicationPackageName+'|'+applicationVersionCode+'|'+repositoryHashid).hashCode()
	 */
	public void reuse(String iconRemotePathTail, int applicationFullHashid) {
		this.values = new ContentValues(Constants.NUMBER_OF_COLUMNS_ICON_INFO);
		setIconRemotePathTail(iconRemotePathTail);
		setAppFullHashid(applicationFullHashid);
	}
	
}
