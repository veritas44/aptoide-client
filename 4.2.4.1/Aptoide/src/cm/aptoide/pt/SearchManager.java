/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoide.pt;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import cm.aptoide.pt.R;
import cm.aptoide.pt.adapters.InstalledAdapter;
import cm.aptoide.pt.configuration.AptoideConfiguration;
import cm.aptoide.pt.contentloaders.SimpleCursorLoader;
import cm.aptoide.pt.util.Utils;
import com.actionbarsherlock.app.SherlockFragmentActivity;


public class SearchManager extends SherlockFragmentActivity implements LoaderCallbacks<Cursor>{
	ListView lv;
	String query;
//	EditText searchBox;
	Database db;
	View v;
	TextView results;
	private InstalledAdapter adapter;

	@Override
	protected void onCreate(Bundle arg0) {
		AptoideThemePicker.setAptoideTheme(this);
		super.onCreate(arg0);


		db = Database.getInstance();
		setContentView(R.layout.searchmanager);
//		getSupportActionBar().hide();
		if(getIntent().hasExtra("search")){
			query = getIntent().getExtras().getString("search");
		}else if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            Uri uri = getIntent().getData();
            query = uri.toString();
        } else {
			query = getIntent().getExtras().getString(android.app.SearchManager.QUERY).replaceAll("\\s{2,}|\\W", " ").trim();
			query = query.replaceAll("\\s{2,}", " ");
		}

		lv = (ListView) findViewById(R.id.listView);
//		searchBox = (EditText) findViewById(R.id.search_box);
		v = LayoutInflater.from(this).inflate(R.layout.footer_search_aptoide, null);

		lv.addFooterView(v);

		results = (TextView) v.findViewById(R.id.results_text);

		Button searchButton =  (Button) v.findViewById(R.id.baz_src);

		if(!ApplicationAptoide.SEARCHSTORES){
			searchButton.setVisibility(View.GONE);
            findViewById(R.id.search_in_other_stores).setVisibility(View.GONE);
        }






		searchButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                String url = AptoideConfiguration.getInstance().getUriSearch() + query + "&q=" + Utils.filters(SearchManager.this);
                Log.d("TAG", "Searching for:" + url);


                Intent i = new Intent(Intent.ACTION_VIEW);
                url = url.replaceAll(" ", "%20");
                i.setData(Uri.parse(url));
                startActivity(i);

            }
        });
		adapter = new InstalledAdapter(this,null,CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER,db);
		lv.setAdapter(adapter);

		getSupportLoaderManager().restartLoader(0x30, null, this);

		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View v, int position,
					long arg3) {
				Intent i = new Intent(SearchManager.this,ApkInfo.class);
				i.putExtra("_id", arg3);
				i.putExtra("category", Category.INFOXML.ordinal());
				startActivity(i);
			}
		});
	}



    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new SimpleCursorLoader(this) {

            @Override
            public Cursor loadInBackground() {

                return db.getSearch(query);
            }
        };
	}
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		adapter.swapCursor(arg1);

		if(adapter.getCount()>0){
			results.setText(getString(R.string.found)+" "+adapter.getCount()+" "+getString(R.string.results));
		}else{
            results.setText(getString(R.string.no_search_result, query));
        }


	}
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		if (item.getItemId() == android.R.id.home) {
//			finish();
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
}
