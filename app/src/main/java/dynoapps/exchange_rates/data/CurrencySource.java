package dynoapps.exchange_rates.data;

import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

import dynoapps.exchange_rates.App;
import dynoapps.exchange_rates.provider.IPollingSource;

/**
 * Created by erdemmac on 01/12/2016.
 */

public class CurrencySource {
    private IPollingSource iPollingSource;
    private String name;
    private boolean enabled;
    private int source_type;
    private int color;

    public CurrencySource(String name, int source_type, @ColorRes int colorRes, boolean enabled) {
        this.name = name;
        this.source_type = source_type;
        this.enabled = enabled;
        this.color= ContextCompat.getColor(App.context(),colorRes);
    }

    public int getColor() {
        return color;
    }

    public int getSource_type() {
        return source_type;
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

    public boolean isAvgType(){
        return source_type ==Type.YORUMLAR || source_type ==Type.BIGPARA || source_type ==Type.TLKUR || source_type ==Type.YAHOO;
    }

    public interface Type {
        int YORUMLAR = 1;
        int ENPARA = 2;
        int BIGPARA = 3;
        int TLKUR = 4;
        int YAPIKREDI = 5;
        int YAHOO = 6;

    }
}