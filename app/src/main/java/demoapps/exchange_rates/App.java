package demoapps.exchange_rates;

import android.app.Application;
import android.content.Context;


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
    }

}
