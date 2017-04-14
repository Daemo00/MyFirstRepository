package com.daemo.myfirstapp.firebase.database.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class RecentPostsFragment extends PostListFragment {

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // Last 100 posts, these are automatically the 100 most recent due to sorting by push() keys
        return databaseReference.child("posts").limitToFirst(100);
    }

    @Override
    public String getTitle() {
        return "Recent";
    }
}