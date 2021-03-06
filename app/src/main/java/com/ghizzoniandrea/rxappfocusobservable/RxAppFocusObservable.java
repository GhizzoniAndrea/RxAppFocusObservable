package com.ghizzoniandrea.rxappfocusobservable;

import android.app.Activity;
import android.app.Application;

import io.reactivex.Observable;
import io.reactivex.subjects.ReplaySubject;

/**
 * Created by ghizzoniandrea on 2017/3/30.
 */
public class RxAppFocusObservable {

    private boolean changingConfig;
    private int foregroundCounter;
    private Activity visibleActivity;

    private final ReplaySubject<Boolean> appFocusSubject = ReplaySubject.createWithSize(1);

    private final Application.ActivityLifecycleCallbacks callbacks = new DefaultActivityLifecycleCallbacks() {

        @Override
        public void onActivityStarted(Activity activity) {
            visibleActivity = activity;
            if (changingConfig) {
                // ignore activity start, just a config change
                changingConfig = false;
            } else {
                final boolean justBecomingVisible = !isVisible();
                foregroundCounter++;
                if (justBecomingVisible) {
                    appFocusSubject.onNext(true);
                }
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
            visibleActivity = null;
            if (activity.isChangingConfigurations()) {
                // ignore activity stop, just a config change
                changingConfig = true;
            } else {
                foregroundCounter--;
                if (!isVisible()) {
                    appFocusSubject.onNext(false);
                }
            }
        }

    };

    public RxAppFocusObservable(Application app) {
        app.registerActivityLifecycleCallbacks(callbacks);
    }

    /**
     * Returns an Observable that emits a Boolean indicating whether the app is currently visible, and again each time the app's visibility changes.
     */
    public Observable<Boolean> getAppFocus() {
        return appFocusSubject;
    }

    /**
     * Returns true if the app is currently visible, or false if not.
     */
    public boolean isVisible() {
        return foregroundCounter > 0;
    }

    /**
     * Returns the currently visible Activity, or null if none of the app's activities is currently visible.
     */
    public Activity getVisibleActivity() {
        return visibleActivity;
    }
}
