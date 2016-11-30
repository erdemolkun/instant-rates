package dynoapps.exchange_rates;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import dynoapps.exchange_rates.data.BuySellRate;
import dynoapps.exchange_rates.data.DolarTlKurRate;
import dynoapps.exchange_rates.data.Rate;
import dynoapps.exchange_rates.data.YapıKrediRate;
import dynoapps.exchange_rates.data.YorumlarRate;
import dynoapps.exchange_rates.provider.BigparaRateProvider;
import dynoapps.exchange_rates.provider.DolarTlKurRateProvider;
import dynoapps.exchange_rates.provider.EnparaRateProvider;
import dynoapps.exchange_rates.provider.IPollingSource;
import dynoapps.exchange_rates.provider.YapıKrediRateProvider;
import dynoapps.exchange_rates.provider.YorumlarRateProvider;
import dynoapps.exchange_rates.util.ViewUtils;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class RatesActivity extends AppCompatActivity {

    @BindView(R.id.line_usd_chart)
    LineChart usdLineChart;

    @BindView(R.id.v_progress_wheel)
    View vProgress;

    private long startMilis;
    ArrayList<IPollingSource> providers = new ArrayList<>();
    ArrayList<DataSource> dataSources = new ArrayList<>();
    SimpleDateFormat hourFormatter = new SimpleDateFormat("hh:mm:ss", Locale.ENGLISH);
    private int white;

    private static final float threshold_error_usd_try = 0.2f;

    public static <E> E getInstance(List<E> list, Class clazz) {
        for (E e : list) {
            if (clazz.isInstance(e)) {
                return e;
            }
        }
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rates);
        ButterKnife.bind(this);
        white = ContextCompat.getColor(getApplicationContext(), android.R.color.white);
        startMilis = System.currentTimeMillis();
        initUsdChart();

        vProgress.setVisibility(View.GONE);

        providers.add(new YorumlarRateProvider(new ProviderSourceCallbackAdapter<List<YorumlarRate>>() {
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
        providers.add(new EnparaRateProvider(new ProviderSourceCallbackAdapter<List<BuySellRate>>() {
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
                new BigparaRateProvider(new ProviderSourceCallbackAdapter<List<BuySellRate>>() {
                    @Override
                    public void onResult(List<BuySellRate> value) {
                        addEntry(value.get(0).value_sell_real, 3);
                    }
                }));

        providers.add(new DolarTlKurRateProvider(new ProviderSourceCallbackAdapter<List<DolarTlKurRate>>() {
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

        YapıKrediRateProvider yapıKrediRateProvider = new YapıKrediRateProvider(new ProviderSourceCallbackAdapter<List<YapıKrediRate>>() {
            @Override
            public void onResult(List<YapıKrediRate> value) {
                super.onResult(value);
            }
        });
        yapıKrediRateProvider.start();

        initDataSourceSelections();
        refreshSources();
    }

    private void updateSourceStates() {
        String sources = Prefs.getSources(this);
        if (!TextUtils.isEmpty(sources)) {
            String[] splits = sources.split(";");
            for (String str : splits) {
                if (TextUtils.isEmpty(str)) continue;
                int sourceType;
                try {
                    sourceType = Integer.parseInt(str);
                    for (DataSource dataSource : dataSources) {
                        if (dataSource.getSourceType() == sourceType) {
                            dataSource.setEnabled(true);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } else {
            for (DataSource dataSource : dataSources) {
                dataSource.setEnabled(true);
            }
        }
    }

    private void saveSources(List<DataSource> dataSources) {
        String sources = "";
        for (int i = 0; i < dataSources.size(); i++) {
            DataSource dataSource = dataSources.get(i);
            if (dataSource.isEnabled()) {
                sources += dataSource.getSourceType() + ";";
            }
        }
        Prefs.saveSources(getApplicationContext(), sources);
    }

    private void initDataSourceSelections() {
        DataSource dataSource0 = new DataSource("Yorumlar", SourceType.YORUMLAR);
        DataSource dataSource1 = new DataSource("Enpara", SourceType.ENPARA);
        DataSource dataSource2 = new DataSource("Bigpara", SourceType.BIGPARA);
        DataSource dataSource3 = new DataSource("TlKur", SourceType.TLKUR);

        dataSources.add(dataSource0);
        dataSources.add(dataSource1);
        dataSources.add(dataSource2);
        dataSources.add(dataSource3);

        dataSource0.setiPollingSource(getInstance(providers, YorumlarRateProvider.class));
        dataSource1.setiPollingSource(getInstance(providers, EnparaRateProvider.class));
        dataSource2.setiPollingSource(getInstance(providers, BigparaRateProvider.class));
        dataSource3.setiPollingSource(getInstance(providers, DolarTlKurRateProvider.class));
        updateSourceStates();
    }

    private void refreshSources() {
        for (DataSource dataSource : dataSources) {
            IPollingSource iPollingSource = dataSource.getRateProvider();
            if (dataSource.isEnabled()) {
                iPollingSource.start();
            } else {
                iPollingSource.stop();
            }
        }
    }

    private void initUsdChart() {

//        Description description = new Description();
//        description.setTextSize(12f);
//        description.setText("Dolar-TL Grafiği");
//        description.setXOffset(8);
//        description.setYOffset(8);
//        description.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        //usdLineChart.setDescription(description);
        usdLineChart.getDescription().setEnabled(false);
        usdLineChart.setBackgroundColor(ContextCompat.getColor(this, R.color.colorGraph));

        // add an empty data object
        usdLineChart.setData(new LineData());
//        mChart.getXAxis().setDrawLabels(false);
        usdLineChart.getXAxis().setDrawGridLines(true);

        usdLineChart.getXAxis().setLabelCount(6);
//        usdLineChart.getAxisRight().setAxisMaximum(3.48f);
//        usdLineChart.getAxisRight().setAxisMinimum(3.42f);
        usdLineChart.getAxisLeft().setEnabled(false);
        usdLineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Calendar calendar = Calendar.getInstance();
                int time = (int) value;
                calendar.add(Calendar.SECOND, time);
                Date date = calendar.getTime();

                int minutes = time / (60);
                int seconds = (time) % 60;
//                return time > 0 ? String.format(Locale.ENGLISH, "%d:%02d", minutes, seconds) : "";
                return time > 0 ? hourFormatter.format(date) : "";
            }

        });

        final IAxisValueFormatter axisValueFormatter = new DefaultAxisValueFormatter(3);
        usdLineChart.getAxisRight().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return axisValueFormatter.getFormattedValue(value, axis) + " TL";
            }
        });
        usdLineChart.setScaleEnabled(false);
        usdLineChart.invalidate();

        usdLineChart.setExtraBottomOffset(12);
        usdLineChart.setExtraTopOffset(12);
        usdLineChart.setPinchZoom(false);

        LineData data = usdLineChart.getData();
        data.addDataSet(createDataSet(0));
        data.addDataSet(createDataSet(1));
        data.addDataSet(createDataSet(2));
        data.addDataSet(createDataSet(3));
        data.addDataSet(createDataSet(4));

        Legend legend = usdLineChart.getLegend();
        legend.setTextSize(13);
        legend.setTextColor(white);
        legend.setYOffset(6);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setWordWrapEnabled(true);
        legend.setXEntrySpace(10);

        usdLineChart.setHighlightPerTapEnabled(true);
        CustomMarkerView customMarkerView = new CustomMarkerView(this, R.layout.view_marker);
        customMarkerView.setOffset(ViewUtils.dpToPx(4), -customMarkerView.getMeasuredHeight() - ViewUtils.dpToPx(4));
        usdLineChart.setMarker(customMarkerView);

        usdLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        usdLineChart.getXAxis().setTextColor(white);
        usdLineChart.getAxisRight().setTextColor(white);

    }

    @SuppressLint("ViewConstructor")
    static class CustomMarkerView extends MarkerView {

        private TextView tvMarker;

        public CustomMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            tvMarker = (TextView) findViewById(R.id.tv_marker);
        }

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            tvMarker.setText("" + e.getY()); // set the entry-value as the display text
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rates, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_item_sources) {
            selectSources();
            return true;
        } else if (id == R.id.menu_item_refresh) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle(R.string.refresh);
            builder.setMessage(R.string.clear_sure_message);
            builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    for (int i = 0; i < usdLineChart.getData().getDataSetCount(); i++) {
                        IDataSet iDataSet = usdLineChart.getData().getDataSetByIndex(i);
                        iDataSet.clear();

                    }
                    usdLineChart.getXAxis().resetAxisMaximum();
                    usdLineChart.invalidate();
                    usdLineChart.notifyDataSetChanged();
                    startMilis = System.currentTimeMillis();
                    usdLineChart.moveViewToX(0);
                }
            });

            builder.setNegativeButton(R.string.dismiss, null);
            AlertDialog dialog = builder.create();
            dialog.show();


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
        String[] data_set_names = new String[dataSources.size()];
        for (int i = 0; i < dataSources.size(); i++) {
            data_set_names[i] = dataSources.get(i).getName();
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
                saveSources(dataSources);
            }
        });

        builder.setNegativeButton(R.string.dismiss, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static final int MAX_SECONDS = 240; // 4 mins
    private static final int VISIBLE_SECONDS = 120; // 2 mins

    private void addEntry(float value, int chartIndex) {
        if (threshold_error_usd_try > value) return;
        LineData data = usdLineChart.getData();
        int newX = (int) (((System.currentTimeMillis() - startMilis) / 1000));

        Entry entry = new Entry(newX, value);
        data.addEntry(entry, chartIndex);
        data.notifyDataChanged();
        IDataSet dataSet = data.getDataSetByIndex(chartIndex);
        if (Math.abs(dataSet.getXMin() - dataSet.getXMax()) > MAX_SECONDS && dataSet.getEntryCount() > MAX_SECONDS / 2) {
            dataSet.removeEntry(0);
        }

        // let the chart know it's data has changed
        usdLineChart.notifyDataSetChanged();

        //mChart.setVisibleYRangeMaximum(15, AxisDependency.LEFT);
        usdLineChart.setVisibleXRangeMaximum(VISIBLE_SECONDS);

        if (usdLineChart.getXAxis().getAxisMaximum() <= newX) {
            usdLineChart.moveViewToX(newX);
        } else if (usdLineChart.getVisibleXRange() < newX) {
            usdLineChart.moveViewToX(newX + usdLineChart.getVisibleXRange());
        } else {
            usdLineChart.invalidate();
        }
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
            color = ContextCompat.getColor(this, R.color.colorYorumlar);
        } else if (chartIndex == 1) {
            color = ContextCompat.getColor(this, R.color.colorEnpara);
        } else if (chartIndex == 2) {
            color = ContextCompat.getColor(this, R.color.colorEnpara);
        } else if (chartIndex == 3) {
            color = ContextCompat.getColor(this, R.color.colorDolarTlKur);
        } else {
            color = ContextCompat.getColor(this, R.color.colorBigPara);
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
            for (IPollingSource iPollingSource : providers) {
                iPollingSource.stop();
            }
        }
        super.onDestroy();
    }

    /***
     * Adapter class for {@link IPollingSource.SourceCallback}
     */
    static class ProviderSourceCallbackAdapter<T> implements IPollingSource.SourceCallback<T> {
        @Override
        public void onResult(T value) {

        }

        @Override
        public void onError() {

        }
    }

    static class DataSource {
        private IPollingSource iPollingSource;
        private String name;
        private boolean enabled;
        private int sourceType;

        public DataSource(String name, int sourceType) {
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

        public void setiPollingSource(IPollingSource iPollingSource) {
            this.iPollingSource = iPollingSource;
        }

        public IPollingSource getRateProvider() {
            return iPollingSource;
        }
    }

    interface SourceType {
        int YORUMLAR = 1;
        int ENPARA = 2;
        int BIGPARA = 3;
        int TLKUR = 4;
    }
}


