package com.meeba.google.adapters;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.meeba.google.R;
import com.meeba.google.objects.User;
import com.meeba.google.util.UserFunctions;
import com.meeba.google.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by Eidan on 11/19/13.
 */
public class GuestArrayAdapter extends ArrayAdapter<User> {

    private final List<User> list;
    private final Activity mContext;
    private ImageLoader mImageLoader;

    public GuestArrayAdapter(Activity context, List<User> list) {
        super(context, R.layout.guestlistlayout, list);
        this.mContext = context;
        this.list = list;
    }

    static class ViewHolder {

        protected TextView guestlist_name;
        protected ImageView invite_status;
        protected ImageView guestPicture;
        protected int position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {

            final ViewHolder viewHolder = new ViewHolder();
            LayoutInflater inflator = mContext.getLayoutInflater();

            view = inflator.inflate(R.layout.guestlistlayout, null);
            viewHolder.guestlist_name = (TextView) view.findViewById(R.id.guestlist_name);
            viewHolder.invite_status = (ImageView) view.findViewById(R.id.img_invite_status);
            viewHolder.guestPicture = (ImageView) view.findViewById(R.id.guestPicture);
            viewHolder.position = position;

            view.setTag(viewHolder);
        } else {
            view = convertView;
        }
        final ViewHolder holder = (ViewHolder) view.getTag();

        User guest = list.get(position);

        //String name = guest.getName();
        String name = UserFunctions.translateUserName(guest, mContext);
        holder.guestlist_name.setText(name);
        holder.invite_status.setImageResource(getDrawable(guest.getInvite_status()));

        final String picture_url = guest.getPicture_url();

        Utils.LOGD("guest="+guest);
        if(!TextUtils.isEmpty(picture_url)) {
            Utils.LOGD("Changing image of pos="+position+", guest=" + holder.guestlist_name.getText());
            mImageLoader = Utils.getImageLoader(mContext);
            mImageLoader.displayImage(picture_url, holder.guestPicture);
        }
        return view;
    }

    private int getDrawable(int invite_status) {
        if(invite_status == -1)
            return R.drawable.red_cross;

        if(invite_status == 1)
            return R.drawable.green_check;

        return R.drawable.ic_action_help;
    }

    public List<User> getList() {
        return list;
    }

}
