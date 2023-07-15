package com.lightmatter.voice_talk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerDrawable;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.lightmatter.voice_talk.util.AudioRecorder;
import com.lightmatter.voice_talk.util.BaiduAi;
import com.lightmatter.voice_talk.util.ChatAi;
import com.lightmatter.voice_talk.util.Constant;
import com.lightmatter.voice_talk.util.Ifly2VoiceAi;

import java.io.File;

import static com.lightmatter.voice_talk.util.Constant.IS_PLAY_VOICE_KEY;
import static com.lightmatter.voice_talk.util.Constant.LANGUAGE_KEY;
import static com.lightmatter.voice_talk.util.Constant.ROLE_KEY;
import static com.lightmatter.voice_talk.util.Constant.ROLE_MAP;
import static com.lightmatter.voice_talk.util.Constant.SELECTED_IS_PLAY_VOICE;
import static com.lightmatter.voice_talk.util.Constant.SELECTED_LANGUAGE;
import static com.lightmatter.voice_talk.util.Constant.SELECTED_ROLE;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 100;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final String TAG = "AudioRecording";
    private static String AUDIO_FILE_PATH = "";

    private ShimmerFrameLayout shimmerLayout;
    private Button btnRecord;
    private TextView textView,textViewBg;
    private MenuItem itemStopPlay;
    private Handler pic_hdl;

    private View view ;
    private ImageView ivAnim;
    private LayerDrawable animation;
    private ImageView micImage;
   private  PopupWindow mPop;
   private  WindowManager.LayoutParams lp;

    private Ifly2VoiceAi ifly2VoiceAi;
    private boolean isStopPlay = false;

    private SharedPreferences preferences;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        shimmerLayout = findViewById(R.id.shimmerLayout);
        shimmerLayout.startShimmer();
        btnRecord = findViewById(R.id.btn_record);

        textViewBg = findViewById(R.id.textViewbg);
        textView = findViewById(R.id.textView);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        textView.setTextIsSelectable(true);
        textView.setText("----------->\n\n\n长按说话即可。\n");
        checkPermission();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);

        pic_hdl = new Handler(new ProcessHandler());
        obtainPreference();
        AudioRecorder myRecord = new AudioRecorder();
        myRecord.setListener(db -> {
            Message message = new Message();
            message.what = 0x005;
            message.obj = db;
            pic_hdl.sendMessage(message);
        });
        AUDIO_FILE_PATH = getFilesDir().getAbsolutePath() + "/audio" + ".pcm";
        btnRecord.setOnLongClickListener(v -> {
            if (ifly2VoiceAi != null && !isStopPlay) {
                ifly2VoiceAi.stopPlay();
                isStopPlay = true;
            }


            itemStopPlay.setVisible(false);
            // 长按事件响应
            myRecord.start(new File(AUDIO_FILE_PATH));
            //
            mPop.setWidth(500);
            mPop.setHeight(500);
            mPop.showAtLocation(view, Gravity.CENTER,0,0);

            return true;
        });
        btnRecord.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                myRecord.stop();
                pic_hdl.sendEmptyMessage(0x001);
                btnRecord.setEnabled(false);
            }
            if(mPop!=null){
                mPop.dismiss();
            }
            return false;
        });

        ifly2VoiceAi = new Ifly2VoiceAi(MainActivity.this);
        initVoiceDialog();
    }

    private void initVoiceDialog() {
        view = View.inflate(this, R.layout.popup_window, null);
        //设置空白的背景色
        lp = MainActivity.this.getWindow().getAttributes();
        mPop  = new PopupWindow(view);
        micImage= view.findViewById(R.id.iv_pro);


    }





    @Override
    protected void onResume() {
        super.onResume();
        ifly2VoiceAi.initIflyAiVoice();

    }

    private void obtainPreference() {
        if (preferences==null){
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
        } else {
            SELECTED_ROLE = preferences.getString(ROLE_KEY, SELECTED_ROLE);
            SELECTED_LANGUAGE = preferences.getString(LANGUAGE_KEY, SELECTED_LANGUAGE);
            SELECTED_IS_PLAY_VOICE = preferences.getBoolean(IS_PLAY_VOICE_KEY, SELECTED_IS_PLAY_VOICE);

        }
        Log.d(TAG, "obtainPreference: " + SELECTED_ROLE + "-" + SELECTED_LANGUAGE + "+" + SELECTED_IS_PLAY_VOICE);

    }

    private void checkPermission() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions, REQUEST_RECORD_AUDIO_PERMISSION);
            Log.d(TAG, "checkPermission: REQUEST_RECORD_AUDIO_PERMISSION ");
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
            Log.d(TAG, "checkPermission: WRITE_EXTERNAL_STORAGE  ");
        }
    }


    private void voice2String() {
        textView.setText("稍等一下哈。。。");
        new Thread(() -> {
            try {
                long start = System.currentTimeMillis();
                final String result = BaiduAi.postRaw(AUDIO_FILE_PATH);

                pic_hdl.post(() -> {
                    if (result == null || result.trim().equals("")) {
                        textView.setText("我走神了，你再说一次吧");
                        btnRecord.setEnabled(true);
                    } else {
                        textView.setText(result);
                        Message msg = new Message();
                        msg.what = 0x002;
                        msg.obj = result;
                        pic_hdl.sendMessage(msg);
                    }

                    Log.d(TAG, "stopRecording: use time:" + (System.currentTimeMillis() - start));
                });
            } catch (Exception e) {
                e.printStackTrace();
                pic_hdl.sendEmptyMessage(0x003);
            }
        }).start();
    }


    class ProcessHandler implements Handler.Callback {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 0x001:
                    voice2String();
                    break;
                case 0x002:
                    String content = (String) msg.obj;
                    new Thread(() -> {
                        final String result = ChatAi.chatResponse(generatePrefix() + content);
                        pic_hdl.post(() -> textView.setText(result));

                        Message iflyMsg = new Message();
                        iflyMsg.what = 0x004;
                        iflyMsg.obj = result;
                        pic_hdl.sendMessage(iflyMsg);
                    }).start();
                    break;
                case 0x003:
                    btnRecord.setEnabled(true);
                    break;
                case 0x004:
                    Log.d(TAG, "handleMessage: "+SELECTED_IS_PLAY_VOICE);
                    if (ifly2VoiceAi != null && SELECTED_IS_PLAY_VOICE) {
                        ifly2VoiceAi.startText2Voice((String) msg.obj);
                        itemStopPlay.setIcon(android.R.drawable.ic_lock_silent_mode_off);
                        itemStopPlay.setVisible(true);
                        isStopPlay = false;
                    }
                    pic_hdl.sendEmptyMessage(0x003);
                    break;
                case 0x005:
                    double db = (double) msg.obj;
                    if(micImage!=null){
                        micImage.getDrawable().setLevel((int) (3000 + 6000 * db / 100));
                    }
            }

            return false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        itemStopPlay = menu.findItem(R.id.action_play);
        View actionView = itemStopPlay.getActionView();
        if (actionView != null) {
            itemStopPlay.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }


    private boolean voiceFlag = true;
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.action_play:
                if (ifly2VoiceAi != null && !isStopPlay) {
                    ifly2VoiceAi.stopPlay();
                    isStopPlay = true;
                }
                if (voiceFlag){
                    item.setIcon(android.R.drawable.ic_lock_silent_mode);
                } else {
                    item.setIcon(android.R.drawable.ic_lock_silent_mode_off);
                }
                voiceFlag = !voiceFlag;
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private String generatePrefix() {
        if (SELECTED_LANGUAGE.equals(Constant.LANGUAGE_CHINESE)) {
            return ROLE_MAP.getOrDefault(SELECTED_ROLE, "") + Constant.CHINESE_PREFIX + Constant.PREFIX;
        } else {
            return ROLE_MAP.getOrDefault(SELECTED_ROLE, "") + Constant.ENGLISH_PREFIX + Constant.PREFIX;
        }
    }
}
