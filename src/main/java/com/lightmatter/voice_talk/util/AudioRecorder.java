package com.lightmatter.voice_talk.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class AudioRecorder {
    private static final int SAMPLE_RATE = 16000; // 采样率
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private File outFile;
    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void start(File file) {
        isRecording = true;
        outFile = file;
        new Thread(() -> {
            try {
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                                AudioFormat.ENCODING_PCM_16BIT));
                audioRecord.startRecording();

                writeAudioDataToFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stop() {
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    private void writeAudioDataToFile() {
        byte[] buffer = new byte[AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT)];
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (isRecording) {
            int read = audioRecord.read(buffer, 0, buffer.length);

            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                try {
                    fos.write(buffer);
                    postVoice(buffer,read);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void postVoice(byte[] buffer, int len){
        double sum = 0;
        for (short s : buffer) {
            sum += s * s;
        }
        double rms = Math.sqrt(sum / len)+ Math.random()*30;
        listener.processVoiceDb(rms);
    }

    public interface Listener {
         void processVoiceDb(double db);
    }
}
