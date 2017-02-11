package com.daemo.myfirstapp.monitor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.ListViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import com.daemo.myfirstapp.MySuperApplication;
import com.daemo.myfirstapp.MySuperFragment;
import com.daemo.myfirstapp.R;

import static com.daemo.myfirstapp.monitor.ProcessList.COLUMN_PROCESS_NAME;
import static com.daemo.myfirstapp.monitor.ProcessList.COLUMN_PROCESS_PID;

public class ProcessesFragment extends MySuperFragment implements MonitorService.ServiceCallback {

    private static ProcessesFragment inst;
    private SimpleAdapter processesAdapter;

    public static ProcessesFragment getInstance() {
        return inst == null ? inst = new ProcessesFragment() : inst;
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
        processesAdapter = new SimpleAdapter(getContext(),
                ((MySuperApplication) getActivity().getApplication()).getProcessesList(),
                R.layout.my_two_line_listitem,
                new String[]
                        {
                                COLUMN_PROCESS_NAME,
                                COLUMN_PROCESS_PID,
                        },
                new int[]
                        {
                                android.R.id.text1,
                                android.R.id.text2
                        });

        listView.setAdapter(processesAdapter);
    }

    public ProcessesFragment() {
    }

    @Override
    public void sendResults(int resultCode, Bundle b) {
        processesAdapter.notifyDataSetChanged();
    }
}
