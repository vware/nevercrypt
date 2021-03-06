package com.igeltech.nevercrypt.android.filemanager.tasks;

import android.content.ClipData;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.igeltech.nevercrypt.android.Logger;
import com.igeltech.nevercrypt.android.fragments.TaskFragment;
import com.igeltech.nevercrypt.android.service.ActionSendTask;
import com.igeltech.nevercrypt.android.service.FileOpsService;
import com.igeltech.nevercrypt.fs.Path;
import com.igeltech.nevercrypt.locations.Location;
import com.igeltech.nevercrypt.locations.LocationsManager;
import com.igeltech.nevercrypt.settings.GlobalConfig;

import java.util.ArrayList;
import java.util.Collection;

import static com.igeltech.nevercrypt.android.filemanager.tasks.CopyToClipboardTask.makeClipData;

public class PrepareToSendTask extends TaskFragment
{
    public static final String TAG = "PrepareToSendTask";
    protected Context _context;

    public static PrepareToSendTask newInstance(Location loc, Collection<? extends Path> paths)
    {
        Bundle args = new Bundle();
        LocationsManager.storePathsInBundle(args, loc, paths);
        PrepareToSendTask f = new PrepareToSendTask();
        f.setArguments(args);
        return f;
    }

    @Override
    public void initTask(FragmentActivity activity)
    {
        _context = activity.getApplicationContext();
    }

    @Override
    protected void doWork(TaskState state) throws Exception
    {
        if (GlobalConfig.isDebug())
            Logger.debug("PrepareToSendTask args: " + getArguments());
        ArrayList<Path> paths = new ArrayList<>();
        Location location = LocationsManager.
                getLocationsManager(_context).
                getFromBundle(getArguments(), paths);
        ArrayList<Uri> uris = new ArrayList<>();
        ArrayList<Path> checkedPaths = new ArrayList<>();
        String mime1 = null, mime2 = null;
        for (Path p : paths)
        {
            if (p.isFile())
            {
                Uri uri = location.getDeviceAccessibleUri(p);
                if (uri != null)
                    uris.add(uri);
                checkedPaths.add(p);
                String[] mimeType = FileOpsService.getMimeTypeFromExtension(_context, p).split("/", 2);
                if (mime1 == null)
                {
                    mime1 = mimeType[0];
                    mime2 = mimeType[1];
                }
                else if (!mime1.equals("*"))
                {
                    if (!mime1.equals(mimeType[0]))
                    {
                        mime1 = "*";
                        mime2 = "*";
                    }
                    else if (!mime2.equals("*"))
                    {
                        if (!mime2.equals(mimeType[1]))
                            mime2 = "*";
                    }
                }
            }
        }
        PrepareSendResult result = new PrepareSendResult();
        result.mimeType = mime1 != null && mime2 != null ? (mime1 + "/" + mime2) : null;
        result.location = location;
        if (!checkedPaths.isEmpty())
        {
            if (uris.size() == checkedPaths.size())
            {
                result.urisToSend = uris;
                result.clipData = makeClipData(_context, location, checkedPaths);
            }
            else
                result.tempFilesToPrepare = checkedPaths;
        }
        state.setResult(result);
    }

    @Override
    protected TaskCallbacks getTaskCallbacks(final FragmentActivity activity)
    {
        return new TaskCallbacks()
        {
            @Override
            public void onUpdateUI(Object state)
            {
            }

            @Override
            public void onPrepare(Bundle args)
            {
            }

            @Override
            public void onResumeUI(Bundle args)
            {
            }

            @Override
            public void onSuspendUI(Bundle args)
            {
            }

            @Override
            public void onCompleted(Bundle args, Result result)
            {
                try
                {
                    PrepareSendResult res = (PrepareSendResult) result.getResult();
                    if (res.urisToSend != null)
                        ActionSendTask.sendFiles(activity, res.urisToSend, res.mimeType, res.clipData);
                    else if (res.tempFilesToPrepare != null)
                        FileOpsService.sendFile(activity, res.mimeType, res.location, res.tempFilesToPrepare);
                }
                catch (Throwable e)
                {
                    Logger.showAndLog(activity, e);
                }
            }
        };
    }

    private static class PrepareSendResult
    {
        public String mimeType;
        public Location location;
        ArrayList<Path> tempFilesToPrepare;
        ArrayList<Uri> urisToSend;
        ClipData clipData;
    }
}