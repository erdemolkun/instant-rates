package demoapps.exchangegraphics;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
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

    @BindView(R.id.line_usd_chart)
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

    private static final int INTERVAL = 2000;
    private static final int INTERVAL_ON_ERROR = 5000;


    private long startMilis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rates);
        ButterKnife.bind(this);

        startMilis = System.currentTimeMillis();
        initUsdChart();

        getHandler().post(runnableYorumlar);
        getHandler().post(runnableEnpara);

        vProgress.setVisibility(View.GONE);

    }

    private void initUsdChart() {

        Description description = new Description();
//        description.setPosition(10, 10);
        description.setTextSize(12f);
        description.setText("Dolar-TL Grafiği");
        description.setXOffset(8);
        description.setYOffset(8);
        description.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        //lineChart.setDescription(description);
        lineChart.getDescription().setEnabled(false);
        lineChart.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));

        // add an empty data object
        lineChart.setData(new LineData());
//        mChart.getXAxis().setDrawLabels(false);
//        mChart.getXAxis().setDrawGridLines(false);

        lineChart.getXAxis().setLabelCount(6);
//        lineChart.getAxisRight().setAxisMaximum(3.48f);
//        lineChart.getAxisRight().setAxisMinimum(3.42f);
        lineChart.getAxisLeft().setEnabled(false);
        final IAxisValueFormatter defaultXFormatter = lineChart.getXAxis().getValueFormatter();
        lineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return defaultXFormatter.getFormattedValue(value, axis) + ".sn";
            }

        });

        final IAxisValueFormatter defaultYFormatter = new DefaultAxisValueFormatter(3);
        lineChart.getAxisRight().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return defaultYFormatter.getFormattedValue(value, axis) + " TL";
            }
        });
        lineChart.setScaleEnabled(false);
        lineChart.invalidate();

        LineData data = lineChart.getData();


        ILineDataSet set0 = createSet(0);
        ILineDataSet set1 = createSet(1);
        ILineDataSet set2 = createSet(2);
        data.addDataSet(set0);
        data.addDataSet(set1);
        data.addDataSet(set2);
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
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setTextColor(ContextCompat.getColor(this, android.R.color.white));
        lineChart.getAxisRight().setTextColor(ContextCompat.getColor(this, android.R.color.white));

        lineChart.setPinchZoom(false);
//          this automatically refreshes the chart (calls invalidate())
        lineChart.moveViewToAnimated(data.getEntryCount() - 7, 250f, YAxis.AxisDependency.LEFT, 400);

//        lineChart.getAxisRight().setAxisMinimum(lineChart.getYMin()-0.01f);
//        lineChart.getAxisRight().setAxisMaximum(lineChart.getYMax()-0.01f);


    }

    private LineDataSet createSet(int chartIndex) {

        String label;
        switch (chartIndex) {
            case 0:
                label = "Piyasa";
                break;
            case 1:
                label = "Enpara Satış";
                break;
            case 2:
                label = "Enpara Alış";
                break;
            default:
                label = "Unknown";
                break;
        }

        LineDataSet set = new LineDataSet(null, label);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawCircleHole(false);
        set.setLineWidth(1f);
        set.setCircleRadius(2f);
        int color;
        if (chartIndex == 0) {
            color = Color.rgb(240, 0, 0);
        } else if (chartIndex == 1) {
            color = Color.rgb(0, 0, 240);
        } else {
            color = Color.rgb(0, 240, 0);
        }


        set.setCircleColor(color);
        set.setHighLightColor(Color.rgb(0, 0, 255));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(color);
//        set.setDrawFilled(true);
        set.setFillAlpha((int) (256 * 0.3f));
        set.setFillColor(color);
        set.setValueTextColor(color);
        set.setValueTextSize(13f);
        set.setDrawValues(false);

        return set;
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
                    //tvRateYorumlar.setText(val);
                    getHandler().postDelayed(runnableYorumlar, INTERVAL);
                    addEntry(rateUsd != null ? rateUsd.realValue : 0.0f, 0);

                } else {
//                    tvRateYorumlar.setText("Exception");
                    getHandler().postDelayed(runnableYorumlar, INTERVAL_ON_ERROR);
                }
                vProgress.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onFailure(Call<List<YorumlarRate>> call, Throwable t) {
                getHandler().postDelayed(runnableYorumlar, INTERVAL_ON_ERROR);
                vProgress.setVisibility(View.INVISIBLE);
//                tvRateYorumlar.setText("Exception");
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
                        addEntry(rateUsd != null ? rateUsd.value_buy_real : 0.0f, 2);
                    }
//                    tvRateEnpara.setText(val);

                } else {
                    getHandler().postDelayed(runnableEnpara, INTERVAL_ON_ERROR);
//                    tvRateEnpara.setText("Exception");
                }
                getHandler().postDelayed(runnableEnpara, INTERVAL);

            }

            @Override
            public void onFailure(Call<List<EnparaRate>> call, Throwable t) {
//                tvRateEnpara.setText("Exception");
                getHandler().postDelayed(runnableEnpara, INTERVAL_ON_ERROR);
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


