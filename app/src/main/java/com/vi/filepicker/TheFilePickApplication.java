package com.vi.filepicker;

import android.app.Application;

/**
 * Created by VickyBoy on 21-Feb-18.
 */

public class TheFilePickApplication extends Application {
    private static TheFilePickApplication mAppInstance;

    /**
     * To get singleton instance of {@link TheFilePickApplication}
     *
     * @return ModelApp instance
     */
    public static TheFilePickApplication getAppInstance() {
        return mAppInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAppInstance = this;
    }
}
