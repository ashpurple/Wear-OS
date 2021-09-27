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

package com.example.android.wearable.watchface.util;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.Callable;

public final class DigitalWatchFaceUtil {
    private static final String TAG = "DigitalWatchFaceUtil";

    
    public static final String KEY_BACKGROUND_COLOR = "BACKGROUND_COLOR";

  
    public static final String KEY_HOURS_COLOR = "HOURS_COLOR";

    
    public static final String KEY_MINUTES_COLOR = "MINUTES_COLOR";

    
    public static final String KEY_SECONDS_COLOR = "SECONDS_COLOR";

   
    public static final String PATH_WITH_FEATURE = "/watch_face_config/Digital";

    /**
     * Name of the default interactive mode background color and the ambient mode background color.
     */
    public static final String COLOR_NAME_DEFAULT_AND_AMBIENT_BACKGROUND = "Black";
    public static final int COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND =
            parseColor(COLOR_NAME_DEFAULT_AND_AMBIENT_BACKGROUND);

    /**
     * Name of the default interactive mode hour digits color and the ambient mode hour digits
     * color.
     */
    public static final String COLOR_NAME_DEFAULT_AND_AMBIENT_HOUR_DIGITS = "White";
    public static final int COLOR_VALUE_DEFAULT_AND_AMBIENT_HOUR_DIGITS =
            parseColor(COLOR_NAME_DEFAULT_AND_AMBIENT_HOUR_DIGITS);

    /**
     * Name of the default interactive mode minute digits color and the ambient mode minute digits
     * color.
     */
    public static final String COLOR_NAME_DEFAULT_AND_AMBIENT_MINUTE_DIGITS = "White";
    public static final int COLOR_VALUE_DEFAULT_AND_AMBIENT_MINUTE_DIGITS =
            parseColor(COLOR_NAME_DEFAULT_AND_AMBIENT_MINUTE_DIGITS);

    /**
     * Name of the default interactive mode second digits color and the ambient mode second digits
     * color.
     */
    public static final String COLOR_NAME_DEFAULT_AND_AMBIENT_SECOND_DIGITS = "Gray";
    public static final int COLOR_VALUE_DEFAULT_AND_AMBIENT_SECOND_DIGITS =
            parseColor(COLOR_NAME_DEFAULT_AND_AMBIENT_SECOND_DIGITS);

    
    public interface FetchConfigDataMapCallback {
       
        void onConfigDataMapFetched(DataMap config);
    }

    private static int parseColor(String colorName) {
        return Color.parseColor(colorName.toLowerCase());
    }

    public static void fetchConfigDataMap(
            final Context context,
            final FetchConfigDataMapCallback callback) {

        // You must first get the local node to pass in as an argument for the DataItem get request.
        NodeClient nodeClient = Wearable.getNodeClient(context);
        Task<Node> localNodeTask = nodeClient.getLocalNode();

        localNodeTask.addOnCompleteListener(
                new OnCompleteListener<Node>() {
                    @Override
                    public void onComplete(@NonNull Task<Node> nodeTask) {
                        if (nodeTask.isSuccessful() && (nodeTask.getResult() != null)) {
                            DataClient dataClient = Wearable.getDataClient(context);

                            String localNode = nodeTask.getResult().getId();

                            // Build request for DataItem with local node as authority.
                            Uri uri = new Uri.Builder()
                                    .scheme("wear")
                                    .path(PATH_WITH_FEATURE)
                                    .authority(localNode)
                                    .build();

                            Task<DataItem> getDataItemResponseTask = dataClient.getDataItem(uri);

                            getDataItemResponseTask.addOnCompleteListener(
                                    new OnCompleteListener<DataItem>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataItem> dataItemTask) {
                                            if (dataItemTask.isSuccessful() &&
                                                    (dataItemTask.getResult() != null)) {

                                                DataItem configDataItem = dataItemTask.getResult();
                                                DataMapItem dataMapItem =
                                                        DataMapItem.fromDataItem(configDataItem);
                                                DataMap startupConfig = dataMapItem.getDataMap();

                                                callback.onConfigDataMapFetched(startupConfig);
                                            } else {
                                                // If the DataItem hasn't been created yet or some
                                                // keys are missing, send back an empty set.
                                                callback.onConfigDataMapFetched(new DataMap());
                                            }
                                        }
                                    });
                        } else {
                            // If the DataItem hasn't been created yet or some keys are missing,
                            // send back an empty set.
                            callback.onConfigDataMapFetched(new DataMap());
                        }
                    }
                });
    }

    
    public static void overwriteKeysInConfigDataMap(
            final Context context,
            final DataMap configKeysToOverwrite,
            final Callable<Void> callable) {

        DigitalWatchFaceUtil.fetchConfigDataMap(
                context,
                new FetchConfigDataMapCallback() {
                    @Override
                    public void onConfigDataMapFetched(DataMap currentConfig) {
                        currentConfig.putAll(configKeysToOverwrite);

                        DigitalWatchFaceUtil.putConfigDataItem(
                                context,
                                currentConfig,
                                callable);
                    }
                }
        );
    }

    
    public static void putConfigDataItem(
            final Context context,
            DataMap newConfig,
            final Callable<Void> callable) {

        DataClient dataClient = Wearable.getDataClient(context);

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_WITH_FEATURE);
        putDataMapRequest.setUrgent();
        DataMap configToPut = putDataMapRequest.getDataMap();
        configToPut.putAll(newConfig);

        Task<DataItem> putDataItemResponseTask =
                dataClient.putDataItem(putDataMapRequest.asPutDataRequest());

        putDataItemResponseTask.addOnCompleteListener(
                new OnCompleteListener<DataItem>() {
                    @Override
                    public void onComplete(@NonNull Task<DataItem> task) {
                        if (task.isSuccessful() && (task.getResult() != null)) {
                            try {
                                if (callable != null) {
                                    callable.call();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Finish callback failed.", e);
                            }
                        } else {
                            Log.e(TAG, "Put failed: " + task.getException());
                        }
                    }
                });
    }
    private DigitalWatchFaceUtil() { }
}
