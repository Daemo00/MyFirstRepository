package com.daemo.myfirstapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.daemo.myfirstapp.MySuperApplication;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Utils;
import com.daemo.myfirstapp.search.MySuggestionProvider;
import com.daemo.myfirstapp.search.SearchableActivity;
import com.daemo.myfirstapp.settings.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.daemo.myfirstapp.common.Utils.debugIntent;

public abstract class MySuperActivity extends AppCompatActivity implements MySuperFragment.OnFragmentInteractionListener {
    public static final int MY_PERMISSIONS_REQUEST = 0;
    private List<Toast> toastList = new ArrayList<>();
    private List<AlertDialog> alertDialogList = new ArrayList<>();
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentManager.enableDebugLogging(true);
        handleIntent(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowTitleEnabled(true);
            bar.setTitle(this.getTitle() == null ? Utils.getTag(this) : this.getTitle());
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
        }
    }

    public MySuperApplication getMySuperApplication() {
        return (MySuperApplication) getApplication();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        setShareIntent(createDummyIntent());

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        // Assumes current activity is the searchable activity
        searchView.setQueryRefinementEnabled(true);
        searchView.setSubmitButtonEnabled(true);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchableActivity.class)));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        // Detect SearchView actions
        MenuItemCompat.OnActionExpandListener searchItemExpansionListener = new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                setItemsVisibility(menu, item, false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                setItemsVisibility(menu, item, true);
                invalidateOptionsMenu();
                return true;
            }
        };
        MenuItemCompat.setOnActionExpandListener(searchItem, searchItemExpansionListener);

        return super.onCreateOptionsMenu(menu);
    }

    private void setItemsVisibility(Menu menu, MenuItem exception, boolean visible) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != exception) item.setVisible(visible);
        }
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
        checkPermissionsRunTime(MY_PERMISSIONS_REQUEST, permissions);
    }

    public void checkPermissionsRunTime(final int requestCode, final String... permissions) {
        final Activity activity = this;
        boolean shouldShowStuff = false;
        for (String permission : permissions)
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                shouldShowStuff = true;
                break;
            }

        if (shouldShowStuff)
            showOkCancelDialog(getText(R.string.request_permission), "I really need " + TextUtils.join(", ", permissions) + " because bla bla",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(activity, permissions, requestCode);
                        }
                    }
            );
        else
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.d(Utils.getTag(this), "onRequestPermissionsResult(" + requestCode + ", " + Arrays.toString(permissions) + ", " + Arrays.toString(grantResults) + ")");
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

    public void showOkCancelDialog(final CharSequence title, final String message, final DialogInterface.OnClickListener okClickListener) {
        showOkCancelDialog(title, message, okClickListener, null);
    }

    public void showOkCancelDialog(final CharSequence title, final String message, final DialogInterface.OnClickListener okClickListener, final DialogInterface.OnClickListener cancelClickListener) {
        final Activity thisActivity = this;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(thisActivity)
                        .setMessage(message)
                        .setTitle(title)
                        .setPositiveButton("Ok", okClickListener)
                        .setNegativeButton("Cancel", cancelClickListener)
                        .setCancelable(false)
                        .create();
                alertDialog.show();
                alertDialogList.add(alertDialog);
            }
        });
    }

    public void showToast(final String message) {
        final Activity thisActivity = this;
        Log.v(Utils.getTag(this), "Toast shown: " + message);
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
        String msg = "Received result with code: " + requestCode + ", result: " + resultCode + " and data:\n";
        msg += debugIntent(data);
        showToast(msg);
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
            if (appData != null)
                jargon = appData.getBoolean(SearchableActivity.JARGON);
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
        showToast(getString(intMsg));
    }

    public void showOkDialog(final String title, final String message, final DialogInterface.OnClickListener okClickListener) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(MySuperActivity.this)
                        .setMessage(message)
                        .setTitle(title)
                        .setPositiveButton("Ok", okClickListener)
                        .setCancelable(false)
                        .create();
                alertDialog.show();
                alertDialogList.add(alertDialog);
            }
        });
    }

    @Override
    public void onFragmentInteraction(MySuperFragment fragment, Bundle bundle) {
        Log.d(Utils.getTag(this), "Bundle " + Utils.debugBundle(bundle) + " received from fragment: " + fragment);
    }

    private ProgressDialog mProgressDialog;

    public void showProgressDialog(final String caption) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog == null) {
                    mProgressDialog = new ProgressDialog(MySuperActivity.this);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setMessage(caption);
                }
                mProgressDialog.show();
            }
        });
    }

    public void hideProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
            }
        });
    }

    public String getUid() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    public void showProgressDialog() {
        showProgressDialog(getString(R.string.loading));
    }
}