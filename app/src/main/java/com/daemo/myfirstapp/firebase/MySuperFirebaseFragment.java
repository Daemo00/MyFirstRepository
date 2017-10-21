package com.daemo.myfirstapp.firebase;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.daemo.myfirstapp.activities.MySuperFragment;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Constants;
import com.google.common.base.Strings;
import com.google.firebase.auth.FirebaseAuth;

public class MySuperFirebaseFragment extends MySuperFragment {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_firebase, menu);

        if (Strings.isNullOrEmpty(getUid())) {
            MenuItem logout = menu.findItem(R.id.menu_item_logout);
            logout.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_logout:
                FirebaseAuth.getInstance().signOut();
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Constants.ACTION_FIREBASE_LOGOUT));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getUid() {
        return getMySuperActivity().getUid();
    }
}