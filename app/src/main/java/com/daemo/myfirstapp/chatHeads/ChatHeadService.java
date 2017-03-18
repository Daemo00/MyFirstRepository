package com.daemo.myfirstapp.chatHeads;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.v4.os.ResultReceiver;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Constants;
import com.daemo.myfirstapp.common.Utils;

public class ChatHeadService extends Service {

    private WindowManager windowManager;
    private ImageView chatHead;
    WindowManager.LayoutParams params;
    private ResultReceiver resultReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Utils.getTag(this), "onStartCommand(" + Utils.debugIntent(intent) + ", " + flags + ", " + startId + ")");
        if (intent != null) resultReceiver = intent.getParcelableExtra(Constants.Location.RECEIVER);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (chatHead != null) {
            windowManager.removeView(chatHead);
            attachChatHead();
            return super.onStartCommand(intent, flags, startId);
        }

        chatHead = new ImageView(this);
        chatHead.setImageResource(R.drawable.empty_photo);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        // This code is for dragging the chat head
        chatHead.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX
                                + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY
                                + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(chatHead, params);
                        return true;
                }
                return false;
            }
        });
        attachChatHead();

        return super.onStartCommand(intent, flags, startId);
    }

    public void attachChatHead() {
        windowManager.addView(chatHead, params);
        Constants.SERVICE_RUNNING_CHATHEAD = true;
        if (resultReceiver != null) resultReceiver.send(Activity.RESULT_OK, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(Utils.getTag(this), "onDestroy");
        if (chatHead != null)
            windowManager.removeView(chatHead);
        Constants.SERVICE_RUNNING_CHATHEAD = false;
        if (resultReceiver != null) resultReceiver.send(Activity.RESULT_CANCELED, null);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(Utils.getTag(this), "onBind(" + Utils.debugIntent(intent) + ")");
        return null;
    }
}