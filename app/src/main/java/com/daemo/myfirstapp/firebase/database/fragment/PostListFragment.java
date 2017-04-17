package com.daemo.myfirstapp.firebase.database.fragment;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daemo.myfirstapp.MySuperFragment;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Utils;
import com.daemo.myfirstapp.firebase.MySuperFirebaseFragment;
import com.daemo.myfirstapp.firebase.database.models.Post;
import com.daemo.myfirstapp.firebase.database.viewholder.PostViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.daemo.myfirstapp.firebase.database.fragment.PostDetailFragment.EXTRA_POST_KEY;

public abstract class PostListFragment extends MySuperFragment {

    private DatabaseReference mDatabase;
    private RecyclerView mRecyclerView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_all_posts, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.messages_list);
        setUpRecyclerView();
    }

    private void onStarClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) return Transaction.success(mutableData);

                if (p.stars.containsKey(getUid())) {
                    // Unstar the post and remove self from stars
                    p.starCount = p.starCount - 1;
                    p.stars.remove(getUid());
                } else {
                    // Star the post and add self to stars
                    p.starCount = p.starCount + 1;
                    p.stars.put(getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(Utils.getTag(this), "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    protected String getUid() {
        MySuperFirebaseFragment firebaseFragment = (MySuperFirebaseFragment) getParentFragment();
        return firebaseFragment.getUid();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRecyclerView.getAdapter() != null)
            ((PostListAdapter) mRecyclerView.getAdapter()).cleanup();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);

    private void setUpRecyclerView() {
        // Set up Layout Manager, reverse layout
        LinearLayoutManager mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(mDatabase);
        if (postsQuery == null) return;
        mRecyclerView.setAdapter(new PostListAdapter(Post.class, R.layout.item_post, PostViewHolder.class, postsQuery));
        mRecyclerView.setHasFixedSize(true);
        setUpItemTouchHelper();
        setUpAnimationDecoratorHelper();
    }

    /**
     * This is the standard support library way of implementing "swipe to delete" feature.
     * You can do custom drawing in onChildDraw method but whatever you draw will disappear once the swipe is over, and while the items are animating to their new position the recycler view background will be visible.
     * That is rarely an desired effect.
     */
    private void setUpItemTouchHelper() {

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            Drawable background;
            Drawable xMark;
            int xMarkMargin;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                xMark = ContextCompat.getDrawable(getContext(), android.R.drawable.ic_delete);
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                xMarkMargin = (int) getContext().getResources().getDimension(R.dimen.margin_medium);
                initiated = true;
            }

            // not important, we don't want drag & drop
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                PostListAdapter postListAdapter = (PostListAdapter) recyclerView.getAdapter();
                if (postListAdapter.isUndoOn() && postListAdapter.isPendingRemoval(position))
                    return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int swipedPosition = viewHolder.getAdapterPosition();
                PostListAdapter adapter = (PostListAdapter) mRecyclerView.getAdapter();
                boolean undoOn = adapter.isUndoOn();
                if (undoOn) adapter.pendingRemoval(swipedPosition);
                else adapter.remove(swipedPosition);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                // not sure why, but this method gets called for viewHolders that are already swiped away
                // not interested in those
                if (viewHolder.getAdapterPosition() < 0) return;

                if (!initiated) init();

                // draw red background
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                // draw x mark
                int itemHeight = itemView.getBottom() - itemView.getTop();
                int intrinsicWidth = xMark.getIntrinsicWidth();
                int intrinsicHeight = xMark.getIntrinsicWidth();

                int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                int xMarkRight = itemView.getRight() - xMarkMargin;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int xMarkBottom = xMarkTop + intrinsicHeight;
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);

                xMark.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };

        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    /**
     * We're gonna setupForUndo another ItemDecorator that will draw the red background in the empty space while the items are animating to their new positions after an item is removed.
     */
    private void setUpAnimationDecoratorHelper() {
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            // we want to cache this and not allocate anything repeatedly in the onDraw method
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                initiated = true;
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

                if (!initiated) init();

                // Only if animation is in progress
                if (parent.getItemAnimator().isRunning()) {

                    // Some items might be animating down and some items might be animating up to close the gap left by the removed item this is not exclusive, both movement can be happening at the same time to reproduce this leave just enough items so the first one and the last one would be just a little off screen then remove one from the middle.

                    // Find first child with translationY > 0 and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    // this is fixed
                    int left = 0;
                    int right = parent.getWidth();

                    // this we need to find out
                    int top = 0;
                    int bottom = 0;

                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = childCount - 1; i >= 0; i--) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(c);

                }
                super.onDraw(c, parent, state);
            }

        });
    }

    /**
     * RecyclerView adapter enabling undo on a swiped away item.
     */
    private class PostListAdapter extends FirebaseRecyclerAdapter<Post, PostViewHolder> {

        private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3sec

        List<String> itemsPendingRemoval = new ArrayList<>();
        boolean undoOn = true; // is undo on, you can turn it on from the toolbar menu

        private Handler handler = new Handler(); // handler for running delayed runnables
        HashMap<String, Runnable> pendingRunnables = new HashMap<>(); // map of items to pending runnables, so we can cancel a removal if need be

        /**
         * @param modelClass      Firebase will marshall the data at a location into an instance of a class that you provide
         * @param modelLayout     This is the layout used to represent a single item in the list. You will be responsible for populating an
         *                        instance of the corresponding view with the data from an instance of modelClass.
         * @param viewHolderClass The class that hold references to all sub-views in an instance modelLayout.
         * @param ref             The Firebase location to watch for data changes. Can also be a slice of a location, using some
         *                        combination of {@code limit()}, {@code startAt()}, and {@code endAt()}.
         */
        PostListAdapter(Class<Post> modelClass, int modelLayout, Class<PostViewHolder> viewHolderClass, Query ref) {
            super(modelClass, modelLayout, viewHolderClass, ref);
        }

        @Override
        protected void populateViewHolder(final PostViewHolder viewHolder, final Post model, final int position) {
            // we need to show the "normal" state
            normalViewHolderSetup(viewHolder, model, position);

            String postKey = getRef(position).getKey();
            // We need to show the "undo" state of the row
            viewHolder.setupForUndo(itemsPendingRemoval.contains(postKey));
        }

        private void normalViewHolderSetup(PostViewHolder viewHolder, final Post model, final int position) {
            viewHolder.itemView.setVisibility(View.VISIBLE);
            viewHolder.undoButton.setVisibility(View.GONE);
            final DatabaseReference postRef = getRef(position);

            // Set click listener for the whole post view
            final String postKey = postRef.getKey();
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Launch PostDetailActivity
                    Bundle fragArgs = new Bundle();
                    fragArgs.putString(EXTRA_POST_KEY, postKey);
                    getMySuperActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, instantiate(getContext(), PostDetailFragment.class.getName(), fragArgs))
                            .addToBackStack("Showing post with key: " + postKey)
                            .commit();
                }
            });

            // Determine if the current user has liked this post and set UI accordingly
            if (model.stars.containsKey(getUid()))
                viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
            else
                viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);

            // Bind Post to ViewHolder, setting OnClickListener for the star button
            viewHolder.bindToPost(model, new View.OnClickListener() {
                @Override
                public void onClick(View starView) {
                    // Need to write to both places the post is stored
                    DatabaseReference globalPostRef = mDatabase.child("posts").child(postRef.getKey());
                    DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());

                    // Run two transactions
                    onStarClicked(globalPostRef);
                    onStarClicked(userPostRef);
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // User wants to undo the removal, let's cancel the pending task
                    Runnable pendingRemovalRunnable = pendingRunnables.get(postKey);
                    pendingRunnables.remove(postKey);
                    if (pendingRemovalRunnable != null)
                        handler.removeCallbacks(pendingRemovalRunnable);
                    itemsPendingRemoval.remove(postKey);
                    // this will rebind the row in "normal" state
                    notifyItemChanged(position);
                }
            });
        }

        public void setUndoOn(boolean undoOn) {
            this.undoOn = undoOn;
        }

        boolean isUndoOn() {
            return undoOn;
        }

        void pendingRemoval(final int position) {
            final String postKey = getRef(position).getKey();
            if (!itemsPendingRemoval.contains(postKey)) {
                itemsPendingRemoval.add(postKey);
                // this will redraw row in "undo" state
                notifyItemChanged(position);
                // let's create, store and post a runnable to remove the item
                Runnable pendingRemovalRunnable = new Runnable() {
                    @Override
                    public void run() {
                        remove(position);
                    }
                };
                handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
                pendingRunnables.put(postKey, pendingRemovalRunnable);
            }
        }

        public void remove(int position) {
            String postKey = getRef(position).getKey();
            if (itemsPendingRemoval.contains(postKey)) {
                itemsPendingRemoval.remove(postKey);
                deletePost(postKey);
                getMySuperActivity().showToast("Item " + position + " 'removed'");

                notifyItemRemoved(position);
            }
        }

        boolean isPendingRemoval(int position) {
            String postKey = getRef(position).getKey();
            return itemsPendingRemoval.contains(postKey);
        }
    }

    private void deletePost(final String postKey) {
        mDatabase.child("posts").child(postKey).child("uid").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userKey = null;
                if (dataSnapshot.getValue(String.class) != null)
                    userKey = dataSnapshot.getValue(String.class);
                if (userKey == null) return;

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/posts/" + postKey, null);
                childUpdates.put("/user-posts/" + userKey + "/" + postKey, null);
                childUpdates.put("/post-comments/" + postKey, null);

                mDatabase.updateChildren(childUpdates);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}