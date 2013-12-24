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
import com.meeba.google.objects.Event;
import com.meeba.google.objects.User;
import com.meeba.google.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class EventArrayAdapter extends ArrayAdapter<Event> {

    private List<Event> mList;
    private final Activity mContext;

    public EventArrayAdapter(Activity context, List<Event> list) {
        super(context, R.layout.eventlistlayout, list);
        this.mContext = context;
        this.mList = list;

        if(mList == null) {
            mList = new ArrayList<Event>();
        }

    }

    static class ViewHolder {
        protected TextView eventHostName;
        protected TextView eventtitle;
        protected TextView eventwhere;
        protected TextView eventwhen;
        protected ImageView hostPicture;
        protected ImageView eventPicture;
    }

    public void setList(List<Event> list) {
        this.mList = list;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            LayoutInflater inflator = mContext.getLayoutInflater();
            view = inflator.inflate(R.layout.eventlistlayout, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.eventwhere = (TextView) view.findViewById(R.id.eventwhere);
            viewHolder.eventtitle = (TextView) view.findViewById(R.id.eventtitle);
            viewHolder.eventwhen = (TextView) view.findViewById(R.id.eventwhen);
            viewHolder.eventHostName = (TextView) view.findViewById(R.id.txtHost);
            viewHolder.hostPicture = (ImageView) view.findViewById(R.id.hostPicture);
            viewHolder.eventPicture = (ImageView) view.findViewById(R.id.eventPicture);

            view.setTag(viewHolder);
        } else {
            view = convertView;
        }
        ViewHolder holder = (ViewHolder) view.getTag();

        Event event = mList.get(position);
        holder.eventwhere.setText(event.getWhere());
        holder.eventtitle.setText(event.getTitle());
        holder.eventwhen.setText(event.getWhen());

        User host = event.getHost();
        holder.eventHostName.setText(host.getName());

        final ImageView hostPicture = holder.hostPicture;
        final ImageView eventPicture = holder.eventPicture;

        String url = host.getPicture_url();
        String eventPictureUrl = event.getEvent_picture();
        ImageLoader imageLoader = Utils.getImageLoader(mContext);
        if(url != null && !TextUtils.isEmpty(url)) {
            imageLoader.displayImage(url.replace("?sz=50", ""), hostPicture);
        }
        if(eventPictureUrl != null && !TextUtils.isEmpty(eventPictureUrl)) {
            imageLoader.displayImage(eventPictureUrl, eventPicture);
        } else {
            eventPicture.setImageResource(R.drawable.pub);
        }

        if (event.getEid() == -1)
            view.setVisibility(View.GONE);

        return view;
    }

    public List<Event> getList() {
        return mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }
}