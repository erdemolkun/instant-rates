package dynoapps.exchange_rates.provider;

/**
 * Created by erdemmac on 05/12/2016.
 */

/***
 * Adapter class for {@link IPollingSource.SourceCallback}
 */
public class ProviderSourceCallbackAdapter<T> implements IPollingSource.SourceCallback<T> {
    @Override
    public void onResult(T value,int type) {

    }

    @Override
    public void onError() {

    }
}