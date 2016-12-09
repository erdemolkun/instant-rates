package dynoapps.exchange_rates;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.SparseArray;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import dynoapps.exchange_rates.model.rates.YapıKrediRate;
import dynoapps.exchange_rates.model.rates.YorumlarRate;
import dynoapps.exchange_rates.time.TimeIntervalManager;
import dynoapps.exchange_rates.util.RateUtils;
import dynoapps.exchange_rates.util.ViewUtils;

/**
 * Created by erdemmac on 24/11/2016.
 */

public class ChartActivity extends BaseActivity {

    public static final String EXTRA_RATE_TYPE = "EXTRA_RATE_TYPE";
    private static final float THRESHOLD_ERROR_USD_TRY = 0.2f;

    @BindView(R.id.tv_chart_title)
    TextView tvTitle;

    @BindView(R.id.line_chart)
    LineChart lineChart;

    @BindView(R.id.v_progress_wheel)
    View vProgress;

    private int rateType = IRate.USD;

    private long startMilis;
    SimpleDateFormat hourFormatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private int white;
    private int black;

    private static final int DATA_COUNT = 20;

    private long getVisibleTimeInMilis() {
        return TimeIntervalManager.getIntervalInMiliseconds() * DATA_COUNT;
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
        }
        tvTitle.setText(title);

        setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        white = ContextCompat.getColor(getApplicationContext(), android.R.color.white);
        black = ContextCompat.getColor(getApplicationContext(), android.R.color.black);
        startMilis = System.currentTimeMillis();
        initChart();

        vProgress.setVisibility(View.GONE);

        SparseArray<RatesEvent<BaseRate>> sparseArray = RatesHolder.getInstance().getAllRates();
        if (sparseArray != null) {
            List<RatesEvent<BaseRate>> cachedEvents = new ArrayList<>();
            for (int i = 0; i < sparseArray.size(); i++) {
                RatesEvent<BaseRate> ratesEvent = sparseArray.valueAt(i);
                CurrencySource currencySource = SourcesManager.getSource(ratesEvent.sourceType);
                if (currencySource != null && currencySource.isEnabled()) {
                    if (ratesEvent.fetchTime < startMilis) {
                        startMilis = ratesEvent.fetchTime;
                    }
                    cachedEvents.add(ratesEvent);
                }
            }
            for (RatesEvent<BaseRate> ratesEvent : cachedEvents) {
                update(ratesEvent.rates, ratesEvent.fetchTime);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void update(List<BaseRate> rates, long fetchMilis) {
        BaseRate rateUsd = RateUtils.getRate(rates, rateType);
        if (rateUsd != null) {
            if (rateUsd instanceof YapıKrediRate) {
                addEntry(((YapıKrediRate) rateUsd).value_sell_real, 5, fetchMilis);
            } else if (rateUsd instanceof DolarTlKurRate) {
                addEntry(((DolarTlKurRate) rateUsd).realValue, 4, fetchMilis);
            } else if (rateUsd instanceof YorumlarRate) {
                addEntry(((YorumlarRate) rateUsd).realValue, 0, fetchMilis);
            } else if (rateUsd instanceof EnparaRate) {
                addEntry(((EnparaRate) rateUsd).value_sell_real, 1, fetchMilis);
                addEntry(((EnparaRate) rateUsd).value_buy_real, 2, fetchMilis);
            } else if (rateUsd instanceof BigparaRate) {
                addEntry(((BuySellRate) rateUsd).value_sell_real, 3, fetchMilis);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RatesEvent ratesEvent) {
        List<BaseRate> rates = ratesEvent.rates;
        update(rates, ratesEvent.fetchTime);
    }


    private void initChart() {
//        Description description = new Description();
//        description.setTextSize(12f);
//        description.setText("Dolar-TL Grafiği");
//        description.setXOffset(8);
//        description.setYOffset(8);
//        description.setTextColor(ContextCompat.getColor(this, android.R.color.white));
//        lineChart.setDescription(description);
        lineChart.getDescription().setEnabled(false);
        lineChart.setBackgroundColor(ContextCompat.getColor(this, R.color.colorGraph));

        // add an empty data object
        lineChart.setData(new LineData());
//        mChart.getXAxis().setDrawLabels(false);
        lineChart.getXAxis().setDrawGridLines(true);

        lineChart.getXAxis().setLabelCount(4);
//        lineChart.getAxisRight().setAxisMaximum(3.48f);
//        lineChart.getAxisRight().setAxisMinimum(3.42f);
        lineChart.getAxisLeft().setEnabled(false);
        lineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Calendar calendar = Calendar.getInstance();
                int time = (int) value;
                calendar.add(Calendar.MILLISECOND, time);
                Date date = calendar.getTime();

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

        lineChart.setExtraBottomOffset(12);
        lineChart.setExtraTopOffset(12);
        lineChart.setPinchZoom(false);

        LineData data = lineChart.getData();
        data.addDataSet(createDataSet(0));
        data.addDataSet(createDataSet(1));
        data.addDataSet(createDataSet(2));
        data.addDataSet(createDataSet(3));
        data.addDataSet(createDataSet(4));
        data.addDataSet(createDataSet(5));

        Legend legend = lineChart.getLegend();
        legend.setTextSize(13);
        legend.setTextColor(black);
        legend.setYOffset(6);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setWordWrapEnabled(true);
        legend.setXEntrySpace(10);

        lineChart.setHighlightPerTapEnabled(true);
        CustomMarkerView customMarkerView = new CustomMarkerView(this, R.layout.view_marker, rateType);
        customMarkerView.setOffset(ViewUtils.dpToPx(4), -customMarkerView.getMeasuredHeight() - ViewUtils.dpToPx(4));
        lineChart.setMarker(customMarkerView);

        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setTextColor(black);
        lineChart.getAxisRight().setTextColor(black);

    }

    @SuppressLint("ViewConstructor")
    static class CustomMarkerView extends MarkerView {

        private TextView tvMarker;
        private int rateType;

        public CustomMarkerView(Context context, int layoutResource, int rateType) {
            super(context, layoutResource);
            this.rateType = rateType;
            tvMarker = (TextView) findViewById(R.id.tv_marker);
        }

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            String val = (rateType == IRate.USD || rateType == IRate.EUR) ?
                    App.context().getString(R.string.placeholder_tl, "" + e.getY()) : "" + e.getY();
            tvMarker.setText(val); // set the entry-value as the display text
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
            SourcesManager.selectSources(this);
            return true;
        } else if (id == R.id.menu_time_interval) {
            TimeIntervalManager.selectInterval(this);
            return true;
        } else if (id == R.id.menu_item_refresh) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle(R.string.refresh);
            builder.setMessage(R.string.clear_sure_message);
            builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    for (int i = 0; i < lineChart.getData().getDataSetCount(); i++) {
                        IDataSet iDataSet = lineChart.getData().getDataSetByIndex(i);
                        iDataSet.clear();

                    }
                    lineChart.getXAxis().resetAxisMaximum();
                    lineChart.invalidate();
                    lineChart.notifyDataSetChanged();
                    startMilis = System.currentTimeMillis();
                    lineChart.moveViewToX(0);
                }
            });

            builder.setNegativeButton(R.string.dismiss, null);
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

    private LineDataSet createDataSet(int chartIndex) {

        /**
         * TODO: get label via {@link SourcesManager}
         * */
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
            case 5:
                label = "Yapı Kredi";
                break;
            default:
                label = "Unknown";
                break;
        }

        LineDataSet set = new LineDataSet(null, label);
        set.setCubicIntensity(0.1f);
        set.setDrawCircleHole(false);
        set.setLineWidth(2f);
        set.setCircleRadius(2.5f);
        set.setDrawCircles(true);
        int color;
        if (chartIndex == 0) {
            color = ContextCompat.getColor(this, R.color.colorYorumlar);
        } else if (chartIndex == 1) {
            color = ContextCompat.getColor(this, R.color.colorEnpara);
        } else if (chartIndex == 2) {
            color = ContextCompat.getColor(this, R.color.colorEnpara);
        } else if (chartIndex == 3) {
            color = ContextCompat.getColor(this, R.color.colorBigPara);
        } else if (chartIndex == 4) {
            color = ContextCompat.getColor(this, R.color.colorDolarTlKur);
        } else if (chartIndex == 5) {
            color = ContextCompat.getColor(this, R.color.colorYapıKredi);
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
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_rates;
    }


}


