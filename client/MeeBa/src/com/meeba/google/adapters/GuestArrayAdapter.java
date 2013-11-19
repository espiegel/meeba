package com.meeba.google.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.meeba.google.R;
import com.meeba.google.objects.User;

import java.util.List;

/**
 * Created by Eidan on 11/19/13.
 */
public class GuestArrayAdapter extends ArrayAdapter<User> {

    private final List<User> list;
    private final Activity context;

    public GuestArrayAdapter(Activity context, List<User> list) {
        super(context, R.layout.guestlistlayout, list);
        this.context = context;
        this.list = list;
    }

    static class ViewHolder {
        protected TextView guestlist_name;
        protected ImageView invite_status;
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

            view.setTag(viewHolder);
        } else {
            view = convertView;
        }
        ViewHolder holder = (ViewHolder) view.getTag();

        String name = list.get(position).getName();
        // For now just show the name
        holder.guestlist_name.setText(name);

        holder.invite_status.setImageResource(getDrawable(list.get(position).getInvite_status()));
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
