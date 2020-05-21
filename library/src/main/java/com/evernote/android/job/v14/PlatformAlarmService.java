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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.app.SafeJobIntentService;

import com.evernote.android.job.JobIdsInternal;
import com.evernote.android.job.JobProxy;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.JobCat;

/**
 * @author rwondratschek
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class PlatformAlarmService extends SafeJobIntentService {

    private static final JobCat CAT = new JobCat("PlatformAlarmService");

    public static void start(Context context, int jobId, @Nullable Bundle transientExtras) {
        Intent intent = new Intent();
        intent.putExtra(PlatformAlarmReceiver.EXTRA_JOB_ID, jobId);
        if (transientExtras != null) {
            intent.putExtra(PlatformAlarmReceiver.EXTRA_TRANSIENT_EXTRAS, transientExtras);
        }

        enqueueWork(context, PlatformAlarmService.class, JobIdsInternal.JOB_ID_PLATFORM_ALARM_SERVICE, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        runJob(intent, this, CAT);
    }

    /*package*/ static void runJob(@Nullable Intent intent, @NonNull Context context, @NonNull JobCat cat) {
        if (intent == null) {
            cat.i("Delivered intent is null");
            return;
        }

        int jobId = intent.getIntExtra(PlatformAlarmReceiver.EXTRA_JOB_ID, -1);
        Bundle transientExtras = intent.getBundleExtra(PlatformAlarmReceiver.EXTRA_TRANSIENT_EXTRAS);
        final JobProxy.Common common = new JobProxy.Common(context, cat, jobId);

        // create the JobManager. Seeing sometimes exceptions, that it wasn't created, yet.
        final JobRequest request = common.getPendingRequest(true, true);
        if (request != null) {
            common.executeJobRequest(request, transientExtras);
        }
    }
}
