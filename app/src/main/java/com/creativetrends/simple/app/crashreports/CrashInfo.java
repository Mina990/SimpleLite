package com.creativetrends.simple.app.crashreports;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.creativetrends.simple.app.lite.R;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;
import java.util.Locale;



public class CrashInfo {

    private final static String TAG = "CrashInfo";

    //Extras passed to the error activity
    private static final String EXTRA_RESTART_ACTIVITY_CLASS = "cat.ereza.customactivityoncrash.EXTRA_RESTART_ACTIVITY_CLASS";
    private static final String EXTRA_SHOW_ERROR_DETAILS = "cat.ereza.customactivityoncrash.EXTRA_SHOW_ERROR_DETAILS";
    private static final String EXTRA_STACK_TRACE = "cat.ereza.customactivityoncrash.EXTRA_STACK_TRACE";
    private static final String EXTRA_IMAGE_DRAWABLE_ID = "cat.ereza.customactivityoncrash.EXTRA_IMAGE_DRAWABLE_ID";
    private static final String EXTRA_EVENT_LISTENER = "cat.ereza.customactivityoncrash.EXTRA_EVENT_LISTENER";

    //General constants
    private static final String INTENT_ACTION_ERROR_ACTIVITY = "cat.ereza.customactivityoncrash.ERROR";
    private static final String INTENT_ACTION_RESTART_ACTIVITY = "cat.ereza.customactivityoncrash.RESTART";
    private static final String CAOC_HANDLER_PACKAGE_NAME = "cat.ereza.customactivityoncrash";
    private static final String DEFAULT_HANDLER_PACKAGE_NAME = "com.android.internal.os";
    private static final int MAX_STACK_TRACE_SIZE = 131071; //128 KB - 1
    private static final int TIMESTAMP_DIFFERENCE_TO_AVOID_RESTART_LOOPS_IN_MILLIS = 2000;

    //Shared preferences
    private static final String SHARED_PREFERENCES_FILE = "custom_activity_on_crash";
    private static final String SHARED_PREFERENCES_FIELD_TIMESTAMP = "last_crash_timestamp";

    //Internal variables
    @SuppressLint("StaticFieldLeak")
    private static Application application;
    private static WeakReference<Activity> lastActivityCreated = new WeakReference<>(null);
    private static boolean isInBackground = false;

    //Settable properties and their defaults
    private static boolean launchErrorActivityWhenInBackground = true;
    private static boolean showErrorDetails = true;
    private static boolean enableAppRestart = true;
    private static int defaultErrorActivityDrawableId = R.drawable.ic_launcher_s;
    private static Class<? extends Activity> errorActivityClass = null;
    private static Class<? extends Activity> restartActivityClass = null;
    private static EventListener eventListener = null;


    public static void install(Context context) {
        try {
            if (context == null) {
                Log.e(TAG, "Install failed: context is null!");
            } else {

                final Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();

                if (oldHandler != null && oldHandler.getClass().getName().startsWith(CAOC_HANDLER_PACKAGE_NAME)) {
                    Log.e(TAG, "You have already installed CrashInfo, doing nothing!");
                } else {
                    if (oldHandler != null && !oldHandler.getClass().getName().startsWith(DEFAULT_HANDLER_PACKAGE_NAME)) {
                        Log.e(TAG, "IMPORTANT WARNING! You already have an UncaughtExceptionHandler, are you sure this is correct? If you use ACRA, Crashlytics or similar libraries, you must initialize them AFTER CrashInfo! Installing anyway, but your original handler will not be called.");
                    }

                    application = (Application) context.getApplicationContext();
                    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                        @Override
                        public void uncaughtException(Thread thread, final Throwable throwable) {
                            Log.e(TAG, "App has crashed, executing CrashInfo's UncaughtExceptionHandler", throwable);

                            if (hasCrashedInTheLastSeconds(application)) {
                                Log.e(TAG, "App already crashed in the last 2 seconds, not starting custom error activity because we could enter a restart loop. Are you sure that your app does not crash directly on init?", throwable);
                                if (oldHandler != null) {
                                    oldHandler.uncaughtException(thread, throwable);
                                    return;
                                }
                            } else {
                                setLastCrashTimestamp(application, new Date().getTime());

                                if (errorActivityClass == null) {
                                    errorActivityClass = guessErrorActivityClass(application);
                                }

                                if (isStackTraceLikelyConflictive(throwable, errorActivityClass)) {
                                    Log.e(TAG, "Your application class or your error activity have crashed, the custom activity will not be launched!");
                                    if (oldHandler != null) {
                                        oldHandler.uncaughtException(thread, throwable);
                                        return;
                                    }
                                } else if (launchErrorActivityWhenInBackground || !isInBackground) {

                                    final Intent intent = new Intent(application, errorActivityClass);
                                    StringWriter sw = new StringWriter();
                                    PrintWriter pw = new PrintWriter(sw);
                                    throwable.printStackTrace(pw);
                                    String stackTraceString = sw.toString();

                                    if (stackTraceString.length() > MAX_STACK_TRACE_SIZE) {
                                        String disclaimer = " [stack trace too large]";
                                        stackTraceString = stackTraceString.substring(0, MAX_STACK_TRACE_SIZE - disclaimer.length()) + disclaimer;
                                    }

                                    if (enableAppRestart && restartActivityClass == null) {

                                        restartActivityClass = guessRestartActivityClass(application);
                                    } else if (!enableAppRestart) {
                                        restartActivityClass = null;
                                    }

                                    intent.putExtra(EXTRA_STACK_TRACE, stackTraceString);
                                    intent.putExtra(EXTRA_RESTART_ACTIVITY_CLASS, restartActivityClass);
                                    intent.putExtra(EXTRA_SHOW_ERROR_DETAILS, showErrorDetails);
                                    intent.putExtra(EXTRA_EVENT_LISTENER, eventListener);
                                    intent.putExtra(EXTRA_IMAGE_DRAWABLE_ID, defaultErrorActivityDrawableId);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    if (eventListener != null) {
                                        eventListener.onLaunchErrorActivity();
                                    }
                                    application.startActivity(intent);
                                }
                            }
                            final Activity lastActivity = lastActivityCreated.get();
                            if (lastActivity != null) {

                                lastActivity.finish();
                                lastActivityCreated.clear();
                            }
                            killCurrentProcess();
                        }
                    });
                    application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                        int currentlyStartedActivities = 0;

                        @Override
                        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                            if (activity.getClass() != errorActivityClass) {
                                lastActivityCreated = new WeakReference<>(activity);
                            }
                        }

                        @Override
                        public void onActivityStarted(Activity activity) {
                            currentlyStartedActivities++;
                            isInBackground = (currentlyStartedActivities == 0);
                        }

                        @Override
                        public void onActivityResumed(Activity activity) {
                        }

                        @Override
                        public void onActivityPaused(Activity activity) {
                        }

                        @Override
                        public void onActivityStopped(Activity activity) {
                            currentlyStartedActivities--;
                            isInBackground = (currentlyStartedActivities == 0);
                        }

                        @Override
                        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                        }

                        @Override
                        public void onActivityDestroyed(Activity activity) {
                        }
                    });

                    Log.i(TAG, "CrashInfo has been installed.");
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "An unknown error occurred while installing CrashInfo, it may not have been properly initialized. Please report this as a bug if needed.", t);
        }
    }


    public static boolean isShowErrorDetailsFromIntent(Intent intent) {
        return intent.getBooleanExtra(CrashInfo.EXTRA_SHOW_ERROR_DETAILS, true);
    }

    public static int getDefaultErrorActivityDrawableIdFromIntent(Intent intent) {
        return intent.getIntExtra(CrashInfo.EXTRA_IMAGE_DRAWABLE_ID, R.drawable.ic_launcher_s);
    }


    private static String getStackTraceFromIntent(Intent intent) {
        return intent.getStringExtra(CrashInfo.EXTRA_STACK_TRACE);
    }


    public static String getAllErrorDetailsFromIntent(Intent intent) {
        String errorDetails = "";
        errorDetails += CrashHelper.getInfo() + " \n \n";
        errorDetails += "Stack trace:  \n";
        errorDetails += getStackTraceFromIntent(intent);
        return errorDetails;
    }


    @SuppressWarnings("unchecked")
    public static Class<? extends Activity> getRestartActivityClassFromIntent(Intent intent) {
        Serializable serializedClass = intent.getSerializableExtra(CrashInfo.EXTRA_RESTART_ACTIVITY_CLASS);

        if (serializedClass != null && serializedClass instanceof Class) {
            return (Class<? extends Activity>) serializedClass;
        } else {
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    public static EventListener getEventListenerFromIntent(Intent intent) {
        Serializable serializedClass = intent.getSerializableExtra(CrashInfo.EXTRA_EVENT_LISTENER);

        if (serializedClass != null && serializedClass instanceof EventListener) {
            return (EventListener) serializedClass;
        } else {
            return null;
        }
    }


    public static void restartApplicationWithIntent(Activity activity, Intent intent, EventListener eventListener) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (eventListener != null) {
            eventListener.onRestartAppFromErrorActivity();
        }
        activity.finish();
        activity.startActivity(intent);
        killCurrentProcess();
    }


    public static void closeApplication(Activity activity, EventListener eventListener) {
        if (eventListener != null) {
            eventListener.onCloseAppFromErrorActivity();
        }
        activity.finish();
        killCurrentProcess();
    }


    private static boolean isStackTraceLikelyConflictive(Throwable throwable, Class<? extends Activity> activityClass) {
        do {
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                if ((element.getClassName().equals("android.app.ActivityThread") && element.getMethodName().equals("handleBindApplication")) || element.getClassName().equals(activityClass.getName())) {
                    return true;
                }
            }
        } while ((throwable = throwable.getCause()) != null);
        return false;
    }

    private static String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private static String getPackageName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return packageInfo.packageName;
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private static String getAndroidVersion() {
        //String release = Build.VERSION.RELEASE;
        //int sdkVersion = Build.VERSION.SDK_INT;
        return Build.VERSION.RELEASE;
    }

    private static String getAndroidLanguage() {
        //String release = Build.VERSION.RELEASE;
        //int sdkVersion = Build.VERSION.SDK_INT;
        return Locale.getDefault().toString();
    }


    private static String getDeviceModelName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }


    private static Class<? extends Activity> guessRestartActivityClass(Context context) {
        Class<? extends Activity> resolvedActivityClass;

        //If action is defined, use that
        resolvedActivityClass = getRestartActivityClassWithIntentFilter(context);

        //Else, get the default launcher activity
        if (resolvedActivityClass == null) {
            resolvedActivityClass = getLauncherActivity(context);
        }

        return resolvedActivityClass;
    }


    @SuppressWarnings("unchecked")
    private static Class<? extends Activity> getRestartActivityClassWithIntentFilter(Context context) {
        Intent searchedIntent = new Intent().setAction(INTENT_ACTION_RESTART_ACTIVITY).setPackage(context.getPackageName());
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(searchedIntent,
                PackageManager.GET_RESOLVED_FILTER);

        if (resolveInfos != null && resolveInfos.size() > 0) {
            ResolveInfo resolveInfo = resolveInfos.get(0);
            try {
                return (Class<? extends Activity>) Class.forName(resolveInfo.activityInfo.name);
            } catch (ClassNotFoundException e) {
                //Should not happen, print it to the log!
                Log.e(TAG, "Failed when resolving the restart activity class via intent filter, stack trace follows!", e);
            }
        }

        return null;
    }


    @SuppressWarnings("unchecked")
    private static Class<? extends Activity> getLauncherActivity(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            try {
                return (Class<? extends Activity>) Class.forName(intent.getComponent().getClassName());
            } catch (ClassNotFoundException e) {
                //Should not happen, print it to the log!
                Log.e(TAG, "Failed when resolving the restart activity class via getLaunchIntentForPackage, stack trace follows!", e);
            }
        }

        return null;
    }


    private static Class<? extends Activity> guessErrorActivityClass(Context context) {
        Class<? extends Activity> resolvedActivityClass;

        //If action is defined, use that
        resolvedActivityClass = getErrorActivityClassWithIntentFilter(context);

        //Else, get the default launcher activity
        if (resolvedActivityClass == null) {
            resolvedActivityClass = CrashActivity.class;
        }

        return resolvedActivityClass;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Activity> getErrorActivityClassWithIntentFilter(Context context) {
        Intent searchedIntent = new Intent().setAction(INTENT_ACTION_ERROR_ACTIVITY).setPackage(context.getPackageName());
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(searchedIntent,
                PackageManager.GET_RESOLVED_FILTER);

        if (resolveInfos != null && resolveInfos.size() > 0) {
            ResolveInfo resolveInfo = resolveInfos.get(0);
            try {
                return (Class<? extends Activity>) Class.forName(resolveInfo.activityInfo.name);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Failed when resolving the error activity class via intent filter, stack trace follows!", e);
            }
        }

        return null;
    }

    private static void killCurrentProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }


    private static void setLastCrashTimestamp(Context context, long timestamp) {
        context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE).edit().putLong(SHARED_PREFERENCES_FIELD_TIMESTAMP, timestamp).apply();
    }


    private static long getLastCrashTimestamp(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE).getLong(SHARED_PREFERENCES_FIELD_TIMESTAMP, -1);
    }


    private static boolean hasCrashedInTheLastSeconds(Context context) {
        long lastTimestamp = getLastCrashTimestamp(context);
        long currentTimestamp = new Date().getTime();

        return (lastTimestamp <= currentTimestamp && currentTimestamp - lastTimestamp < TIMESTAMP_DIFFERENCE_TO_AVOID_RESTART_LOOPS_IN_MILLIS);
    }


    public interface EventListener extends Serializable {
        void onLaunchErrorActivity();

        void onRestartAppFromErrorActivity();

        void onCloseAppFromErrorActivity();
    }
}