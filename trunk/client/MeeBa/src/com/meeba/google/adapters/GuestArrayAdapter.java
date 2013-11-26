package com.meeba.google.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.meeba.google.R;
import com.meeba.google.objects.User;
import com.meeba.google.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import java.util.List;

/**
 * Created by Eidan on 11/19/13.
 */
public class GuestArrayAdapter extends ArrayAdapter<User> {

    private final List<User> list;
    private final Activity context;
    private ImageLoader mImageLoader;

    public GuestArrayAdapter(Activity context, List<User> list) {
        super(context, R.layout.guestlistlayout, list);
        this.context = context;
        this.list = list;

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).build();
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(config);
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
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.guestlistlayout, null);
            final ViewHolder viewHolder = new ViewHolder();
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
        String name = guest.getName();
        holder.guestlist_name.setText(name);
        holder.invite_status.setImageResource(getDrawable(guest.getInvite_status()));

        final String picture_url = guest.getPicture_url();

        Utils.LOGD("guest="+guest);
        if(!TextUtils.isEmpty(picture_url)) {
            Utils.LOGD("Changing image of pos="+position+", guest=" + holder.guestlist_name.getText());

            final int pos = position;
            mImageLoader.loadImage(picture_url, new ImageLoadingListener() {
                ViewHolder curHolder;
                @Override
                public void onLoadingStarted(String s, View view) {
                    curHolder = holder;
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    Utils.LOGD("pos="+pos+", holder.position="+curHolder.position);
                    curHolder.guestPicture.setImageBitmap(bitmap);
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });
        }
        return view;
    }

    private int getDrawable(int invite_status) {
        if(invite_status == -1)
            return R.drawable.red_cross;

        if(invite_status == 1)
            return R.drawable.green_check;

        return R.drawable.question_mark;
    }

    public List<User> getList() {
        return list;
    }

}
