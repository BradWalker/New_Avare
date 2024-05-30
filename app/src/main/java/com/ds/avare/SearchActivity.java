/*
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/


package com.ds.avare;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ds.avare.adapters.SearchAdapter;
import com.ds.avare.place.Destination;
import com.ds.avare.place.DestinationFactory;
import com.ds.avare.storage.StringPreference;
import com.ds.avare.touch.LongTouchDestination;
import com.ds.avare.utils.AirportInfo;
import com.ds.avare.utils.DecoratedAlertDialogBuilder;
import com.ds.avare.utils.DestinationAlertDialog;
import com.ds.avare.utils.GenericCallback;
import com.ds.avare.utils.Helper;

import java.util.LinkedHashMap;
import java.util.Observable;
import java.util.Observer;
 
/**
 * 
 * @author zkhan
 *
 */
public class SearchActivity extends BaseActivity implements Observer {

    private ListView mSearchListView;
    private EditText mSearchText;
    private Toast mToast;
    private SearchAdapter mAdapter;
    private SearchTask mSearchTask;
    private ProgressBar mProgressBar;
    private String mSelected;
    private DestinationAlertDialog mDestinationAlertDialog;
    private AirportInfo mClosestTask;
    private boolean mIsWaypoint;

    /**
     * Shows edit dialog
     */
    private AlertDialog mAlertDialogEdit;

    
    /**
     * Current destination info
     */
    private Destination mDestination;

    /**
     * 
     * @param dst
     */
    private void goTo(String dst, String type, String dbType) {
        mIsWaypoint = false;
        mDestination = DestinationFactory.build(dst, type);
        mDestination.addObserver(SearchActivity.this);
        mToast.setText(getString(R.string.Searching) + " " + dst);
        mToast.show();
        mDestination.find(dbType);
        mSearchText.setText("");
    }

    /**
     * 
     * @param dst
     */
    private void planTo(String dst, String type, String dbType) {
        mIsWaypoint = true;
        mDestination = DestinationFactory.build(dst, type);
        mDestination.addObserver(SearchActivity.this);
        mToast.setText(getString(R.string.Searching) + " " + dst);
        mToast.show();
        mDestination.find(dbType);
        mSearchText.setText("");
    }

    /**
     * 
     */
    private void initList() {
        String [] vals = mService.getDBResource().getUserRecents();
        mAdapter = new SearchAdapter(SearchActivity.this, vals);
        mSearchListView.setAdapter(mAdapter);
        mService.getFavorites().update(mService);
    }
    
    @Override
    /**
     * 
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsWaypoint = false;

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.search, null);
        setContentView(view);

        /*
         * Create toast beforehand so multiple clicks dont throw up a new toast
         */
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        /*
         * Lose info
         */
        mSelected = null;

        /*
         * For a search query
         */
        mSearchListView = (ListView) view.findViewById(R.id.search_list_view);

        /*
         * Progress bar
         */
        mProgressBar = (ProgressBar) (view.findViewById(R.id.search_progress_bar));

        /*
         * Set on click
         */
        mSearchListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                /*
                 * Commas not allowed
                 */
                String txt = mAdapter.getItem(position).replace(",", " ");
                String id = StringPreference.parseHashedNameId(txt);
                String destType = StringPreference.parseHashedNameDestType(txt);
                String dbType = StringPreference.parseHashedNameDbType(txt);
                if (id == null || destType == null) {
                    return;
                }
                // It's ok if dbType is null
                goTo(id, destType, dbType);
            }
        });

        mSearchListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                           int index, long arg3) {
                mSelected = mAdapter.getItem(index);
                if (mSelected == null) {
                    return true;
                }

                // stop previous lookup
                if (null != mClosestTask) {
                    mClosestTask.cancel(true);
                }

                // Don't display the plates button if there are no plates
                String id = StringPreference.parseHashedNameId(mSelected);
                String base = StringPreference.parseHashedNameDestType(mSelected);
                if (!base.equals(Destination.BASE)) {
                    LongTouchDestination ltd = new LongTouchDestination();
                    ltd.setMoreButtons(true);
                    ltd.setAirport(id);

                    mDestinationAlertDialog.setData(ltd);
                    mDestinationAlertDialog.show();

                    return true;
                } else {

                    //airport
                    mClosestTask = new AirportInfo();

                    mClosestTask.execute(null, null, id,
                            SearchActivity.this, mService, mPref, null, false,
                            new GenericCallback() {
                                @Override
                                public Object callback(Object o, Object o1) {
                                    LongTouchDestination ltd = (LongTouchDestination) o1;
                                    ltd.setMoreButtons(true);
                                    mDestinationAlertDialog.show();
                                    mDestinationAlertDialog.setData(ltd);

                                    // If the long press event has already occurred, we need to do the gesture callback here
                                    return null;
                                }
                            });
                }
                return (true);
            }

            ;
        });

        mDestinationAlertDialog = new DestinationAlertDialog(SearchActivity.this);
        mDestinationAlertDialog.setCallback(
                new GenericCallback() {
                    @Override
                    public Object callback(Object o, Object o1) {
                        String param = (String) o;
                        try {
                            mDestinationAlertDialog.dismiss();
                        } catch (Exception e) {
                        }

                        if (null == mSelected) {
                            return null;
                        }

                        if (param.equals("CSup")) {
                            if (null != mSelected) {
                                String id = StringPreference.parseHashedNameId(mSelected);
                                if (id == null) {
                                    return null;
                                }
                                String base = StringPreference.parseHashedNameDestType(mSelected);
                                if (!base.equals(Destination.BASE)) {
                                    return null; // no CSup for !airport
                                }

                                mService.setLastAfdAirport(id);
                                ((MainActivity) SearchActivity.this.getParent()).showAfdTab();
                            }
                        } else if (param.equals("Plate")) {
                            if (null != mSelected) {
                                String id = StringPreference.parseHashedNameId(mSelected);
                                if (id == null) {
                                    return null;
                                }
                                if (PlatesActivity.doesAirportHavePlates(mPref.getServerDataFolder(), id)) {
                                    mService.setLastPlateAirport(id);
                                    mService.setLastPlateIndex(0);
                                    ((MainActivity) SearchActivity.this.getParent()).showPlatesTab();
                                }
                            }
                        } else if (param.equals("+Plan")) {
                            if (null != mSelected) {
                                String id = StringPreference.parseHashedNameId(mSelected);
                                String destType = StringPreference.parseHashedNameDestType(mSelected);
                                String dbType = StringPreference.parseHashedNameDbType(mSelected);
                                if (id == null || destType == null) {
                                    return null;
                                }
                                // It's ok if dbType is null
                                planTo(id, destType, dbType);
                            }
                        } else if (param.equals("->D")) {
                            String id = StringPreference.parseHashedNameId(mSelected);
                            String destType = StringPreference.parseHashedNameDestType(mSelected);
                            String dbType = StringPreference.parseHashedNameDbType(mSelected);
                            if (id == null || destType == null) {
                                return null;
                            }
                            // It's ok if dbType is null
                            goTo(id, destType, dbType);
                        } else if (param.equals("Save")) {
                            if (null != mSelected) {
                                String id = StringPreference.parseHashedNameId(mSelected);
                                String destType = StringPreference.parseHashedNameDestType(mSelected);
                                String dbType = StringPreference.parseHashedNameDbType(mSelected);
                                String name = StringPreference.parseHashedNameFacilityName(mSelected);
                                mService.getDBResource().setUserRecent(new StringPreference(destType, dbType, name, id));
                                mSearchText.setText("");
                            }
                        } else if (param.equals("Delete")) {
                            if (null != mSelected) {
                                mService.getDBResource().deleteUserRecent(StringPreference.parseHashedNameId(mSelected));
                                initList();
                                mSearchText.setText("");
                            }
                            mSelected = null;
                        } else if (param.equals("Label")) {
                            if (null != mSelected) {
                                final EditText edit = new EditText(SearchActivity.this);
                                String type = StringPreference.parseHashedNameDbType(mSelected);
                                if (type == null) {
                                    mToast.setText(R.string.GpsOnly);
                                    mToast.show();
                                    return null;
                                }
                                if (!type.equals(Destination.GPS)) {
                                    mToast.setText(R.string.GpsOnly);
                                    mToast.show();
                                    return null;
                                }

                                edit.setText(StringPreference.parseHashedNameIdBefore(mSelected));

                                mAlertDialogEdit = new DecoratedAlertDialogBuilder(SearchActivity.this).create();
                                mAlertDialogEdit.setTitle(getString(R.string.Label));
                                mAlertDialogEdit.setCanceledOnTouchOutside(true);
                                mAlertDialogEdit.setCancelable(true);
                                mAlertDialogEdit.setView(edit);
                                mAlertDialogEdit.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.OK), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        /*
                                         * Edit and save description field
                                         */
                                        String nameid = StringPreference.parseHashedNameId(mSelected);
                                        String id = StringPreference.parseHashedNameIdAfter(nameid);
                                        String newName = edit.getText().toString().toUpperCase() + "@" + id;
                                        mService.getDBResource().replaceUserRecentName(nameid, newName);
                                        initList();
                                        mSelected = null;
                                        dialog.dismiss();

                                    }
                                });
                                mAlertDialogEdit.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        mSelected = null;
                                        dialog.dismiss();
                                    }
                                });
                                if (!isFinishing()) {
                                    mAlertDialogEdit.show();
                                }
                            }
                        }
                        return null;
                    }
                });

        /*
         * For searching, start search on every new key press
         */
        mSearchText = (EditText)view.findViewById(R.id.search_edit_text);
        mSearchText.addTextChangedListener(new TextWatcher() { 
            @Override
            public void afterTextChanged(Editable arg0) {
            }
    
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
    
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int after) {
                
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if(null != mSearchTask) {
                    if (!mSearchTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
                        /*
                         * Cancel the last query
                         */
                        mSearchTask.cancel(true);
                    }
                }
                
                /*
                 * If text is 0 length or too long, then do not search, show last list
                 */
                if(0 == s.length()) {
                    initList();
                    return;
                }
                
                if(s.toString().startsWith("address,")) {
                    String [] vals = new String[1];
                    String addr = s.toString().substring(8); // 8 = length of "address,"
                    if(addr.length() > 1) {
                        StringPreference sp = new StringPreference(Destination.MAPS, Destination.MAPS, Destination.MAPS, addr);
                        vals[0] = sp.getHashedName();
                        mAdapter = new SearchAdapter(SearchActivity.this, vals);
                        mSearchListView.setAdapter(mAdapter);
                    }
                    return;
                }
                
                /*
                 * This is a geo coordinate?
                 */
                if(Helper.isGPSCoordinate(s.toString())) {
                    String [] vals = new String[1];
                    StringPreference sp = new StringPreference(Destination.GPS, Destination.GPS, Destination.GPS, s.toString());
                    vals[0] = sp.getHashedName();
                    mAdapter = new SearchAdapter(SearchActivity.this, vals);
                    mSearchListView.setAdapter(mAdapter);
                    return;
                }
                mProgressBar.setVisibility(ProgressBar.VISIBLE);

                mSearchTask = new SearchTask();
                mSearchTask.execute(s.toString());

            }
        });

    }


    /* (non-Javadoc)
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();

        /*
         * Now initialize the list to recent in case someone needs to go there, and not search
         */
        initList();
        mService.registerGpsListener(mGpsInfc);
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();
        
        mService.unregisterGpsListener(mGpsInfc);

        if(null != mSearchText) {
            mSearchText.setText("");
        }

        if(null != mAlertDialogEdit) {
            try {
                mAlertDialogEdit.dismiss();
            }
            catch (Exception e) {
            }
        }

        if(null != mDestinationAlertDialog) {
            try {
                mDestinationAlertDialog.dismiss();
            }
            catch (Exception e) {
            }
        }

    }
    
    /**
     * 
     */
    @Override
    public void update(Observable arg0, Object arg1) {
        /*
         * Destination found?
         */
        if(arg0 instanceof Destination) {
            Boolean result = (Boolean)arg1;
            if(result) {
            
                /*
                 * Temporarily move to destination by giving false GPS signal.
                 */
                if(null == mDestination) {
                    mToast.setText(getString(R.string.DestinationNF));
                    mToast.show();
                    return;
                }
                if((Destination)arg0 != mDestination) {
                    /*
                     * If user presses a selection repeatedly, reject previous
                     */
                    return;                    
                }
                StringPreference s = new StringPreference(mDestination.getType(), mDestination.getDbType(), mDestination.getFacilityName(), mDestination.getID());
                mService.getDBResource().setUserRecent(s);

                if(!mIsWaypoint) {
                    mService.setDestination((Destination)arg0);
                    mToast.setText(getString(R.string.DestinationSet) + ((Destination)arg0).getID());
                    mToast.show();
                    MainActivity m = (MainActivity)this.getParent();
                    if(m != null) {
                        m.showMapTab();
                    }
                }
                else {
                    if(mService.getPlan().appendDestination((Destination)arg0)) {
                        mToast.setText(((Destination)arg0).getID() + getString(R.string.PlanSet));
                    }
                    else {
                        mToast.setText(((Destination)arg0).getID() + getString(R.string.PlanNoset));
                    }
                    mToast.show();
                }
            }
            else {
                mToast.setText(getString(R.string.DestinationNF));
                mToast.show();
            }
        }
    }

    /**
     * @author zkhan
     *
     */
    private class SearchTask extends AsyncTask<Object, Void, Boolean> {

        private String[] selection;

        /* (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected Boolean doInBackground(Object... vals) {
            
            Thread.currentThread().setName("Search");

            String srch = (String)vals[0];

            LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
            synchronized (SearchActivity.class) {
                /*
                 * This is not to be done repeatedly with new text input so sync.
                 */
                mService.getDBResource().search(srch, params, false);
                mService.getUDWMgr().search(srch, params);	// From user defined points of interest
                StringPreference s = mService.getDBResource().getUserRecent(srch);
                if (null != s) {
                    s.putInHash(params);
                }
                if(params.size() > 0) {
                    selection = new String[params.size()];
                    int iterator = 0;
                    for(String key : params.keySet()){
                        selection[iterator] = StringPreference.getHashedName(params.get(key), key);
                        iterator++;
                    }
                }
            }
            return true;
        }
        
        /* (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(Boolean result) {
            /*
             * Set new search adapter
             */

            if(null == selection) {
                return;
            }
            mAdapter = new SearchAdapter(SearchActivity.this, selection);
            mSearchListView.setAdapter(mAdapter);
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        }
    }

}