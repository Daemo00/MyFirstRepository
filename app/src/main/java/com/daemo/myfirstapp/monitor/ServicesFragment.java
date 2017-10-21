package com.daemo.myfirstapp.monitor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import com.daemo.myfirstapp.MySuperApplication;
import com.daemo.myfirstapp.activities.MySuperFragment;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Utils;

import java.util.ArrayList;
import java.util.HashMap;

import static com.daemo.myfirstapp.monitor.ProcessList.COLUMN_SERVICE_NAME;
import static com.daemo.myfirstapp.monitor.ProcessList.COLUMN_SERVICE_PID;

public class ServicesFragment extends MySuperFragment implements MonitorService.ServiceCallback {

    private static ServicesFragment inst = new ServicesFragment();
    private SimpleAdapter servicesAdapter;

    ArrayList<HashMap<String, Object>> servicesList = new ArrayList<>();

    public static ServicesFragment getInstance() {
        return inst == null ? inst = new ServicesFragment() : inst;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ListViewCompat lvc = new ListViewCompat(getContext());
        createAdapter(lvc);

        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            viewGroup.addView(lvc);
            return viewGroup;
        }
        return lvc;
    }

    private void createAdapter(ListViewCompat listView) {
        servicesAdapter = new SimpleAdapter(getContext(),
                ((MySuperApplication) getActivity().getApplication()).getServicesList(),
                R.layout.my_two_line_listitem,
                new String[]
                        {
                                COLUMN_SERVICE_NAME,
                                COLUMN_SERVICE_PID,
                        },
                new int[]
                        {
                                android.R.id.text1,
                                android.R.id.text2
                        });

        listView.setAdapter(servicesAdapter);
    }

    public ServicesFragment() {
    }

    @Override
    public void onRefresh() {
        Log.d(Utils.getTag(this), "onRefresh");
        ((MonitorActivity) getActivity()).backgroundService.fillServicesList();
    }

    @Override
    public void sendResults(int resultCode, Bundle b) {
        servicesAdapter.notifyDataSetChanged();
        super.stopRefreshing();
    }
}
