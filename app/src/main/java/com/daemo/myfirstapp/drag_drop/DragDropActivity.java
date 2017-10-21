package com.daemo.myfirstapp.drag_drop;

import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.daemo.myfirstapp.activities.MySuperActivity;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Constants;
import com.daemo.myfirstapp.graphics.displayingbitmaps.provider.Images;
import com.daemo.myfirstapp.graphics.displayingbitmaps.util.ImageFetcher;

public class DragDropActivity extends MySuperActivity {

    ImageFetcher mImageFetcher;
    private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {

        // Defines the one method for the interface, which is called when the View is long-clicked
        public boolean onLongClick(View v) {

            // Create a new ClipData.
            // This is done in two steps to provide clarity.
            // The convenience method ClipData.newPlainText() can create a plain text ClipData in one step.

            // Create a new ClipData.Item from the ImageView object's tag
            ClipData.Item item = new ClipData.Item(Images.imageThumbUrls[0]);

            // Create a new ClipData using the tag as a label, the plain text MIME type, and the already-created item.
            // This will create a new ClipDescription object within the ClipData, and set its MIME type entry to "text/plain"
            ClipData dragData = new ClipData((CharSequence) v.getTag(), new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);

            // Instantiates the drag shadow builder.
            View.DragShadowBuilder myShadow = new MyDragShadowBuilder(v);

            // Starts the drag
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                v.startDragAndDrop(dragData,  // the data to be dragged
                        myShadow,  // the drag shadow builder
                        null,      // no need to use local data
                        0          // flags (not currently used, set to 0)
                );
            } else {
                //noinspection deprecation
                v.startDrag(dragData,
                        myShadow,
                        null,
                        0);
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_drop);
        setupImageFetcher();
        setupDraggableImageView((ImageView) findViewById(R.id.imageView_draggable));
        int[] ids = {R.id.NWQuadrant, R.id.NEQuadrant, R.id.SWQuadrant, R.id.SEQuadrant};
        for (int id : ids) findViewById(id).setOnDragListener(new MyDragEventListener());
    }

    private void setupImageFetcher() {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        mImageFetcher = new ImageFetcher(this, Math.round(displayMetrics.widthPixels / 4));
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        mImageFetcher.addImageCache(getMySuperApplication());
    }

    void setupDraggableImageView(ImageView imageView) {
        // Sets the tag
        imageView.setTag(Constants.IMAGE_TAG);
        // Sets the bitmap for the ImageView from an icon bit map (defined elsewhere)
        mImageFetcher.loadImage(Images.imageThumbUrls[0], imageView);

        // Sets a long click listener for the ImageView using an anonymous listener object that implements the OnLongClickListener interface
        imageView.setOnLongClickListener(onLongClickListener);
    }
}