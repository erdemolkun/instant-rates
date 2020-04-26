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
import dynoapps.exchange_rates.util.ViewUtils;

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

        Bundle args = new Bundle();

        BottomSheetNavigationFragment fragment = new BottomSheetNavigationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        tvVersion.setText(getString(R.string.version_placeholder, AppUtils.getPlainVersion()));
        tvDrawerItemUsd.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), ChartActivity.class);
            i.putExtra(ChartActivity.EXTRA_RATE_TYPE, IRate.USD);
            startActivity(i);
            dismiss();
        });

        tvDrawerItemEur.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), ChartActivity.class);
            i.putExtra(ChartActivity.EXTRA_RATE_TYPE, IRate.EUR);
            startActivity(i);
            dismiss();
        });

        tvDrawerItemEurUsd.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), ChartActivity.class);
            i.putExtra(ChartActivity.EXTRA_RATE_TYPE, IRate.EUR_USD);
            startActivity(i);
            dismiss();
        });

        tvDrawerItemOns.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), ChartActivity.class);
            i.putExtra(ChartActivity.EXTRA_RATE_TYPE, IRate.ONS);
            startActivity(i);
            dismiss();
        });

        tvDrawerItemAlarms.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), AlarmsActivity.class);
            startActivity(i);
            dismiss();
        });
        ViewUtils.tintDrawable(tvDrawerItemUsd, R.color.colorPrimary);
        ViewUtils.tintDrawable(tvDrawerItemEur, R.color.colorPrimary);
        ViewUtils.tintDrawable(tvDrawerItemEurUsd, R.color.colorPrimary);
        ViewUtils.tintDrawable(tvDrawerItemOns, R.color.colorPrimary);
        ViewUtils.tintDrawable(tvDrawerItemAlarms, R.color.colorPrimary);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.navdrawer_landing, container, false);
    }
}