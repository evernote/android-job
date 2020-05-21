/*
 * Copyright (C) 2018 Evernote Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evernote.android.job.v14;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.evernote.android.job.WakeLockUtil;
import com.evernote.android.job.JobConfig;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.JobCat;

import java.util.HashSet;
import java.util.Set;

/**
 * @author rwondratschek
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class PlatformAlarmServiceExact extends Service {

    private static final JobCat CAT = new JobCat("PlatformAlarmServiceExact");

    public static Intent createIntent(Context context, int jobId, @Nullable Bundle transientExtras) {
        Intent intent = new Intent(context, PlatformAlarmServiceExact.class);
        intent.putExtra(PlatformAlarmReceiver.EXTRA_JOB_ID, jobId);
        if (transientExtras != null) {
            intent.putExtra(PlatformAlarmReceiver.EXTRA_TRANSIENT_EXTRAS, transientExtras);
        }
        return intent;
    }

    /**
     * This is used for Android 7.0+ where the Service start might be restricted either by
     * a power user that changed the AppOps setting or by the OS itself. If this case
     * happens, we will run the job directly on an executor instead of using a Service.
     *
     * Note that for Android 7.0+, this Service is still used for immediate jobs and exact jobs.
     *
     * @see JobRequest#getJobApi
     */
    public static void runJobWithoutUsingService(final Context context, final Intent intent) {
        JobConfig.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    PlatformAlarmService.runJob(intent, context, CAT);
                } finally {
                    // call here, our own wake lock could be acquired too late
                    WakeLockUtil.completeWakefulIntent(intent);
                }
            }
        });
    }

    private final Object mMonitor = new Object();

    private volatile Set<Integer> mStartIds;
    private volatile int mLastStartId;

    @Override
    public void onCreate() {
        super.onCreate();
        mStartIds = new HashSet<>();
    }

    @Override
    public int onStartCommand(@Nullable final Intent intent, int flags, final int startId) {
        synchronized (mMonitor) {
            mStartIds.add(startId);
            mLastStartId = startId;
        }

        JobConfig.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    PlatformAlarmService.runJob(intent, PlatformAlarmServiceExact.this, CAT);
                } finally {
                    // call here, our own wake lock could be acquired too late
                    WakeLockUtil.completeWakefulIntent(intent);
                    stopSelfIfNecessary(startId);
                }
            }
        });
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        synchronized (mMonitor) {
            mStartIds = null;
            mLastStartId = 0;
        }
    }

    @Override
    public final IBinder onBind(Intent intent) {
        return null;
    }

    private void stopSelfIfNecessary(int startId) {
        synchronized (mMonitor) {
            Set<Integer> startIds = mStartIds;
            if (startIds != null) {
                // service not destroyed
                startIds.remove(startId);
                if (startIds.isEmpty()) {
                    stopSelfResult(mLastStartId);
                }
            }
        }
    }
}
