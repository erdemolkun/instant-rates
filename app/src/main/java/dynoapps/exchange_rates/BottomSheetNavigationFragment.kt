package dynoapps.exchange_rates

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dynoapps.exchange_rates.alarm.AlarmsActivity
import dynoapps.exchange_rates.model.rates.IRate
import dynoapps.exchange_rates.util.AppUtils

class BottomSheetNavigationFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.bottom_navigation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.v_navdrawer_version).text =
            getString(R.string.version_placeholder, AppUtils.getPlainVersion())

        mapOf(
            R.id.v_drawer_item_usd to IRate.USD,
            R.id.v_drawer_item_eur to IRate.EUR,
            R.id.v_drawer_item_eur_usd to IRate.EUR_USD,
            R.id.v_drawer_item_ons to IRate.ONS,
        ).forEach { (viewId, rateType) ->
            view.findViewById<View>(viewId).setOnClickListener {
                startChart(rateType)
                dismiss()
            }
        }

        view.findViewById<View>(R.id.v_drawer_item_alarms).setOnClickListener {
            startActivity(Intent(requireContext(), AlarmsActivity::class.java))
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.let {
                BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    private fun startChart(@IRate.RateDef rateType: Int) {
        startActivity(
            Intent(requireContext(), ChartActivity::class.java).putExtra(ChartActivity.EXTRA_RATE_TYPE, rateType)
        )
    }

    companion object {
        fun newInstance(): BottomSheetNavigationFragment = BottomSheetNavigationFragment()
    }
}
