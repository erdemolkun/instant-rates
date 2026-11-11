package dynoapps.exchange_rates.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import dynoapps.exchange_rates.R

class SimpleSpinnerAdapter<T : Any>(context: Context, objects: List<T>) :
    ArrayAdapter<T>(context, R.layout.item_spinner_dropdown, 0, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
        super.getView(position, convertView, parent).also { bind(it, position) }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.item_spinner_dropdown, parent, false)
        bind(view, position)
        return view
    }

    private fun bind(view: View, position: Int) {
        view.findViewById<TextView?>(R.id.tv_item_spinner)?.text = getItem(position)?.toString()
    }
}
