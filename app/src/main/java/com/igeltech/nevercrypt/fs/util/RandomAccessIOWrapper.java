package com.igeltech.nevercrypt.fs.util;

import com.igeltech.nevercrypt.fs.RandomAccessIO;

import java.io.IOException;

public class RandomAccessIOWrapper implements RandomAccessIO
{
    private final RandomAccessIO _base;

    public RandomAccessIOWrapper(RandomAccessIO base)
    {
        _base = base;
    }

    @Override
    public void close() throws IOException
    {
        _base.close();
    }

    @Override
    public void seek(long position) throws IOException
    {
        _base.seek(position);
    }

    @Override
    public long getFilePointer() throws IOException
    {
        return _base.getFilePointer();
    }

    @Override
    public long length() throws IOException
    {
        return _base.length();
    }

    @Override
    public int read() throws IOException
    {
        return _base.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        return _base.read(b, off, len);
    }

    @Override
    public void write(int b) throws IOException
    {
        _base.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        _base.write(b, off, len);
    }

    @Override
    public void flush() throws IOException
    {
        _base.flush();
    }

    @Override
    public void setLength(long newLength) throws IOException
    {
        _base.setLength(newLength);
    }

    public RandomAccessIO getBase()
    {
        return _base;
    }
}
