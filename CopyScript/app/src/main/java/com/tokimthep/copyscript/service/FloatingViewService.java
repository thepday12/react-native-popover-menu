package com.tokimthep.copyscript.service;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.tokimthep.copyscript.R;
import com.tokimthep.copyscript.SharedPref;

import java.util.ArrayList;
import java.util.List;


public class FloatingViewService extends Service {

    private WindowManager mWindowManager;
    private View mFloatingView;
    private SharedPref sharedPref;
    private Gson gson = new Gson();
    private MyAsyncTask myAsyncTask;
    public FloatingViewService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        //Inflate the floating view layout we created
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);
        final WindowManager.LayoutParams params;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }


        //Specify the view position
        params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view will be added to top-left corner
        params.x = 0;
        params.y = 100;

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        mWindowManager.addView(mFloatingView, params);


        //Set the close button
        ImageView closeButtonCollapsed = mFloatingView.findViewById(R.id.close_btn);
        closeButtonCollapsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    myAsyncTask.cancel(true);
                }catch (Exception e){

                }
                //close the service and remove the from from the window
                stopSelf();
                onDestroy();
            }
        });


        //Drag and move floating view using user's touch action.
        mFloatingView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        //remember the initial position.
                        initialX = params.x;
                        initialY = params.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);

                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        });

        sharedPref.init(getApplicationContext());
        TextView tvCopyIndex = mFloatingView.findViewById(R.id.tvCopyIndex);
        myAsyncTask= new MyAsyncTask(tvCopyIndex);
        myAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
        try{
            myAsyncTask.cancel(true);
        }catch (Exception e){

        }
    }

    class MyAsyncTask extends AsyncTask<Void, Integer, Void> {
        private long timeWait;
        private List<String> copyList;
        private int copyIndex = 0;
        private  int maxIndex;
        private TextView tvCopyIndex;
        private  ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        public MyAsyncTask(TextView tvCopyIndex) {
            this.copyList = gson.fromJson(sharedPref.read(SharedPref.DATA_COPY, "[]"), ArrayList.class);
            this.timeWait = sharedPref.read(SharedPref.TIME_COPY, 5) * 1000;
            this.tvCopyIndex= tvCopyIndex;
            this.maxIndex = this.copyList.size() - 1;
        }


        @Override
        protected Void doInBackground(Void... params) {

            while (true) {
                try {
                    publishProgress(copyIndex);
                    Thread.sleep(timeWait);
                    if (copyIndex < maxIndex) {
                        copyIndex++;
                    } else {
                        copyIndex = 0;
                    }
                } catch (InterruptedException e) {
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int index = values[0];
            ClipData clip = ClipData.newPlainText("", copyList.get(index));
            clipboard.setPrimaryClip(clip);
            tvCopyIndex.setText(String.valueOf(index+1));
        }


    }
}