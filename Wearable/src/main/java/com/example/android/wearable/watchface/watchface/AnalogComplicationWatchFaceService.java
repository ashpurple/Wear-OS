/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wearable.watchface.watchface;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.rendering.ComplicationDrawable;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.android.wearable.watchface.R;
import com.example.android.wearable.watchface.config.AnalogComplicationConfigActivity;
import com.example.android.wearable.watchface.config.AnalogComplicationConfigRecyclerViewAdapter;
import com.example.android.wearable.watchface.util.DigitalWatchFaceUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/** Demonstrates two simple complications in a watch face. */
public class AnalogComplicationWatchFaceService extends CanvasWatchFaceService implements DataClient.OnDataChangedListener {
    private static final String TAG = "AnalogWatchFace";
    public String datapath = "/data_path";
    public String userinfo = "";
    String why = "";
    // Unique IDs for each complication. The settings activity that supports allowing users
    // to select their complication data provider requires numbers to be >= 0.
    private static final int BACKGROUND_COMPLICATION_ID = 0;
    public int checking = 0;
    private static final int LEFT_COMPLICATION_ID = 100;
    private static final int RIGHT_COMPLICATION_ID = 101;
    public int checkingwifi = 0;
    public int checkingLTE = 0;
    public String urlStr = "http://15.164.45.229:8888/users/OUM6NUE6NDQ6Qjc6Qjk6OEU=";
    public static Context context;
    // Background, Left and right complication IDs as array for Complication API.
    private static final int[] COMPLICATION_IDS = {
            BACKGROUND_COMPLICATION_ID, LEFT_COMPLICATION_ID, RIGHT_COMPLICATION_ID
    };
    boolean mTimeZoneReceiverRegistered = false;
    private static final Typeface BOLD_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    Paint mBackgroundPaint;
    Paint mDatePaint;
    Paint mHourPaint;
    Paint mMinutePaint;
    Paint mSecondPaint;
    Paint mAmPmPaint;
    Paint mColonPaint;
    Paint mCirclePaint;
    Paint mMessagePaint;
    Paint mlocationPaint;
    float mColonWidth;
    boolean mMute;

    Calendar mCalendar;
    Date mDate;
    SimpleDateFormat mDayOfWeekFormat;
    java.text.DateFormat mDateFormat;
    private LocationListener locationListener;
    private LocationManager locationManager;
    boolean mShouldDrawColons;
    float mXOffset;
    float mYOffset;
    float mLineHeight;
    String mAmString;
    String mPmString;
    int mInteractiveBackgroundColor =
            DigitalWatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND;
    int mInteractiveHourDigitsColor =
            DigitalWatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_HOUR_DIGITS;
    int mInteractiveMinuteDigitsColor =
            DigitalWatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_MINUTE_DIGITS;
    int mInteractiveSecondDigitsColor =
            DigitalWatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_SECOND_DIGITS;

    /**
     * Whether the display supports fewer bits for each color in ambient mode. When true, we
     * disable anti-aliasing in ambient mode.
     */
    boolean mLowBitAmbient;

    // Left and right dial supported types.
    private static final int[][] COMPLICATION_SUPPORTED_TYPES = {
            {ComplicationData.TYPE_LARGE_IMAGE},
            {
                    ComplicationData.TYPE_RANGED_VALUE,
                    ComplicationData.TYPE_ICON,
                    ComplicationData.TYPE_SHORT_TEXT,
                    ComplicationData.TYPE_SMALL_IMAGE
            },
            {
                    ComplicationData.TYPE_RANGED_VALUE,
                    ComplicationData.TYPE_ICON,
                    ComplicationData.TYPE_SHORT_TEXT,
                    ComplicationData.TYPE_SMALL_IMAGE
            }
    };


    // Used by {@link AnalogComplicationConfigRecyclerViewAdapter} to check if complication location
    // is supported in settings config activity.
    public static int getComplicationId(
            AnalogComplicationConfigRecyclerViewAdapter.ComplicationLocation complicationLocation) {
        // Add any other supported locations here.
        switch (complicationLocation) {
            case BACKGROUND:
                return BACKGROUND_COMPLICATION_ID;
            case LEFT:
                return LEFT_COMPLICATION_ID;
            case RIGHT:
                return RIGHT_COMPLICATION_ID;
            default:
                return -1;
        }
    }

    // Used by {@link AnalogComplicationConfigRecyclerViewAdapter} to retrieve all complication ids.
    public static int[] getComplicationIds() {
        return COMPLICATION_IDS;
    }

    // Used by {@link AnalogComplicationConfigRecyclerViewAdapter} to see which complication types
    // are supported in the settings config activity.
    public static int[] getSupportedComplicationTypes(
            AnalogComplicationConfigRecyclerViewAdapter.ComplicationLocation complicationLocation) {
        // Add any other supported locations here.
        switch (complicationLocation) {
            case BACKGROUND:
                return COMPLICATION_SUPPORTED_TYPES[0];
            case LEFT:
                return COMPLICATION_SUPPORTED_TYPES[1];
            case RIGHT:
                return COMPLICATION_SUPPORTED_TYPES[2];
            default:
                return new int[]{};
        }
    }

    /*
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        Log.d(TAG, "onDataChanged: " + dataEventBuffer);
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (datapath.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    String message = dataMapItem.getDataMap().getString("message");
                    Log.v(TAG, "Wear activity received message: " + message);
                    // Display message in UI

                } else {
                    Log.e(TAG, "Unrecognized path: " + path);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.v(TAG, "Data deleted : " + event.getDataItem().toString());
            } else {
                Log.e(TAG, "Unknown data event Type = " + event.getType());
            }
        }
    }


    private class Engine extends CanvasWatchFaceService.Engine implements DataClient.OnDataChangedListener {
        private static final int MSG_UPDATE_TIME = 0;
        public int color3 = 0;
        public int color2 = 0;
        private static final float HOUR_STROKE_WIDTH = 5f;
        private static final float MINUTE_STROKE_WIDTH = 3f;
        private static final float SECOND_TICK_STROKE_WIDTH = 2f;
        static final String COLON_STRING = ":";
        private static final float CENTER_GAP_AND_CIRCLE_RADIUS = 4f;

        private static final int SHADOW_RADIUS = 6;

        private Calendar mCalendar;
        private boolean mRegisteredTimeZoneReceiver = false;
        private boolean mMuteMode;

        private float mCenterX;
        private float mCenterY;

        private float mSecondHandLength;
        private float mMinuteHandLength;
        private float mHourHandLength;

        private Bitmap mailon, mailoff;
        private Bitmap soson, sosoff;
        private Bitmap wifion, wifioff;

        // Colors for all hands (hour, minute, seconds, ticks) based on photo loaded.
        private int mWatchHandAndComplicationsColor;
        private int mWatchHandHighlightColor;
        private int mWatchHandShadowColor;
        private float TextSize;
        private int mBackgroundColor;
        public String datapath = "/data_path";
        private Paint mHourPaint;
        private Paint mMinutePaint;
        private Paint mSecondAndHighlightPaint;
        private Paint mTickAndCirclePaint;
        int Rightnum = 1;
        int Leftnum = 1;
        int SOSBUTTON = 0;
        private Paint mBackgroundPaint;

        /* Maps active complication ids to the data for that complication. Note: Data will only be
         * present if the user has chosen a provider via the settings activity for the watch face.
         */
        private SparseArray<ComplicationData> mActiveComplicationDataSparseArray;

        /* Maps complication ids to corresponding ComplicationDrawable that renders the
         * the complication data on the watch face.
         */
        private SparseArray<ComplicationDrawable> mComplicationDrawableSparseArray;

        private boolean mAmbient;
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;
        public Bitmap backgroundBitmap;
        // Used to pull user's preferences for background color, highlight color, and visual
        // indicating there are unread notifications.
        SharedPreferences mSharedPref;

        // User's preference for if they want visual shown to indicate unread notifications.
        private boolean mUnreadNotificationsPreference;
        private int mNumberOfUnreadNotifications = 0;

        private final BroadcastReceiver mTimeZoneReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        mCalendar.setTimeZone(TimeZone.getDefault());
                        initFormats();
                        invalidate();
                    }
                };

        @Override
        public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
            Log.d(TAG, "onDataChanged: " + dataEventBuffer);
            for (DataEvent event : dataEventBuffer) {
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    String path = event.getDataItem().getUri().getPath();
                    if (datapath.equals(path)) {
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        String message = dataMapItem.getDataMap().getString("message");
                        Log.v(TAG, "Wear activity received message: " + message);
                        // Display message in UI

                    } else {
                        Log.e(TAG, "Unrecognized path: " + path);
                    }
                } else if (event.getType() == DataEvent.TYPE_DELETED) {
                    Log.v(TAG, "Data deleted : " + event.getDataItem().toString());
                } else {
                    Log.e(TAG, "Unknown data event Type = " + event.getType());
                }
            }
        }

        // Handler to update the time once a second in interactive mode.
        private final Handler mUpdateTimeHandler =
                new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs =
                                    INTERACTIVE_UPDATE_RATE_MS
                                            - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                    }
                };

        Engine() {
            //  Ask for a hardware accelerated canvas.
            super(true);
        }

        @Override
        public void onCreate(SurfaceHolder holder) {
            Log.d(TAG, "onCreate");
            Log.d("on", "on");
            super.onCreate(holder);
            context = getApplicationContext();
            mDate = new Date();
            Resources resources = AnalogComplicationWatchFaceService.this.getResources();
            wifion = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.wifi_on);
            wifion = Bitmap.createScaledBitmap(wifion, 32, 32, true);
            wifioff = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.wifi_off);
            wifioff = Bitmap.createScaledBitmap(wifioff, 32, 32, true);
            //sosoff=BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.sos_off2);
            // soson=BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.sos_on2);
            int len = Math.round(mCenterX / 8f * 2);
           /** soson = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.sos_on2), 60, 60, true);
            sosoff = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.sos_off2), 60, 60, true);
            mailon = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.mail_on), 60, 60, true);
            mailoff = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.mail_off), 60, 60, true);
            **/
            soson = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.sos_on2), 50, 50, true);
            sosoff = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.sos_off2), 50, 50, true);
            mailon = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.mail_on), 50, 50, true);
            mailoff = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.mail_off), 50, 50, true);

            mYOffset = resources.getDimension(R.dimen.digital_y_offset);
            mLineHeight = resources.getDimension(R.dimen.digital_line_height);
            mAmString = resources.getString(R.string.digital_am);
            mPmString = resources.getString(R.string.digital_pm);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(mInteractiveBackgroundColor);
            mDatePaint = createTextPaint(
                    ContextCompat.getColor(getApplicationContext(), R.color.digital_date));

            mlocationPaint = createTextPaint(
                    ContextCompat.getColor(getApplicationContext(), R.color.digital_date));
            mHourPaint = createTextPaint(mInteractiveHourDigitsColor, BOLD_TYPEFACE);
            mMinutePaint = createTextPaint(mInteractiveMinuteDigitsColor);
            mSecondPaint = createTextPaint(mInteractiveSecondDigitsColor);
            mAmPmPaint = createTextPaint(
                    ContextCompat.getColor(getApplicationContext(), R.color.digital_am_pm));
            mColonPaint = createTextPaint(
                    ContextCompat.getColor(getApplicationContext(), R.color.digital_colons));
            mCirclePaint = createTextPaint(ContextCompat.getColor(getApplicationContext(), R.color.red_a200));
            initFormats();
            // Used throughout watch face to pull user's preferences.
            Context context = getApplicationContext();
            mSharedPref =
                    context.getSharedPreferences(
                            getString(R.string.analog_complication_preference_file_key),
                            Context.MODE_PRIVATE);

            mCalendar = Calendar.getInstance();

            setWatchFaceStyle(
                    new WatchFaceStyle.Builder(AnalogComplicationWatchFaceService.this)
                            .setAcceptsTapEvents(true)
                            .setHideNotificationIndicator(true)
                            .build());

            loadSavedPreferences();
            initializeComplicationsAndBackground();
            initializeWatchFace();
            ///////////////////////////////////////////////////////////


        }

        // Pulls all user's preferences for watch face appearance.
        private void loadSavedPreferences() {

            String backgroundColorResourceName =
                    getApplicationContext().getString(R.string.saved_background_color);

            mBackgroundColor = mSharedPref.getInt(backgroundColorResourceName, Color.BLACK);

            String markerColorResourceName =
                    getApplicationContext().getString(R.string.saved_marker_color);

            // Set defaults for colors
            mWatchHandHighlightColor = mSharedPref.getInt(markerColorResourceName, Color.RED);

            if (mBackgroundColor == Color.WHITE) {
                mWatchHandAndComplicationsColor = Color.BLACK;
                mWatchHandShadowColor = Color.WHITE;
            } else {
                mWatchHandAndComplicationsColor = Color.WHITE;
                mWatchHandShadowColor = Color.BLACK;
            }

            String unreadNotificationPreferenceResourceName =
                    getApplicationContext().getString(R.string.saved_unread_notifications_pref);

            mUnreadNotificationsPreference =
                    mSharedPref.getBoolean(unreadNotificationPreferenceResourceName, true);
        }

        private void initializeComplicationsAndBackground() {
            Log.d(TAG, "initializeComplications()");

            // Initialize background color (in case background complication is inactive).
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(mBackgroundColor);

            mActiveComplicationDataSparseArray = new SparseArray<>(COMPLICATION_IDS.length);

            // Creates a ComplicationDrawable for each location where the user can render a
            // complication on the watch face. In this watch face, we create one for left, right,
            // and background, but you could add many more.
            ComplicationDrawable leftComplicationDrawable =
                    new ComplicationDrawable(getApplicationContext());

            ComplicationDrawable rightComplicationDrawable =
                    new ComplicationDrawable(getApplicationContext());

            ComplicationDrawable backgroundComplicationDrawable =
                    new ComplicationDrawable(getApplicationContext());

            // Adds new complications to a SparseArray to simplify setting styles and ambient
            // properties for all complications, i.e., iterate over them all.
            //초기화면 기능들 붙혀주기
            mComplicationDrawableSparseArray = new SparseArray<>(COMPLICATION_IDS.length);

            mComplicationDrawableSparseArray.put(LEFT_COMPLICATION_ID, leftComplicationDrawable);
            mComplicationDrawableSparseArray.put(RIGHT_COMPLICATION_ID, rightComplicationDrawable);
            mComplicationDrawableSparseArray.put(
                    BACKGROUND_COMPLICATION_ID, backgroundComplicationDrawable);
            setComplicationsActiveAndAmbientColors(mWatchHandHighlightColor);
            setActiveComplications(COMPLICATION_IDS);
        }

        private void initializeWatchFace() {

            mHourPaint = new Paint();
            mHourPaint.setColor(mWatchHandAndComplicationsColor);
            mHourPaint.setStrokeWidth(HOUR_STROKE_WIDTH);
            mHourPaint.setAntiAlias(true);
            mHourPaint.setStrokeCap(Paint.Cap.ROUND);
            mHourPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mMinutePaint = new Paint();
            mMinutePaint.setColor(mWatchHandAndComplicationsColor);
            mMinutePaint.setStrokeWidth(MINUTE_STROKE_WIDTH);
            mMinutePaint.setAntiAlias(true);
            mMinutePaint.setStrokeCap(Paint.Cap.ROUND);
            mMinutePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mSecondAndHighlightPaint = new Paint();
            mSecondAndHighlightPaint.setColor(mWatchHandHighlightColor);
            mSecondAndHighlightPaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mSecondAndHighlightPaint.setAntiAlias(true);
            mSecondAndHighlightPaint.setStrokeCap(Paint.Cap.ROUND);
            mSecondAndHighlightPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mTickAndCirclePaint = new Paint();
            mTickAndCirclePaint.setColor(mWatchHandAndComplicationsColor);
            mTickAndCirclePaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mTickAndCirclePaint.setAntiAlias(true);
            mTickAndCirclePaint.setStyle(Paint.Style.STROKE);
            mTickAndCirclePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
        }

        /* Sets active/ambient mode colors for all complications.
         *
         * Note: With the rest of the watch face, we update the paint colors based on
         * ambient/active mode callbacks, but because the ComplicationDrawable handles
         * the active/ambient colors, we only set the colors twice. Once at initialization and
         * again if the user changes the highlight color via AnalogComplicationConfigActivity.
         */
        private void setComplicationsActiveAndAmbientColors(int primaryComplicationColor) {
            int complicationId;
            ComplicationDrawable complicationDrawable;

            for (int i = 0; i < COMPLICATION_IDS.length; i++) {
                complicationId = COMPLICATION_IDS[i];
                complicationDrawable = mComplicationDrawableSparseArray.get(complicationId);

                if (complicationId == BACKGROUND_COMPLICATION_ID) {
                    // It helps for the background color to be black in case the image used for the
                    // watch face's background takes some time to load.
                    complicationDrawable.setBackgroundColorActive(Color.BLACK);
                } else {
                    // Active mode colors.
                    complicationDrawable.setBorderColorActive(primaryComplicationColor);
                    complicationDrawable.setRangedValuePrimaryColorActive(primaryComplicationColor);

                    // Ambient mode colors.
                    complicationDrawable.setBorderColorAmbient(Color.WHITE);
                    complicationDrawable.setRangedValuePrimaryColorAmbient(Color.WHITE);
                }
            }
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            Log.d(TAG, "onPropertiesChanged: low-bit ambient = " + mLowBitAmbient);

            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);

            // Updates complications to properly render in ambient mode based on the
            // screen's capabilities.
            ComplicationDrawable complicationDrawable;

            for (int i = 0; i < COMPLICATION_IDS.length; i++) {
                complicationDrawable = mComplicationDrawableSparseArray.get(COMPLICATION_IDS[i]);

                complicationDrawable.setLowBitAmbient(mLowBitAmbient);
                complicationDrawable.setBurnInProtection(mBurnInProtection);
            }
        }

        /*
         * Called when there is updated data for a complication id.
         */
        @Override
        public void onComplicationDataUpdate(
                int complicationId, ComplicationData complicationData) {
            Log.d(TAG, "onComplicationDataUpdate() id: " + complicationId);

            // Adds/updates active complication data in the array.
            mActiveComplicationDataSparseArray.put(complicationId, complicationData);

            // Updates correct ComplicationDrawable with updated data.
            ComplicationDrawable complicationDrawable =
                    mComplicationDrawableSparseArray.get(complicationId);
            complicationDrawable.setComplicationData(complicationData);

            invalidate();
        }

        @SuppressLint("WrongConstant")
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            Log.d(TAG, "OnTapCommand()");
            Log.d(TAG, "TaPTYPE" + String.valueOf(tapType));

            Log.d(TAG, "x : " + String.valueOf(x) + "  y  :  " + String.valueOf(y));
            switch (tapType) {
                case TAP_TYPE_TAP:
                    // If your background complication is the first item in your array, you need
                    // to walk backward through the array to make sure the tap isn't for a
                    // complication above the background complication.
                    if (x < 20 * mCenterX / 15f + 30 && x > 20 * mCenterX / 15f - 30 && y < 14 * mCenterY / 8f + 30 && y > 14 * mCenterY / 8f - 30) {

                        Log.d(TAG, "Button4 : SOS BUTTON");
                        if (SOSBUTTON == 0) {
                            sendData("SOS");
                            Log.d(TAG, "Button4 : SOS BUTTON" + SOSBUTTON);

                            SOSBUTTON = 1;
                            Intent intent = new Intent(getApplicationContext(), SosActivity.class);
                            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                        } else if (SOSBUTTON == 1) {
                            Log.d(TAG, "Button4 : SOS BUTTON" + SOSBUTTON);
                            if (((SosActivity) SosActivity.context).locationinfo != null) {
                                ((SosActivity) SosActivity.context).locationinfo = "END";
                            }
                            SOSBUTTON = 0;
                        }
                    }

                    if (x < 20 * mCenterX / 15f + 30 && x > 20 * mCenterX / 15f - 30 && y < 2 * 3 * mCenterY / 8f + 30 && y > 3 * mCenterY / 8f - 30) {

                        Log.d(TAG, "Button1 : TEXT SENDING");
                        Intent intent = new Intent(getApplicationContext(), MyActivity.class);
                        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                    if (x > 198 && y < 230 && y > 180) {
                        //initialize watchface hhhhhhhhhhhhh
                        checking = 0;
                        String message = "Right" + Rightnum;
                        Rightnum++;
                        sendData(message);
                        /**
                         Context context = getApplicationContext();
                         mSharedPref =
                         context.getSharedPreferences(
                         getString(R.string.analog_complication_preference_file_key),
                         Context.MODE_PRIVATE);

                         mCalendar = Calendar.getInstance();

                         setWatchFaceStyle(
                         new WatchFaceStyle.Builder(AnalogComplicationWatchFaceService.this)
                         .setAcceptsTapEvents(true)
                         .setHideNotificationIndicator(true)
                         .build());

                         loadSavedPreferences();
                         initializeComplicationsAndBackground();
                         initializeWatchFace();
                         **/
                        /**if(((AnalogComplicationConfigActivity)AnalogComplicationConfigActivity.context).color!=null)
                         {
                         //change the watchface background color, It could change background imgae if content is changed.
                         /** color2=Integer.parseInt(((AnalogComplicationConfigActivity)AnalogComplicationConfigActivity.context).color);
                         Context context = getApplicationContext();
                         mSharedPref =
                         context.getSharedPreferences(
                         getString(R.string.analog_complication_preference_file_key),
                         Context.MODE_PRIVATE);
                         String backgroundColorResourceName =
                         getApplicationContext().getString(R.string.saved_background_color);
                         SharedPreferences sharedPref = context.getSharedPreferences(
                         context.getString(R.string.analog_complication_preference_file_key),
                         Context.MODE_PRIVATE);
                         SharedPreferences.Editor editor = sharedPref.edit();

                         Log.d("editselectedcolor", String.valueOf(color2));
                         editor.putInt("saved_background_color",color2);
                         editor.commit();
                         Log.d("AnalogCOmplicationWatchFAceService : getbackgroundcolor : ", backgroundColorResourceName);
                         updateWatchPaintStyles();
                         onVisibilityChanged(true);
                         /** Intent intent=new Intent(getApplicationContext(), ChangeScreen.class);
                         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                         startActivity(intent);

                         }**/
                        Log.d("AnalogComplicationWatchfaceservice", "RIGHT");
                    }
                    if (x < 199 && y < 230 && y > 180) {
                        checking = 1;
                        String message = "Left" + Leftnum;
                        Leftnum++;
                        sendData(message);

                        /**if(((AnalogComplicationConfigActivity)AnalogComplicationConfigActivity.context).color2!=null)
                         {
                         /**
                         color3=Integer.parseInt(((AnalogComplicationConfigActivity)AnalogComplicationConfigActivity.context).color2);
                         Context context = getApplicationContext();
                         mSharedPref =
                         context.getSharedPreferences(
                         getString(R.string.analog_complication_preference_file_key),
                         Context.MODE_PRIVATE);
                         String backgroundColorResourceName =
                         getApplicationContext().getString(R.string.saved_background_color);
                         SharedPreferences sharedPref = context.getSharedPreferences(
                         context.getString(R.string.analog_complication_preference_file_key),
                         Context.MODE_PRIVATE);
                         SharedPreferences.Editor editor = sharedPref.edit();

                         Log.d("editselectedcolor", String.valueOf(color3));
                         editor.putInt("saved_background_color",color3);
                         editor.commit();
                         Log.d("AnalogCOmplicationWatchFAceService : getbackgroundcolor : ", backgroundColorResourceName);
                         updateWatchPaintStyles();
                         onVisibilityChanged(true);
                         /** Intent intent=new Intent(getApplicationContext(), ChangeScreen.class);
                         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                         startActivity(intent);
                         }**/
                        Log.d("AnalogCOmplicationWatchfaceservice", "Left");
                    }
            }
        }

        private void initFormats() {
            mDayOfWeekFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
            mDayOfWeekFormat.setCalendar(mCalendar);
            mDateFormat = DateFormat.getDateFormat(AnalogComplicationWatchFaceService.this);
            mDateFormat.setCalendar(mCalendar);
        }

        private Paint createTextPaint(int defaultInteractiveColor) {
            return createTextPaint(defaultInteractiveColor, NORMAL_TYPEFACE);
        }

        private Paint createTextPaint(int defaultInteractiveColor, Typeface typeface) {
            Paint paint = new Paint();
            paint.setColor(defaultInteractiveColor);
            paint.setTypeface(typeface);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            Log.d(TAG, "onAmbientModeChanged: " + inAmbientMode);

            mAmbient = inAmbientMode;
            if (mLowBitAmbient) {
                boolean antiAlias = !inAmbientMode;
                mDatePaint.setAntiAlias(antiAlias);
                mlocationPaint.setAntiAlias(antiAlias);
                mHourPaint.setAntiAlias(antiAlias);
                mMinutePaint.setAntiAlias(antiAlias);
                mSecondPaint.setAntiAlias(antiAlias);
                mAmPmPaint.setAntiAlias(antiAlias);
                mColonPaint.setAntiAlias(antiAlias);
            }
            updateWatchPaintStyles();

            // Update drawable complications' ambient state.
            // Note: ComplicationDrawable handles switching between active/ambient colors, we just
            // have to inform it to enter ambient mode.
            ComplicationDrawable complicationDrawable;

            for (int i = 0; i < COMPLICATION_IDS.length; i++) {
                complicationDrawable = mComplicationDrawableSparseArray.get(COMPLICATION_IDS[i]);
                complicationDrawable.setInAmbientMode(mAmbient);
            }

            // Check and trigger whether or not timer should be running (only in active mode).
            updateTimer();
        }

        private void updateWatchPaintStyles() {
            if (mAmbient) {

                mBackgroundPaint.setColor(Color.BLACK);

                mHourPaint.setColor(Color.WHITE);
                mMinutePaint.setColor(Color.WHITE);
                mSecondAndHighlightPaint.setColor(Color.WHITE);
                mTickAndCirclePaint.setColor(Color.WHITE);

                mHourPaint.setAntiAlias(false);
                mMinutePaint.setAntiAlias(false);

                mSecondAndHighlightPaint.setAntiAlias(false);
                mTickAndCirclePaint.setAntiAlias(false);

                mHourPaint.clearShadowLayer();
                mMinutePaint.clearShadowLayer();
                mSecondAndHighlightPaint.clearShadowLayer();
                mTickAndCirclePaint.clearShadowLayer();

            } else {
                Log.d("AnalogComplicaationwatchfaceservice", "color is here00");
                mBackgroundPaint.setColor(mBackgroundColor);

                mHourPaint.setColor(mWatchHandAndComplicationsColor);
                mMinutePaint.setColor(mWatchHandAndComplicationsColor);
                mTickAndCirclePaint.setColor(mWatchHandAndComplicationsColor);

                mSecondAndHighlightPaint.setColor(mWatchHandHighlightColor);

                mHourPaint.setAntiAlias(true);
                mMinutePaint.setAntiAlias(true);
                mSecondAndHighlightPaint.setAntiAlias(true);
                mTickAndCirclePaint.setAntiAlias(true);

                mHourPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
                mMinutePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
                mSecondAndHighlightPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
                mTickAndCirclePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
            }
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);

            /* Dim display in mute mode. */
            if (mMuteMode != inMuteMode) {
                mMuteMode = inMuteMode;
                mHourPaint.setAlpha(inMuteMode ? 100 : 255);
                mMinutePaint.setAlpha(inMuteMode ? 100 : 255);
                mSecondAndHighlightPaint.setAlpha(inMuteMode ? 80 : 255);
                invalidate();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            /*
             * Find the coordinates of the center point on the screen, and ignore the window
             * insets, so that, on round watches with a "chin", the watch face is centered on the
             * entire screen, not just the usable portion.
             */
            mCenterX = width / 2f;
            mCenterY = height / 2f;

            /*
             * Calculate lengths of different hands based on watch screen size.
             */
            mSecondHandLength = (float) (mCenterX * 0.875);
            mMinuteHandLength = (float) (mCenterX * 0.75);
            mHourHandLength = (float) (mCenterX * 0.5);

            /*
             * Calculates location bounds for right and left circular complications. Please note,
             * we are not demonstrating a long text complication in this watch face.
             *
             * We suggest using at least 1/4 of the screen width for circular (or squared)
             * complications and 2/3 of the screen width for wide rectangular complications for
             * better readability.
             */

            // For most Wear devices, width and height are the same, so we just chose one (width).
            int sizeOfComplication = width / 4;
            int midpointOfScreen = width / 2;

            int horizontalOffset = (midpointOfScreen - sizeOfComplication) / 2;
            int verticalOffset = midpointOfScreen - (sizeOfComplication / 2);

            Rect leftBounds =
                    // Left, Top, Right, Bottom
                    new Rect(
                            horizontalOffset,
                            verticalOffset,
                            (horizontalOffset + sizeOfComplication),
                            (verticalOffset + sizeOfComplication));

            ComplicationDrawable leftComplicationDrawable =
                    mComplicationDrawableSparseArray.get(LEFT_COMPLICATION_ID);
            leftComplicationDrawable.setBounds(leftBounds);

            Rect rightBounds =
                    // Left, Top, Right, Bottom
                    new Rect(
                            (midpointOfScreen + horizontalOffset),
                            verticalOffset,
                            (midpointOfScreen + horizontalOffset + sizeOfComplication),
                            (verticalOffset + sizeOfComplication));

            ComplicationDrawable rightComplicationDrawable =
                    mComplicationDrawableSparseArray.get(RIGHT_COMPLICATION_ID);
            rightComplicationDrawable.setBounds(rightBounds);

            Rect screenForBackgroundBound =
                    // Left, Top, Right, Bottom
                    new Rect(0, 0, width, height);

            ComplicationDrawable backgroundComplicationDrawable =
                    mComplicationDrawableSparseArray.get(BACKGROUND_COMPLICATION_ID);
            backgroundComplicationDrawable.setBounds(screenForBackgroundBound);
        }

        private String formatTwoDigitNumber(int hour) {
            return String.format("%02d", hour);
        }

        private String getAmPmString(int amPm) {
            return amPm == Calendar.AM ? mAmString : mPmString;
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onApplyWindowInsets: " + (insets.isRound() ? "round" : "square"));
            }
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = AnalogComplicationWatchFaceService.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);
            float amPmSize = resources.getDimension(isRound
                    ? R.dimen.digital_am_pm_size_round : R.dimen.digital_am_pm_size);
            TextSize = textSize;
            mDatePaint.setTextSize(resources.getDimension(R.dimen.digital_date_text_size));
            mlocationPaint.setTextSize(resources.getDimension(R.dimen.location_size));
            mHourPaint.setTextSize(textSize);
            mMinutePaint.setTextSize(textSize);
            mSecondPaint.setTextSize(textSize / 3);
            mAmPmPaint.setTextSize(textSize / 3);
            mColonPaint.setTextSize(textSize / 3);

            mColonWidth = mColonPaint.measureText(COLON_STRING);
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            RequestThread thread = new RequestThread();
            thread.start();

            // 파싱
            JsonParser jsonParser = new JsonParser();
            UserInfo userInfo = new UserInfo();
            String name = userInfo.getName();
            String group = userInfo.getGroup();
            try {
                userInfo = jsonParser.getUserInfo(why);
                name = userInfo.getName();
                group = userInfo.getGroup();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("Parsing Name",name);
            Log.d("Parsing Group",group);

            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);
            Log.d("onDraw", String.valueOf(canvas));
            drawBackground(canvas);
            drawComplications(canvas, now);
            drawUnreadNotificationIcon(canvas);

            //WIFI checking
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo conwifi = connManager.getActiveNetworkInfo();
            if (conwifi != null && conwifi.isConnectedOrConnecting()) {
                switch (conwifi.getType()) {
                    case ConnectivityManager.TYPE_MOBILE:
                        Log.d(TAG, "3G");
                        checkingLTE = 1;
                        break;
                    case ConnectivityManager.TYPE_WIFI:
                        Log.d(TAG, "WIFI");
                        checkingwifi = 1;
                        break;
                    default:
                        checkingwifi = 0;
                        checkingLTE = 0;
                }

            } else {
                Log.d(TAG, "WIFI no connected");
                checkingwifi = 0;
            }
            //LTE checking
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
                Log.d(TAG, "LTE Connected");
                checkingLTE = 1;
            } else {
                Log.d(TAG, "LTE no connected");
                checkingLTE = 0;
            }
            //this is where determine the watchhhhh
            if (SOSBUTTON == 1) {
                mDate.setTime(now);
                boolean is24Hour = DateFormat.is24HourFormat(AnalogComplicationWatchFaceService.this);

                // Show colons for the first half of each second so the colons blink on when the time
                // updates.
                mShouldDrawColons = (System.currentTimeMillis() % 1000) < 500;

                // Draw the background.
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);

                // 사용자 이름 디스플레이
                canvas.drawText(name,160, 130, mSecondPaint);
                canvas.drawText(group,180, 100, mSecondPaint);

                // Draw the hours.
                float x = mXOffset;
                String hourString;
                if (is24Hour) {
                    hourString = formatTwoDigitNumber(mCalendar.get(Calendar.HOUR_OF_DAY));
                } else {
                    int hour = mCalendar.get(Calendar.HOUR);
                    if (hour == 0) {
                        hour = 12;
                    }
                    hourString = String.valueOf(hour);
                }
                canvas.drawText(hourString, x, mYOffset, mHourPaint);
                x += mHourPaint.measureText(hourString);

                // In ambient and mute modes, always draw the first colon. Otherwise, draw the
                // first colon for the first half of each second.
                if (isInAmbientMode() || mMute || mShouldDrawColons) {
                    canvas.drawText(COLON_STRING, x, mYOffset, mColonPaint);
                }
                x += mColonWidth;
                if (checkingwifi == 1) {
                    canvas.drawBitmap(wifion, 130, 30, null);
                    Log.d("wifion ", "wifion");
                }
                if (checkingwifi == 0)
                    canvas.drawBitmap(wifioff, 130, 30, null);
                /**   if(checkingLTE==1)
                 canvas.drawCircle(146,30,16,mCirclePaint);
                 if(checkingLTE==0)
                 canvas.drawCircle(146,30,16,mColonPaint);
                 **/
                //BUTTONS ARE HERE

                canvas.drawBitmap(mailon, 6 * mCenterX / 5f, 1 * mCenterY / 2 - 2 * mCenterX / 8f, null);
                canvas.drawCircle(8 * mCenterX / 5f, mCenterY - 1 * mCenterY / 5f, 25, mColonPaint);
                canvas.drawCircle(8 * mCenterX / 5f, 3 * mCenterY / 2 - 1 * mCenterY / 5f, 25, mColonPaint);
                canvas.drawBitmap(soson, 6 * mCenterX / 5f, 2 * mCenterY - 3 * mCenterX / 8f, null);
                // Draw the minutes.
                String minuteString = formatTwoDigitNumber(mCalendar.get(Calendar.MINUTE));
                canvas.drawText(minuteString, mCenterX, mCenterY / 2, mMinutePaint);
                x = mCenterX;
                // In unmuted interactive mode, draw a second blinking colon followed by the seconds.
                // Otherwise, if we're in 12-hour mode, draw AM/PM
                /** if (!isInAmbientMode() && !mMute) {
                 if (mShouldDrawColons) {
                 canvas.drawText(COLON_STRING, x, mYOffset, mColonPaint);
                 }
                 x += mColonWidth;
                 canvas.drawText(formatTwoDigitNumber(
                 mCalendar.get(Calendar.SECOND)), x, mYOffset, mSecondPaint);
                 } else if (!is24Hour) {
                 x += mColonWidth;
                 canvas.drawText(getAmPmString(
                 mCalendar.get(Calendar.AM_PM)), x, mYOffset, mAmPmPaint);
                 }
                 **/
                String locationin = "";
                /** if(((SosActivity)SosActivity.context).locationinfo!=null){
                 locationin=((SosActivity)SosActivity.context).locationinfo;
                 }**/
                // Day of week
                canvas.drawText(locationin, mCenterX / 3f, mCenterY + TextSize, mMinutePaint);


            }
            if (checking == 1 && SOSBUTTON == 0) {
                drawWatchFace(canvas);
            }
            if (checking == 0 && SOSBUTTON == 0) {
                mDate.setTime(now);
                boolean is24Hour = DateFormat.is24HourFormat(AnalogComplicationWatchFaceService.this);

                // Show colons for the first half of each second so the colons blink on when the time
                // updates.
                mShouldDrawColons = (System.currentTimeMillis() % 1000) < 500;

                // Draw the background.
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);

                canvas.drawText(name,160, 130, mSecondPaint);
                canvas.drawText(group,180, 100, mSecondPaint);
                // Draw the hours.
                float x = mXOffset;
                String hourString;
                if (is24Hour) {
                    hourString = formatTwoDigitNumber(mCalendar.get(Calendar.HOUR_OF_DAY));
                } else {
                    int hour = mCalendar.get(Calendar.HOUR);
                    if (hour == 0) {
                        hour = 12;
                    }
                    hourString = String.valueOf(hour);
                }

                canvas.drawText(hourString, mCenterX / 3f, mCenterY, mHourPaint);
                x += mHourPaint.measureText(hourString);

                // In ambient and mute modes, always draw the first colon. Otherwise, draw the
                // first colon for the first half of each second.
                if (isInAmbientMode() || mMute || mShouldDrawColons) {
                    //canvas.drawText(COLON_STRING, x, mYOffset, mColonPaint);
                }
                x += mColonWidth;
                if (checkingwifi == 1) {
                    canvas.drawBitmap(wifion, 130f, 30f, null);
                    Log.d("wifion ", "wifion");
                }
                if (checkingwifi == 0)
                    canvas.drawBitmap(wifioff, 130f, 30f, null);
                /**if(checkingLTE==1)
                 canvas.drawCircle(146,30,16,mCirclePaint);
                 if(checkingLTE==0)
                 canvas.drawCircle(146,30,16,mColonPaint);
                 **/
                //BUTTONS ARE HERE
                //canvas.drawBitmap(mailon,61f,283f,null);
                canvas.drawBitmap(mailon, 6 * mCenterX / 5f, 1 * mCenterY / 2 - 2 * mCenterX / 8f, null);
                canvas.drawCircle(8 * mCenterX / 5f, mCenterY - 1 * mCenterY / 5f, 25, mColonPaint);
                canvas.drawCircle(8 * mCenterX / 5f, 3 * mCenterY / 2 - 1 * mCenterY / 5f, 25, mColonPaint);
                canvas.drawBitmap(sosoff, 6 * mCenterX / 5f, 2 * mCenterY - 3 * mCenterX / 8f, null);
                /**
                 canvas.drawCircle(20*mCenterX/15f,3*mCenterY/8f,30,mColonPaint);
                 canvas.drawCircle(20*mCenterX/15f,14*mCenterY/8f,30,mColonPaint);
                 **/

                //canvas.drawCircle(7*mCenterX/5f,2*mCenterY-2*mCenterX/8f,mCenterX/8f,mColonPaint);
                //canvas.drawBitmap(sosoff,295f,283f,null);
                // Draw the minutes.
                String minuteString = formatTwoDigitNumber(mCalendar.get(Calendar.MINUTE));
                canvas.drawText(minuteString, mCenterX / 3f, mCenterY + TextSize, mMinutePaint);
                x = mCenterX / 3f + TextSize + 10;
                // In unmuted interactive mode, draw a second blinking colon followed by the seconds.
                // Otherwise, if we're in 12-hour mode, draw AM/PM
                if (!isInAmbientMode() && !mMute) {
                    if (mShouldDrawColons) {
                        canvas.drawText(COLON_STRING, x, mCenterY + TextSize, mColonPaint);
                    }
                    x += mColonWidth;
                    canvas.drawText(formatTwoDigitNumber(
                            mCalendar.get(Calendar.SECOND)), x, mCenterY + TextSize, mSecondPaint);
                } else if (!is24Hour) {
                    x += mColonWidth;
                    canvas.drawText(getAmPmString(
                            mCalendar.get(Calendar.AM_PM)), x, mCenterY + TextSize, mAmPmPaint);
                }

                // Day of week
                /**canvas.drawText(
                 mDayOfWeekFormat.format(mDate),
                 mXOffset, mYOffset + mLineHeight*4, mDatePaint);

                 Log.d(TAG,mDayOfWeekFormat.format(mDate).substring(1));
                 // Date
                 canvas.drawText(
                 mDateFormat.format(mDate),
                 mXOffset, mYOffset + mLineHeight * 3, mDatePaint);**/
            }
        }

        private void drawUnreadNotificationIcon(Canvas canvas) {

            if (mUnreadNotificationsPreference && (mNumberOfUnreadNotifications > 0)) {

                int width = canvas.getWidth();
                int height = canvas.getHeight();

                canvas.drawCircle(width / 2, height - 40, 10, mTickAndCirclePaint);

                /*
                 * Ensure center highlight circle is only drawn in interactive mode. This ensures
                 * we don't burn the screen with a solid circle in ambient mode.
                 */
                if (!mAmbient) {
                    canvas.drawCircle(width / 2, height - 40, 4, mSecondAndHighlightPaint);
                }
            }
        }

        private void drawBackground(Canvas canvas) {

            if (mAmbient && (mLowBitAmbient || mBurnInProtection)) {
                canvas.drawColor(Color.BLACK);

            } else {
                canvas.drawColor(mBackgroundColor);
            }
        }

        private void drawComplications(Canvas canvas, long currentTimeMillis) {
            int complicationId;
            ComplicationDrawable complicationDrawable;

            for (int i = 0; i < COMPLICATION_IDS.length; i++) {
                complicationId = COMPLICATION_IDS[i];
                complicationDrawable = mComplicationDrawableSparseArray.get(complicationId);

                complicationDrawable.draw(canvas, currentTimeMillis);
            }
        }

        private void drawWatchFace(Canvas canvas) {
            /*
             * Draw ticks. Usually you will want to bake this directly into the photo, but in
             * cases where you want to allow users to select their own photos, this dynamically
             * creates them on top of the photo.
             */
            float innerTickRadius = mCenterX - 10;
            float outerTickRadius = mCenterX;
            for (int tickIndex = 0; tickIndex < 12; tickIndex++) {
                float tickRot = (float) (tickIndex * Math.PI * 2 / 12);
                float innerX = (float) Math.sin(tickRot) * innerTickRadius;
                float innerY = (float) -Math.cos(tickRot) * innerTickRadius;
                float outerX = (float) Math.sin(tickRot) * outerTickRadius;
                float outerY = (float) -Math.cos(tickRot) * outerTickRadius;
                canvas.drawLine(
                        mCenterX + innerX,
                        mCenterY + innerY,
                        mCenterX + outerX,
                        mCenterY + outerY,
                        mTickAndCirclePaint);
            }

            /*
             * These calculations reflect the rotation in degrees per unit of time, e.g.,
             * 360 / 60 = 6 and 360 / 12 = 30.
             */
            final float seconds =
                    (mCalendar.get(Calendar.SECOND) + mCalendar.get(Calendar.MILLISECOND) / 1000f);
            final float secondsRotation = seconds * 6f;

            final float minutesRotation = mCalendar.get(Calendar.MINUTE) * 6f;

            final float hourHandOffset = mCalendar.get(Calendar.MINUTE) / 2f;
            final float hoursRotation = (mCalendar.get(Calendar.HOUR) * 30) + hourHandOffset;

            /*
             * Save the canvas state before we can begin to rotate it.
             */
            canvas.save();

            canvas.rotate(hoursRotation, mCenterX, mCenterY);
            canvas.drawLine(
                    mCenterX,
                    mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
                    mCenterX,
                    mCenterY - mHourHandLength,
                    mHourPaint);

            canvas.rotate(minutesRotation - hoursRotation, mCenterX, mCenterY);
            canvas.drawLine(
                    mCenterX,
                    mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
                    mCenterX,
                    mCenterY - mMinuteHandLength,
                    mMinutePaint);

            /*
             * Ensure the "seconds" hand is drawn only when we are in interactive mode.
             * Otherwise, we only update the watch face once a minute.
             */
            if (!mAmbient) {
                canvas.rotate(secondsRotation - minutesRotation, mCenterX, mCenterY);
                canvas.drawLine(
                        mCenterX,
                        mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
                        mCenterX,
                        mCenterY - mSecondHandLength,
                        mSecondAndHighlightPaint);
            }
            canvas.drawCircle(
                    mCenterX, mCenterY, CENTER_GAP_AND_CIRCLE_RADIUS, mTickAndCirclePaint);

            /* Restore the canvas' original orientation. */
            canvas.restore();

        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            initFormats();
            if (visible) {

                // Preferences might have changed since last time watch face was visible.
                loadSavedPreferences();

                // With the rest of the watch face, we update the paint colors based on
                // ambient/active mode callbacks, but because the ComplicationDrawable handles
                // the active/ambient colors, we only need to update the complications' colors when
                // the user actually makes a change to the highlight color, not when the watch goes
                // in and out of ambient mode.
                setComplicationsActiveAndAmbientColors(mWatchHandHighlightColor);
                Log.d("AnalogComplicationWatchFaceService", "this is where changes the color");
                updateWatchPaintStyles();

                registerReceiver();
                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();
            }

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        @Override
        public void onUnreadCountChanged(int count) {
            Log.d(TAG, "onUnreadCountChanged(): " + count);

            if (mUnreadNotificationsPreference) {

                if (mNumberOfUnreadNotifications != count) {
                    mNumberOfUnreadNotifications = count;
                    invalidate();
                }
            }
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            AnalogComplicationWatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            AnalogComplicationWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts/stops the {@link #mUpdateTimeHandler} timer based on the state of the watch face.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run in active mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !mAmbient;
        }

        private void sendData(String message) {
            PutDataMapRequest dataMap = PutDataMapRequest.create(datapath);
            dataMap.getDataMap().putString("message", message);
            Log.d("WEARABLE STUFF", dataMap.getDataMap().getString("message"));
            PutDataRequest request = dataMap.asPutDataRequest();
            request.setUrgent();

            Task<DataItem> dataItemTask = Wearable.getDataClient(AnalogComplicationWatchFaceService.this).putDataItem(request);
            dataItemTask
                    .addOnSuccessListener(new OnSuccessListener<DataItem>() {
                        @Override
                        public void onSuccess(DataItem dataItem) {
                            Log.d(TAG, "Sending message was successful: " + dataItem);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Sending message failed: " + e);
                        }
                    })
            ;
        }


    }


    class RequestThread extends Thread {
        public String urlStr = "http://15.164.45.229:8888/users/MDg6OTc6OTg6MEU6RTY6REE=";
        Handler handler = new Handler();

        @Override
        public void run() {
            try {
                URL url = new URL(urlStr);
                Log.d("hI", String.valueOf(url));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn != null) {
                    conn.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
                    conn.setRequestMethod("GET");
                    Log.d("hI", String.valueOf(url));
                    conn.setDoInput(true);


                    int resCode = conn.getResponseCode();
                    Log.d("hi", String.valueOf(resCode));
                    Log.d("hi", String.valueOf(HttpURLConnection.HTTP_OK));

                    if (resCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String line = null;
                        while (true) {
                            line = reader.readLine();
                            if (line == null)
                                break;
                            println(line);
                        }
                        reader.close();
                    }
                    conn.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void println(final String data) {
            handler.post(new Runnable() {
                @Override

                public void run() {
                    String temp = "";
                    int temp2 = 0;
                    try {
                        for (int i = 0; i < data.length(); i++) {
                            if (data.charAt(i) == ':')
                                temp2 = i;
                        }
                        temp = AES256s.decryptToString(data.substring(temp2 + 2, data.length() - 2), "08:97:98:0E:E6:DA");

                        Log.d("hiypypy", temp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    why = temp;
                }
            });
        }
    }
}