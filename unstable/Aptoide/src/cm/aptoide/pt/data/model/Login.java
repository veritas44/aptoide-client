/*
 * Login,		auxilliary class to Aptoide's ServiceData
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

import cm.aptoide.pt.data.Constants;
import android.content.ContentValues;


 /**
 * Login, models a download's authentication
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class Login {


	private ContentValues loginValues;
	

	public Login(String username, String password) {
		loginValues = new ContentValues(Constants.NUMBER_OF_COLUMNS_LOGIN);
		setUsername(username);
		setPassword(password);
	}

	private void setUsername(String username){
		loginValues.put(Constants.KEY_LOGIN_USERNAME, username);
	}

	private void setPassword(String password){
		loginValues.put(Constants.KEY_LOGIN_PASSWORD, password);
	}

	public String getUsername() {
		return loginValues.getAsString(Constants.KEY_LOGIN_USERNAME);
	}

	public String getPassword() {
		return loginValues.getAsString(Constants.KEY_LOGIN_PASSWORD);
	}
	
	public void setRepoHashid(int repoHashid){
		loginValues.put(Constants.KEY_LOGIN_REPO_HASHID, repoHashid);
	}
	
	public ContentValues getValues(){
		return loginValues;
	}
	
	
	public void clean(){
		this.loginValues = null;
	}

	public void reuse(String username, String password){
		loginValues = new ContentValues(Constants.NUMBER_OF_COLUMNS_LOGIN);
		setUsername(username);
		setPassword(password);
	}
	
}