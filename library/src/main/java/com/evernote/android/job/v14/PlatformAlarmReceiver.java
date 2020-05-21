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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.evernote.android.job.WakeLockUtil;

/**
 * @author rwondratschek
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class PlatformAlarmReceiver extends BroadcastReceiver {

    /*package*/ static final String EXTRA_JOB_ID = "EXTRA_JOB_ID";
    /*package*/ static final String EXTRA_JOB_EXACT = "EXTRA_JOB_EXACT";
    /*package*/ static final String EXTRA_TRANSIENT_EXTRAS = "EXTRA_TRANSIENT_EXTRAS";

    /*package*/ static Intent createIntent(Context context, int jobId, boolean exact, @Nullable Bundle transientExtras) {
        Intent intent = new Intent(context, PlatformAlarmReceiver.class).putExtra(EXTRA_JOB_ID, jobId).putExtra(EXTRA_JOB_EXACT, exact);
        if (transientExtras != null) {
            intent.putExtra(EXTRA_TRANSIENT_EXTRAS, transientExtras);
        }
        return intent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.hasExtra(EXTRA_JOB_ID) && intent.hasExtra(EXTRA_JOB_EXACT)) {
            int jobId = intent.getIntExtra(EXTRA_JOB_ID, -1);
            Bundle transientExtras = intent.getBundleExtra(EXTRA_TRANSIENT_EXTRAS);

            if (intent.getBooleanExtra(EXTRA_JOB_EXACT, false)) {
                Intent serviceIntent = PlatformAlarmServiceExact.createIntent(context, jobId, transientExtras);
                WakeLockUtil.startWakefulPlatformAlarmServiceExact(context, serviceIntent);
            } else {
                PlatformAlarmService.start(context, jobId, transientExtras);
            }
        }
    }
}
