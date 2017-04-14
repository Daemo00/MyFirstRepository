package com.daemo.myfirstapp.savingData;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.daemo.myfirstapp.MySuperFragment;
import com.daemo.myfirstapp.R;

public class SavingKeyValueData extends MySuperFragment implements View.OnClickListener {

    private final String saved_key = SavingKeyValueData.class.getPackage().getName().concat(".saved_key");
    private final String file_save = SavingKeyValueData.class.getPackage().getName().concat(".saved_file");

    private SharedPreferences sharedPreferences = null;
    private static SavingKeyValueData instance;

    public static SavingKeyValueData getInstance() {
        return getInstance(new Bundle());
    }

    public static SavingKeyValueData getInstance(Bundle args) {
        if (instance == null) instance = new SavingKeyValueData();
        instance.setArguments(args);
        return instance;
    }

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
        return inflater.inflate(R.layout.fragment_saving_key_value_data, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnSaveKeyValue).setOnClickListener(this);
        view.findViewById(R.id.btnLoadKeyValue).setOnClickListener(this);
    }

    private SharedPreferences getSelectedPreferences() {
        RadioButton rbSharedPreferences = (RadioButton) getActivity().findViewById(R.id.rbtnSharedPreferences);
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
            getMySuperActivity().showToast(R.string.success);
        else
            getMySuperActivity().showToast(R.string.fail);
    }

    public void load(View view) {
        String savedValue = getSelectedPreferences().getString(saved_key, "");
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
