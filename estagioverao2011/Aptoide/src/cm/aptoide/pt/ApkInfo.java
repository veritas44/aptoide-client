package cm.aptoide.pt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import cm.aptoide.summerinternship2011.FailedRequestException;
import cm.aptoide.summerinternship2011.SetBlank;
import cm.aptoide.summerinternship2011.Status;
import cm.aptoide.summerinternship2011.comments.Comment;
import cm.aptoide.summerinternship2011.comments.CommentsAdapter;
import cm.aptoide.summerinternship2011.comments.LoadOnScrollCommentList;
import cm.aptoide.summerinternship2011.credentials.Login;
import cm.aptoide.summerinternship2011.multiversion.MultiversionSpinnerAdapter;
import cm.aptoide.summerinternship2011.multiversion.VersionApk;
import cm.aptoide.summerinternship2011.taste.TasteGetter;




import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class ApkInfo extends Activity implements OnDismissListener{

	private final static String WS_img = "http://www.bazaarandroid.com/webservices/listApkScreens/";
	
	private Intent apkinfo = null;
	private Context mctx = null;
	
	private Intent rtrn_intent = null;
	
	private boolean jback = false;
	
	/*private Integer[] imageIDs = {
			R.drawable.no_screen
	};*/
	
	private Drawable[] imageDrwb = null;
	
	//private Gallery galry = null;
	
	private String apk_name_str = null;
	private TextView noscreens = null;
	private List<ImageView> screens = null;
	
	
	private Spinner spinnerMulti;
	private ArrayList<Comment> comments;
	private Comment replyTo;
	private ListView listViewGlobal;
	
	private String apk_repo_str;
	private String apk_ver_str;
	private String apk_id;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.apkinfo);
		
		Event.REPLY.setString(this.getString(R.string.reply));
		Event.COPY_TO_CLIPBOARD.setString(this.getString(R.string.copyclip));
		Event.GENERATE_QR_CODE.setString(this.getString(R.string.qrcodeview));
		
		ListView listView = (ListView) findViewById(R.id.listComments);
		listViewGlobal = listView;
		
		LayoutInflater inflater = this.getLayoutInflater();
		final LinearLayout linearLayout = (LinearLayout)inflater.inflate(R.layout.headercomments,listView, false);
		updateScreenshots = new ScreenShotsUpdate(linearLayout);
		
		mctx = this;
		screens = new ArrayList<ImageView>();
		screens.add((ImageView) linearLayout.findViewById(R.id.shot1));
		screens.add((ImageView) linearLayout.findViewById(R.id.shot2));
		screens.add((ImageView) linearLayout.findViewById(R.id.shot3));
		screens.add((ImageView) linearLayout.findViewById(R.id.shot4));
		screens.add((ImageView) linearLayout.findViewById(R.id.shot5));

		noscreens = (TextView)linearLayout.findViewById(R.id.noscreens);
		
		rtrn_intent = new Intent();
		
		apkinfo = getIntent();
		
		apk_id = apkinfo.getStringExtra("apk_id");
		final int type = apkinfo.getIntExtra("type", 0);
		
		String icon_path = apkinfo.getStringExtra("icon");
		apk_name_str = apkinfo.getStringExtra("name");
		String apk_descr = apkinfo.getStringExtra("about");
		apk_repo_str = apkinfo.getStringExtra("server");
		apk_ver_str = apkinfo.getStringExtra("version");
		String apk_dwon_str = apkinfo.getStringExtra("dwn");
		String apk_rat_str = apkinfo.getStringExtra("rat");
		String apk_size_str = apkinfo.getStringExtra("size");
		
		Button serch_mrkt = (Button)findViewById(R.id.btn_market);
		serch_mrkt.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setData(Uri.parse("market://details?id="+apk_id));
				try{
					startActivity(intent);
				}catch (ActivityNotFoundException e){
					Toast.makeText(mctx, getText(R.string.error_no_market), Toast.LENGTH_LONG).show();
				}
			}
			
		});
		
		final Button action = (Button) findViewById(R.id.btn1);
		switch (type) {
		case 0:
			action.setText(getString(R.string.install));
			break;

		case 1:
			action.setText(getString(R.string.rem));
			break;
			
		case 2:
			action.setText(getString(R.string.update));
			break;
		}
		
		spinnerMulti = ((Spinner)linearLayout.findViewById(R.id.spinnerMultiVersion));
		
		action.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				int pos = -1;
				if(apkinfo.hasExtra("position")){
					pos = apkinfo.getIntExtra("position", -1);
				}
				switch (type) {
				case 0:
					
					rtrn_intent.putExtra("apkid", apk_id);
					rtrn_intent.putExtra("in", true);
					rtrn_intent.putExtra("position", pos);
					rtrn_intent.putExtra("version", ((VersionApk)spinnerMulti.getSelectedItem()).getVersion());
					jback = true;
					break;

				case 1:
					rtrn_intent.putExtra("apkid", apk_id);
					rtrn_intent.putExtra("rm", true);
					rtrn_intent.putExtra("position", pos);
					jback = true;
					break;


				case 2:
					rtrn_intent.putExtra("apkid", apk_id);
					rtrn_intent.putExtra("version", ((VersionApk)spinnerMulti.getSelectedItem()).getVersion());
					jback = true;
					break;
				}
				finish();
			}
		});
		
		ImageView icon = (ImageView) findViewById(R.id.appicon);
		File test_icon = new File(icon_path);
		
		
		if(test_icon.exists() && test_icon.length() > 0){
			icon.setImageDrawable(new BitmapDrawable(icon_path));
		}else{
			icon.setImageResource(android.R.drawable.sym_def_app_icon);
		}
		
		TextView apk_name = (TextView)findViewById(R.id.app_name);
		apk_name.setText(apk_name_str);
		
		TextView apk_about = (TextView)linearLayout.findViewById(R.id.descript);
		String desc_parsed = Html.fromHtml(apk_descr).toString();
		apk_about.setText(desc_parsed);
		
		final TextView apk_repo = (TextView)findViewById(R.id.app_repo);
		apk_repo.setText(apk_repo_str);
		
		TextView apk_version = (TextView)findViewById(R.id.app_ver);
		
		
		final VersionApk versionInstApk = (VersionApk) apkinfo.getParcelableExtra("instversion");
		
		if(type == 1){ 
			apk_version.setText(this.getString(R.string.version_inst)+": " + versionInstApk.getVersion());
		}else{
			apk_version.setVisibility(View.INVISIBLE);
		}
		
		
		TextView apk_down_n = (TextView)linearLayout.findViewById(R.id.dwn);
		apk_down_n.setText("Downloads: " + apk_dwon_str.replaceAll("\\n", "").replaceAll("\\t", "").trim());
		
		RatingBar apk_rat_n = (RatingBar) findViewById(R.id.rating);
		apk_rat_n.setRating(new Float(apk_rat_str));
		
		TextView apk_size_n = (TextView) linearLayout.findViewById(R.id.size);
		apk_size_n.setText(apk_size_str);

		new Thread(){
			public void run(){
				try{
					String ws_repo = apk_repo_str.substring(7).split("[\\.]")[0];
					String fetch_imgs = WS_img+ws_repo+"/"+apk_id+"/"+apk_ver_str.trim()+"/json";

					Log.d("Aptoide",apk_repo_str + " vs " + ws_repo);
					Log.d("Aptoide","Get img from: " + fetch_imgs);
					
					HttpResponse response_ws = NetworkApis.imgWsGet(fetch_imgs);
					if(response_ws != null && response_ws.getStatusLine().getStatusCode() == 200){
						String json_str = null;
						json_str = EntityUtils.toString(response_ws.getEntity());
						response_ws.getEntity().consumeContent();
						Log.d("Aptoide","Resp: " + json_str);
						JSONObject json_resp = new JSONObject(json_str);
						
						JSONArray img_url = json_resp.getJSONArray("listing");
						if(img_url.length()>0)
							imageDrwb = new Drawable[img_url.length()];
						for(int i = 0; i< img_url.length(); i++){
							String a = (String)img_url.get(i);
							Log.d("Aptoide","* " + a);
							HttpResponse pic = NetworkApis.imgWsGet(a);
							InputStream pic_st = pic.getEntity().getContent();
							Drawable pic_drw = Drawable.createFromStream(pic_st, "src");
							imageDrwb[i] = pic_drw;
						}
					}
					
				}catch (Exception e ){ }
				finally{
					updateScreenshots.sendEmptyMessage(0);
				}
			}
		}.start();
		
		
		
		
		
		/*Multiversion*/
		if(type!=1){//If we aren't in the installed tab
		
			ArrayList<VersionApk> versions = apkinfo.getParcelableArrayListExtra("oldVersions");
			versions.add(new VersionApk(apk_ver_str.replaceAll("[^0-9\\.]", "") ,apk_id,Integer.parseInt(apk_size_str.replaceAll("[^0-9]",""))));
			Collections.sort(versions, Collections.reverseOrder());
			final MultiversionSpinnerAdapter<VersionApk> spinnerMultiAdapter 
				= new MultiversionSpinnerAdapter<VersionApk>(this, R.layout.textviewfocused, versions, "Version", "Size");
			spinnerMultiAdapter.setDropDownViewResource(R.layout.multiversionspinneritem);
			spinnerMulti.setAdapter(spinnerMultiAdapter );
			if(type==2){
				spinnerMulti.setOnItemSelectedListener(new OnItemSelectedListener(){
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
							VersionApk versionApk = ((VersionApk)spinnerMultiAdapter.getItem(position));
						
							int result = versionApk.compareTo(versionInstApk);
							if(result>0){
								action.setText("Upgrade");
								action.setEnabled(true);
							}else if(result<0) {
								action.setText("Downgrade");
								action.setEnabled(true);
							}else{
								action.setEnabled(false);
								action.setText(ApkInfo.this.getString(R.string.isinstalled));
							}
							//TODO Select correct comments and likes for this version
					}
					public void onNothingSelected(AdapterView<?> parent) {}
				});
			}
			//Select the current version installed by the user
		}else{
			//Otherwise
			spinnerMulti.getLayoutParams().height = 0;
			spinnerMulti.getLayoutParams().width = 0;
			spinnerMulti.setVisibility(View.INVISIBLE);
		}
		
		
		
		
		
		/*Comments*/
//		comments.add(new Comment(new BigInteger("1"), "Hey", new BigInteger("1"), "Hello", "António", new Date()));
//		if(comments.size()==0)
//			((TextView)linearLayout.findViewById(R.id.commentsLabel)).getLayoutParams().height=0;
		listView.addHeaderView(linearLayout, null, false);
		comments = new ArrayList<Comment>();
		CommentsAdapter<Comment> arrayAdapter 
			= new CommentsAdapter<Comment>(this, R.layout.commentlistviewitem ,comments);
		listView.setAdapter(arrayAdapter);
		listView.setOnScrollListener(new LoadOnScrollCommentList(this, arrayAdapter));
		((Button)findViewById(R.id.clearReply)).setOnClickListener(new OnClickListener(){
			public void onClick(View arg) {
				((EditText)findViewById(R.id.subject)).setText("");
				((EditText)findViewById(R.id.comment)).setText("");
			}
		});
		final Button submit = ((Button)findViewById(R.id.submitComment));
		submit.setOnClickListener(new OnClickListener(){
			public void onClick(View arg) {
					
				SharedPreferences sharePreferences = getSharedPreferences("aptoide_prefs", Context.MODE_PRIVATE);
				if(sharePreferences.getString("usernameLogin", null)==null){				
					Login loginComments = new Login(ApkInfo.this, Login.InvoqueNature.NO_CREDENTIALS_SET);
					loginComments.setOnDismissListener(ApkInfo.this);
					loginComments.show();
				}else{
					ApkInfo.this.onDismiss(null);
				}
					
			}
		});
		class SetBlankSubmitComment extends SetBlank{
			public SetBlankSubmitComment() { super(); }
			public void onFocusChange(View viewEdit, boolean hasFocus) {
				super.onFocusChange(viewEdit, hasFocus);
				if(!submit.isEnabled())
					submit.setEnabled(true);
			}
		}
		((EditText)findViewById(R.id.comment)).setOnFocusChangeListener(new SetBlankSubmitComment());
		((EditText)findViewById(R.id.subject)).setOnFocusChangeListener(new SetBlank());
		((Button)findViewById(R.id.undoReplyLabel)).setOnClickListener(new OnClickListener(){
			public void onClick(View view) {
				replyTo = null;
				
				TextView to = ((TextView)listViewGlobal.findViewById(R.id.replyTo));
				to.setText("");
				to.getLayoutParams().height = 0;
				to.setLayoutParams(to.getLayoutParams());
				
        		Button undo =((Button)listViewGlobal.findViewById(R.id.undoReplyLabel));
        		undo.getLayoutParams().height = 0;
        		undo.setLayoutParams(undo.getLayoutParams());
			}
		});
		
		
		
		
		
		/*Taste*/
		TasteGetter tasteGetter = new TasteGetter("market","cm.aptoide.pt","2.0.2");
		TextView likes = (TextView)linearLayout.findViewById(R.id.likes);
		TextView dislikes = (TextView)linearLayout.findViewById(R.id.dislikes);
		try {
			tasteGetter.parse(this, null);
			if(tasteGetter.getStatus().equals(Status.OK)){
				likes.append(tasteGetter.getLikes().toString());
				dislikes.append(tasteGetter.getDislikes().toString());
			} else { throw new Exception(); }
		} catch(Exception e){
			likes.getLayoutParams().height=0;
			dislikes.getLayoutParams().height=0;
		}
		
		registerForContextMenu(listView);
		
	}
	
	
	
	
	
	public enum Event{
		REPLY(0), COPY_TO_CLIPBOARD(1), GENERATE_QR_CODE(2);
		private int id;
		private String string;
		
		private Event(int id){
			this.id = Menu.FIRST+id;
			this.string=null;
		}
		public int getId() {
			return id;
		}
		public String getString() {
			return string;
		}
		public void setString(String string) {
			this.string = string;
		}
		public static Event getEventFromId(int id){
			for(Event event:values()){
				if(event.getId()==id)
					return event;
			}
			return null;
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		
		if (((AdapterView.AdapterContextMenuInfo)menuInfo).id!=-1){
			menu.setHeaderTitle(this.getString(R.string.whattodo));
				for(Event item:Event.values())
					menu.add(0, item.getId(), 0, item.getString());
		}else{
			super.onCreateContextMenu(menu, view, menuInfo);
		}
		
	}
	
	/**
	 * 
	 * @param item
	 * @return If we handled the event or not. True in the first case.
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Event event = Event.getEventFromId(item.getItemId());
		Comment getted = comments.get(((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position-1);
		if(event!=null){
			switch (event) {
	        	case REPLY: 
	        		replyTo = getted;
	        		
	        		TextView to = ((TextView)listViewGlobal.findViewById(R.id.replyTo));
					to.setText("@"+getted.getUsername());
					to.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
					to.setLayoutParams(to.getLayoutParams());
	        		
	        		Button undoReply = ((Button)listViewGlobal.findViewById(R.id.undoReplyLabel));
	        		undoReply.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
	        		undoReply.setLayoutParams(undoReply.getLayoutParams());
	        		return true;
	        		
	        	case COPY_TO_CLIPBOARD:
	        		ClipboardManager clipManager = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
	        		clipManager.setText(getted.toString());
	        		return true;
	        	case GENERATE_QR_CODE:
	        		Dialog dialog = new Dialog(this);
	        		try {
		        		dialog.setContentView(R.layout.qrviewer);
		        		dialog.setOwnerActivity((Activity)this);
	        			((ImageView)dialog.findViewById(R.id.qrImage)).setImageBitmap(getted.giveQrCode());
	        			dialog.setTitle(getString(R.string.qrcodecomment));
					} catch (IOException e) {
						dialog.setTitle(getString(R.string.qrcodeunavailable));
					}
					dialog.show();
					return true;
	        	default : break;
	        }
		}
		
		return false;
	}
	
	
	public void onDismiss(DialogInterface dialog) {
		EditText subject = ((EditText)findViewById(R.id.subject));
		
		SharedPreferences sharedPreferences = getSharedPreferences("aptoide_prefs", Context.MODE_PRIVATE);
		try {
			//Comment.sendComment(this, apk_repo_str.substring("http://".length(),apk_repo_str.indexOf(".bazaarandroid.com")), apk_id, apk_ver_str.replaceAll("[^0-9\\.]", ""), ((EditText)findViewById(R.id.comment)).getText().toString(), sharedPreferences.getString("usernameLogin", null), sharedPreferences.getString("passwordLogin", null));
			Comment.sendComment(this, 
								"market", 
								"cm.aptoide.pt", 
								"2.0.2", 
								((SetBlank)subject.getOnFocusChangeListener()).getAlreadySetted()?subject.getText().toString():null,
								((EditText)findViewById(R.id.comment)).getText().toString(), 
								sharedPreferences.getString("usernameLogin", null), 
								sharedPreferences.getString("passwordLogin", null),
								replyTo!=null?replyTo.getId():null);
			Toast.makeText(this, getString(R.string.commentadded), Toast.LENGTH_LONG);
		} catch (FailedRequestException e) {
			Toast.makeText(this, getString(R.string.failedcredentials), Toast.LENGTH_LONG);
		} catch (IOException e) {
			Toast.makeText(this, getString(R.string.unabletoexecute), Toast.LENGTH_LONG);
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void screenshotClick(View v){
		//Log.d("Aptoide","This view.....");
		final Dialog dialog = new Dialog(mctx);

		dialog.setContentView(R.layout.screenshoot);
		dialog.setTitle(apk_name_str);

		ImageView image = (ImageView) dialog.findViewById(R.id.image);
		ImageView fetch = (ImageView) v;
		image.setImageDrawable(fetch.getDrawable());
		image.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		dialog.setCanceledOnTouchOutside(true);
		
		dialog.show();
		
	}
	
	private Handler updateScreenshots;
	
	private class ScreenShotsUpdate extends Handler{
		
		private LinearLayout header;
		
		public ScreenShotsUpdate(LinearLayout header) { this.header= header; }
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			ProgressBar pd = (ProgressBar) header.findViewById(R.id.pscreens);
			pd.setVisibility(View.GONE);
			int i = 0;
			if(imageDrwb != null){
				noscreens.setVisibility(View.GONE);
				for (Drawable pic : imageDrwb) {
					screens.get(i).setImageDrawable(pic);
					i++;
					if(i>=5)
						break;
				}
			}else{
				noscreens.setVisibility(View.VISIBLE);
				noscreens.setText("No screenshots available.");
			}
			//galry.setAdapter(new GalAdpt(mctx));
		}	
		
	}
	
	@Override
	public void finish() {
		if(jback)
			this.setResult(RESULT_OK, rtrn_intent);
		super.finish();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	

}
