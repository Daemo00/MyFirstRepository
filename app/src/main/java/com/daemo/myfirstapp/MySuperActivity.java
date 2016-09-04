package com.daemo.myfirstapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    protected abstract int getLayoutResID();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        setShareIntent(createDummyIntent());
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
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                startActivity(intent);
                return true;
            case R.id.menu_item_share:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void checkPermissionsRunTime(final String[] permissions) {
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

    protected AlertDialog showOkCancelDialog(String title, String message, DialogInterface.OnClickListener clickListener) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage(message)
                .setTitle(title)
                .setPositiveButton("Ok", clickListener)
                .setNegativeButton("Cancel", null)
                .create();
        alertDialog.show();
        alertDialogList.add(alertDialog);
        return alertDialog;
    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
        toastList.add(toast);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Toast toast : toastList) toast.cancel();
        for (AlertDialog alertDialog : alertDialogList) alertDialog.cancel();
    }
}
