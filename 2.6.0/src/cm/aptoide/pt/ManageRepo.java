/*
 * Copyright (C) 2009  Roberto Jacinto
 * roberto.jacinto@caixamagica.pt
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

package cm.aptoide.pt;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ManageRepo extends ListActivity{
	
	private DbHandler db = null;
	
	private final int ADD_REPO = 1;
	private final int REM_REPO = 2;
	private final int EDIT_REPO = 3;
	
	private final int REM_REPO_POP = 4;
	private final int EDIT_REPO_POP = 5;
	
	private boolean change = false;
	private boolean forceUpdate=false;
	
	private Intent rtrn = new Intent();
	
	private Vector<ServerNode> server_lst = new Vector<ServerNode>();
	
	private Vector<String> server_to_reset_count = new Vector<String>();
	
	Context ctx;
	
	private String updt_repo;
	private AlertDialog alert2;
	
	private AlertDialog alrt = null;

	private String repo;
	
	private enum returnStatus {OK, LOGIN_REQUIRED, BAD_LOGIN, FAIL, EXCEPTION};
	

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.repolist);
		ctx=this;
		registerForContextMenu(this.getListView());
		
		db = new DbHandler(this);
		
		Intent i = getIntent();
		if(i.hasExtra("empty")){
			final String uri = i.getStringExtra("uri");
			AlertDialog alrt = new AlertDialog.Builder(this).create();
			alrt.setTitle(getString(R.string.title_repo_alrt));
			alrt.setIcon(android.R.drawable.ic_dialog_alert);
			alrt.setMessage(getString(R.string.myrepo_alrt) +
					uri);
			alrt.setButton(getText(R.string.btn_yes), new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			    	  db.addServer(uri);
			    	  change = true;
			    	  redraw();
			    	  return;
			      } }); 
			alrt.setButton2(getText(R.string.btn_no), new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			    	  return;
			      }});
			alrt.show();
		}else if(i.hasExtra("uri")){
			//String uri = i.getStringExtra("uri");
			//Vector<String> new_serv_lst = getRemoteServLst(uri);
			Vector<String> exist_server = db.getServersName();
			ArrayList<String> new_serv_lst = (ArrayList<String>) i.getSerializableExtra("uri");
			boolean isChanged=false;
			for(final String uri_str: new_serv_lst){
				final String srv = serverCheck(uri_str);
				if(serverContainsRepo(exist_server,srv)){
					Toast.makeText(this, "Repo "+ srv+ " already exists.", 5000).show();
					continue;
//					finish();
					
					
				}else{
				AlertDialog alrt = new AlertDialog.Builder(this).create();
				alrt.setTitle(getString(R.string.title_repo_alrt));
				alrt.setIcon(android.R.drawable.ic_dialog_alert);
				alrt.setMessage(getString(R.string.newrepo_alrt) + srv);
				alrt.setButton(getText(R.string.btn_yes), new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int which) {
				    	  db.addServer(srv);
				    	  change = true;
				    	  redraw();
				    	  return;
				      } }); 
				alrt.setButton2(getText(R.string.btn_no), new DialogInterface.OnClickListener() {
				      public void onClick(DialogInterface dialog, int which) {
				    	  return;
				      }});
				alrt.show();
				isChanged=true;
				
			}}
			if(!isChanged){
				finish();
			}
		}else if(i.hasExtra("newrepo")){
			
			
			repo = i.getStringExtra("newrepo");
			if (!repo.endsWith("/")) {
				
				repo = repo + "/";
			}
			Vector<String> server_lst = db.getServersName();
			if(serverContainsRepo(server_lst, repo)){
				Toast.makeText(this, "Repo "+ repo+ " already exists.", 5000).show();
				finish();
				
				
			}else{
			AlertDialog alrt = new AlertDialog.Builder(this).create();
			alrt.setTitle(getString(R.string.title_repo_alrt));
			alrt.setIcon(android.R.drawable.ic_dialog_alert);
			alrt.setMessage(getString(R.string.newrepo_alrt) + repo);
			alrt.setButton(getText(R.string.btn_yes), new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			    	  db.addServer(repo);
			    	  change = true;
			    	  redraw();
			    	  return;
			      } }); 
			alrt.setButton2(getText(R.string.btn_no), new DialogInterface.OnClickListener() {
			      public void onClick(DialogInterface dialog, int which) {
			    	  //exit
			      }});
			alrt.show();
		}}
	}
	
	private boolean serverContainsRepo(Vector<String> serverList, String repo){
		if(serverList.contains(repo)){
			Log.d("Aptoide-ManageRepo", "Repo already exists");
			return true;
		}else{
			for (String existingRepo : serverList) {
				if(repo.length()== (existingRepo.length()-1)){
					if(repo.equals(existingRepo.substring(0, existingRepo.length()-1))){
						Log.d("Aptoide-ManageRepo", "Repo equal to existant one but without final forward slash");
						return true;
					}
				}else if(repo.length()== (existingRepo.length()-8)){ 
					if(repo.equals(existingRepo.substring(7, existingRepo.length()-1))){
						Log.d("Aptoide-ManageRepo", "Repo equal to existant one but without initial http:// and the final forward slash");
						return true;
					}
				}else{
					repo = repo+".bazaarandroid.com/";
					if(repo.equals(existingRepo.substring(7, existingRepo.length()))){
						Log.d("Aptoide-ManageRepo", "Repo equal to existant one but without initial http:// and without .bazaarandroid.com extension");
						return true;
					}
					if(repo.equals(existingRepo.substring(7, existingRepo.length()-1))){
						Log.d("Aptoide-ManageRepo", "Repo equal to existant one but without initial http:// , without .bazaarandroid.com extension, and the final forward slash");
						return true;
					}
				}
			}
		}
		Log.d("Aptoide-ManageRepo", "Repo is new");
		return false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		redraw();
	}

	private void redraw(){
		server_lst = db.getServers();
		 
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Map<String, Object> server_line;
        
        for(ServerNode node: server_lst){
        	server_line = new HashMap<String, Object>();
        	server_line.put("uri", node.uri);
        	if(node.inuse){
        		server_line.put("inuse", R.drawable.btn_check_on);
        	}else{
        		server_line.put("inuse", R.drawable.btn_check_off);
        	}
        	if(node.napk >= 0)
        		server_line.put("napk", getString(R.string.repo_app) + " " + node.napk);
        	else
        		server_line.put("napk", getString(R.string.repo_noinfo));
        	result.add(server_line);
        }
        SimpleAdapter show_out = new SimpleAdapter(this, result, R.layout.repolisticons, 
        		new String[] {"uri", "inuse", "napk"}, new int[] {R.id.uri, R.id.img, R.id.numberapks});
        
        setListAdapter(show_out);
	}
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		try{
			String node = server_lst.get(position).uri;
			ImageView pic = (ImageView)v.findViewById(R.id.img);

			if(!server_lst.get(position).inuse){
				server_lst.get(position).inuse = true;
				server_to_reset_count.add(node);
				pic.setImageResource(R.drawable.btn_check_on);
				Log.d("Aptoide", "check on");
			}else{
				server_lst.get(position).inuse = false;
				server_to_reset_count.remove(node);
				pic.setImageResource(R.drawable.btn_check_off);
				Log.d("Aptoide", "check off");
			}

			db.changeServerStatus(node);
			change = true;
		}catch (Exception e) {}
		//redraw();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE,ADD_REPO,1,R.string.menu_add_repo)
			.setIcon(android.R.drawable.ic_menu_add);
		menu.add(Menu.NONE, EDIT_REPO, 2, R.string.menu_edit_repo)
			.setIcon(android.R.drawable.ic_menu_edit);
		menu.add(Menu.NONE, REM_REPO, 3, R.string.menu_rem_repo)
		.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		
		super.onCreateContextMenu(menu, v, menuInfo);
		
		menu.setHeaderTitle(getString(R.string.repo_ctx));  
		menu.add(0, EDIT_REPO_POP, 0, getString(R.string.repo_ctx_edt));  
		menu.add(0, REM_REPO_POP, 0, getString(R.string.repo_ctx_del)); 
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		TextView tmp = (TextView) ((View)(info.targetView)).findViewById(R.id.uri);
		final String repo_selected = tmp.getText().toString();
		switch (item.getItemId()) {
		case EDIT_REPO_POP:
			editRepo(repo_selected);
			db.resetServerCacheUse(repo_selected);
			break;

		case REM_REPO_POP:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.repo_del));
			builder.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			builder.setMessage(getString(R.string.repo_del_msg) + " " + repo_selected + " ?");
			builder.setPositiveButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {
                		

						public void onClick(DialogInterface dialog, int	whichButton) {
                			db.removeServer(repo_selected);
                			change = false;
                			forceUpdate=true;
                			redraw();
                		}
            });
			builder.setNegativeButton(getString(R.string.btn_no), new DialogInterface.OnClickListener() {
                		public void onClick(DialogInterface dialog, int whichButton) {
                			return;
                		}
            }); 
			AlertDialog alert = builder.create();
			alert.show();
			
			
			break;
			
		default:
			break;
		}
		
		
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		
		LayoutInflater li = LayoutInflater.from(this); 
		
		switch(item.getItemId()){
		case ADD_REPO:
			View view = li.inflate(R.layout.addrepo, null);
			view.setBackgroundColor(Color.WHITE);
			final TextView sec_msg = (TextView) view.findViewById(R.id.sec_msg);
			final TextView sec_msg2 = (TextView) view.findViewById(R.id.sec_msg2);
			
			final EditText sec_user = (EditText) view.findViewById(R.id.sec_user);
			final EditText sec_pwd = (EditText) view.findViewById(R.id.sec_pwd);
			
			final CheckBox sec = (CheckBox) view.findViewById(R.id.secure_chk);
			sec.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						sec_user.setEnabled(true);
						sec_pwd.setEnabled(true);
					}else{
						sec_user.setEnabled(false);
						sec_pwd.setEnabled(false);
					}
				}
			});
			
			
			Builder p = new AlertDialog.Builder(this).setView(view);
			
			
			alrt = p.create();
			
			alrt.setIcon(android.R.drawable.ic_menu_add);
			alrt.setTitle(getText(R.string.manage_repo_add));

			alrt.setButton(getText(R.string.btn_add), new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					Message msg = new Message();
					EditText uri = (EditText) alrt.findViewById(R.id.edit_uri);
					String uri_str = uri.getText().toString();
//					
					uri_str = serverCheck(uri_str);
					sec_msg.setVisibility(View.GONE);
					sec_msg2.setVisibility(View.GONE);
					
					String user = null;
					String pwd = null;
					
					if(sec.isChecked()){
						user = sec_user.getText().toString();
						pwd = sec_pwd.getText().toString();
					}
					
					returnStatus result = checkServerConnection(uri_str, user, pwd);
					switch (result) {
					case OK:
						Log.d("Aptoide-ManageRepo", "return ok");
						Vector<String> serverList = db.getServersName();
						msg.obj = 0;
						if(serverContainsRepo(serverList, uri_str)){
							Toast.makeText(ctx, "Repo "+ uri_str+ " already exists.", 5000).show();
//							finish();
						}else{
						db.addServer(uri_str);
						if(user != null && pwd != null){
							db.addLogin(user, pwd, uri_str);	
						}
						change = true;}
						redraw();
						break;
					
					case LOGIN_REQUIRED:
						Log.d("Aptoide-ManageRepo", "return login_required");
						sec_msg2.setText(getText(R.string.manage_repo_answ_lr));
						sec_msg2.setVisibility(View.VISIBLE);
						msg.obj = 1;
						
						break;
						
					case BAD_LOGIN:
						Log.d("Aptoide-ManageRepo", "return bad_login");
						sec_msg2.setText(getText(R.string.manage_repo_answ_wrongl));
						sec_msg2.setVisibility(View.VISIBLE);
						msg.obj = 1;
						break;
						
					case FAIL:
						Log.d("Aptoide-ManageRepo", "return fail");
						uri_str = uri_str.substring(0, uri_str.length()-1)+".bazaarandroid.com/";
						Log.d("Aptoide-ManageRepo", "repo uri: "+uri_str);
						msg.obj = 1;
						break;

					default:
						Log.d("Aptoide-ManageRepo", "return exception");
						uri_str = uri_str.substring(0, uri_str.length()-1)+".bazaarandroid.com/";
						Log.d("Aptoide-ManageRepo", "repo uri: "+uri_str);
						msg.obj = 1;
						break;
					}
					if(result.equals(returnStatus.FAIL) || result.equals(returnStatus.EXCEPTION)){
						returnStatus result2 = checkServerConnection(uri_str, user, pwd);
						switch (result2) {
						case OK:
							Log.d("Aptoide-ManageRepo", "return ok");
							msg.obj = 0;
							Vector<String> serverList = db.getServersName();
							if(serverContainsRepo(serverList, uri_str)){
								Toast.makeText(ctx, "Repo "+ uri_str+ " already exists.", 5000).show();
//								finish();
							}else{
							db.addServer(uri_str);
							if(user != null && pwd != null){
								db.addLogin(user, pwd, uri_str);	
							}
							change = true;}
							redraw();
							break;
						
						case LOGIN_REQUIRED:
							Log.d("Aptoide-ManageRepo", "return login_required");
							sec_msg2.setText(getText(R.string.manage_repo_answ_lr));
							sec_msg2.setVisibility(View.VISIBLE);
							msg.obj = 1;
							
							break;
							
						case BAD_LOGIN:
							Log.d("Aptoide-ManageRepo", "return bad_login");
							sec_msg2.setText(getText(R.string.manage_repo_answ_wrongl));
							sec_msg2.setVisibility(View.VISIBLE);
							msg.obj = 1;	
							break;
							
						case FAIL:
							Log.d("Aptoide-ManageRepo", "return fail");
							sec_msg.setText(getText(R.string.manage_repo_answ_cc));
							sec_msg.setVisibility(View.VISIBLE);
							msg.obj = 1;
								
							break;

						default:
							Log.d("Aptoide-ManageRepo", "return exception");
							msg.obj = 1;	
							break;
						}
					}				
					new_repo.sendMessage(msg);
				} });

			alrt.setButton2(getText(R.string.btn_cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					alrt.dismiss();
				} });
			alrt.show();
			break;
		
		case REM_REPO:
			final Vector<String> rem_lst = new Vector<String>();	
			CharSequence[] b = new CharSequence[server_lst.size()];
			for(int i=0; i<server_lst.size(); i++){
				b[i] = server_lst.get(i).uri;
			}
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.repo_del_msg2));
			builder.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
			builder.setMultiChoiceItems(b, null, new DialogInterface.OnMultiChoiceClickListener() {
                		public void onClick(DialogInterface dialog,int whichButton, boolean isChecked) {
                		        if(isChecked){
                		        	rem_lst.addElement(server_lst.get(whichButton).uri);
                		        }else{
                		        	rem_lst.removeElement(server_lst.get(whichButton).uri);
                		        }
                		 }
             }); 
			builder.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
                		public void onClick(DialogInterface dialog, int	whichButton) {
                			db.removeServer(rem_lst);
                			change = false;
                			forceUpdate=true;
                			redraw();
                		}
            });
			builder.setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                		public void onClick(DialogInterface dialog, int whichButton) {
                			return;
                		}
            }); 
			AlertDialog alert = builder.create();
			alert.show();
			break;
			
		case EDIT_REPO:
			CharSequence[] b2 = new CharSequence[server_lst.size()];
			for(int i=0; i<server_lst.size(); i++){
				b2[i] = server_lst.get(i).uri;
			}
			
			AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
			builder2.setTitle(getString(R.string.repo_edt_msg1));
			builder2.setIcon(android.R.drawable.ic_menu_edit);
			builder2.setSingleChoiceItems(b2, -1, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
						updt_repo = server_lst.get(which).uri;
				}
			}); 
			builder2.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int	whichButton) {
					alert2.dismiss();
					editRepo(updt_repo);
					return;
				}
			});
			builder2.setNegativeButton(getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					return;
				}
			}); 
			 alert2 = builder2.create();
			alert2.show();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	
	private void editRepo(final String repo){
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.addrepo, null);
		Builder p = new AlertDialog.Builder(this).setView(view);
		final AlertDialog alrt = p.create();
		final EditText uri = (EditText) view.findViewById(R.id.edit_uri);
		uri.setText(repo);
		
		final EditText sec_user = (EditText) view.findViewById(R.id.sec_user);
		final EditText sec_pwd = (EditText) view.findViewById(R.id.sec_pwd);
		final CheckBox sec = (CheckBox) view.findViewById(R.id.secure_chk);
		sec.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					sec_user.setEnabled(true);
					sec_pwd.setEnabled(true);
				}else{
					sec_user.setEnabled(false);
					sec_pwd.setEnabled(false);
				}
			}
		});
		
		String[] logins = null; 
		logins = db.getLogin(repo);
		if(logins != null){
			sec.setChecked(true);
			sec_user.setText(logins[0]);																																																				 
			sec_pwd.setText(logins[1]);
		}else{
			sec.setChecked(false);
		}
	
		alrt.setIcon(android.R.drawable.ic_menu_add);
		alrt.setTitle(getString(R.string.repo_edt_msg2));
		alrt.setButton(getText(R.string.btn_done), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String new_repo = uri.getText().toString();
				db.updateServer(repo, new_repo);
				if(sec.isChecked()){
					db.addLogin(sec_user.getText().toString(), sec_pwd.getText().toString(), new_repo);
				}else{
					db.disableLogin(new_repo);
				}
				change = true;
				redraw();
			} });

		alrt.setButton2(getText(R.string.btn_cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			} });
		//alert2.dismiss();
		alrt.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			Bundle b = data.getExtras();
			String a = b.getString("URI");
			db.addServer(a);
			change = true;
			redraw();
		}
	}
	
	/*private Vector<String> getRemoteServLst(String file){
		SAXParserFactory spf = SAXParserFactory.newInstance();
		Vector<String> out = new Vector<String>();
	    try {
	    	SAXParser sp = spf.newSAXParser();
	    	XMLReader xr = sp.getXMLReader();
	    	NewServerRssHandler handler = new NewServerRssHandler(this);
	    	xr.setContentHandler(handler);
	    	
	    	InputStreamReader isr = new FileReader(new File(file));
	    	InputSource is = new InputSource(isr);
	    	xr.parse(is);
	    	File xml_file = new File(file);
	    	xml_file.delete();
	    	out = handler.getNewSrvs();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    } catch (SAXException e) {
	    	e.printStackTrace();
	    } catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	    return out;
	}*/
	

	
	private returnStatus checkServerConnection(String uri, String user, String pwd){
		
		int result;
		
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
		HttpConnectionParams.setSoTimeout(httpParameters, 10000);
		           
		DefaultHttpClient mHttpClient = new DefaultHttpClient(httpParameters);
		
//		DefaultHttpClient mHttpClient = Threading.getThreadSafeHttpClient();
		
		mHttpClient.setRedirectHandler(new RedirectHandler() {
			public boolean isRedirectRequested(HttpResponse response,
					HttpContext context) {
				return false;
			}

			public URI getLocationURI(HttpResponse response, HttpContext context)
			throws ProtocolException {
				return null;
			}
		});
		
        HttpGet mHttpGet = new HttpGet(uri+"/info.xml");
        
        SharedPreferences sPref = this.getSharedPreferences("aptoide_prefs", Context.MODE_PRIVATE);
		String myid = sPref.getString("myId", "NoInfo");
		String myscr = sPref.getInt("scW", 0)+"x"+sPref.getInt("scH", 0);
        
        mHttpGet.setHeader("User-Agent", "aptoide-" + this.getString(R.string.ver_str)+";"+ Configs.TERMINAL_INFO+";"+myscr+";id:"+myid+";"+sPref.getString(Configs.LOGIN_USER_NAME, ""));
        
        try {
        	if(user != null && pwd != null){
        		URL mUrl = new URL(uri);
        		mHttpClient.getCredentialsProvider().setCredentials(
        				new AuthScope(mUrl.getHost(), mUrl.getPort()),
        				new UsernamePasswordCredentials(user, pwd));
        	}
        	
			HttpResponse mHttpResponse = mHttpClient.execute(mHttpGet);
			
			Header[] azz = mHttpResponse.getHeaders("Location");
			if(azz.length > 0){
				String newurl = azz[0].getValue();

				mHttpGet = null;
				mHttpGet = new HttpGet(newurl);
				
				if(user != null && pwd != null){
	        		URL mUrl = new URL(newurl);
	        		mHttpClient.getCredentialsProvider().setCredentials(
	        				new AuthScope(mUrl.getHost(), mUrl.getPort()),
	        				new UsernamePasswordCredentials(user, pwd));
	        	}
				
				mHttpResponse = null;
				mHttpResponse = mHttpClient.execute(mHttpGet);
			}

			result = mHttpResponse.getStatusLine().getStatusCode();
			
			if(result == 200){
				return returnStatus.OK;
			}else if (result == 401){
				return returnStatus.BAD_LOGIN;
			}else{
				return returnStatus.FAIL;
			}
		} catch (ClientProtocolException e) { return returnStatus.EXCEPTION;} 
		catch (IOException e) { return returnStatus.EXCEPTION;}
		catch (IllegalArgumentException e) { return returnStatus.EXCEPTION;}
		catch (Exception e) {return returnStatus.EXCEPTION;	}
	}
	
	
	private Handler new_repo = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if((Integer)msg.obj == 1)
				alrt.show();
		}
	};

	@Override
	public void finish() {
		if (forceUpdate){
			rtrn.putExtra("forceupdate", true);
			for (String node: server_to_reset_count) {
				db.resetServerCacheUse(node);
			}
			
		}else if(change){
			rtrn.putExtra("update", true);
			for (String node: server_to_reset_count) {
				db.resetServerCacheUse(node);
			}
		}
		this.setResult(RESULT_OK, rtrn);
		super.finish();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private String serverCheck(String uri_str) {
		if(uri_str.length()!=0 && uri_str.charAt(uri_str.length()-1)!='/'){
			uri_str = uri_str+'/';
			Log.d("Aptoide-ManageRepo", "repo uri: "+uri_str);
		}
		if(!uri_str.startsWith("http://")){
			uri_str = "http://"+uri_str;
			Log.d("Aptoide-ManageRepo", "repo uri: "+uri_str);
		}
		return uri_str;
	}
	
}
