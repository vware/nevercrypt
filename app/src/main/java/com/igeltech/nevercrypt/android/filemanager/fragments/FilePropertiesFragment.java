package com.igeltech.nevercrypt.android.filemanager.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textview.MaterialTextView;
import com.igeltech.nevercrypt.android.Logger;
import com.igeltech.nevercrypt.android.R;
import com.igeltech.nevercrypt.android.filemanager.activities.FileManagerActivity;
import com.igeltech.nevercrypt.android.fragments.TaskFragment;
import com.igeltech.nevercrypt.android.fragments.TaskFragment.Result;
import com.igeltech.nevercrypt.android.fragments.TaskFragment.TaskCallbacks;
import com.igeltech.nevercrypt.fs.Directory;
import com.igeltech.nevercrypt.fs.Path;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class FilePropertiesFragment extends Fragment
{
    public static final String TAG = "FilePropertiesFragment";

    public static final String ARG_CURRENT_PATH = "current_path";
    private MaterialTextView _sizeTextView, _numberOfFilesTextView, _fullPathTextView, _modDateTextView;
    private FilesInfo _lastInfo;
    private final TaskFragment.TaskCallbacks _calcPropertiesCallbacks = new TaskCallbacks()
    {
        @Override
        public void onUpdateUI(Object state)
        {
            _lastInfo = (FilesInfo) state;
            updateUI(_lastInfo, false);
        }

        @Override
        public void onSuspendUI(Bundle args)
        {
        }

        @Override
        public void onResumeUI(Bundle args)
        {
        }

        @Override
        public void onPrepare(Bundle args)
        {
        }

        @Override
        public void onCompleted(Bundle args, Result result)
        {
            try
            {
                if (!result.isCancelled())
                {
                    _lastInfo = (FilesInfo) result.getResult();
                    updateUI(_lastInfo, true);
                }
            }
            catch (Throwable e)
            {
                Logger.showAndLog(getActivity(), e);
            }
        }

    };

    public static FilePropertiesFragment newInstance(Path currentPath)
    {
        Bundle args = new Bundle();
        if (currentPath != null)
            args.putString(ARG_CURRENT_PATH, currentPath.getPathString());
        FilePropertiesFragment f = new FilePropertiesFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
        {
            _lastInfo = new FilesInfo();
            _lastInfo.load(savedInstanceState);
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null)
            startCalcTask();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.file_properties_fragments, container, false);
        _sizeTextView = view.findViewById(R.id.selectionPropertiesSizeTextView);
        _numberOfFilesTextView = view.findViewById(R.id.selectionPropertiesNumberOfFilesTextView);
        _fullPathTextView = view.findViewById(R.id.fullPathTextView);
        _modDateTextView = view.findViewById(R.id.lastModifiedTextView);
        if (_lastInfo != null)
            updateUI(_lastInfo, true);
        return view;
    }

    @Override
    public void onStop()
    {
        FragmentActivity fa = getActivity();
        if (fa != null)
        {
            if (!fa.isChangingConfigurations() || (fa instanceof FileManagerActivity))
                cancelCalcTask();
        }
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (_lastInfo != null)
            _lastInfo.save(outState);
    }

    private TaskFragment.TaskCallbacks getCalcPropertiesCallbacks()
    {
        return _calcPropertiesCallbacks;
    }

    private void updateUI(FilesInfo info, boolean isLast)
    {
        Context ctx = getActivity();
        if (ctx == null)
            return;
        String tmp = Formatter.formatFileSize(ctx, info.totalSize);
        if (!isLast)
            tmp = ">=" + tmp;
        _sizeTextView.setText(tmp);
        tmp = Long.toString(info.filesCount);
        if (!isLast)
            tmp = ">=" + tmp;
        _numberOfFilesTextView.setText(tmp);
        if (info.lastModDate != null)
        {
            java.text.DateFormat df = android.text.format.DateFormat.getDateFormat(ctx);
            java.text.DateFormat tf = android.text.format.DateFormat.getTimeFormat(ctx);
            tmp = df.format(info.lastModDate) + " " + tf.format(info.lastModDate);
            if (!isLast)
                tmp = ">=" + tmp;
        }
        else
            tmp = "";
        _modDateTextView.setText(tmp);
        _fullPathTextView.setText(info.path != null ? info.path : "");
    }

    private void cancelCalcTask()
    {
        FragmentManager fm = getParentFragmentManager();
        if (fm == null)
            return;
        TaskFragment tf = (TaskFragment) fm.findFragmentByTag(CalcPropertiesTaskFragment.TAG);
        if (tf != null)
            tf.cancel();
    }

    private void startCalcTask()
    {
        cancelCalcTask();
        FragmentManager fm = getParentFragmentManager();
        if (fm != null)
            fm.beginTransaction().add(CalcPropertiesTaskFragment.newInstance(getArguments()), CalcPropertiesTaskFragment.TAG).commit();
    }

    public static class CalcPropertiesTaskFragment extends TaskFragment
    {
        public static final String TAG = "CalcPropertiesTaskFragment";
        private ArrayList<Path> _paths;
        private long _lastUpdate;

        public static CalcPropertiesTaskFragment newInstance(Bundle args)
        {
            CalcPropertiesTaskFragment f = new CalcPropertiesTaskFragment();
            f.setArguments(args);
            return f;
        }

        @Override
        public void initTask(FragmentActivity activity)
        {
            try
            {
                FileListDataFragment df = (FileListDataFragment) getParentFragmentManager().findFragmentByTag(FileListDataFragment.TAG);
                if (df != null && df.isAdded())
                    _paths = new ArrayList<>(df.getSelectedPaths());
                else
                    _paths = new ArrayList<>();
                if (df != null && _paths.size() == 0 && getArguments().containsKey(ARG_CURRENT_PATH))
                    _paths.add(df.getLocation().getFS().getPath(getArguments().getString(ARG_CURRENT_PATH)));
            }
            catch (Exception e)
            {
                Logger.showAndLog(activity, e);
            }
        }

        @Override
        protected TaskCallbacks getTaskCallbacks(FragmentActivity activity)
        {
            FragmentManager fm = getParentFragmentManager();
            if (fm == null)
                return null;
            FilePropertiesFragment f = (FilePropertiesFragment) fm.findFragmentByTag(FilePropertiesFragment.TAG);
            if (f == null)
                return null;
            return f.getCalcPropertiesCallbacks();
        }

        @Override
        protected void doWork(TaskState state) throws Exception
        {
            FilesInfo info = new FilesInfo();
            Iterator<Path> pathsIterator = _paths.iterator();
            while (pathsIterator.hasNext())
            {
                if (state.isTaskCancelled())
                    break;
                Path p = pathsIterator.next();
                calcPath(state, info, p);
                pathsIterator.remove();

            }
            state.setResult(info);
        }

        private void calcPath(final TaskState state, final FilesInfo info, Path rec)
        {
            info.filesCount++;
            if (info.path == null)
                info.path = rec.getPathDesc();
            else if (!info.path.endsWith(", ..."))
                info.path += ", ...";
            try
            {
                if (rec.isFile())
                {
                    info.totalSize += rec.getFile().getSize();
                    Date mdt = rec.getFile().getLastModified();
                    if (info.lastModDate == null || mdt.after(info.lastModDate))
                        info.lastModDate = mdt;
                }
                else if (rec.isDirectory())
                {
                    try (Directory.Contents dc = rec.getDirectory().list())
                    {
                        for (Path p : dc)
                        {
                            if (state.isTaskCancelled())
                                break;
                            calcPath(state, info, p);
                        }
                    }
                }
            }
            catch (IOException ignored)
            {
            }
            long curTime = System.currentTimeMillis();
            if (curTime - _lastUpdate > 500)
            {
                state.updateUI(info.copy());
                _lastUpdate = curTime;
            }
        }
    }

    private static class FilesInfo implements Cloneable
    {
        public String path;
        public int filesCount;
        public long totalSize;
        public Date lastModDate;

        public void save(Bundle b)
        {
            b.putString("path", path);
            b.putInt("count", filesCount);
            b.putLong("size", totalSize);
            if (lastModDate != null)
                b.putString("mod_date", SimpleDateFormat.getDateTimeInstance().format(lastModDate));
        }

        public void load(Bundle b)
        {
            path = b.getString("path");
            filesCount = b.getInt("count");
            totalSize = b.getLong("size");
            try
            {
                String s = b.getString("mod_date");
                if (s != null)
                    lastModDate = SimpleDateFormat.getDateTimeInstance().parse(s);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }

        public FilesInfo copy()
        {
            try
            {
                return (FilesInfo) clone();
            }
            catch (CloneNotSupportedException e)
            {
                return null;
            }
        }

    }
}
