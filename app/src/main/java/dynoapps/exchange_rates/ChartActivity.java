package dynoapps.exchange_rates;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import dynoapps.exchange_rates.data.CurrencySource;
import dynoapps.exchange_rates.data.RatesHolder;
import dynoapps.exchange_rates.event.RatesEvent;
import dynoapps.exchange_rates.model.rates.BaseRate;
import dynoapps.exchange_rates.model.rates.BigparaRate;
import dynoapps.exchange_rates.model.rates.BuySellRate;
import dynoapps.exchange_rates.model.rates.DolarTlKurRate;
import dynoapps.exchange_rates.model.rates.EnparaRate;
import dynoapps.exchange_rates.model.rates.IRate;
import dynoapps.exchange_rates.model.rates.ParaGarantiRate;
import dynoapps.exchange_rates.model.rates.YahooRate;
import dynoapps.exchange_rates.model.rates.YapıKrediRate;
import dynoapps.exchange_rates.model.rates.YorumlarRate;
import dynoapps.exchange_rates.time.TimeIntervalManager;
import dynoapps.exchange_rates.util.RateUtils;
import dynoapps.exchange_rates.util.ViewUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Copyright 2016 Erdem OLKUN
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
public class ChartActivity extends BaseActivity {

    public static final String EXTRA_RATE_TYPE = "EXTRA_RATE_TYPE";
    private static final float THRESHOLD_ERROR_USD_TRY = 0.2f;
    private static final int DATA_COUNT = 20;
    @BindView(R.id.tv_chart_title)
    TextView tvTitle;
    @BindView(R.id.line_chart)
    LineChart lineChart;
    @BindView(R.id.swipe_to_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    SimpleDateFormat hourFormatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private
    @IRate.RateDef
    int rateType = IRate.USD;
    private long startMilis;
    private int chart_text_color;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private long getVisibleTimeInMilis() {
        return TimeIntervalManager.getPollingInterval() * DATA_COUNT;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rateType = getIntent().getExtras().getInt(EXTRA_RATE_TYPE, rateType);

        String title = getString(R.string.dollar_tl_graph);
        if (rateType == IRate.EUR) {
            title = getString(R.string.euro_tl_graph);
        } else if (rateType == IRate.EUR_USD) {
            title = getString(R.string.euro_usd_graph);
        } else if (rateType == IRate.ONS) {
            title = getString(R.string.ons_dollar_grapsh);
        }
        tvTitle.setText(title);

        setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        getActionBarToolbar().setNavigationOnClickListener(v -> finish());

        chart_text_color = ContextCompat.getColor(getApplicationContext(), android.R.color.black);
        startMilis = System.currentTimeMillis();
        initChart();


        List<RatesEvent<BaseRate>> cachedEvents = new ArrayList<>();
        for (CurrencySource currencySource : SourcesManager.getCurrencySources()) {
            if (currencySource != null && currencySource.isEnabled()) {
                RatesEvent ratesEvent = RatesHolder.getInstance().getLatestEvent(currencySource.getType());
                if (ratesEvent == null) continue;
                if (ratesEvent.fetchTime < startMilis) {
                    startMilis = ratesEvent.fetchTime;
                }
                cachedEvents.add(ratesEvent);
            }
        }
        for (RatesEvent<BaseRate> ratesEvent : cachedEvents) {
            update(ratesEvent.rates, ratesEvent.sourceType, ratesEvent.fetchTime);
        }

        swipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3);
        swipeRefreshLayout.setEnabled(true);
        int top = ViewUtils.calculateActionBarSize(this);
        int progressBarStartMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_start_margin);
        int progressBarEndMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_end_margin);
        swipeRefreshLayout.setProgressViewOffset(true, top + progressBarStartMargin, top + progressBarEndMargin);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            ProvidersManager.getInstance().triggerUpdate();
            AndroidSchedulers.mainThread().scheduleDirect(() -> swipeRefreshLayout.setRefreshing(false), 1, TimeUnit.SECONDS);
        });

        compositeDisposable.add(ProvidersManager.getInstance().getRatesEventPublishSubject()
                .observeOn(AndroidSchedulers.mainThread()).subscribe(ratesEvent -> {
                    List<BaseRate> rates = ratesEvent.rates;
                    update(rates, ratesEvent.sourceType, ratesEvent.fetchTime);
                }));
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    private void initChart() {

        lineChart.getDescription().setEnabled(false);

        // add an empty data object
        lineChart.setData(new LineData());
//        mChart.getXAxis().setDrawLabels(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getAxisRight().setDrawGridLines(true);

        lineChart.getXAxis().setLabelCount(4);
//        lineChart.getAxisRight().setAxisMaximum(3.48f);
//        lineChart.getAxisRight().setAxisMinimum(3.42f);
        lineChart.getAxisLeft().setEnabled(false);


        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                Calendar calendar = Calendar.getInstance();
                int time = (int) value;
                calendar.add(Calendar.MILLISECOND, time);
                Date date = calendar.getTime();

                return time > 0 ? hourFormatter.format(date) : "";
            }
        });

        lineChart.getAxisRight().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return RateUtils.valueToUI(value, rateType);
            }
        });

        lineChart.setScaleEnabled(false);
        lineChart.invalidate();

        lineChart.setExtraBottomOffset(12);
        lineChart.setExtraTopOffset(12);
        lineChart.setPinchZoom(false);

        LineData data = lineChart.getData();
        ArrayList<CurrencySource> sources = SourcesManager.getCurrencySources();
        for (CurrencySource source : sources) {
            if (SourcesManager.isAvgType(source.getType())) {
                data.addDataSet(createDataSet(source.getColor(), source.getName()));
            } else {
                data.addDataSet(createDataSet(source.getColor(), source.getName() + " " + getString(R.string.sell)));
                data.addDataSet(createDataSet(source.getColor(), source.getName() + " " + getString(R.string.buy)));
            }
        }

        Legend legend = lineChart.getLegend();
        legend.setTextSize(13);
        legend.setTextColor(chart_text_color);
        legend.setYOffset(6);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setWordWrapEnabled(true);
        legend.setXEntrySpace(10);

        lineChart.setHighlightPerTapEnabled(true);
        CustomMarkerView customMarkerView = new CustomMarkerView(this, R.layout.view_marker, rateType);
        customMarkerView.setOffset(ViewUtils.dpToPx(4), -customMarkerView.getMeasuredHeight() - ViewUtils.dpToPx(4));
        lineChart.setMarker(customMarkerView);

        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setTextColor(chart_text_color);
        lineChart.getAxisRight().setTextColor(chart_text_color);
    }

    private synchronized void update(List<BaseRate> rates, int source_type, long fetch_time_millis) {

        BaseRate rate = RateUtils.getRate(rates, rateType);
        CurrencySource currencySource = SourcesManager.getSource(source_type);
        if (currencySource == null) return;
        int chart_index = currencySource.getChartIndex();
        if (rate != null) {
            if (rate instanceof YorumlarRate) {
                addEntry(((YorumlarRate) rate).val_real_avg, chart_index, fetch_time_millis);
            } else if (rate instanceof EnparaRate) {
                addEntry(((EnparaRate) rate).valueSellReal, chart_index, fetch_time_millis);
                addEntry(((EnparaRate) rate).valueBuyReal, chart_index + 1, fetch_time_millis);
            } else if (rate instanceof BigparaRate) {
                addEntry(((BuySellRate) rate).valueSellReal, chart_index, fetch_time_millis);
                addEntry(((BuySellRate) rate).valueBuyReal, chart_index + 1, fetch_time_millis);
            } else if (rate instanceof DolarTlKurRate) {
                addEntry(((DolarTlKurRate) rate).val_real_avg, chart_index, fetch_time_millis);
            } else if (rate instanceof YapıKrediRate) {
                addEntry(((YapıKrediRate) rate).valueSellReal, chart_index, fetch_time_millis);
                addEntry(((YapıKrediRate) rate).valueBuyReal, chart_index + 1, fetch_time_millis);
            } else if (rate instanceof YahooRate) {
                addEntry(((YahooRate) rate).val_real_avg, chart_index, fetch_time_millis);
            } else if (rate instanceof ParaGarantiRate) {
                addEntry(((ParaGarantiRate) rate).val_real_avg, chart_index, fetch_time_millis);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_item_sources) {
            SourcesManager.selectSources(this);
            return true;
        } else if (id == R.id.menu_time_interval) {
            TimeIntervalManager.selectInterval(this);
            return true;
        } else if (id == R.id.menu_item_refresh) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Alert);
            builder.setCancelable(true);
            builder.setTitle(R.string.refresh);
            builder.setMessage(R.string.clear_sure_message);
            builder.setPositiveButton(R.string.sure, (dialog, which) -> {
                for (int i = 0; i < lineChart.getData().getDataSetCount(); i++) {
                    IDataSet iDataSet = lineChart.getData().getDataSetByIndex(i);
                    iDataSet.clear();

                }
                lineChart.getXAxis().resetAxisMaximum();
                lineChart.invalidate();
                lineChart.notifyDataSetChanged();
                startMilis = System.currentTimeMillis();
                lineChart.moveViewToX(0);
            });

            builder.setNegativeButton(R.string.ignore, null);
            AlertDialog dialog = builder.create();
            dialog.show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addEntry(float value, int chartIndex, long milis) {
        if (THRESHOLD_ERROR_USD_TRY > value) return;
        LineData data = lineChart.getData();
        long newX = ((milis - startMilis));

        Entry entry = new Entry(newX, value);
        data.addEntry(entry, chartIndex);
        data.notifyDataChanged();
        IDataSet dataSet = data.getDataSetByIndex(chartIndex);
        if (Math.abs(dataSet.getXMin() - dataSet.getXMax()) > getVisibleTimeInMilis() * 3
                && dataSet.getEntryCount() > DATA_COUNT) {
            dataSet.removeEntry(0);
        }

        // let the chart know it's data has changed
        lineChart.notifyDataSetChanged();

        //mChart.setVisibleYRangeMaximum(15, AxisDependency.LEFT);
        lineChart.setVisibleXRangeMaximum(getVisibleTimeInMilis());

        if (lineChart.getXAxis().getAxisMaximum() <= newX) {
            lineChart.moveViewToX(newX);
        } else if (lineChart.getVisibleXRange() < newX) {
            lineChart.moveViewToX(newX + lineChart.getVisibleXRange());
        } else {
            lineChart.invalidate();
        }
    }

    private LineDataSet createDataSet(@ColorInt int color, String name) {

        LineDataSet set = new LineDataSet(null, name);
        set.setCubicIntensity(0.1f);
        set.setDrawCircleHole(false);
        set.setCircleColor(color);
        set.setLineWidth(2f);
        set.setCircleRadius(2.5f);
        set.setDrawCircles(true);


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
    public int getLayoutId() {
        return R.layout.activity_chart;
    }

    @SuppressLint("ViewConstructor")
    static class CustomMarkerView extends MarkerView {

        private final TextView tvMarker;
        private final int rateType;

        public CustomMarkerView(Context context, int layoutResource, int rateType) {
            super(context, layoutResource);
            this.rateType = rateType;
            tvMarker = findViewById(R.id.tv_marker);
        }

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            String val = RateUtils.valueToUI(e.getY(), rateType);
            tvMarker.setText(val); // set the entry-value as the display text
        }
    }


}


