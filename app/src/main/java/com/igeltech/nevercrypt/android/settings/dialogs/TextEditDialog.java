package com.igeltech.nevercrypt.android.settings.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.igeltech.nevercrypt.android.Logger;
import com.igeltech.nevercrypt.android.R;
import com.igeltech.nevercrypt.android.settings.PropertyEditor;
import com.igeltech.nevercrypt.android.settings.PropertyEditor.Host;
import com.igeltech.nevercrypt.android.settings.views.PropertiesView;

public class TextEditDialog extends AppCompatDialogFragment
{
    public static final String TAG = "TextEditDialog";
    public static final String ARG_TEXT = "com.igeltech.nevercrypt.android.ARG_TEXT";
    public static final String ARG_MESSAGE_ID = "com.igeltech.nevercrypt.android.ARG_MESSAGE_ID";
    public static final String ARG_EDIT_TEXT_RES_ID = "com.igeltech.nevercrypt.android.EDIT_TEXT_RES_ID";
    private EditText _input;

    @NonNull
    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        int mid = getArguments().getInt(ARG_MESSAGE_ID);
        if (mid != 0)
            alert.setMessage(getString(mid));
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        _input = (EditText) inflater.inflate(getArguments().getInt(ARG_EDIT_TEXT_RES_ID, R.layout.settings_edit_text), null);
        _input.setText(savedInstanceState == null ? getArguments().getString(ARG_TEXT) : savedInstanceState.getString(ARG_TEXT));
        alert.setView(_input);
        alert.setPositiveButton(getString(android.R.string.ok), (dialog, whichButton) -> {
            Host host = PropertiesView.getHost(TextEditDialog.this);
            if (host != null)
            {
                PropertyEditor pe = host.getPropertiesView().getPropertyById(getArguments().getInt(PropertyEditor.ARG_PROPERTY_ID));
                if (pe != null)
                    try
                    {
                        ((TextResultReceiver) pe).setResult(_input.getText().toString());
                    }
                    catch (Exception e)
                    {
                        Logger.showAndLog(getActivity(), e);
                    }
            }
        });

		/*alert.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						// Canceled.
					}
				});*/
        return alert.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_TEXT, _input.getText().toString());
    }

    public interface TextResultReceiver
    {
        void setResult(String text) throws Exception;
    }
}
