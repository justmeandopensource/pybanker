package com.example.pybanker.ui.fragments


import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.pybanker.R
import com.example.pybanker.model.DBHelper
import com.github.mikephil.charting.data.*
import kotlinx.android.synthetic.main.frg_category_stats.*
import kotlinx.android.synthetic.main.frg_dashboard.*


/**
 * A simple [Fragment] subclass.
 *
 */
class FrgCategoryStats : Fragment() {

    private var dbhelper: DBHelper? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        dbhelper = DBHelper(activity)
    }

    override fun onDetach() {
        super.onDetach()
        dbhelper?.close()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frg_category_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoriesList = dbhelper?.listCategories()
        categoriesList?.add(0,"Choose a category")
        categoriesList?.remove("TRANSFER IN")
        categoriesList?.remove("TRANSFER OUT")
        categoriesList?.add(1,"ALL EXPENSES")
        categoriesList?.add("ALL INCOME")

        val categoriesAdapter = ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item,
                                             categoriesList!!.toMutableList())
        f_catstats_category_spinner.adapter = categoriesAdapter

        f_catstats_category_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val category = parent?.selectedItem.toString()
                if (category != "Choose a category") {
                    categoryStatsBar(dbhelper?.getCatsStatsLast12(category))
                    categoryStats(dbhelper?.getCatStatsMonthly(category), "monthly")
                    categoryStats(dbhelper?.getCatsStatsYearly(category), "yearly")
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    private fun categoryStats(res: Cursor?, period: String) {
        val entries = ArrayList<Entry>()

        try {
            var i = 0
            while (res!!.moveToNext()) {
                i++
                entries.add(Entry(i.toFloat(), res.getFloat(1)))
            }
        } catch (e: Exception) {
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
        } finally {
            res?.close()
        }

        val dataSet = LineDataSet(entries, "Trend")
        dataSet.setDrawFilled(true)
        dataSet.color = Color.GRAY
        dataSet.fillColor = Color.GRAY

        val chartData = LineData(dataSet)

        val lineChart =  when (period) {
            "monthly"   -> linechart_catstats_monthly
            else        -> linechart_catstats_yearly
        }

        if (period == "monthly") {
            dataSet.setDrawCircles(false)
            chartData.setDrawValues(false)
        } else {
            lineChart.axisLeft.setDrawLabels(false)
            lineChart.axisLeft.setDrawAxisLine(false)
        }

        lineChart.data = chartData
        lineChart.xAxis.setDrawGridLines(false)
        lineChart.xAxis.setDrawAxisLine(false)
        lineChart.xAxis.setDrawLabels(false)
        lineChart.axisRight.setDrawLabels(false)
        lineChart.axisRight.setDrawAxisLine(false)
        lineChart.description.text = "Category trend $period"
        lineChart.animateY(500)
        lineChart.invalidate()
    }

    private fun categoryStatsBar(res: Cursor?) {
        val chartEntries = ArrayList<BarEntry>()
        try {
            var i = 0
            while (res!!.moveToNext()) {
                i++
                chartEntries.add(BarEntry(i.toFloat(), res.getFloat(1)))
            }
        } catch (e: Exception) {
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
        } finally {
            res?.close()
        }

        val expenseDataSet = BarDataSet(chartEntries, "Last 12 months trend")
        barchart_catstats_last12.animateY(500)
        expenseDataSet.valueTextSize = 8f
        val chartData = BarData(expenseDataSet)
        barchart_catstats_last12.xAxis.setDrawLabels(false)
        barchart_catstats_last12.xAxis.setDrawAxisLine(false)
        barchart_catstats_last12.xAxis.setDrawGridLines(false)
        barchart_catstats_last12.axisLeft.setDrawLabels(false)
        barchart_catstats_last12.axisLeft.setDrawAxisLine(false)
        barchart_catstats_last12.axisRight.setDrawLabels(false)
        barchart_catstats_last12.axisRight.setDrawAxisLine(false)
        barchart_catstats_last12.description.isEnabled = false
        barchart_catstats_last12.data = chartData
    }

}
