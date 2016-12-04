package com.daemo.myfirstapp.userinfo;

import android.annotation.SuppressLint;
import android.os.Build;
import android.provider.ContactsContract;

import java.lang.reflect.Field;
import java.util.Arrays;

public class Contract {

    /*
         * Defines an array that contains column names to move from
         * the Cursor to the ListView.
         */
    @SuppressLint("InlinedApi")
    final static String[] FROM_COLUMNS = {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                    ContactsContract.Contacts.DISPLAY_NAME
    };
    /*
     * Defines an array that contains resource ids for the layout views
     * that get the Cursor column contents. The id is pre-defined in
     * the Android framework, so it is prefaced with "android.R.id"
     */
    final static int[] TO_IDS = {
            android.R.id.text1
    };

    @SuppressLint("InlinedApi")
    static final String[] DETAILS_PROJECTION = new String[]{};

    static {
        for (Field field : ContactsContract.Data.class.getFields())
            try {
                Arrays.fill(DETAILS_PROJECTION, field.get(null));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        Arrays.sort(DETAILS_PROJECTION, String.CASE_INSENSITIVE_ORDER);
    }
//            {
//                    ContactsContract.CommonDataKinds.Phone._ID,
//                    ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
//                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
//                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY :
//                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ,
//                    ContactsContract.CommonDataKinds.Phone.NUMBER,
//                    ContactsContract.CommonDataKinds.Phone.DATA1,
//                    ContactsContract.CommonDataKinds.Phone.DATA2,
//                    ContactsContract.CommonDataKinds.Phone.DATA3,
//                    ContactsContract.CommonDataKinds.Phone.DATA4,
//                    ContactsContract.CommonDataKinds.Phone.DATA5,
//                    ContactsContract.CommonDataKinds.Phone.DATA6,
//                    ContactsContract.CommonDataKinds.Phone.DATA7,
//                    ContactsContract.CommonDataKinds.Phone.DATA8,
//                    ContactsContract.CommonDataKinds.Phone.DATA9,
//                    ContactsContract.CommonDataKinds.Phone.DATA10,
//                    ContactsContract.CommonDataKinds.Phone.DATA11,
//                    ContactsContract.CommonDataKinds.Phone.DATA12,
//                    ContactsContract.CommonDataKinds.Phone.DATA13,
//                    ContactsContract.CommonDataKinds.Phone.DATA14,
//                    ContactsContract.CommonDataKinds.Phone.DATA15
//            };

    // The column index for the PHONE column
//    static final int PHONE_NUMBER_INDEX = 3;


    @SuppressLint("InlinedApi")
    static final String[] LIST_PROJECTION =
            {
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                            ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
            };

    // The column index for the _ID column
    static final int CONTACT_ID_INDEX = 0;

    // The column index for the LOOKUP_KEY column
    static final int CONTACT_LOOKUP_KEY_INDEX = 1;

    // The column index for the NAME column
    static final int CONTACT_NAME_INDEX = 2;

    final static int PHOTO_THUMBNAIL_INDEX = 3;

    // Defines the text expression
    @SuppressLint("InlinedApi")
    static final String LIST_SELECTION =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?" :
                    ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?";

    // Defines the text expression
    @SuppressLint("InlinedApi")
    static final String LIST_ORDERING =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                    ContactsContract.Contacts.DISPLAY_NAME;

    // Defines the text expression
    @SuppressLint("InlinedApi")
    static final String DETAILS_SELECTION =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Data.DISPLAY_NAME_PRIMARY + " = ?" :
                    ContactsContract.Data.DISPLAY_NAME + " = ?";

    // Defines a variable for the search string
    static String mSearchString;
    // Defines the array to hold values that replace the ?
    static String[] mSelectionArgs = {mSearchString};

    static final int LIST_QUERY_ID = 0;
    static final int DETAILS_QUERY_ID = 1;
}
