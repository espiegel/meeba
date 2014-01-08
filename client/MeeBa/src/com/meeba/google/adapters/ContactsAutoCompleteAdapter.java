package com.meeba.google.adapters;

/**
 * Created by Padi on 01/01/14.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meeba.google.R;
import com.meeba.google.activities.ContactsActivity;
import com.meeba.google.objects.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eidan on 12/18/13.
 */
public class ContactsAutoCompleteAdapter extends ArrayAdapter<User> implements Filterable {
    private final SearchAutoComplete mCallback;
    private List<User> list;
    private List<User> filterList;
    private final Context mContext;

    public ContactsAutoCompleteAdapter(Context context, int layoutResourceId, int textViewResourceId , List<User> list, SearchAutoComplete callback) {
        super(context, layoutResourceId, textViewResourceId);

        this.list = list;
        this.filterList = list;
        mContext = context;
        mCallback = callback;
    }

    @Override
    public int getCount() {
        return filterList.size();
    }

    @Override
    public User getItem(int index) {
        return filterList.get(index);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.dropdown_autocomplete, parent, false);
        }

        final TextView txtView = (TextView) convertView.findViewById(R.id.txtViewSearch);

        if(position >= filterList.size()) {
            return convertView;
        }

        User currentUser = filterList.get(position);

        txtView.setText(currentUser.getName());

        txtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == null) {
                    return;
                }
                mCallback.autoCompleteItemClicked("");
                ContactsActivity.addToInviteList(filterList.get(position));
            }
        });

        RelativeLayout layout = (RelativeLayout) convertView.findViewById(R.id.layoutAutoComplete);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == null) {
                    return;
                }

                for (int itemPos = 0; itemPos < ((ViewGroup)view).getChildCount(); itemPos++) {
                    View child = ((ViewGroup)view).getChildAt(itemPos);
                    if (child instanceof TextView) {
                        TextView textView = (TextView) child; //Found it!

                        mCallback.autoCompleteItemClicked(textView.getText().toString());

                        break;
                    }
                }
            }
        });
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(final CharSequence constraint) {
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
                        if (p.getName().toLowerCase().startsWith(constraint.toString().toLowerCase()))
                            filterData.add(p);
                    }

                    results.values = filterData;
                    results.count = filterData.size();

                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    filterList = (List<User>) results.values;
                    notifyDataSetChanged();
                } else {
                    filterList = (List<User>) results.values;
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    public interface SearchAutoComplete {
        public void autoCompleteItemClicked(String query);
    }
}
