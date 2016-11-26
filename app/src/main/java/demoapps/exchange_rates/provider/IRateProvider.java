package demoapps.exchange_rates.provider;

/**
 * Created by erdemmac on 25/11/2016.
 */

public interface IRateProvider {
    void start();

    void stop();

    void cancel();

    interface Callback<T> {
        void onResult(T value);

        void onError();
    }
}



