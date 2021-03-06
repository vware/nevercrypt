package com.igeltech.nevercrypt.android.filemanager.dialogs;

import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.igeltech.nevercrypt.android.R;
import com.igeltech.nevercrypt.android.filemanager.fragments.FileListViewFragment;
import com.igeltech.nevercrypt.fs.util.StringPathUtil;

public class RenameFileDialog extends DialogFragment
{
    public static final String TAG = "RenameFileDialog";
    private static final String ARG_CURRENT_PATH = "com.igeltech.nevercrypt.android.PATH";
    private static final String ARG_FILENAME = "com.igeltech.nevercrypt.android.FILENAME";

    public static void showDialog(FragmentManager fm, String currentPath, String fileName)
    {
        DialogFragment newFragment = new RenameFileDialog();
        Bundle b = new Bundle();
        b.putString(ARG_CURRENT_PATH, currentPath);
        b.putString(ARG_FILENAME, fileName);
        newFragment.setArguments(b);
        newFragment.show(fm, TAG);
    }

    @NonNull
    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setMessage(getString(R.string.enter_new_file_name));
        // Set an EditText view to get user input
        final String filename = getArguments().getString(ARG_FILENAME);
        final EditText input = new EditText(getActivity());
        input.setId(android.R.id.edit);
        input.setSingleLine();
        input.setText(filename);
        StringPathUtil spu = new StringPathUtil(filename);
        String fnWoExt = spu.getFileNameWithoutExtension();
        if (fnWoExt.length() > 0)
            input.setSelection(0, fnWoExt.length());
        alert.setView(input);
        alert.setPositiveButton(getString(android.R.string.ok), (dialog, whichButton) -> renameFile(input.getText().toString()));
        alert.setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> {
            // Canceled.
        });
        return alert.create();
    }

    private void renameFile(String newName)
    {
        FileListViewFragment frag = (FileListViewFragment) getParentFragmentManager().findFragmentByTag(FileListViewFragment.TAG);
        if (frag != null)
        {
            String prevName = getArguments().getString(ARG_CURRENT_PATH);
            frag.renameFile(prevName, newName);
        }
    }
}
