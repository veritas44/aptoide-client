/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.utils.IconSizes;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;


import java.util.ArrayList;
import java.util.List;

public class ImageGalleryAdapter extends BaseAdapter {

	private Context context;
	ImageLoader imageLoader;
    List<String> url;
//	private String hashCode;
	private boolean hd;
	DisplayImageOptions options;
    private String sizeString;

    public ImageGalleryAdapter(Context context, List<String> imagesurl, boolean hd) {

		this.context=context;
		imageLoader = ImageLoader.getInstance();

		this.url=imagesurl;
		this.hd=hd;
//		this.hashCode=hashCode.hashCode()+"";
//		System.out.println("hash:Method:"+hashCode + " " + this.hashCode);
//		options = new DisplayImageOptions.Builder()
//		 .displayer(new FadeInBitmapDisplayer(1000))
//		 .resetViewBeforeLoading()
//		 .cacheOnDisc()
//		 .build();

		options = new DisplayImageOptions.Builder()
		.showImageForEmptyUri(android.R.drawable.sym_def_app_icon)
		.cacheOnDisc()
		.build();
	}

//	@Override
//	public Object instantiateItem(ViewGroup container, final int position) {
//		final String hashCode=this.hashCode+"."+position;
//		final View v = LayoutInflater.from(context).inflate(R.layout.screenshots, null);
//		final ProgressBar pb = (ProgressBar) v.findViewById(R.id.screenshots_pb);
//		imageLoader.displayImage(hd?url.get(position):screenshotToThumb(url.get(position)),(ImageView) v.findViewById(R.id.screenshot),options, new ImageLoadingListener() {
//
//			@Override
//			public void onLoadingStarted() {
//				pb.setVisibility(View.VISIBLE);
//			}
//
//			@Override
//			public void onLoadingFailed(FailReason failReason) {
//				((ImageView) v.findViewById(R.id.screenshot)).setImageResource(android.R.drawable.ic_delete);
//				pb.setVisibility(View.GONE);
//			}
//
//			@Override
//			public void onLoadingComplete(Bitmap loadedImage) {
//				pb.setVisibility(View.GONE);
//			}
//
//			@Override
//			public void onLoadingCancelled() {
//
//			}
//		}, hashCode);
//		container.addView(v);
//		if(!hd){
//			v.setOnClickListener(new OnClickListener() {
//
//				public void onClick(View v) {
//					Intent i = new Intent(context,ScreenshotsViewer.class);
//					i.putStringArrayListExtra("url", url);
//					i.putExtra("position", position);
//					i.putExtra("hashCode", hashCode+".hd");
//					context.startActivity(i);
//				}
//			});
//		}
//		return v;
//
//	}
//	@Override
//	public int getCount() {
//		return url.size();
//	}
//
//	@Override
//	public boolean isViewFromObject(View arg0, Object arg1) {
//		return arg0.equals(arg1);
//	}
//
//	@Override
//	public void destroyItem(ViewGroup container, int position, Object object) {
//		container.removeView((View) object);
//
//	}
//
    protected String screenshotToThumb(String string) {

        String screen;

        if (string.contains("_screen")) {

            sizeString = IconSizes.generateSizeStringScreenshots(context, string.split("\\|")[0]);

            String[] splittedUrl = string.split("\\|")[1].split("\\.(?=[^\\.]+$)");
            screen = splittedUrl[0] + "_" + sizeString + "." + splittedUrl[1];
        } else {


            String[] splitedString = string.split("/");
            StringBuilder db = new StringBuilder();
            for (int i = 0; i != splitedString.length - 1; i++) {
                db.append(splitedString[i]);
                db.append("/");
            }
            db.append("thumbs/mobile/");
            db.append(splitedString[splitedString.length - 1]);
            screen = db.toString();
        }

        return screen;
    }

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
//		String hashCode=this.hashCode+"."+position;
		final View v;

		if(convertView==null){
            v = LayoutInflater.from(context).inflate(R.layout.row_item_screenshots_gallery, null);
		}else{
			v = convertView;
		}


		final ProgressBar pb = (ProgressBar) v.findViewById(R.id.screenshot_loading_item);


        String icon;

        if (hd) {
            Log.d("Aptoide-Screenshots" , "Icon is hd: " + url.get(position));

            if (url.get(position).contains("_screen")) {
                icon = url.get(position).split("\\|")[1];
                Log.d("Aptoide-Screenshots" , "Icon is : " + icon);
            } else {
                icon = url.get(position);
            }

        } else {
            icon = screenshotToThumb(url.get(position));
        }

		imageLoader.displayImage(icon,(ImageView) v.findViewById(R.id.screenshot_image_item), options, new SimpleImageLoadingListener() {

			@Override
			public void onLoadingStarted(String uri, View v) {
				pb.setVisibility(View.VISIBLE);
			}

			@Override
			public void onLoadingFailed(String uri, View v,FailReason failReason) {
				((ImageView) v.findViewById(R.id.screenshot_image_item)).setImageResource(android.R.drawable.ic_delete);
				pb.setVisibility(View.GONE);
				Log.d("onLoadingFailed", "Failed to load screenshot " + failReason.getCause());
			}

			@Override
			public void onLoadingComplete(String uri, View v,Bitmap loadedImage) {
				pb.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingCancelled(String uri, View v) {	}
		});

		return v;
	}

	@Override
	public int getCount() {
		return url.size();
	}


}
