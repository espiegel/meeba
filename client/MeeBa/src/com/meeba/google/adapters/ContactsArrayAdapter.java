package com.meeba.google.adapters;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.meeba.google.R;
import com.meeba.google.objects.User;
import com.meeba.google.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by Eidan on 11/20/13.
 */
public class ContactsArrayAdapter extends ArrayAdapter<User> implements Filterable {

    private List<User> list;
    //private List<User> filterList;
    private Activity mContext;
    private ImageLoader mImageLoader;
    private OnListDeleteClick mCallback;

    public ContactsArrayAdapter(Activity context, List<User> list, OnListDeleteClick callback) {
        super(context, R.layout.contactlistlayout, list);
        this.mContext = context;
        this.list = list;
        this.mCallback = callback;
        //this.filterList = list;
    }

    static class ViewHolder {
        protected TextView text;
        protected ImageView deleteMe;
       // protected CheckBox checkbox;
        protected ImageView imageView;
    }

    public int getCount() {
        return list.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Utils.LOGD("position ="+position);
        Utils.LOGD("filterResults="+list);
        View view;
        if (convertView == null) {
            LayoutInflater inflator = mContext.getLayoutInflater();
            view = inflator.inflate(R.layout.contactlistlayout, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) view.findViewById(R.id.label);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.personPicture);
            viewHolder.deleteMe=(ImageView) view.findViewById(R.id.deleteMe);
            /*viewHolder.deleteMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    User element = (User) viewHolder.deleteMe
                            .getTag();
                    ContactsActivity.addToInviteList(element);
                }
            });*/

           /* viewHolder.checkbox = (CheckBox) view.findViewById(R.id.check);*/
           /*viewHolder.checkbox
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            User element = (User) viewHolder.checkbox
                                    .getTag();
                            element.setSelected(buttonView.isChecked());
                            Utils.LOGD("changed selection to "+buttonView.isChecked());
                            Utils.LOGD("element= " + element.isSelected());
                        }
                    });*/
            view.setTag(viewHolder);
           /* viewHolder.checkbox.setTag(list.get(position));*/
        } else {
            view = convertView;
            /*((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));*/
        }
        final ViewHolder holder = (ViewHolder) view.getTag();

        final User user = list.get(position);
        // For now just show the name
        String name = user.getName();
        //boolean check = user.isSelected();
        String picture_url = user.getPicture_url();

        holder.deleteMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onListDeleteClicked(user);
            }
        });

        holder.text.setText(name);
        /*holder.checkbox.setChecked(check);*/
        Utils.LOGD("getView: position="+position+", user="+user);


        if(!TextUtils.isEmpty(picture_url)) {
            // Load image, decode it to Bitmap and return Bitmap to callback
            Utils.LOGD("Changing image of "+holder.text.getText());
            mImageLoader = Utils.getImageLoader(mContext);
            mImageLoader.displayImage(picture_url, holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.no_photo);
        }

        return view;
    }

    public List<User> getList() { return list; }

   /* @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                // We implement here the filter logic
                if (constraint == null || constraint.length() == 0) {
                    // No filter implemented we return all the list
                    results.values = list;
                    results.count = list.size();
                }
                else {
                    // We perform filtering operation
                    ArrayList<User> filterData = new ArrayList<User>();

                    for (User p : list) {
                        if (p.getName().toLowerCase().contains(constraint.toString().toLowerCase()))
                            filterData.add(p);
                    }

                    results.values = filterData;
                    results.count = filterData.size();

                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                filterList = (List<User>) results.values;
                notifyDataSetChanged();
            }
        };
    }*/

    public interface OnListDeleteClick {
        public void onListDeleteClicked(User user);
    }
}

