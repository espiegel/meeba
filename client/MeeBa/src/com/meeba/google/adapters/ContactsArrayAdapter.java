package com.meeba.google.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.meeba.google.R;
import com.meeba.google.objects.User;

import java.util.ArrayList;
import java.util.List;





/**
 * Created by or malka on 19/11/13.
 */
public class ContactsArrayAdapter extends ArrayAdapter<User> implements Filterable {

    private List<User> list;
    private List<User> filteredList;
    private Activity context;

    public ContactsArrayAdapter(Activity context, List<User> list) {
        super(context, R.layout.rowbuttonlayout, list);
        this.context = context;
        this.list = list;
        this.filteredList = list;
    }

    public int getCount() {
        return filteredList.size();
    }

    //This should return a data object, not an int
    public User getItem(int position) {
        return filteredList.get(position);
    }

    static class ViewHolder {
        protected TextView text;
        protected CheckBox checkbox;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.rowbuttonlayout, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) view.findViewById(R.id.label);
            viewHolder.checkbox = (CheckBox) view.findViewById(R.id.check);
            viewHolder.checkbox
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            User element = (User) viewHolder.checkbox
                                    .getTag();
                            element.setSelected(buttonView.isChecked());
                            Log.d("mc","changed selection to "+buttonView.isChecked());
                            Log.d("element","element= "+element.isSelected());

                        }
                    });
            view.setTag(viewHolder);
            viewHolder.checkbox.setTag(filteredList.get(position));
        } else {
            view = convertView;
            ((ViewHolder) view.getTag()).checkbox.setTag(filteredList.get(position));
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        // For now just show the name
        holder.text.setText(filteredList.get(position).getName());
        holder.checkbox.setChecked(filteredList.get(position).isSelected());
        Log.d("isSelected",String.valueOf(filteredList.get(position).isSelected()));
        return view;
    }

    public List<User> getList() {
        return list;
    }

    @Override
    public Filter getFilter()
    {
        return new Filter()
        {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence)
            {
                FilterResults results = new FilterResults();

                //If there's nothing to filter on, return the original data for your list
                if(charSequence == null || charSequence.length() == 0)
                {
                    results.values = list;
                    results.count = filteredList.size();
                }
                else
                {
                    ArrayList<User> filterResultsData = new ArrayList<User>();

                    for(User contact : list)
                    {
                        //In this loop, you'll filter through originalData and compare each item to charSequence.
                        //If you find a match, add it to your new ArrayList
                        //I'm not sure how you're going to do comparison, so you'll need to fill out this conditional
                        if(contact.getName().toLowerCase().contains(charSequence.toString().toLowerCase()))
                        {
                            filterResultsData.add(contact);
                        }
                    }

                    results.values = filterResultsData;
                    results.count = filterResultsData.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults)
            {
                filteredList = (ArrayList<User>)filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
