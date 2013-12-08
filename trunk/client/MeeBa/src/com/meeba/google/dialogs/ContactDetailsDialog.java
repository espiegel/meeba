package com.meeba.google.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.meeba.google.R;
import com.meeba.google.objects.User;
import com.meeba.google.util.Utils;

/**
 * Created by Eidan on 12/8/13.
 */
public class ContactDetailsDialog extends SherlockDialogFragment {

    public static String TAG = "ContactDetailsDialog";
    private User mUser;

    public ContactDetailsDialog(User user) {
        mUser = user;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View dialog = inflater.inflate(R.layout.dialog_contact_details, null);
        ImageView contactPicture = (ImageView) dialog.findViewById(R.id.contactPicture);
        TextView contactName = (TextView) dialog.findViewById(R.id.contactName);
        TextView contactPhone = (TextView) dialog.findViewById(R.id.contactPhone);
        Button button = (Button) dialog.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        if(!TextUtils.isEmpty(mUser.getPicture_url())) {
            String url = mUser.getPicture_url();
            url = url.replace("?sz=50","?sz=150");
            Utils.getImageLoader(getActivity()).displayImage(url, contactPicture);
        }
        contactName.setText(mUser.getName());
        contactPhone.setText(mUser.getPhone_number());

        final AlertDialog alertDialog = builder.create();
        alertDialog.setView(dialog, 0, 0, 0, 0);

        return alertDialog;
    }
}
