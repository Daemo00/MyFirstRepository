package com.daemo.myfirstapp.drag_drop;

import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Color;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;

import com.daemo.myfirstapp.common.Utils;

class MyDragEventListener implements View.OnDragListener {

    // This is the method that the system calls when it dispatches a drag event to the listener.
    public boolean onDrag(View view, DragEvent event) {
        DragDropActivity activity = (DragDropActivity) view.getContext();
        ImageView imageView = (ImageView) view;
        // Defines a variable to store the action type for the incoming event
        final int action = event.getAction();

        // Handles each of the expected events
        switch (action) {

            case DragEvent.ACTION_DRAG_STARTED:

                // Determines if this View can accept the dragged data
                if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    imageView.setImageDrawable(null);
                    // As an example of what your application might do, applies a blue color tint to the View to indicate that it can accept data.
                    imageView.setBackgroundColor(Color.BLUE);

                    // Invalidate the view to force a redraw in the new tint
                    imageView.invalidate();

                    // returns true to indicate that the View can accept the dragged data.
                    return true;
                }

                // Returns false.
                // During the current drag and drop operation, this View will not receive events again until ACTION_DRAG_ENDED is sent.
                return false;

            case DragEvent.ACTION_DRAG_ENTERED:

                // Applies a green tint to the View.
                // Return true; the return value is ignored.

                imageView.setBackgroundColor(Color.GREEN);

                // Invalidate the view to force a redraw in the new tint
                imageView.invalidate();

                return true;

            case DragEvent.ACTION_DRAG_LOCATION:
//                 v.setText("(" + event.getX() + ", " + event.getY() + ")");
                // Ignore the event
                return true;

            case DragEvent.ACTION_DRAG_EXITED:

                // Re-sets the color tint to blue. Returns true; the return value is ignored.
                imageView.setBackgroundColor(Color.BLUE);

                // Invalidate the view to force a redraw in the new tint
                imageView.invalidate();

                return true;

            case DragEvent.ACTION_DROP:

                // Gets the item containing the dragged data
                ClipData.Item item = event.getClipData().getItemAt(0);

                // Gets the text data from the item.
                CharSequence dragData = item.getText();

                activity.mImageFetcher.loadImage(dragData, (ImageView) view);

                // Invalidates the view to force a redraw
//                v.invalidate();

                // Returns true. DragEvent.getResult() will return true.
                return true;

            case DragEvent.ACTION_DRAG_ENDED:

                // Turns off any color tinting
//                v.setBackground(null);

                // Invalidates the view to force a redraw
//                v.invalidate();

                // Does a getResult(), and displays what happened.
                if (event.getResult())
                    activity.showToast("The drop was handled, " + imageView.getId());
                else activity.showToast("The drop didn't work, " + imageView.getId());

                // returns true; the value is ignored.
                return true;

            // An unknown action type was received.
            default:
                Log.e(Utils.getTag(this), "Unknown action type received by OnDragListener.");
                break;
        }

        return false;
    }
}
