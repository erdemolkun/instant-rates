package demoapps.exchange_rates;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
import com.github.mikephil.charting.interfaces.datasets.IDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import demoapps.exchange_rates.data.BuySellRate;
import demoapps.exchange_rates.data.DolarTlKurRate;
import demoapps.exchange_rates.data.Rate;
import demoapps.exchange_rates.data.YorumlarRate;
import demoapps.exchange_rates.provider.BigparaRateProvider;
import demoapps.exchange_rates.provider.DolarTlKurRateProvider;
import demoapps.exchange_rates.provider.EnparaRateProvider;
import demoapps.exchange_rates.provider.IRateProvider;
import demoapps.exchange_rates.provider.YorumlarRateProvider;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class RatesActivity extends AppCompatActivity {

    @BindView(R.id.line_usd_chart)
    LineChart lineChart;

    @BindView(R.id.v_progress_wheel)
    View vProgress;

    private long startMilis;
    ArrayList<IRateProvider> providers = new ArrayList<>();
    ArrayList<DataSource> dataSources = new ArrayList<>();
    SimpleDateFormat hourFormatter = new SimpleDateFormat("hh:mm:ss",Locale.ENGLISH);

    private static float threshold_error_usd_try = 0.2f;

    static final String[] data_set_names = new String[]{
            "Yorumlar.Altin.in",
            "Enpara",
            "Bigpara",
            "dolar.tlkur.com",
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rates);
        ButterKnife.bind(this);

        startMilis = System.currentTimeMillis();
        initUsdChart();

        vProgress.setVisibility(View.GONE);

        providers.add(new YorumlarRateProvider(new ProviderCallbackAdapter<List<YorumlarRate>>() {
            @Override
            public void onResult(List<YorumlarRate> rates) {
                YorumlarRate rateUsd = null;
                for (Rate rate : rates) {
                    if (rate.rateType == Rate.RateTypes.USD) {
                        rateUsd = (YorumlarRate) rate;
                    }
                }
                addEntry(rateUsd != null ? rateUsd.realValue : 0.0f, 0);
            }
        }));
        providers.add(new EnparaRateProvider(new ProviderCallbackAdapter<List<BuySellRate>>() {
            @Override
            public void onResult(List<BuySellRate> rates) {
                BuySellRate rateUsd = null;
                for (Rate rate : rates) {
                    if (rate.rateType == Rate.RateTypes.USD) {
                        rateUsd = (BuySellRate) rate;
                    }
                    addEntry(rateUsd != null ? rateUsd.value_sell_real : 0.0f, 1);
                    addEntry(rateUsd != null ? rateUsd.value_buy_real : 0.0f, 2);
                }
            }
        }));

        providers.add(
                new BigparaRateProvider(new ProviderCallbackAdapter<List<BuySellRate>>() {
                    @Override
                    public void onResult(List<BuySellRate> value) {
                        addEntry(value.get(0).value_sell_real, 3);
                    }
                }));

        providers.add(new DolarTlKurRateProvider(new ProviderCallbackAdapter<List<DolarTlKurRate>>() {
            @Override
            public void onResult(List<DolarTlKurRate> rates) {
                DolarTlKurRate rateUsd = null;
                for (Rate rate : rates) {
                    if (rate.rateType == Rate.RateTypes.USD) {
                        rateUsd = (DolarTlKurRate) rate;
                    }

                }
                addEntry(rateUsd != null ? rateUsd.realValue : 0.0f, 4);
            }
        }));
        initDataSourceSelections();
        refreshSources();
    }

    private void initDataSourceSelections() {
        for (int i = 0; i < data_set_names.length; i++) {
            DataSource dataSource = new DataSource(data_set_names[i]);
            dataSource.setEnabled(true); // TODO: 26/11/2016 make persistent
            dataSource.setiRateProvider(providers.get(i));
            dataSources.add(dataSource);
        }
    }

    private void refreshSources() {
        for (DataSource dataSource : dataSources) {
            IRateProvider iRateProvider = dataSource.getRateProvider();
            if (dataSource.isEnabled()) {
                iRateProvider.start();
            } else {
                iRateProvider.stop();
            }
        }
    }

    private void initUsdChart() {

        Description description = new Description();
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
        lineChart.getXAxis().setDrawGridLines(true);

        lineChart.getXAxis().setLabelCount(6);
//        lineChart.getAxisRight().setAxisMaximum(3.48f);
//        lineChart.getAxisRight().setAxisMinimum(3.42f);
        lineChart.getAxisLeft().setEnabled(false);
        lineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Calendar calendar = Calendar.getInstance();

                int time = (int) value;
                calendar.add(Calendar.SECOND, time);
                Date date = calendar.getTime();

//                int minutes = time / (60);
//                int seconds = (time) % 60;
                //return time > 0 ? String.format(Locale.ENGLISH, "%d:%02d", minutes, seconds) : "";
                return time > 0 ? hourFormatter.format(date) : "";
            }

        });

        final IAxisValueFormatter axisValueFormatter = new DefaultAxisValueFormatter(3);
        lineChart.getAxisRight().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return axisValueFormatter.getFormattedValue(value, axis) + " TL";
            }
        });
        lineChart.setScaleEnabled(false);
        lineChart.invalidate();

        LineData data = lineChart.getData();


        data.addDataSet(createDataSet(0));
        data.addDataSet(createDataSet(1));
        data.addDataSet(createDataSet(2));
        data.addDataSet(createDataSet(3));
        data.addDataSet(createDataSet(4));

        lineChart.setExtraBottomOffset(12);
        lineChart.setExtraTopOffset(12);
        lineChart.setPinchZoom(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rates, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item_sources) {
            selectSources();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Boolean array for initial enabled items
    boolean[] temp_data_source_states;

    private void selectSources() {
        temp_data_source_states = new boolean[dataSources.size()];
        for (int i = 0; i < temp_data_source_states.length; i++) {
            temp_data_source_states[i] = dataSources.get(i).isEnabled();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMultiChoiceItems(data_set_names, temp_data_source_states, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                temp_data_source_states[which] = isChecked;
            }
        });

        builder.setCancelable(true);
        builder.setTitle(R.string.select_sources);
        builder.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < dataSources.size(); i++) {
                    dataSources.get(i).setEnabled(temp_data_source_states[i]);
                }
                refreshSources();
            }
        });

        builder.setNegativeButton(R.string.dismiss, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static final int MAX_SECONDS = 240; // 4 mins
    private static final int VISIBLE_SECONDS = 60; // 1 mins

    private void addEntry(float value, int chartIndex) {
        if (threshold_error_usd_try>value)return;
        LineData data = lineChart.getData();
        int diffSeconds = (int) (((System.currentTimeMillis() - startMilis) / 1000));

        Entry entry = new Entry(diffSeconds, value);
        data.addEntry(entry, chartIndex);
        data.notifyDataChanged();
        IDataSet dataSet = data.getDataSetByIndex(chartIndex);
        if (Math.abs(dataSet.getXMin() - dataSet.getXMax()) > MAX_SECONDS && dataSet.getEntryCount() > MAX_SECONDS / 2) {
            dataSet.removeEntry(0);
        }

        // let the chart know it's data has changed
        lineChart.notifyDataSetChanged();

        //mChart.setVisibleYRangeMaximum(15, AxisDependency.LEFT);
        lineChart.setVisibleXRangeMaximum(VISIBLE_SECONDS);

        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setTextColor(ContextCompat.getColor(this, android.R.color.white));
        lineChart.getAxisRight().setTextColor(ContextCompat.getColor(this, android.R.color.white));

//          this automatically refreshes the chart (calls invalidate())
        lineChart.moveViewToX(data.getEntryCount());

//        lineChart.getAxisRight().setAxisMinimum(lineChart.getYMin()-0.01f);
//        lineChart.getAxisRight().setAxisMaximum(lineChart.getYMax()-0.01f);

    }

    private LineDataSet createDataSet(int chartIndex) {

        String label;
        switch (chartIndex) {
            case 0:
                label = "yorumlar.altin.in";
                break;
            case 1:
                label = "Enpara Satış";
                break;
            case 2:
                label = "Enpara Alış";
                break;
            case 3:
                label = "Bigpara";
                break;
            case 4:
                label = "dolar.tlkur.com";
                break;
            default:
                label = "Unknown";
                break;
        }

        LineDataSet set = new LineDataSet(null, label);
        set.setCubicIntensity(0.1f);
        set.setDrawCircleHole(false);
        set.setLineWidth(1.5f);
        set.setCircleRadius(2f);
        set.setDrawCircles(true);
        int color;
        if (chartIndex == 0) {
            color = Color.rgb(179, 138, 44);
        } else if (chartIndex == 1) {
            color = Color.rgb(169, 85, 156);
        } else if (chartIndex == 2) {
            color = Color.rgb(169, 85, 156);
        } else if (chartIndex == 3) {
            color = Color.rgb(252, 131, 36);
        } else {
            color = Color.rgb(120, 120, 40);
        }


        set.setCircleColor(color);
        set.setHighLightColor(Color.rgb(155, 155, 155));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(color);
//        set.setDrawFilled(true);
        set.setFillAlpha((int) (256 * 0.3f));
        set.setFillColor(color);
        set.setValueTextColor(color);
        set.setValueTextSize(16f);
        set.setDrawValues(false);
        return set;
    }


    @Override
    protected void onDestroy() {
        if (providers != null) {
            for (IRateProvider iRateProvider : providers) {
                iRateProvider.stop();
            }
        }
        super.onDestroy();
    }

    /***
     * Adapter class for {@link demoapps.exchange_rates.provider.IRateProvider.Callback}
     */
    static class ProviderCallbackAdapter<T> implements IRateProvider.Callback<T> {
        @Override
        public void onResult(T value) {

        }

        @Override
        public void onError() {

        }
    }

    static class DataSource {
        private IRateProvider iRateProvider;
        private String name;
        private boolean enabled;

        public DataSource(String name) {
            this.name = name;
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

        public void setiRateProvider(IRateProvider iRateProvider) {
            this.iRateProvider = iRateProvider;
        }

        public IRateProvider getRateProvider() {
            return iRateProvider;
        }
    }


}


