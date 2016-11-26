package demoapps.exchange_rates.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class DynamicDataResponse {

    public ArrayList<String> secKeys;
    public DataResponse PiyasaData;

    public static class DataResponse {
        @SerializedName("XU100 Index")
        public Data XU100_Index;

        @SerializedName("USDTRY Curncy")
        public Data USDTRY_Curncy;

        @SerializedName("EURTRY Curncy")
        public Data EURTRY_Curncy;

        @SerializedName("EURUSD Curncy")
        public Data EURUSD_Curncy;
    }

    public static class Data {
        public Integer sec_type;
        public String son_fiyat;
        public String degisim_fiyat;
        public String degisim_yuzde;
        public Integer degisim;
    }
}
