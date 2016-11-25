package demoapps.exchangegraphics.provider;

import java.util.ArrayList;
import java.util.List;

import demoapps.exchangegraphics.data.Rate;

/**
 * Created by erdemmac on 25/11/2016.
 */

public interface IRateProvider{
    void start();

    void stop();

    interface Callback<T extends Rate> {
        void onResult(List<T> rates);
        void onError();
    }
}



