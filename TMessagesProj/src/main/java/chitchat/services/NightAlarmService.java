package chitchat.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.tisad.chitchat2.BuildConfig;

import org.telegram.messenger.NotificationCenter;

import chitchat.ChitSettings;
import chitchat.Log;
import chitchat.NightModeUtil;

/**
 * Created by RaminBT on 24/01/2016.
 */
public class NightAlarmService extends Service{
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        boolean isInsideNightMode=NightModeUtil.isNowNightMode();
        if(BuildConfig.DEBUG)
            Log.d("night mode started? " + isInsideNightMode);
        NightModeUtil.insideNightMode=isInsideNightMode;
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.updateTheme);
        stopSelf();


        ChitSettings.nightModeChanged();
    }
}
