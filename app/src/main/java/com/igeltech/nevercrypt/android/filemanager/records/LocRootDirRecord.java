package com.igeltech.nevercrypt.android.filemanager.records;

import android.content.Context;

import com.igeltech.nevercrypt.fs.Path;
import com.igeltech.nevercrypt.locations.Location;

import java.io.IOException;

public class LocRootDirRecord extends FolderRecord
{
    private String _rootFolderName;

    public LocRootDirRecord(Context context) throws IOException
    {
        super(context);
    }

    @Override
    public void init(Location location, Path path) throws IOException
    {
        super.init(location, path);
        _rootFolderName = super.getName();
        if ((_rootFolderName == null || _rootFolderName.isEmpty()) && location != null)
            _rootFolderName = location.getTitle() + "/";
    }

    @Override
    public String getName()
    {
        return _rootFolderName;
    }

    @Override
    public boolean isFile()
    {
        return false;
    }

    @Override
    public boolean isDirectory()
    {
        return true;
    }
}