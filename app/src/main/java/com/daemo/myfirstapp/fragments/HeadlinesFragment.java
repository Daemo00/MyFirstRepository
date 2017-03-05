package com.daemo.myfirstapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.daemo.myfirstapp.common.Constants;
import com.daemo.myfirstapp.common.Utils;

public class HeadlinesFragment extends ListFragment {
    OnHeadlineSelectedListener mCallback;

    // The container Activity must implement this interface so the frag can deliver messages
    interface OnHeadlineSelectedListener {
        void onArticleSelected(int position);
    }

    private static HeadlinesFragment inst;

    public static HeadlinesFragment getInstance(Bundle args) {
        if (inst == null)
            inst = new HeadlinesFragment();
        inst.setArguments(args);
        return inst;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This makes sure that the container activity has implemented the callback interface. If not, it throws an exception.
        if (context instanceof OnHeadlineSelectedListener)
            mCallback = (OnHeadlineSelectedListener) context;
        else
            throw new ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // We need to use a different list item layout for devices older than Honeycomb
        int layout = Utils.hasHoneycomb() ?
                android.R.layout.simple_list_item_activated_1 : android.R.layout.simple_list_item_1;
        // Create an array adapter for the list view, using the Ipsum headlines array
        setListAdapter(new ArrayAdapter<>(getActivity(), layout, Ipsum.Headlines));
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // When in two-pane layout, set the ListView to highlight the selected list item (We do this during onStart because at this point the ListView is available.)
        if (!((FragmentsActivity) getActivity()).isMonoFragment()) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            int article = savedInstanceState == null ? 0 : savedInstanceState.getInt(Constants.ARTICLE_SELECTED);
            onListItemClick(getListView(), getListView().getSelectedView(),
                    article,
                    getListView().getSelectedItemId());
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Notify the parent activity of selected item
        mCallback.onArticleSelected(position);
    }

    void select(int position) {
        // Set the item as checked be highlighted when in two-pane layout
        getListView().setItemChecked(position, true);
    }
}