package com.daemo.myfirstapp.firebase.database.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.firebase.database.models.Post;


public class PostViewHolder extends RecyclerView.ViewHolder {

    public Button undoButton;
    private TextView titleView;
    private TextView authorView;
    public ImageView starView;
    private TextView numStarsView;
    private TextView bodyView;

    public PostViewHolder(View itemView) {
        super(itemView);
        titleView = (TextView) itemView.findViewById(R.id.post_title);
        authorView = (TextView) itemView.findViewById(R.id.post_author);
        starView = (ImageView) itemView.findViewById(R.id.star);
        numStarsView = (TextView) itemView.findViewById(R.id.post_num_stars);
        bodyView = (TextView) itemView.findViewById(R.id.post_body);
        undoButton = (Button) itemView.findViewById(R.id.button_undo);
    }

    public void bindToPost(Post post, View.OnClickListener starClickListener, View.OnClickListener undoClickListener) {
        titleView.setText(post.title);
        authorView.setText(post.author);
        numStarsView.setText(String.valueOf(post.starCount));
        bodyView.setText(post.body);
        starView.setOnClickListener(starClickListener);
        undoButton.setOnClickListener(undoClickListener);
    }

    public void setupForUndo(boolean isUndo) {
        itemView.findViewById(R.id.post_layout).setAlpha(isUndo ? 0.5f : 1);
        itemView.findViewById(R.id.button_undo).setVisibility(isUndo ? View.VISIBLE : View.GONE);
    }
}
