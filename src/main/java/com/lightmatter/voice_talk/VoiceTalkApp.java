package com.lightmatter.voice_talk;

import android.app.Application;

import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechUtility;

public class VoiceTalkApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SpeechUtility.createUtility(VoiceTalkApp.this, "appid=9415cd0f");
//         以下语句用于设置日志开关（默认开启），设置成false时关闭语音云SDK日志打印
        Setting.setShowLog(true);

    }
}
