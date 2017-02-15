package com.daemo.myfirstapp.userinfo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.ListViewCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.daemo.myfirstapp.BuildConfig;
import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Utils;
import com.daemo.myfirstapp.graphics.displayingbitmaps.util.ImageFetcher;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import static com.daemo.myfirstapp.userinfo.Contract.CONTACT_ID_INDEX;
import static com.daemo.myfirstapp.userinfo.Contract.CONTACT_LOOKUP_KEY_INDEX;
import static com.daemo.myfirstapp.userinfo.Contract.CONTACT_NAME_INDEX;
import static com.daemo.myfirstapp.userinfo.Contract.DETAILS_PROJECTION;
import static com.daemo.myfirstapp.userinfo.Contract.DETAILS_QUERY_ID;
import static com.daemo.myfirstapp.userinfo.Contract.DETAILS_SELECTION;
import static com.daemo.myfirstapp.userinfo.Contract.FROM_COLUMNS;
import static com.daemo.myfirstapp.userinfo.Contract.LIST_ORDERING;
import static com.daemo.myfirstapp.userinfo.Contract.LIST_PROJECTION;
import static com.daemo.myfirstapp.userinfo.Contract.LIST_QUERY_ID;
import static com.daemo.myfirstapp.userinfo.Contract.LIST_SELECTION;
import static com.daemo.myfirstapp.userinfo.Contract.TO_IDS;
import static com.daemo.myfirstapp.userinfo.Contract.mSelectionArgs;

public class UserInfoActivity extends MySuperActivity implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {


    private ListView mContactsList;
    private SimpleCursorAdapter mCursorAdapter;
    private long mContactId;
    private String mContactLookupKey;
    private Uri mSelectedContactUri;
    private EditText mTextView;
    private String mContactName;
    private Intent editIntent;
    private ImageFetcher mImageFetcher;
    private LoaderManager loaderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR);

//        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        TypedValue value = new TypedValue();

        getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mImageFetcher = new ImageFetcher(this, Math.round(value.getDimension(metrics))) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return loadContactPhotoThumbnail((String) data, getImageSize());
            }
        };
        mImageFetcher.setLoadingImage(R.drawable.ic_contact_picture_holo_light);
        mImageFetcher.addImageCache(getMySuperApplication());
        // Gets the ListView from the View list of the parent activity
        mContactsList = (ListViewCompat) findViewById(android.R.id.list);
        // Gets a CursorAdapter
        mCursorAdapter = new SimpleCursorAdapter(
                this,
                R.layout.contact_list_item,
                null,
                FROM_COLUMNS, TO_IDS,
                0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                // Inflates the list item layout.
                final View itemLayout = LayoutInflater.from(getApplicationContext()).inflate(R.layout.contact_list_item, parent, false);

                // Creates a new ViewHolder in which to store handles to each view resource.
                // This allows bindView() to retrieve stored references instead of calling findViewById for each instance of the layout.
                final ViewHolder holder = new ViewHolder();
                holder.text1 = (TextView) itemLayout.findViewById(android.R.id.text1);
                holder.icon = (QuickContactBadge) itemLayout.findViewById(R.id.quickContactBadge);

                // Stores the resourceHolder instance in itemLayout.
                // This makes resourceHolder available to bindView and other methods that receive a handle to the item view.
                itemLayout.setTag(holder);

                // Returns the item layout view
                return itemLayout;
            }

            /**
             * A class that defines fields for each resource ID in the list item layout. This allows
             * ContactsAdapter.newView() to store the IDs once, when it inflates the layout, instead of
             * calling findViewById in each iteration of bindView.
             */
            class ViewHolder {
                TextView text1;
                QuickContactBadge icon;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                final ViewHolder holder = (ViewHolder) view.getTag();

                final String photoUri = cursor.getString(Contract.PHOTO_THUMBNAIL_INDEX);
                final String displayName = cursor.getString(Contract.CONTACT_NAME_INDEX);


                final Uri contactUri = ContactsContract.Contacts.getLookupUri(
                        cursor.getLong(Contract.CONTACT_ID_INDEX),
                        cursor.getString(Contract.CONTACT_LOOKUP_KEY_INDEX));

                Log.d(Utils.getTag(this), "Cursor is at position " + cursor.getPosition() + " of " + cursor.getCount());
                Log.d(Utils.getTag(this), "Name is " + displayName + " and photoUri is " + photoUri);

                holder.text1.setText(displayName);
                holder.icon.assignContactUri(contactUri);
                mImageFetcher.loadImage(photoUri, holder.icon);

//                holder.icon.setImageBitmap(loadContactPhotoThumbnail(photoUri));

                super.bindView(view, context, cursor);
            }
        };
        // Sets the adapter for the ListView
        mContactsList.setAdapter(mCursorAdapter);
        // Set the item click listener to be the current fragment.
        mContactsList.setOnItemClickListener(this);
        mContactsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause image loader to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    mImageFetcher.setPauseWork(true);
                } else {
                    mImageFetcher.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            }
        });
        mTextView = (EditText) findViewById(android.R.id.text1);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_user_info;
    }

    private Bitmap loadContactPhotoThumbnail(String photoData, int imageSize) {
        // Instantiates an AssetFileDescriptor. Given a content Uri pointing to an image file, the
        // ContentResolver can return an AssetFileDescriptor for the file.
        AssetFileDescriptor afd = null;

        // This "try" block catches an Exception if the file descriptor returned from the Contacts
        // Provider doesn't point to an existing file.
        try {
            Uri thumbUri;
            // If Android 3.0 or later, converts the Uri passed as a string to a Uri object.
            thumbUri = Uri.parse(photoData);
            // Retrieves a file descriptor from the Contacts Provider. To learn more about this
            // feature, read the reference documentation for
            // ContentResolver#openAssetFileDescriptor.
            afd = getContentResolver().openAssetFileDescriptor(thumbUri, "r");

            // Gets a FileDescriptor from the AssetFileDescriptor. A BitmapFactory object can
            // decode the contents of a file pointed to by a FileDescriptor into a Bitmap.
            FileDescriptor fileDescriptor = null;
            if (afd != null) {
                fileDescriptor = afd.getFileDescriptor();
            }

            if (fileDescriptor != null) {
                // Decodes a Bitmap from the image pointed to by the FileDescriptor, and scales it
                // to the specified width and height
                return ImageFetcher.decodeSampledBitmapFromDescriptor(
                        fileDescriptor, imageSize, imageSize);
            }
        } catch (FileNotFoundException e) {
            // If the file pointed to by the thumbnail URI doesn't exist, or the file can't be
            // opened in "read" mode, ContentResolver.openAssetFileDescriptor throws a
            // FileNotFoundException.
            if (BuildConfig.DEBUG) {
                Log.d(Utils.getTag(this), "Contact photo thumbnail not found for contact " + photoData
                        + ": " + e.toString());
            }
        } finally {
            // If an AssetFileDescriptor was returned, try to close it
            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException e) {
                    // Closing a file descriptor might cause an IOException if the file is
                    // already closed. Nothing extra is needed to handle this.
                }
            }
        }

        // If the decoding failed, returns null
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.users, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_contact:
                Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                // Sets the MIME type to match the Contacts Provider
                intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                intent.putExtra(ContactsContract.Intents.Insert.EMAIL, "email");
                //new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
                startActivity(intent);
                break;
            case R.id.edit_contact:
                if (editIntent == null) {
                    showToast("Select a contact");
                    break;
                }
                startActivity(editIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void buttonClick(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            checkPermissionsRunTime(Manifest.permission.READ_CONTACTS);
            return;
        }
        // Initializes the loader
//        getSupportLoaderManager().initLoader(0, null, this);
        loaderManager = getSupportLoaderManager();
        Loader<Cursor> cursorLoader = loaderManager.restartLoader(LIST_QUERY_ID, null, this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        // Get the Cursor
        Cursor cursor = ((SimpleCursorAdapter) adapterView.getAdapter()).getCursor();
        // Move to the selected contact
        cursor.moveToPosition(i);
        // Get the _ID value
        mContactId = cursor.getLong(CONTACT_ID_INDEX);
        // Get the selected LOOKUP KEY
        mContactLookupKey = cursor.getString(CONTACT_LOOKUP_KEY_INDEX);
        // Get the selected NAME
        mContactName = cursor.getString(CONTACT_NAME_INDEX);

        // Create the contact's content Uri
        mSelectedContactUri = ContactsContract.Contacts.getLookupUri(mContactId, mContactLookupKey);

        /*
         * You can use mSelectedContactUri as the content URI for retrieving
         * the details for a contact.
         */
        getSupportLoaderManager().restartLoader(DETAILS_QUERY_ID, null, this);
        // Creates a new Intent to edit a contact
        editIntent = new Intent(Intent.ACTION_EDIT);
        /*
         * Sets the contact URI to edit, and the data type that the
         * Intent must match
         */
        editIntent.setDataAndType(mSelectedContactUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LIST_QUERY_ID:
                mSelectionArgs[0] = "%" + mTextView.getText() + "%";
                return new CursorLoader(
                        this,
                        ContactsContract.Contacts.CONTENT_URI,
                        LIST_PROJECTION,
                        LIST_SELECTION,
                        mSelectionArgs,
                        LIST_ORDERING
                );

            case DETAILS_QUERY_ID:
                mSelectionArgs[0] = mContactName;
                return new CursorLoader(
                        this,
                        ContactsContract.Data.CONTENT_URI,
                        DETAILS_PROJECTION,
                        DETAILS_SELECTION,
                        mSelectionArgs,
                        null
                );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case LIST_QUERY_ID:
                mCursorAdapter.swapCursor(cursor);
                break;
            case DETAILS_QUERY_ID:
                StringBuilder dataStringBuilder = new StringBuilder();
                dataStringBuilder.append(cursor.getCount()).append(" contacts found\n");
                if (cursor.moveToFirst()) {
                    for (int r = 0; r < cursor.getCount(); r++, cursor.moveToNext()) {
                        StringBuilder dataRow = new StringBuilder();
                        for (int c = 0; c < cursor.getColumnCount(); c++) {
                            switch (cursor.getType(c)) {
                                case Cursor.FIELD_TYPE_BLOB:
                                    dataRow.append(Utils.capitalize(cursor.getColumnName(c))).append(": ").append(Arrays.toString(cursor.getBlob(c))).append("\n");
                                    break;
                                case Cursor.FIELD_TYPE_FLOAT:
                                    dataRow.append(Utils.capitalize(cursor.getColumnName(c))).append(": ").append(cursor.getFloat(c)).append("\n");
                                    break;
                                case Cursor.FIELD_TYPE_INTEGER:
                                    dataRow.append(Utils.capitalize(cursor.getColumnName(c))).append(": ").append(cursor.getInt(c)).append("\n");
                                    break;
                                case Cursor.FIELD_TYPE_STRING:
                                    dataRow.append(Utils.capitalize(cursor.getColumnName(c))).append(": ").append(cursor.getString(c)).append("\n");
                                    break;
                                default:
                            }
                        }
                        dataStringBuilder.append(dataRow).append("\n");
                    }
                } else {
                    dataStringBuilder = new StringBuilder("empty");
                }
                showOkCancelDialog("Selected contact: " + mContactName + ", " + dataStringBuilder.length() + " chars", dataStringBuilder.toString(), null);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LIST_QUERY_ID:
                mCursorAdapter.swapCursor(null);
                break;
            case DETAILS_QUERY_ID:
                break;
        }
    }
}