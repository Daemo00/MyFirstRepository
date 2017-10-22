package com.daemo.myfirstapp.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Constants;
import com.daemo.myfirstapp.common.Utils;
import com.daemo.myfirstapp.firebase.FirebaseAuthenticationFragment;
import com.daemo.myfirstapp.firebase.database.FirebaseDatabaseFragment;
import com.daemo.myfirstapp.firebase.storage.FirebaseStorageFragment;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends MySuperActivity {

    private static final String TARGETS = "targets";
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private ArrayList<String> targets;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Arrays.asList(Constants.ACTION_FIREBASE_LOGIN, Constants.ACTION_FIREBASE_LOGOUT).contains(intent.getAction()))
                hideProgressDialog();
            try {
                fillRadioActivities();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            replaceFragment((MySuperFragment) Fragment.instantiate(getBaseContext(), FirebaseAuthenticationFragment.class.getName()), false);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                (Toolbar) findViewById(R.id.toolbar),  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        );

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        if (savedInstanceState == null)
            try {
                fillRadioActivities();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_FIREBASE_LOGIN));
        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.ACTION_FIREBASE_LOGOUT));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns true, then it has handled the app icon touch event
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    public void replaceFragment(MySuperFragment fragment, boolean addToBackStack) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment, Utils.getTag(fragment));
        if (addToBackStack) ft.addToBackStack("replace with " + Utils.getTag(fragment));
        ft.commit();

        // update selected item title, then close the drawer
        setTitle(fragment.getTitle());
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void onFragmentInteraction(MySuperFragment fragment, Bundle bundle) {
        Log.d(Utils.getTag(this), "Bundle " + Utils.debugBundle(bundle) + " received from fragment: " + fragment);
        String action = bundle.getString(Constants.ACTION_FRAGMENT);
        boolean addToBackstack = bundle.getBoolean(Constants.ACTION_ADD_TO_BACKSTACK);
        if (Constants.ACTION_REPLACE_FRAGMENT.equals(action))
            replaceFragment(fragment, addToBackstack);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(TARGETS, targets);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        targets = savedInstanceState.getStringArrayList(TARGETS);
        try {
            fillRadioActivities();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void fillRadioActivities() throws PackageManager.NameNotFoundException {
        ListView list_activities = (ListView) findViewById(R.id.list_activities);
        ActivityInfo[] activities = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES).activities;
        Arrays.sort(activities, new PackageItemInfo.DisplayNameComparator(getPackageManager()));

        if (targets == null) {
            targets = new ArrayList<>();
            for (ActivityInfo activityInfo : activities)
                if (activityInfo.parentActivityName != null && activityInfo.parentActivityName.equals(this.getClass().getName()))
                    targets.add(activityInfo.name);

            targets.add(FirebaseAuthenticationFragment.class.getName());
        }

        List<String> firebaseFragments = Arrays.asList(
                FirebaseDatabaseFragment.class.getName(),
                FirebaseStorageFragment.class.getName());

        if (Strings.isNullOrEmpty(getUid()) && targets.containsAll(firebaseFragments))
            targets.removeAll(firebaseFragments);
        else if (!Strings.isNullOrEmpty(getUid()) && !targets.containsAll(firebaseFragments))
            targets.addAll(firebaseFragments);

        final int list_item_layout = android.R.layout.simple_list_item_1;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, list_item_layout, targets) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                // inflate layout
                if (convertView == null)
                    convertView = LayoutInflater.from(getContext()).inflate(list_item_layout, parent, false);
                String target = getItem(position);

                TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
                if (tv != null && target != null)
                    tv.setText(target.substring(target.lastIndexOf(".") + 1));

                return convertView;
            }
        };

        list_activities.setAdapter(adapter);
        list_activities.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String selectedItem = (String) parent.getItemAtPosition(position);
                        try {
                            Class aClass = Class.forName(selectedItem);
                            aClass.asSubclass(Activity.class);
                            startActivity(new Intent().setComponent(new ComponentName(view.getContext(), aClass)));
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (ClassCastException e) {
                            replaceFragment((MySuperFragment) Fragment.instantiate(getBaseContext(), selectedItem), false);
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
        else
            showOkCancelDialog("Sure?", "Are you sure you want to exit?", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    MainActivity.this.finish();
                    MainActivity.super.onBackPressed();
                }
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(mBroadcastReceiver);
    }
}