package com.daemo.myfirstapp.savingData;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.daemo.myfirstapp.R;

public class SavingKeyValueData extends AppCompatActivity {

    private final String saved_key = SavingKeyValueData.class.getPackage().getName().concat(".saved_key");
    private final String file_save = SavingKeyValueData.class.getPackage().getName().concat(".saved_file");

    private SharedPreferences sharedPreferences = null;

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences == null ?
                sharedPreferences = getSharedPreferences(file_save, MODE_PRIVATE) :
                sharedPreferences;
    }

    private SharedPreferences preferences = null;

    public SharedPreferences getPreferences() {
        return preferences == null ?
                preferences = getPreferences(MODE_PRIVATE) :
                preferences;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_key_value_data);
    }

    private SharedPreferences getSelectedPreferences() {
        RadioButton rbSharedPreferences = (RadioButton) findViewById(R.id.rbSharedPreferences);
        if (rbSharedPreferences != null && rbSharedPreferences.isChecked())
            return getSharedPreferences();
        return getPreferences();
    }

    public void save(View view) {
        String valueToSave = "";
        EditText etValueToSave = (EditText) findViewById(R.id.etValueToSave);
        if (etValueToSave != null) valueToSave = etValueToSave.getText().toString();

        SharedPreferences.Editor editor = getSelectedPreferences().edit();
        editor.putString(saved_key, valueToSave);
        if (editor.commit())
            Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, R.string.fail, Toast.LENGTH_SHORT).show();
    }

    public void load(View view) {
        String defaultValue = "";
        String savedValue = getSelectedPreferences().getString(saved_key, defaultValue);
        TextView tvValueLoaded = (TextView) findViewById(R.id.tvValueLoaded);
        if (tvValueLoaded != null) tvValueLoaded.setText(savedValue);
    }
}
