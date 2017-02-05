package com.krishna.javacv;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;
import java.util.Stack;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_reverse).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                reverseVideo(new File("/storage/emulated/0/DCIM/Camera/VID_20161229_124842.mp4"));
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void reverseVideo(File file) {
        final int NO_OF_FRAME = 2;
        Stack<org.bytedeco.javacv.Frame> frameStack = new Stack<>();
        FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(file.getAbsolutePath());
        try {
            FrameRecorder recorder = new FFmpegFrameRecorder("/storage/emulated/0/DCIM/Camera/reversed-VID_20161229_124842.mp4", 300, 300);
            recorder.setVideoCodec(13);
            recorder.setFrameRate(24);
            recorder.setFormat("mp4");
            recorder.setAudioChannels(1);
            recorder.start();
            frameGrabber.start();

            int length = frameGrabber.getLengthInFrames();

            int endOfCurBucket = length;
            int startOfCurBucket = length - NO_OF_FRAME;
            frameGrabber.setFrameNumber(startOfCurBucket);
            while (true) {
                try {
                    org.bytedeco.javacv.Frame captured_frame = frameGrabber.grab();
                    if (captured_frame != null) {
                        frameStack.push(captured_frame);
                    }
                    if (startOfCurBucket <= endOfCurBucket) {
                        while (!frameStack.isEmpty()) {
                            org.bytedeco.javacv.Frame frame = frameStack.pop();
                            recorder.record(frame);
                        }
                        endOfCurBucket = startOfCurBucket - 1;
                        startOfCurBucket = startOfCurBucket - NO_OF_FRAME;
                        if (startOfCurBucket < 1) break;
                        frameGrabber.setFrameNumber(startOfCurBucket);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            recorder.stop();
            recorder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
