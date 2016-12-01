package dynoapps.exchange_rates;

import android.app.Application;
import android.content.Context;

import com.orhanobut.logger.AndroidLogTool;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;


/**
 * Created by erdemmac on 21/10/2016.
 */

public class App extends Application {
    private static App appInstance;

    public static App getInstance() {
        return appInstance;
    }

    public static Context context() {
        if (appInstance == null) return null;
        return appInstance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;
        Logger.init("ExchangeRates")                 // default PRETTYLOGGER or use just init()
                .methodCount(0)                 // default 2
                .hideThreadInfo()               // default shown
                .logLevel(PublishSettings.isAlphaOrDeveloper() ? LogLevel.FULL : LogLevel.NONE)        // default LogLevel.FULL
                .methodOffset(2)                // default 0
                .logTool(new AndroidLogTool()); // custom log tool, optional

    }

}
