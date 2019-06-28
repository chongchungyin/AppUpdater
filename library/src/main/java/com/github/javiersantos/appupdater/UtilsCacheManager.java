package com.github.javiersantos.appupdater;

import android.support.annotation.Nullable;

import com.github.javiersantos.appupdater.objects.Update;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class UtilsCacheManager {

    private static int CACHE_EXPIRED_IN_MINS = 5;
    private static Update lastSyncUpdateResult = null;
    private static long lastUpdateTime = -1;

    static void setLastSyncUpdateResult(Update result) {
        lastSyncUpdateResult = result;
        lastUpdateTime = Calendar.getInstance().getTimeInMillis();
    }

    @Nullable
    static Update getLastSyncUpdateResult() {
        return lastSyncUpdateResult;
    }

    static boolean isUpdateResultCacheExpired() {
        if (lastUpdateTime < 0) {
            return true;
        }

        long timeDiffInMilli = Calendar.getInstance().getTimeInMillis() - lastUpdateTime;
        long expirationInMilli = TimeUnit.MILLISECONDS.convert(CACHE_EXPIRED_IN_MINS, TimeUnit.MINUTES);

        return timeDiffInMilli > expirationInMilli;
    }

}
