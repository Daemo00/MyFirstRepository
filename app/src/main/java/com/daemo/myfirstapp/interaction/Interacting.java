package com.daemo.myfirstapp.interaction;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;

import java.util.List;

public class Interacting extends MySuperActivity {

    private static final int PICK_CONTACT_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_interacting;
    }

    public void sendIntent(View v) {
        // Build the intent
        Uri location = Uri.parse("geo:0,0?q=1600+Amphitheatre+Parkway,+Mountain+View,+California");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);

        // Verify it resolves
        List<ResolveInfo> activities = getPackageManager().queryIntentActivities(mapIntent, 0);
        boolean isIntentSafe = activities.size() > 0; //same as mapIntent.resolveActivity(getPackageManager()) != null

        if (wantsChooser())
            mapIntent = Intent.createChooser(mapIntent, "I am a chooser");

        // Start an activity if it's safe
        if (isIntentSafe)
            startActivity(mapIntent);
    }

    public void pickContact(View v) {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
        if (wantsChooser())
            pickContactIntent = Intent.createChooser(pickContactIntent, "I am a chooser");

        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the URI that points to the selected contact
                Uri contactUri = data.getData();
                // We only need the NUMBER column, because there will be only one row in the result
                String[] projection = {
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                };
                Log.d(this.getClass().getSimpleName(), "contactUri is " + contactUri.toString());
                // Perform the query on the contact to get the NUMBER column
                // We don't need a selection or sort order (there's only one result for the given URI)
                // CAUTION: The query() method should be called from a separate thread to avoid blocking
                // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
                // Consider using CursorLoader to perform the query.
                try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
                    if (cursor != null) {
                        cursor.moveToFirst();

                        // Retrieve the phone number from the NUMBER column
                        String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        showToast("Number is " + number, Toast.LENGTH_SHORT);
                    }
                }
                // Do something with the phone number...
            } else {
                showToast("Result is " + resultCode, Toast.LENGTH_SHORT);
            }
        }
    }

    private boolean wantsChooser() {
        RadioGroup rgShowChooser = (RadioGroup) findViewById(R.id.rgShowChooser);
        switch (rgShowChooser.getCheckedRadioButtonId()) {
            case R.id.rbtnShowChooserYes:
                return true;
            case R.id.rbtnShowChooserNo:
                return false;
            default:
                return false;
        }
    }
}
