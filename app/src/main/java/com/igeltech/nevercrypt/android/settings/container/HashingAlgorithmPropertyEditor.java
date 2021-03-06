package com.igeltech.nevercrypt.android.settings.container;

import com.igeltech.nevercrypt.android.R;
import com.igeltech.nevercrypt.android.locations.fragments.CreateContainerFragmentBase;
import com.igeltech.nevercrypt.android.locations.tasks.CreateContainerTaskFragmentBase;
import com.igeltech.nevercrypt.android.settings.ChoiceDialogPropertyEditor;
import com.igeltech.nevercrypt.container.VolumeLayout;
import com.igeltech.nevercrypt.container.VolumeLayoutBase;
import com.igeltech.nevercrypt.crypto.hash.RIPEMD160;
import com.igeltech.nevercrypt.crypto.hash.Whirlpool;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class HashingAlgorithmPropertyEditor extends ChoiceDialogPropertyEditor
{
    public HashingAlgorithmPropertyEditor(CreateContainerFragmentBase createContainerFragment)
    {
        super(createContainerFragment, R.string.hash_algorithm, 0, createContainerFragment.getTag());
    }

    public static String getHashFuncName(MessageDigest md)
    {
        if (md instanceof RIPEMD160)
            return "RIPEMD-160";
        if (md instanceof Whirlpool)
            return "Whirlpool";
        return md.getAlgorithm();
    }

    @Override
    protected int loadValue()
    {
        List<MessageDigest> algs = getCurrentHashAlgList();
        if (algs == null)
            return -1;
        String algName = getHostFragment().getState().getString(CreateContainerTaskFragmentBase.ARG_HASHING_ALG);
        if (algName != null)
        {
            MessageDigest md = VolumeLayoutBase.findHashFunc(algs, algName);
            return algs.indexOf(md);
        }
        else if (!algs.isEmpty())
            return 0;
        else
            return -1;
    }

    @Override
    protected void saveValue(int value)
    {
        List<MessageDigest> algs = getCurrentHashAlgList();
        MessageDigest md = algs.get(value);
        getHostFragment().getState().putString(CreateContainerTaskFragmentBase.ARG_HASHING_ALG, md.getAlgorithm());
    }

    @Override
    protected ArrayList<String> getEntries()
    {
        ArrayList<String> res = new ArrayList<>();
        List<MessageDigest> supportedEngines = getCurrentHashAlgList();
        if (supportedEngines != null)
        {
            for (MessageDigest eng : supportedEngines)
                res.add(getHashFuncName(eng));
        }
        return res;
    }

    protected CreateContainerFragmentBase getHostFragment()
    {
        return (CreateContainerFragmentBase) getHost();
    }

    private List<MessageDigest> getCurrentHashAlgList()
    {
        VolumeLayout vl = getHostFragment().getSelectedVolumeLayout();
        return vl != null ? vl.getSupportedHashFuncs() : null;
    }
}
