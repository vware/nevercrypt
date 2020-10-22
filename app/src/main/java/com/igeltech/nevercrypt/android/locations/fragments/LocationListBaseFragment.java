package com.igeltech.nevercrypt.android.locations.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.ListFragment;

import com.igeltech.nevercrypt.android.Logger;
import com.igeltech.nevercrypt.android.R;
import com.igeltech.nevercrypt.android.locations.activities.CreateLocationActivity;
import com.igeltech.nevercrypt.android.locations.activities.LocationSettingsActivity;
import com.igeltech.nevercrypt.android.locations.closer.fragments.LocationCloserBaseFragment;
import com.igeltech.nevercrypt.android.locations.dialogs.RemoveLocationConfirmationDialog;
import com.igeltech.nevercrypt.android.settings.UserSettings;
import com.igeltech.nevercrypt.locations.Location;
import com.igeltech.nevercrypt.locations.LocationsManager;

import java.util.ArrayList;
import java.util.List;

public abstract class LocationListBaseFragment extends ListFragment
{
    public static final String TAG = "LocationListBaseFragment";

    public class LocationInfo
    {
        public Location location;
        boolean isSelected;
        public boolean hasSettings() { return false; }
        public Drawable getIcon() { return null; }
        boolean allowRemove() { return true;}
    }

    public class ListViewAdapter extends ArrayAdapter<LocationInfo>
    {
        ListViewAdapter(Context context, List<LocationInfo> backingList)
        {
            super(context, R.layout.locations_list_row, backingList);
            TypedValue typedValue = new TypedValue();
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent)
        {
            View v;
            if (convertView == null)
            {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                v = inflater.inflate(R.layout.locations_list_row, null);
            } else v = convertView;

            final LocationInfo item = getItem(position);
            v.setTag(item);
            if (item == null)
                return v;

            AppCompatTextView tv = ((AppCompatTextView) v.findViewById(android.R.id.text1));
            tv.setText(item.location.getTitle());
            AppCompatImageView iv = (AppCompatImageView) v.findViewById(android.R.id.icon);
            if (iv != null)
                iv.setImageDrawable(item.getIcon());
            return v;
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        setEmptyText(getEmptyText());
        _locationsList = initAdapter();
        loadLocations();
        if(savedInstanceState!=null)
            restoreSelection(savedInstanceState);

        if(haveSelectedLocations())
            startSelectionMode();

        initListView();
        setListShown(true);
    }

    @Override
    public void onSaveInstanceState (Bundle outState)
    {
        super.onSaveInstanceState(outState);
        ArrayList<Location> selectedLocations = getSelectedLocations();
        if(!selectedLocations.isEmpty())
            LocationsManager.storeLocationsInBundle(outState, selectedLocations);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater)
    {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.location_list_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.add).setVisible(getDefaultLocationType() != null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        MenuHandlerInfo mhi = new MenuHandlerInfo();
        mhi.menuItemId = menuItem.getItemId();
        boolean res = handleMenu(mhi);
        if(res && mhi.clearSelection)
            clearSelectedFlag();
        return res || super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getActivity().registerReceiver(_reloadLocationsReceiver, new IntentFilter(LocationsManager.BROADCAST_LOCATION_CHANGED));
        getActivity().registerReceiver(_reloadLocationsReceiver, new IntentFilter(LocationsManager.BROADCAST_LOCATION_REMOVED));
        getActivity().registerReceiver(_reloadLocationsReceiver, new IntentFilter(LocationsManager.BROADCAST_LOCATION_CREATED));
        loadLocations();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        getActivity().unregisterReceiver(_reloadLocationsReceiver);
    }

    public void removeLocation(Location loc)
    {
        LocationsManager.getLocationsManager(getActivity()).removeLocation(loc);
        UserSettings.getSettings(getActivity()).setLocationSettingsString(loc.getId(), null);
        LocationsManager.broadcastLocationRemoved(getActivity(), loc);
    }

    protected ListViewAdapter _locationsList;
    protected static class MenuHandlerInfo
    {
        int menuItemId;
        boolean clearSelection;
    }

    protected abstract void loadLocations();

    protected LocationInfo getSelectedLocationInfo()
    {
        for(int i=0;i<_locationsList.getCount();i++)
        {
            LocationInfo li = _locationsList.getItem(i);
            if(li!=null && li.isSelected)
                return li;
        }
        return null;
    }

    protected ArrayList<Location> getSelectedLocations()
    {
        ArrayList<Location> res = new ArrayList<>();
        for(int i=0;i<_locationsList.getCount();i++)
        {
            LocationInfo li = _locationsList.getItem(i);
            if(li != null && li.isSelected)
                res.add(li.location);
        }
        return res;
    }

    protected boolean haveSelectedLocations()
    {
        return !getSelectedLocations().isEmpty();
    }

    protected String getEmptyText()
    {
        return getString(R.string.list_is_empty);
    }

    protected void selectLocation(LocationInfo li)
    {
        if(!haveSelectedLocations())
        {
            li.isSelected = true;
            startSelectionMode();
        }
        else if(isSingleSelectionMode())
        {
            clearSelectedFlag();
            li.isSelected = true;
        }
        onSelectionChanged();
        updateRowView(getListView(), getItemPosition(li));
    }

    protected boolean isSingleSelectionMode()
    {
        return true;
    }

    protected void unselectLocation(LocationInfo li)
    {
        li.isSelected = false;
        if(!haveSelectedLocations())
            stopSelectionMode();
        onSelectionChanged();
    }

    protected void onLocationClicked(LocationInfo li)
    {
        if(li.isSelected)
            unselectLocation(li);
        else
            selectLocation(li);

    }

    protected String getDefaultLocationType()
    {
        return null;
    }

    protected void closeLocation(Location loc)
    {
        Bundle args = new Bundle();
        //args.putString(LocationCloserBaseFragment.PARAM_RECEIVER_FRAGMENT_TAG, getTag());
        LocationsManager.storePathsInBundle(args, loc, null);
        LocationCloserBaseFragment closer = LocationCloserBaseFragment.getDefaultCloserForLocation(loc);
        closer.setArguments(args);
        getFragmentManager().beginTransaction().add(closer, LocationCloserBaseFragment.getCloserTag(loc)).commit();
    }

    protected int getContextMenuId()
    {
        return R.menu.location_context_menu;
    }

    protected boolean handleMenu(MenuHandlerInfo mhi)
    {
        switch (mhi.menuItemId)
        {
            case R.id.add:
                addNewLocation(getDefaultLocationType());
                return true;
            case R.id.settings:
                openSelectedLocationSettings();
                mhi.clearSelection = true;
                return true;
            case R.id.remove:
                removeSelectedLocation();
                mhi.clearSelection = true;
                return true;
            case R.id.close:
                closeSelectedLocation();
                mhi.clearSelection = true;
                return true;
            default:
                return false;
        }
    }

    protected boolean prepareContextMenu(LocationInfo selectedLocationInfo, android.view.Menu menu)
    {
        Location sl = selectedLocationInfo.location;
        MenuItem mi = menu.findItem(R.id.close);
        boolean closeVisible = LocationsManager.isOpenableAndOpen(sl);
        mi.setVisible(closeVisible);
        mi = menu.findItem(R.id.remove);
        mi.setVisible(!closeVisible && selectedLocationInfo.allowRemove());
        mi = menu.findItem(R.id.settings);
        mi.setVisible(selectedLocationInfo.hasSettings());
        return true;
    }

    protected void addNewLocation(String locationType)
    {
        Intent i = new Intent(getActivity(), CreateLocationActivity.class);
        i.putExtra(CreateLocationActivity.EXTRA_LOCATION_TYPE, locationType);
        startActivity(i);
    }

    private final BroadcastReceiver _reloadLocationsReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            loadLocations();
        }
    };

    private ActionMode _actionMode;

    private void openSelectedLocationSettings()
    {
        openLocationSettings(getSelectedLocationInfo());
    }

    private void removeSelectedLocation()
    {
        RemoveLocationConfirmationDialog.showDialog(
                getFragmentManager(),
                getSelectedLocationInfo().location
        );
    }

    private void closeSelectedLocation()
    {
        closeLocation(getSelectedLocationInfo().location);
    }

    private void restoreSelection(Bundle state)
    {
        if(state.containsKey(LocationsManager.PARAM_LOCATION_URIS))
        {
            try
            {
                ArrayList<Location> selectedLocations = LocationsManager.getLocationsManager(getActivity()).getLocationsFromBundle(state);
                for(Location loc: selectedLocations)
                    for(int i=0;i<_locationsList.getCount();i++)
                    {
                        LocationInfo li = _locationsList.getItem(i);
                        if(li!= null && li.location.getLocationUri().equals(loc.getLocationUri()))
                            li.isSelected = true;
                    }
            }
            catch (Exception e)
            {
                Logger.showAndLog(getActivity(), e);
            }

        }
    }

    private void clearSelectedFlag()
    {
        ListView lv = getListView();
        for(int i=0, count = lv.getCount(); i<count;i++)
        {
            LocationInfo li = (LocationInfo) lv.getItemAtPosition(i);
            if (li.isSelected)
            {
                li.isSelected = false;
                updateRowView(lv, i);
            }
        }
    }

    private int getItemPosition(LocationInfo li)
    {
        ListView lv = getListView();
        for(int i=0, n = lv.getCount();i<n;i++)
        {
            LocationInfo info = (LocationInfo) lv.getItemAtPosition(i);
            if(li == info)
                return i;
        }
        return -1;
    }

    private void updateRowView(ListView lv, int pos)
    {
        int start = lv.getFirstVisiblePosition();
        if(pos >= start && pos <= lv.getLastVisiblePosition())
        {
            View view = lv.getChildAt(pos - start);
            lv.getAdapter().getView(pos, view, lv);
        }
    }

    private ListViewAdapter initAdapter()
    {
        return new ListViewAdapter(getActivity(), new ArrayList<LocationInfo>());
    }

    private void initListView()
    {
        final ListView lv = getListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_NONE);
        lv.setItemsCanFocus(true);

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long itemId)
            {
                LocationInfo rec = (LocationInfo) adapterView.getItemAtPosition(pos);
                if (rec != null)
                    selectLocation(rec);
                return true;
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l)
            {
                LocationInfo rec = (LocationInfo) adapterView.getItemAtPosition(pos);
                if (rec != null)
                {
                    if (rec.isSelected && !isSingleSelectionMode())
                        unselectLocation(rec);
                    else if (haveSelectedLocations())
                        selectLocation(rec);
                    else
                        onLocationClicked(rec);
                }
            }
        });
        lv.setAdapter(_locationsList);
    }

    private void startSelectionMode()
    {
        _actionMode =  ((AppCompatActivity)getActivity()).startSupportActionMode(new ActionMode.Callback()
        {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu)
            {
                mode.getMenuInflater().inflate(getContextMenuId(), menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu)
            {
                if(!haveSelectedLocations())
                {
                    mode.finish();
                    return true;
                }
                return prepareContextMenu(getSelectedLocationInfo(), menu);
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item)
            {
                MenuHandlerInfo mhi = new MenuHandlerInfo();
                mhi.menuItemId = item.getItemId();
                boolean res = handleMenu(mhi);
                if(res && mhi.clearSelection)
                    mode.finish();

                return res;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode)
            {
                clearSelectedFlag();
                _actionMode = null;
            }
        });
    }

    private void stopSelectionMode()
    {
        if(_actionMode!=null)
        {
            _actionMode.finish();
            _actionMode = null;
        }
    }

    private void onSelectionChanged()
    {
        if(_actionMode!=null)
            _actionMode.invalidate();
       getActivity().invalidateOptionsMenu();
    }

    private void openLocationSettings(LocationInfo li)
    {
        Intent i = new Intent(getActivity(), LocationSettingsActivity.class);
        LocationsManager.storePathsInIntent(i, li.location, null);
        startActivity(i);
    }
}