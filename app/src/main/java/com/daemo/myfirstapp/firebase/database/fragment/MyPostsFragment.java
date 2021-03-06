package com.daemo.myfirstapp.firebase.database.fragment;

import com.google.common.base.Strings;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class MyPostsFragment extends PostListFragment {

    public MyPostsFragment() {
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        String uid = getUid();
        return Strings.isNullOrEmpty(uid) ? null : databaseReference.child("user-posts").child(uid);
    }

    @Override
    public String getTitle() {
        return "My Posts";
    }
}
