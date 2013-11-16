package com.meeba.google.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.meeba.google.objects.Event;
import com.meeba.google.R;

import java.util.List;

public class EventArrayAdapter extends ArrayAdapter<Event> {

    private List<Event> list;
    private final Activity context;

    public EventArrayAdapter(Activity context, List<Event> list) {
        super(context, R.layout.eventlistlayout, list);
        this.context = context;
        this.list = list;
    }

    static class ViewHolder {
        protected TextView eventlabel;
        protected TextView eventHostName;
        protected TextView eventwhere;
        protected TextView eventwhen;
    }

    public void setList(List<Event> list) {
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.eventlistlayout, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.eventHostName = (TextView) view.findViewById(R.id.eventhostname);
            viewHolder.eventwhere = (TextView) view.findViewById(R.id.eventwhere);
            viewHolder.eventwhen = (TextView) view.findViewById(R.id.eventwhen);

            view.setTag(viewHolder);
        } else {
            view = convertView;
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        // For now just show the name
        holder.eventHostName.setText("Host: "+list.get(position).getHost_name());
        holder.eventwhere.setText(list.get(position).getWhere());
        holder.eventwhen.setText(list.get(position).getWhen());

        return view;
    }

    public List<Event> getList() {
        return list;
    }
}