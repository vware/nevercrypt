package com.igeltech.nevercrypt.fs.encfs;

public interface NameCodec
{
    String encodeName(String plaintextName);

    String decodeName(String encodedName);

    byte[] getChainedIV(String plaintextName);

    void init(byte[] key);

    void close();

    byte[] getIV();

    void setIV(byte[] iv);

    int getIVSize();
}
