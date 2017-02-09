package com.daemo.myfirstapp;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.daemo.myfirstapp.common.Utils;
import com.daemo.myfirstapp.search.MySuggestionProvider;
import com.daemo.myfirstapp.search.SearchableActivity;
import com.daemo.myfirstapp.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

public abstract class MySuperActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST = 0;
    private List<Toast> toastList = new ArrayList<>();
    private List<AlertDialog> alertDialogList = new ArrayList<>();
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResID());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowTitleEnabled(true);
            bar.setTitle(Utils.getTag(this));
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
        }
        handleIntent(getIntent());
    }

    protected abstract int getLayoutResID();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        setShareIntent(createDummyIntent());

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_item_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setQueryRefinementEnabled(true);
        searchView.setSubmitButtonEnabled(true);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchableActivity.class)));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        // Return true to display menu
        return true;
    }

    private Intent createDummyIntent() {
        return (new Intent()).setAction(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, "dummy text");
    }

    // Call to update the share intent
    protected void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_item_share:
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_item_search:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void checkPermissionsRunTime(final String... permissions) {
        final Activity activity = this;
        boolean shouldShowStuff = false;
        for (String permission : permissions)
            shouldShowStuff = shouldShowStuff || ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
        if (shouldShowStuff)
            showOkCancelDialog("Pliiis", "I really need " + TextUtils.join(", ", permissions) + " because bla bla",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(activity, permissions, MY_PERMISSIONS_REQUEST);
                        }
                    }
            );
        else
            ActivityCompat.requestPermissions(activity, permissions, MY_PERMISSIONS_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                List<String> notGrantedPerms = new ArrayList<>();
                List<String> grantedPerms = new ArrayList<>();
                for (int i = 0; i < grantResults.length; i++)
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                        grantedPerms.add(permissions[i]);
                    else
                        notGrantedPerms.add(permissions[i]);

                String message = "";
                if (!notGrantedPerms.isEmpty())
                    message += "Permission(s) denied: " + TextUtils.join(", ", notGrantedPerms);
                if (!grantedPerms.isEmpty())
                    message += (message.isEmpty() ? "" : "\n") +
                            "Permission(s) granted: " + TextUtils.join(", ", grantedPerms);

                showToast(message);
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void showOkCancelDialog(final String title, final String message, final DialogInterface.OnClickListener clickListener) {
        final Activity thisActivity = this;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(thisActivity)
                        .setMessage(message)
                        .setTitle(title)
                        .setPositiveButton("Ok", clickListener)
                        .setNegativeButton("Cancel", null)
                        .create();
                alertDialog.show();
                alertDialogList.add(alertDialog);
            }
        });
    }

    public void showToast(final String message) {
        final Activity thisActivity = this;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(thisActivity, message, Toast.LENGTH_SHORT);
                toast.show();
                toastList.add(toast);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Show that something has been received
        showToast("Received result with code: " + requestCode + ", result: " + resultCode + " and data: " + data.toString());
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Bundle appData = getIntent().getBundleExtra(SearchManager.APP_DATA);
            Boolean jargon = null;
            if (appData != null) jargon = appData.getBoolean(SearchableActivity.JARGON);
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            doMySearch(SearchableActivity.JARGON + " = " + jargon + ", " + query);
        }
    }

    private void doMySearch(String query) {
        showToast("Searching " + query + "...");
    }

    @Override
    public boolean onSearchRequested() {
        showToast("Search started");
        return super.onSearchRequested();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Toast toast : toastList) toast.cancel();
        for (AlertDialog alertDialog : alertDialogList) alertDialog.cancel();
    }

    public void showToast(int intMsg) {
        showToast(getResources().getString(intMsg));
    }
}