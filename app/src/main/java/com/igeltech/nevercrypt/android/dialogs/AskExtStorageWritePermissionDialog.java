package com.igeltech.nevercrypt.android.dialogs;


import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.igeltech.nevercrypt.android.R;
import com.igeltech.nevercrypt.android.locations.opener.fragments.ExternalStorageOpenerFragment;
import com.igeltech.nevercrypt.android.locations.opener.fragments.LocationOpenerBaseFragment;

public class AskExtStorageWritePermissionDialog extends DialogFragment
{
	public static void showDialog(FragmentManager fm, String openerTag)
	{
		Bundle args = new Bundle();
		args.putString(LocationOpenerBaseFragment.PARAM_RECEIVER_FRAGMENT_TAG, openerTag);
		DialogFragment newFragment = new AskExtStorageWritePermissionDialog();
		newFragment.setArguments(args);
	    newFragment.show(fm, "AskExtStorageWritePermissionDialog");
	}
	
	@NonNull
    @Override
	public AppCompatDialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.ext_storage_write_permission_request)
                .setPositiveButton(R.string.grant,
						(dialog, id) ->
						{
                            dialog.dismiss();
                            ExternalStorageOpenerFragment f = getRecFragment();
                            if(f!=null)
                                f.showSystemDialog();
                        })
				.setNegativeButton(android.R.string.cancel,
						(dialog, id) ->
						{
                            ExternalStorageOpenerFragment f = getRecFragment();
                            if(f!=null)
                                f.setDontAskPermissionAndOpenLocation();

                        });
		return builder.create();		
	}

    @Override
    public void onCancel(DialogInterface dialog)
    {
        super.onCancel(dialog);
        ExternalStorageOpenerFragment f = getRecFragment();
        if(f!=null)
            f.cancelOpen();
    }

    private ExternalStorageOpenerFragment getRecFragment()
	{
		return (ExternalStorageOpenerFragment) getFragmentManager().
				findFragmentByTag(
						getArguments().getString(
								LocationOpenerBaseFragment.PARAM_RECEIVER_FRAGMENT_TAG
						)
				);
	}

}