package dynoapps.exchange_rates;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import dynoapps.exchange_rates.alarm.AlarmsActivity;
import dynoapps.exchange_rates.model.rates.IRate;
import dynoapps.exchange_rates.util.AppUtils;

public class BottomSheetNavigationFragment extends BottomSheetDialogFragment {

    @BindView(R.id.v_drawer_item_usd)
    TextView tvDrawerItemUsd;

    @BindView(R.id.v_drawer_item_eur)
    TextView tvDrawerItemEur;

    @BindView(R.id.v_drawer_item_eur_usd)
    TextView tvDrawerItemEurUsd;

    @BindView(R.id.v_drawer_item_ons)
    TextView tvDrawerItemOns;

    @BindView(R.id.v_drawer_item_alarms)
    TextView tvDrawerItemAlarms;

    @BindView(R.id.v_navdrawer_version)
    TextView tvVersion;

    public static BottomSheetNavigationFragment newInstance() {
        return new BottomSheetNavigationFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        tvVersion.setText(getString(R.string.version_placeholder, AppUtils.getPlainVersion()));
        tvDrawerItemUsd.setOnClickListener(v -> {
            startChart(IRate.USD);
            dismiss();
        });

        tvDrawerItemEur.setOnClickListener(v -> {
            startChart(IRate.EUR);
            dismiss();
        });

        tvDrawerItemEurUsd.setOnClickListener(v -> {
            startChart(IRate.EUR_USD);
            dismiss();
        });

        tvDrawerItemOns.setOnClickListener(v -> {
            startChart(IRate.ONS);
            dismiss();
        });

        tvDrawerItemAlarms.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), AlarmsActivity.class);
            startActivity(i);
            dismiss();
        });
    }

    private void startChart(@IRate.RateDef int rate) {
        Intent i = new Intent(getContext(), ChartActivity.class);
        i.putExtra(ChartActivity.EXTRA_RATE_TYPE, rate);
        startActivity(i);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_navigation, container, false);
    }
}