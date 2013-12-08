package com.meeba.google.adapters;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    private Activity context;
    private ImageLoader mImageLoader;

    public ContactsArrayAdapter(Activity context, List<User> list) {
        super(context, R.layout.contactlistlayout, list);
        this.context = context;
        this.list = list;

        mImageLoader = ImageLoader.getInstance();
    }

    public void onDestroy() {
        mImageLoader.stop();
        mImageLoader.destroy();
    }

    static class ViewHolder {
        protected TextView text;
        protected CheckBox checkbox;
        protected ImageView imageView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            LayoutInflater inflator = context.getLayoutInflater();
            view = inflator.inflate(R.layout.contactlistlayout, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) view.findViewById(R.id.label);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.personPicture);
            viewHolder.checkbox = (CheckBox) view.findViewById(R.id.check);

            //make those views not focusable so  OnItemClick  can  be fired on  listView ROW click
            viewHolder.checkbox.setFocusable(false);
            viewHolder.imageView.setFocusable(false);
            viewHolder.text.setFocusable(false);

            viewHolder.checkbox
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            User element = (User) viewHolder.checkbox
                                    .getTag();
                            element.setSelected(buttonView.isChecked());
                            Utils.LOGD("changed selection to "+buttonView.isChecked());
                            Utils.LOGD("element= " + element.isSelected());
                        }
                    });
            view.setTag(viewHolder);
            viewHolder.checkbox.setTag(list.get(position));
        } else {
            view = convertView;
            ((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
        }
        final ViewHolder holder = (ViewHolder) view.getTag();

        User user = list.get(position);
        // For now just show the name
        String name = user.getName();
        boolean check = user.isSelected();
        String picture_url = user.getPicture_url();

        holder.text.setText(name);
        holder.checkbox.setChecked(check);
        Utils.LOGD("getView: position="+position+", user="+user);


        if(!TextUtils.isEmpty(picture_url)) {
            // Load image, decode it to Bitmap and return Bitmap to callback
            Utils.LOGD("Changing image of "+holder.text.getText());
            if(!mImageLoader.isInited()) {
                mImageLoader.init(Utils.getImageLoaderConfig(context));
            }
            mImageLoader.displayImage(picture_url, holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.no_photo);
        }

        return view;
    }

    public List<User> getList() {
        return list;
    }
}
