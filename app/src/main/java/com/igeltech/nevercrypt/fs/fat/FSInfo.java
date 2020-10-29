package com.igeltech.nevercrypt.fs.fat;

import com.igeltech.nevercrypt.fs.RandomAccessIO;
import com.igeltech.nevercrypt.fs.util.Util;

import java.io.IOException;
import java.util.zip.DataFormatException;

class FSInfo
{
    private final BPB32 _bpb;
    int freeCount = -1;
    int lastAllocatedCluster = -1;

    FSInfo(BPB32 bpb)
    {
        _bpb = bpb;
    }

    void read(RandomAccessIO input) throws IOException, DataFormatException
    {
        input.seek(_bpb.bytesPerSector * _bpb.FSInfoSector);
        if (Util.readDoubleWordLE(input) != 0x41615252)
            throw new DataFormatException("Wrong file system information structure signature");
        input.seek(input.getFilePointer() + 480);
        if (Util.readDoubleWordLE(input) != 0x61417272)
            throw new DataFormatException("Wrong file system information structure signature");
        freeCount = (int) Util.readDoubleWordLE(input);
        lastAllocatedCluster = (int) Util.readDoubleWordLE(input);
        input.seek(input.getFilePointer() + 12);
        if (Util.readDoubleWordLE(input) != 0xAA550000)
            throw new DataFormatException("Wrong file system information structure signature");
    }

    void write(RandomAccessIO output) throws IOException
    {
        output.seek(_bpb.bytesPerSector * _bpb.FSInfoSector);
        Util.writeDoubleWordLE(output, 0x41615252);
        for (int i = 0; i < 480; i++)
            output.write(0);
        Util.writeDoubleWordLE(output, 0x61417272);
        Util.writeDoubleWordLE(output, freeCount);
        Util.writeDoubleWordLE(output, lastAllocatedCluster);
        for (int i = 0; i < 12; i++)
            output.write(0);
        Util.writeDoubleWordLE(output, 0xAA550000);
    }
}