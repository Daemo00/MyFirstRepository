package com.daemo.myfirstapp.savingData;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.daemo.myfirstapp.R;

public class SavingKeyValueData extends Fragment implements View.OnClickListener {

    private final String saved_key = SavingKeyValueData.class.getPackage().getName().concat(".saved_key");
    private final String file_save = SavingKeyValueData.class.getPackage().getName().concat(".saved_file");

    private SharedPreferences sharedPreferences = null;

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences == null ?
                sharedPreferences = getActivity().getSharedPreferences(file_save, Context.MODE_PRIVATE) :
                sharedPreferences;
    }

    private SharedPreferences preferences = null;

    public SharedPreferences getPreferences() {
        return preferences == null ?
                preferences = getActivity().getPreferences(Context.MODE_PRIVATE) :
                preferences;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saving_key_value_data, container, false);

        view.findViewById(R.id.btnSaveKeyValue).setOnClickListener(this);
        view.findViewById(R.id.btnLoadKeyValue).setOnClickListener(this);
        return view;
    }

    private SharedPreferences getSelectedPreferences() {
        RadioButton rbSharedPreferences = (RadioButton) getActivity().findViewById(R.id.rbSharedPreferences);
        if (rbSharedPreferences != null && rbSharedPreferences.isChecked())
            return getSharedPreferences();
        return getPreferences();
    }

    public void save(View view) {
        String valueToSave = "";
        EditText etValueToSave = (EditText) getActivity().findViewById(R.id.etValueToSave);
        if (etValueToSave != null) valueToSave = etValueToSave.getText().toString();

        SharedPreferences.Editor editor = getSelectedPreferences().edit();
        editor.putString(saved_key, valueToSave);
        if (editor.commit())
            Toast.makeText(getActivity(), R.string.success, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getActivity(), R.string.fail, Toast.LENGTH_SHORT).show();
    }

    public void load(View view) {
        String defaultValue = "";
        String savedValue = getSelectedPreferences().getString(saved_key, defaultValue);
        TextView tvValueLoaded = (TextView) getActivity().findViewById(R.id.tvValueLoaded);
        if (tvValueLoaded != null) tvValueLoaded.setText(savedValue);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSaveKeyValue:
                save(view);
                break;
            case R.id.btnLoadKeyValue:
                load(view);
                break;
        }
    }
}
