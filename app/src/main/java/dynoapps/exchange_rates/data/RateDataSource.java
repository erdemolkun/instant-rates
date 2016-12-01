package dynoapps.exchange_rates.data;

import dynoapps.exchange_rates.provider.IPollingSource;

/**
 * Created by erdemmac on 01/12/2016.
 */

public class RateDataSource {
    private IPollingSource iPollingSource;
    private String name;
    private boolean enabled;
    private int sourceType;

    public RateDataSource(String name, int sourceType) {
        this.name = name;
        this.sourceType = sourceType;
    }

    public int getSourceType() {
        return sourceType;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setPollingSource(IPollingSource iPollingSource) {
        this.iPollingSource = iPollingSource;
    }

    public IPollingSource getPollingSource() {
        return iPollingSource;
    }


    public interface Type {
        int YORUMLAR = 1;
        int ENPARA = 2;
        int BIGPARA = 3;
        int TLKUR = 4;
        int YAPIKREDI = 5;
    }
}