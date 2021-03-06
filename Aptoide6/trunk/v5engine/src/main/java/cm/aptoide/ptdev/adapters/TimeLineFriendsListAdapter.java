package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.webservices.timeline.json.Friend;

/**
 * Created by asantos   on 09-10-2014.
 */
public class TimeLineFriendsListAdapter extends ArrayAdapter<Friend> {
    private Context ctx;

    public TimeLineFriendsListAdapter(Context context, List<Friend> items) {
        super(context, 0, items);
        this.ctx = context;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        final View v;
	    ViewHolder holder;

	    if(convertView == null){
		    v = LayoutInflater.from(ctx).inflate(R.layout.row_facebook_named_friends, parent, false);
			holder = new ViewHolder();
		    holder.name = (TextView) v.findViewById(R.id.username);
			holder.avatarImage = (ImageView) v.findViewById(R.id.user_avatar);

		    v.setTag(holder);
	    } else {
		    holder = (ViewHolder)convertView.getTag();
            v = convertView;
	    }


        Friend friend = getItem(position);

	    holder.name.setText(friend.getUsername());
        Log.d("AptoideDebug", friend.getUsername());
        ImageLoader.getInstance().displayImage(friend.getAvatar(), holder.avatarImage);
        return v;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder{
        public TextView name;
        public ImageView avatarImage;
    }
}
