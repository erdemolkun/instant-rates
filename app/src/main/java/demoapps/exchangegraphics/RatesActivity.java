package demoapps.exchangegraphics;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import demoapps.exchangegraphics.data.BuySellRate;
import demoapps.exchangegraphics.data.DolarTlKurRate;
import demoapps.exchangegraphics.data.Rate;
import demoapps.exchangegraphics.data.YorumlarRate;
import demoapps.exchangegraphics.provider.BigparaRateProvider;
import demoapps.exchangegraphics.provider.DolarTlKurRateProvider;
import demoapps.exchangegraphics.provider.EnparaRateProvider;
import demoapps.exchangegraphics.provider.IRateProvider;
import demoapps.exchangegraphics.provider.YorumlarRateProvider;

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


    // String array for alert dialog multi choice items
    static final String[] data_set_names = new String[]{
            "Piyasa",
            "Enpara",
            "Bigpara",
            "DolarTlKur",
    };
    // Boolean array for initial selected items
    boolean[] checked_data_sources = new boolean[]{
            true, // Piyasa
            true, // Enpara
            true, // Bigpara
            true, // DolarTlKur

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
            dataSource.setSelected(checked_data_sources[i]);
            dataSource.setiRateProvider(providers.get(i));
            dataSources.add(dataSource);
        }
    }

    private void refreshSources() {
        for (DataSource dataSource : dataSources) {
            IRateProvider iRateProvider = dataSource.getiRateProvider();
            if (dataSource.isSelected()) {
                iRateProvider.start();
            } else {
                iRateProvider.stop();
            }
        }
    }

    final ArrayList<DataSource> dataSources = new ArrayList<>();

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
//        mChart.getXAxis().setDrawGridLines(false);

        lineChart.getXAxis().setLabelCount(6);
//        lineChart.getAxisRight().setAxisMaximum(3.48f);
//        lineChart.getAxisRight().setAxisMinimum(3.42f);
        lineChart.getAxisLeft().setEnabled(false);
        final IAxisValueFormatter defaultXFormatter = lineChart.getXAxis().getValueFormatter();
        lineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int time = (int) value;
                int minutes = time / (60);
                int seconds = (time) % 60;
                String str = String.format("%d:%02d", minutes, seconds, Locale.ENGLISH);
                return str;
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


        data.addDataSet(createSet(0));
        data.addDataSet(createSet(1));
        data.addDataSet(createSet(2));
        data.addDataSet(createSet(3));
        data.addDataSet(createSet(4));

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

    private void selectSources() {
        for (int i = 0; i < checked_data_sources.length; i++) {
            checked_data_sources[i] = dataSources.get(i).isSelected();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMultiChoiceItems(data_set_names, checked_data_sources, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                checked_data_sources[which] = isChecked;
            }
        });

        builder.setCancelable(true);
        builder.setTitle("Select Sources");
        builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < dataSources.size(); i++) {
                    dataSources.get(i).setSelected(checked_data_sources[i]);
                }
                refreshSources();
            }
        });

        builder.setNegativeButton("Dismiss", null);

        AlertDialog dialog = builder.create();
        dialog.show();
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

//          this automatically refreshes the chart (calls invalidate())
        lineChart.moveViewToX(data.getEntryCount());

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
            case 3:
                label = "Bigpara";
                break;
            case 4:
                label = "Dolar TL Kur";
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
            color = Color.rgb(240, 0, 0);
        } else if (chartIndex == 1) {
            color = Color.rgb(0, 0, 240);
        } else if (chartIndex == 3) {
            color = Color.rgb(0, 240, 0);
        } else if (chartIndex == 4) {
            color = Color.rgb(120, 120, 40);
        } else {
            color = Color.rgb(60, 60, 60);
        }


        set.setCircleColor(color);
        set.setHighLightColor(Color.rgb(0, 0, 255));
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
        for (IRateProvider iRateProvider : providers) {
            iRateProvider.stop();
        }
        super.onDestroy();
    }

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
        private boolean selected;

        public DataSource(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public void setiRateProvider(IRateProvider iRateProvider) {
            this.iRateProvider = iRateProvider;
        }

        public IRateProvider getiRateProvider() {
            return iRateProvider;
        }
    }


}


