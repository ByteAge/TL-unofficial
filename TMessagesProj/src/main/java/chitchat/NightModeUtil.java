package chitchat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import com.tisad.chitchat2.BuildConfig;

import java.util.Calendar;

import chitchat.services.NightAlarmService;

/**
 * Created by RaminBT on 24/01/2016.
 */
public class NightModeUtil {

    public static final int requestStart=1, requestEnd=2;

    public static final String fromKey="nmfk", untilKey="nmuk", enabledKey="nmenabled";

    public static int fromHour, fromMinute, untilHour, untilMinute;
    public static boolean nightEnabled, insideNightMode=false, overrideColor=true,
            suspendLed=false, overRideLedColor=false, suspendVibrate=true, suspendSound=true;

    public static void config(Context context, SharedPreferences preferences){

        nightEnabled=preferences.getBoolean(enabledKey, false);

        String fromStr=preferences.getString(fromKey, "22:30");
        String untilStr=preferences.getString(untilKey, "07:00");

        int[] froms=decode(fromStr);
        fromHour=getHour(froms);
        fromMinute=getMinute(froms);

        froms=decode(untilStr);
        untilHour=getHour(froms);
        untilMinute=getMinute(froms);

        if(nightEnabled)
            startAlarm(context);
    }

    public static boolean isInsideNightMode() {
        return insideNightMode && nightEnabled;
    }

    public static boolean blockLed(){
        return isInsideNightMode() && suspendLed;
    }
    public static boolean blockVibrate(){
        return isInsideNightMode() && suspendVibrate;
    }
    public static boolean blockSound(){
        return isInsideNightMode() && suspendSound;
    }
    public static int ledColor(int def){
        return isInsideNightMode() && overRideLedColor ? 0xff0c5776 : def;
    }

    public static void dark(View v){
        //if(isInsideNightMode() && overrideColor)
            //v.setBackgroundColor(0xffA1AAB3);
    }
    public static int darkIfNightMode(int color){
        return //isInsideNightMode() && overrideColor ? 0xffA1AAB3 :
                color;
    }



    public static boolean isNowNightMode(){
        //long start= TimeUnit.HOURS.toMillis(fromHour) + TimeUnit.MINUTES.toMillis(fromMinute);
        //long end  =TimeUnit.HOURS.toMillis(untilHour) + TimeUnit.MINUTES.toMillis(untilMinute) + TimeUnit.DAYS.toMillis(1);

        //Log.d("start: " + start);
        //Log.d("end: " + end);

        Calendar c=Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int nowH=c.get(Calendar.HOUR_OF_DAY);
        Log.d("nowh: " +nowH);
        int nowM=c.get(Calendar.MINUTE);
        Log.d("nowm: " +nowM);

        boolean betWeen= nowH > fromHour || nowH < untilHour;

        if(!betWeen){
            betWeen=nowH==untilHour && nowM < untilMinute;
            if(!betWeen)
                betWeen=nowH==fromHour && nowM >= fromHour;
            Log.d("hours equal");
        }

        return betWeen;
    }


    public static void startAlarm(Context context){
        if(BuildConfig.DEBUG){

            Log.d("starting alarm");
            Log.d("start: " + fromHour + " min: " + fromMinute);
            Log.d("end: " + untilHour + " min: " + untilMinute);
        }

        Calendar start=Calendar.getInstance();
        Calendar end  =Calendar.getInstance();

        start.set(Calendar.HOUR_OF_DAY, fromHour);
        start.set(Calendar.MINUTE, fromMinute);
        start.set(Calendar.SECOND, 0);
        end.set(Calendar.HOUR_OF_DAY, untilHour);
        end.set(Calendar.MINUTE, untilMinute);
        end.set(Calendar.SECOND, 0);


        AlarmManager alarm= (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // start
        Intent d=new Intent(context, NightAlarmService.class);
        PendingIntent intent=PendingIntent.getService(context, requestStart, d,PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, start.getTimeInMillis(), AlarmManager.INTERVAL_DAY, intent);

        // end
        intent=PendingIntent.getService(context, requestEnd, d, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, end.getTimeInMillis(), AlarmManager.INTERVAL_DAY, intent);

        context.startService(new Intent(context, NightAlarmService.class));
    }
    public static void stopAlarm(Context context){
        if(BuildConfig.DEBUG)
        Log.d("stopping alarm");
        AlarmManager alarm= (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent intent=PendingIntent.getService(context, requestStart, new Intent(context, NightAlarmService.class),PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.cancel(intent);

        intent=PendingIntent.getService(context, requestEnd, new Intent(context, NightAlarmService.class),PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.cancel(intent);

        ChitSettings.nightModeChanged();
    }


    public static String fromString(){
        return toString(fromHour, fromMinute);
    }
    public static String untilString(){
        return toString(untilHour, untilMinute);
    }

    public static boolean change(SharedPreferences preference, boolean enabled){
        if(enabled==nightEnabled)
            return nightEnabled;
        boolean s=preference.edit().putBoolean(enabledKey, enabled).commit();
        if(s){
            nightEnabled=enabled;
        }
        return nightEnabled;
    }
    public static boolean change(SharedPreferences preference, boolean enabled, int fh, int fm, int uh, int um){
        if(enabled && (fh<0 || uh <0))
            return NightModeUtil.nightEnabled;
        SharedPreferences.Editor editor=preference.edit();
        editor.putBoolean(enabledKey, enabled);
        if(enabled){
            String str=toString(fh, fm);
            editor.putString(fromKey, str);
            str=toString(uh, um);
            editor.putString(untilKey, str);
        }
        boolean suc=editor.commit();
        if(suc){
            NightModeUtil.nightEnabled=enabled;
            if(enabled){
                fromHour=fh;
                fromMinute=fm;
                untilMinute=um;
                untilHour=uh;
            }
        }
        return NightModeUtil.nightEnabled;
    }
    public static void changeHourMinute(SharedPreferences preferences, int hour, int minute, boolean from){
        String str=toString(hour, minute);
        boolean s=preferences.edit().putString(from?fromKey:untilKey, str).commit();
        if(s){
            if(from){
                fromHour=hour;
                fromMinute=minute;
            }else {
                untilHour=hour;
                untilMinute=minute;
            }
        }
    }



    public static int[] decode(String date){
        int[] times=new int[2];
        String[] spl=date.split(":");
        times[0]=Integer.valueOf(spl[0]);
        times[1]=Integer.valueOf(spl[1]);
        return times;
    }
    public static String toString(int hour, int minute){
        return hour+":"+minute;
    }
    public static int getHour(int[] time){
        return time[0];
    }
    public static int getMinute(int[] time){
        return time[1];
    }

}
