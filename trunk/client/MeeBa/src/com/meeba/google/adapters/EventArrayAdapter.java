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
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.List;

public class EventArrayAdapter extends ArrayAdapter<Event> {

    private final ImageLoader mImageLoader;
    private List<Event> list;
    private final Activity context;

    public EventArrayAdapter(Activity context, List<Event> list) {
        super(context, R.layout.eventlistlayout, list);
        this.context = context;
        this.list = list;

        DisplayImageOptions mDisplayImageOptions = new DisplayImageOptions.Builder().cacheInMemory(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .defaultDisplayImageOptions(mDisplayImageOptions)
                .build();
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(config);
    }

    static class ViewHolder {
        protected TextView eventHostName;
        protected TextView eventwhere;
        protected TextView eventwhen;
        protected ImageView hostPicture;
    }

    public void setList(List<Event> list) {
        this.list = list;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.eventlistlayout, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.eventwhere = (TextView) view.findViewById(R.id.eventwhere);
            viewHolder.eventwhen = (TextView) view.findViewById(R.id.eventwhen);
            viewHolder.eventHostName = (TextView) view.findViewById(R.id.txtHost);
            viewHolder.hostPicture = (ImageView) view.findViewById(R.id.hostPicture);

            view.setTag(viewHolder);
        } else {
            view = convertView;
        }
        ViewHolder holder = (ViewHolder) view.getTag();

        Event event = list.get(position);
        holder.eventwhere.setText(event.getWhere());
        holder.eventwhen.setText(event.getWhen());

        holder.eventHostName.setText(event.getHost_name());

        final ImageView hostPicture = holder.hostPicture;

        if(event.getHost_picture_url() != null && !TextUtils.isEmpty(event.getHost_picture_url())) {
            mImageLoader.displayImage(event.getHost_picture_url(), hostPicture);
        }

        return view;
    }

    public List<Event> getList() {
        return list;
    }
}