package com.daemo.myfirstapp.firebase.database.models;

import com.google.firebase.database.IgnoreExtraProperties;

// [START comment_class]
@IgnoreExtraProperties
public class Comment {

    public String author;
    public String text;

    public Comment() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public Comment(String author, String text) {
        this.author = author;
        this.text = text;
    }

}
// [END comment_class]
