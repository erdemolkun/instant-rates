package demoapps.exchangegraphics;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import demoapps.exchangegraphics.data.EnparaRate;
import demoapps.exchangegraphics.data.Rate;
import demoapps.exchangegraphics.data.YorumlarRate;
import demoapps.exchangegraphics.service.Api;
import demoapps.exchangegraphics.service.EnparaService;
import demoapps.exchangegraphics.service.YorumlarService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class RatesActivity extends AppCompatActivity {


    private List<EnparaRate> enparaRates;
    private List<YorumlarRate> yorumlarRates;

    @BindView(R.id.line_chart)
    LineChart lineChart;

    @BindView(R.id.tv_rate_yorumlar)
    TextView tvRateYorumlar;

    @BindView(R.id.tv_rate_enpara)
    TextView tvRateEnpara;

    @BindView(R.id.v_progress_wheel)
    View vProgress;

    private Runnable runnableYorumlar = new Runnable() {
        @Override
        public void run() {
            fetchRatesYorumlar();
        }
    };

    private Runnable runnableEnpara = new Runnable() {
        @Override
        public void run() {
            fetchEnparaRates();
        }
    };

    private Handler handler;

    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }


    private void addEntry(float value, int chartIndex) {

        LineData data = lineChart.getData();
        long diffSeconds = (System.currentTimeMillis() - startMilis) / 1000;
        Entry entry = new Entry(diffSeconds, value);
        data.addEntry(entry, chartIndex);
        data.notifyDataChanged();

        // let the chart know it's data has changed
        lineChart.notifyDataSetChanged();

        //mChart.setVisibleYRangeMaximum(15, AxisDependency.LEFT);
        lineChart.setVisibleXRangeMaximum(120);
//            // this automatically refreshes the chart (calls invalidate())
        lineChart.moveViewTo(data.getEntryCount() - 7, 50f, YAxis.AxisDependency.LEFT);

    }

    private LineDataSet createSet(int chartIndex) {

        LineDataSet set = new LineDataSet(null, chartIndex == 0 ? "Yorumlar" : "Enpara-Buy");
        set.setLineWidth(1f);
        set.setCircleRadius(2f);
        if (chartIndex == 0) {
            set.setColor(Color.rgb(240, 99, 99));
            set.setCircleColor(Color.rgb(240, 99, 99));
        } else {
            set.setColor(Color.rgb(0, 240, 0));
            set.setCircleColor(Color.rgb(0, 240, 0));
        }

        set.setHighLightColor(Color.rgb(190, 190, 190));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);
        set.setDrawValues(false);
        return set;
    }

    private long startMilis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rates);
        ButterKnife.bind(this);

        startMilis = System.currentTimeMillis();
        lineChart.setDrawGridBackground(false);
        lineChart.getDescription().setEnabled(false);

        // add an empty data object
        lineChart.setData(new LineData());
//        mChart.getXAxis().setDrawLabels(false);
//        mChart.getXAxis().setDrawGridLines(false);

        lineChart.getXAxis().setLabelCount(6);
//        lineChart.getAxisRight().setAxisMaximum(3.48f);
//        lineChart.getAxisRight().setAxisMinimum(3.42f);
        lineChart.getAxisLeft().setEnabled(false);
//        lineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//
//                return value + "";
//            }
//
//        });
        lineChart.invalidate();


        LineData data = lineChart.getData();


        ILineDataSet set0 = createSet(0);
        ILineDataSet set1 = createSet(1);
        data.addDataSet(set0);
        data.addDataSet(set1);

        getHandler().post(runnableYorumlar);
        getHandler().post(runnableEnpara);

        vProgress.setVisibility(View.GONE);

    }

    private void fetchRatesYorumlar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (isDestroyed()) return;
        }
        if (isFinishing()) return;
        //vProgress.setVisibility(View.VISIBLE);
        YorumlarService yorumlarService = Api.getYorumlarApi().create(YorumlarService.class);
        Call<List<YorumlarRate>> call = yorumlarService.getWithType("ons");
        call.enqueue(new Callback<List<YorumlarRate>>() {
            @Override
            public void onResponse(Call<List<YorumlarRate>> call, Response<List<YorumlarRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<YorumlarRate> rates = response.body();
                    yorumlarRates = rates;
                    YorumlarRate rateUsd = null;
                    String val = "";
                    for (Rate rate : rates) {
                        if (rate.rateType == Rate.RateTypes.USD || rate.rateType == Rate.RateTypes.EUR) {
                            val += rate.toString() + "\n";
                        }
                        if (rate.rateType == Rate.RateTypes.USD) {
                            rateUsd = (YorumlarRate) rate;
                        }

                    }
                    tvRateYorumlar.setText(val);
                    getHandler().postDelayed(runnableYorumlar, 2000);
                    addEntry(rateUsd != null ? rateUsd.realValue : 0.0f, 0);

                } else {
                    tvRateYorumlar.setText("Exception");
                    getHandler().postDelayed(runnableYorumlar, 5000);
                }
                vProgress.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onFailure(Call<List<YorumlarRate>> call, Throwable t) {
                getHandler().postDelayed(runnableYorumlar, 5000);
                vProgress.setVisibility(View.INVISIBLE);
                tvRateYorumlar.setText("Exception");
            }
        });
    }

    private void fetchEnparaRates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (isDestroyed()) return;
        }
        if (isFinishing()) return;
        final EnparaService enparaService = Api.getEnparaApi().create(EnparaService.class);
        Call<List<EnparaRate>> call = enparaService.getValues();
        call.enqueue(new Callback<List<EnparaRate>>() {
            @Override
            public void onResponse(Call<List<EnparaRate>> call, Response<List<EnparaRate>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<EnparaRate> rates = response.body();
                    enparaRates = rates;
                    EnparaRate rateUsd = null;
                    String val = "";
                    for (Rate rate : rates) {
                        if (rate.rateType == Rate.RateTypes.USD || rate.rateType == Rate.RateTypes.EUR) {
                            val += rate.toString();
                        }
                        if (rate.rateType == Rate.RateTypes.USD) {
                            rateUsd = (EnparaRate) rate;
                        }
                        addEntry(rateUsd != null ? rateUsd.value_sell_real : 0.0f, 1);
                    }
                    tvRateEnpara.setText(val);

                } else {
                    getHandler().postDelayed(runnableEnpara, 5000);
                    tvRateEnpara.setText("Exception");
                }
                getHandler().postDelayed(runnableEnpara, 2000);

            }

            @Override
            public void onFailure(Call<List<EnparaRate>> call, Throwable t) {
                tvRateEnpara.setText("Exception");
                getHandler().postDelayed(runnableEnpara, 5000);
            }
        });
    }


    @Override
    protected void onDestroy() {
        getHandler().removeCallbacks(runnableYorumlar);
        getHandler().removeCallbacks(runnableEnpara);
        super.onDestroy();
    }


}


