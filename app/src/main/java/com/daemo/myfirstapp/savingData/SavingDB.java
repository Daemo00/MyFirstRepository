package com.daemo.myfirstapp.savingData;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.savingData.DBUtils.FeedReaderContract.FeedEntry;
import com.daemo.myfirstapp.savingData.DBUtils.FeedReaderDbHelper;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SavingDB extends Fragment {

    private SQLiteOpenHelper mDbHelper;
    private View root;
    private Map<Long, String[]> rowsMap = new HashMap<>();
    private SavingActivity savingActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savingActivity = (SavingActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_saving_db, container, false);
        mDbHelper = new FeedReaderDbHelper(getContext());

        bindGridView();
        return root;
    }

    Object[] ee; //maps progressive -> _id of table

    private void bindGridView() {
        ListView listView = (ListView) root.findViewById(R.id.DBList);

        readDBrow();
        final Object[] elements = ee;
        Log.d(this.getClass().getSimpleName(), "keyset has " + rowsMap.keySet().toArray().length + " elements");
        //noinspection unchecked
        ArrayAdapter adapter = new ArrayAdapter(savingActivity, R.layout.my_two_line_listitem, elements) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if (convertView == null) {
                    // inflate layout
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.my_two_line_listitem, parent, false);
                }

                TextView tvTitle = (TextView) convertView.findViewById(android.R.id.text1);
                if (tvTitle != null) tvTitle.setText(rowsMap.get(elements[position])[0]);

                TextView tvSubTitle = (TextView) convertView.findViewById(android.R.id.text2);
                if (tvSubTitle != null) tvSubTitle.setText(rowsMap.get(elements[position])[2]);

                return convertView;
            }
        };

        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                showRowOptions((Long) elements[position]);
            }
        });
    }

    private void showRowOptions(final Long rowId) {
        final Dialog dialog = new Dialog(savingActivity);
        dialog.setContentView(R.layout.db_row_details);
        //use ee
        String[] row = rowsMap.get(rowId);
        if (row == null) row = new String[]{};

        String title = row[0];
        ((EditText) dialog.findViewById(R.id.editText)).setText(title);
        String subtitle = row[1];
        ((EditText) dialog.findViewById(R.id.editText2)).setText(subtitle);
        final String updated = row[2];
        ((EditText) dialog.findViewById(R.id.editText3)).setText(updated);

        Button btnCancel = (Button) dialog.findViewById(R.id.btnDBCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button btnUpdate = (Button) dialog.findViewById(R.id.btnDBUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newTitle = ((EditText) dialog.findViewById(R.id.editText)).getText().toString();
                String newSubTitle = ((EditText) dialog.findViewById(R.id.editText2)).getText().toString();
                savingActivity.showToast("Updated " + updateDBrow(rowId, newTitle, newSubTitle) + " row(s)");

                bindGridView();
                dialog.dismiss();
            }
        });

        Button btnDelete = (Button) dialog.findViewById(R.id.btnDBDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDBrow(rowId);
                bindGridView();
                dialog.dismiss();
            }
        });

        Button btnInsert = (Button) dialog.findViewById(R.id.btnDBInsert);
        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newTitle = ((EditText) dialog.findViewById(R.id.editText)).getText().toString();
                String newSubTitle = ((EditText) dialog.findViewById(R.id.editText2)).getText().toString();

                insertDBrow(newTitle, newSubTitle);
                bindGridView();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public Long insertDBrow(String title, String subtitle) {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_TITLE, title);
        values.put(FeedEntry.COLUMN_NAME_SUBTITLE, subtitle);
        values.put(FeedEntry.COLUMN_NAME_UPDATED, dateFormat.format(new Date()));

        // Insert the new row, returning the primary key value of the new row
        return db.insert(
                FeedEntry.TABLE_NAME,
                null,
                values);
    }

    public void readDBrow() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                FeedEntry._ID,
                FeedEntry.COLUMN_NAME_TITLE,
                FeedEntry.COLUMN_NAME_SUBTITLE,
                FeedEntry.COLUMN_NAME_UPDATED
        };
        String selection = "";
        String[] selectionArgs = new String[]{};
        String groupBy = "";
        String having = "";
        String sortOrder = FeedEntry.COLUMN_NAME_UPDATED + " DESC";

        rowsMap.clear();
        try (Cursor cursor = db.query(
                FeedEntry.TABLE_NAME,  // The table to query
                projection,            // The columns to return
                selection,             // The columns for the WHERE clause
                selectionArgs,         // The values for the WHERE clause
                groupBy,               // don't group the rowsMap
                having,                // don't filter by row groups
                sortOrder              // The sort order
        )) {
            if (cursor.moveToFirst()) {
                long id;
                String title, subtitle, updated;
                do {
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(FeedEntry._ID));
                    title = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_TITLE));
                    subtitle = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_SUBTITLE));
                    updated = cursor.getString(cursor.getColumnIndexOrThrow(FeedEntry.COLUMN_NAME_UPDATED));

                    Log.d(this.getClass().getSimpleName(), "READING:\n id: " + id + ", title: " + title + ", subtitle: " + subtitle + ", updated: " + updated);
                    rowsMap.put(id, new String[]{title, subtitle, updated});
                } while (cursor.moveToNext());
            }
            ee = rowsMap.keySet().toArray();
            Arrays.sort(ee);
        }
    }

    public int deleteDBrow(Long rowId) {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Define 'where' part of query.
        String selection = FeedEntry._ID + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {rowId.toString()};
        // Issue SQL statement.
        return db.delete(FeedEntry.TABLE_NAME, selection, selectionArgs);
    }

    public int updateDBrow(Long rowId, String title, String subtitle) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_TITLE, title);
        values.put(FeedEntry.COLUMN_NAME_SUBTITLE, subtitle);
        values.put(FeedEntry.COLUMN_NAME_UPDATED, dateFormat.format(new Date()));

        // Which row to update, based on the ID
        String selection = FeedEntry._ID + " LIKE ?";
        String[] selectionArgs = {rowId.toString()};

        return db.update(
                FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }
}