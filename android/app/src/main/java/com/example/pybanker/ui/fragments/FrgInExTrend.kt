package com.example.pybanker.ui.fragments


import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.pybanker.R
import com.example.pybanker.model.DBHelper
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.frg_in_ex_trend.*


/**
 * A simple [Fragment] subclass.
 *
 */
class FrgInExTrend : Fragment() {

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
        return inflater.inflate(R.layout.frg_in_ex_trend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inexTrend(dbhelper?.getInExMonthlyTrend(), "monthly")
        inexTrend(dbhelper?.getInExYearlyTrend(), "yearly")
    }

    private fun inexTrend(res: Cursor?, period: String) {
        val incomeEntries = ArrayList<Entry>()
        val expenseEntries = ArrayList<Entry>()

        try {
            var i = 0
            while (res!!.moveToNext()) {
                i++
                incomeEntries.add(Entry(i.toFloat(), res.getFloat(1)))
                expenseEntries.add(Entry(i.toFloat(), res.getFloat(2)))
            }
        } catch (e: Exception) {
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
        } finally {
            res?.close()
        }

        val incomeDataSet = LineDataSet(incomeEntries, "Income")
        incomeDataSet.setDrawFilled(true)
        incomeDataSet.fillAlpha = 200

        val expenseDataSet = LineDataSet(expenseEntries, "Expense")
        expenseDataSet.setDrawFilled(true)
        expenseDataSet.fillAlpha = 150
        expenseDataSet.color = Color.GRAY
        expenseDataSet.fillColor = Color.GRAY

        val chartData = LineData(incomeDataSet, expenseDataSet)

        val lineChart =  when (period) {
            "monthly"   -> linechart_inex_monthly
            else        -> linechart_inex_yearly
        }

        if (period == "monthly") {
            incomeDataSet.setDrawCircles(false)
            expenseDataSet.setDrawCircles(false)
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
        lineChart.description.text = "Income/Expense trend $period"
        lineChart.animateY(500)
    }

}
