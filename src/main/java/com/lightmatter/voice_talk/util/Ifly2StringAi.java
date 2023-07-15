package com.lightmatter.voice_talk.util;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Ifly2StringAi {

    private static final String TAG = "Ifly2StringAi";

    private HashMap<String, String> mIatResults = new LinkedHashMap<>();

    // 语音听写对象
    private SpeechRecognizer mIat;

    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private String language = "zh_cn";
    private int selectedNum = 0;

    private String resultType = "plain";

    private StringBuffer buffer = new StringBuffer();

    private Context content;

    private TextView resultTextView;

    private Handler handler;

    private Listener volumeListener;

    public Ifly2StringAi(Context content, TextView resultTextView, Handler handler) {
        this.content = content;
        this.resultTextView = resultTextView;
        this.handler = handler;
    }

    public void initParameter() {
        mIat = SpeechRecognizer.createRecognizer(content, mInitListener);
        setParam();
    }

    public void setVolumeListener(Listener volumeListener) {
        this.volumeListener = volumeListener;
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = code -> {
        Log.d(TAG, "SpeechRecognizer init() code = " + code);
        if (code != ErrorCode.SUCCESS) {
            showTip("初始化失败，错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
        }
    };

    private void showTip(final String str) {

        Toast.makeText(content, str, Toast.LENGTH_SHORT).show();

    }

    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        if (Constant.SELECTED_LANGUAGE.equals(Constant.LANGUAGE_CHINESE)) {
            String lag = "mandarin";
            // 设置语言
            Log.e(TAG, "language = " + language);
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        } else {
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        }
        Log.e(TAG, "last language:" + mIat.getParameter(SpeechConstant.LANGUAGE));

        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav.
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                content.getExternalFilesDir("msc").getAbsolutePath() + "/iat.wav");
    }


    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            buffer = new StringBuffer();
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            Log.d(TAG, "onError " + error.getPlainDescription(true));
            showTip(error.getPlainDescription(true));

        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());
            if (isLast) {
                Log.d(TAG, "onResult 结束");
            }
            if (resultType.equals("json")) {
                Log.d(TAG, "onResult " + results);
                return;
            }
            if (resultType.equals("plain")) {
                buffer.append(results.getResultString());
                resultTextView.setText(buffer.toString());
//                mResultText.setSelection(mResultText.length());
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            double sum = 0;
            for (short s : data) {
                sum += s * s;
            }
            double rms = Math.sqrt(sum / data.length)+ Math.random()*20;
            if (volumeListener!=null){
                volumeListener.processVoiceDb(rms);
            }

            showTip("当前正在说话，音量大小 = " + volume + " 返回音频数据 = " + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };



    public void start2String(){
        // 不显示听写对话框
        int ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            showTip("听写失败,错误码：" + ret + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
        } else {
            showTip("听写失败,错误码");
        }
    }
    public interface Listener {
        void processVoiceDb(double db);
    }

}


