package com.meeba.google.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meeba.google.R;
import com.meeba.google.util.UserFunctions;
import com.meeba.google.util.Utils;

import java.util.List;

/**
 * Created by Eidan on 12/18/13.
 */
public class WhereAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    private final SearchAutoComplete mCallback;
    private List<String> mResultList;
    private final Context mContext;

    public WhereAutoCompleteAdapter(Context context, int layoutResourceId, int textViewResourceId, SearchAutoComplete callback) {
        super(context, layoutResourceId, textViewResourceId);

        mContext = context;
        mCallback = callback;
    }

    @Override
    public int getCount() {
        return mResultList.size();
    }

    @Override
    public String getItem(int index) {
        return mResultList.get(index);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.dropdown_autocomplete, parent, false);
        }

        final TextView txtView = (TextView) convertView.findViewById(R.id.txtViewSearch);

        if(position >= mResultList.size()) {
            return convertView;
        }

        String currentString = mResultList.get(position);

        txtView.setText(currentString);

        txtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == null) {
                    return;
                }
                mCallback.autoCompleteItemClicked(((TextView)view).getText().toString());
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
                final FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Allow only alpha-numeric characters
                    final String input = constraint.toString().replaceAll(" ","+");//.replaceAll("[^a-zA-Z0-9]","");
                    new AsyncTask<Void,Void,List<String>>() {
                        @Override
                        protected List<String> doInBackground(Void... voids) {
                            // Retrieve the autocomplete results.
                            return UserFunctions.placeAutocomplete(input);
                        }
                        @Override
                        protected void onPostExecute(List<String> result) {
                            if(result == null) {
                                return;
                            }
                            mResultList = result;
                            Utils.LOGD("mResultList="+mResultList+", size="+mResultList.size());
                            if(mResultList.size() > 3) {
                                int size = mResultList.size();
                                for(int i = 3; i < size; i++) {
                                    Utils.LOGD("removing i="+i+", mResultList="+mResultList);
                                    mResultList.remove(3);
                                }
                            }
                            Utils.LOGD("mResultList = "+mResultList);
                            //Collections.sort(mResultList);

                            // Assign the data to the FilterResults
                            filterResults.values = mResultList;
                            filterResults.count = mResultList.size();

                            // Update once we're finished
                            publishResults(input, filterResults);
                        }
                    }.execute();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
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
