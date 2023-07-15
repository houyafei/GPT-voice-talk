package com.lightmatter.voice_talk;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.alibaba.fastjson2.JSON;
import com.lightmatter.voice_talk.model.Role;
import com.lightmatter.voice_talk.util.Constant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.lightmatter.voice_talk.util.Constant.ROLE_MAP;
import static com.lightmatter.voice_talk.util.Constant.SELECTED_IS_PLAY_VOICE;
import static com.lightmatter.voice_talk.util.Constant.SELECTED_LANGUAGE;
import static com.lightmatter.voice_talk.util.Constant.SELECTED_ROLE;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private Button saveRoleBtu, deleteRoleBtu;
    private Spinner roleSpinner, languageSpinner;

    private RadioGroup radioGroup;
    private RadioButton radioButPlayVoice, radioButOffVoice;
    private EditText roleName, roleContent;

    private ArrayAdapter<String> adapter4RoleSpinner;
    private ArrayAdapter<CharSequence> adapter4LanguageSpinner;

    private SharedPreferences preferences;

    private ArrayList<Role> myOwnRoleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        initView();

        obtainPreference();
    }

    private void obtainPreference() {
        SELECTED_LANGUAGE = preferences.getString(Constant.LANGUAGE_KEY, SELECTED_LANGUAGE);
        SELECTED_IS_PLAY_VOICE = preferences.getBoolean(Constant.IS_PLAY_VOICE_KEY, SELECTED_IS_PLAY_VOICE);

        languageSpinner.setSelection(adapter4LanguageSpinner.getPosition(SELECTED_LANGUAGE));
        radioGroup.check(SELECTED_IS_PLAY_VOICE ? R.id.id_radio_play : R.id.id_radio_off_play);

        updateRoleSpinner();
    }

    private void initView() {
        roleSpinner = findViewById(R.id.id_role_spinner);
        languageSpinner = findViewById(R.id.id_language_spinner);
        radioGroup = findViewById(R.id.id_voice_switch);
        radioButPlayVoice = findViewById(R.id.id_radio_play);
        radioButOffVoice = findViewById(R.id.id_radio_off_play);
        roleName = findViewById(R.id.id_role_name);
        roleContent = findViewById(R.id.id_role_content);
        saveRoleBtu = findViewById(R.id.btu_save_role);
        deleteRoleBtu = findViewById(R.id.id_delete_role);

        adapter4RoleSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        adapter4RoleSpinner.addAll(Constant.ROLE_MAP.keySet());
        roleSpinner.setAdapter(adapter4RoleSpinner);
        roleSpinner.setOnItemSelectedListener(roleSpinnerListener);


        adapter4LanguageSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        adapter4LanguageSpinner.add(Constant.LANGUAGE_CHINESE);
        adapter4LanguageSpinner.add(Constant.LANGUAGE_ENGLISH);
        languageSpinner.setAdapter(adapter4LanguageSpinner);
        languageSpinner.setOnItemSelectedListener(languageSpinnerListener);


        radioGroup.setOnCheckedChangeListener(radioCheckedListener);

        saveRoleBtu.setOnClickListener(ownRoleListener);
        deleteRoleBtu.setOnClickListener(ownRoleListener);
    }

    AdapterView.OnItemSelectedListener roleSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constant.ROLE_KEY, adapter4RoleSpinner.getItem(position));
            SELECTED_ROLE = adapter4RoleSpinner.getItem(position);
            editor.apply();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    AdapterView.OnItemSelectedListener languageSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String text = (String) adapter4LanguageSpinner.getItem(position);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constant.LANGUAGE_KEY, text);
            SELECTED_LANGUAGE = text;
            editor.apply();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    RadioGroup.OnCheckedChangeListener radioCheckedListener = (group, checkedId) -> {
        // checkedId为选中RadioButton的id
        boolean isPlayVoice = true;
        switch (checkedId) {
            case R.id.id_radio_play:
                isPlayVoice = true;
                break;
            case R.id.id_radio_off_play:
                isPlayVoice = false;
                break;
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constant.IS_PLAY_VOICE_KEY, isPlayVoice);
        SELECTED_IS_PLAY_VOICE = isPlayVoice;
        editor.apply();
    };


    OnClickListener ownRoleListener = v -> {
        switch (v.getId()){
            case R.id.id_delete_role:
                if (ROLE_MAP.keySet().contains(SELECTED_ROLE)) {
                    Toast.makeText(SettingsActivity.this, "系统角色不可删除", Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("删除操作")
                        .setMessage("确定要删除该项吗？")
                        .setPositiveButton("确定", (dialog, which) -> {
                            if (ROLE_MAP.keySet().contains(SELECTED_ROLE)) {
                                Toast.makeText(SettingsActivity.this, "系统角色不可删除", Toast.LENGTH_SHORT).show();
                            } else {
                                // 执行删除操作
                                adapter4RoleSpinner.remove(SELECTED_ROLE);
                                adapter4RoleSpinner.notifyDataSetChanged();
                                roleSpinner .setSelection( 0 , false );
                            }

                        })
                        .setNegativeButton("取消", null)
                        .show();

                break;
            case R.id.btu_save_role:
                String name = roleName.getText().toString();
                String content = roleContent.getText().toString();
                if (name.trim().isEmpty() || content.trim().isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "内容未正确填写", Toast.LENGTH_SHORT).show();
                } else {
                    Role role = new Role();
                    role.setRoleName(name);
                    role.setRoleContent(content);
                    myOwnRoleList.add(role);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(Constant.MY_ROLE_MAP_KEY, JSON.toJSONString(myOwnRoleList));
                    editor.apply();
                    updateRoleSpinner();
                    Toast.makeText(SettingsActivity.this, "OK 已经保存。", Toast.LENGTH_SHORT).show();

                }
                roleName.setText("");
                roleContent.setText("");
                break;
        }

    };

    private void updateRoleSpinner() {
        SELECTED_ROLE = preferences.getString(Constant.ROLE_KEY, SELECTED_ROLE);
        String ownRoleList = preferences.getString(Constant.MY_ROLE_MAP_KEY, null);
        if (ownRoleList == null) {
            adapter4RoleSpinner.clear();
            adapter4RoleSpinner.addAll(Constant.ROLE_MAP.keySet());

        } else {
            Log.d(TAG, "updateRoleSpinner: " + roleContent);
            myOwnRoleList = (ArrayList<Role>) JSON.parseArray(ownRoleList, Role.class);
            Set<String> roleNamesSet = new HashSet<>(Constant.ROLE_MAP.keySet());
            myOwnRoleList.forEach(r -> roleNamesSet.add(r.getRoleName()));
            adapter4RoleSpinner.clear();
            adapter4RoleSpinner.addAll(roleNamesSet);

        }
        roleSpinner.setSelection(adapter4RoleSpinner.getPosition(SELECTED_ROLE));
        adapter4RoleSpinner.notifyDataSetChanged();
    }
}