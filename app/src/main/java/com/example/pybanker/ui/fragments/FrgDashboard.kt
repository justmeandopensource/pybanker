package com.example.pybanker.ui.fragments


import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.pybanker.R
import com.example.pybanker.model.DBHelper
import com.example.pybanker.model.DashboardCurrentMonthExpAdapter
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.android.synthetic.main.frg_dashboard.*


/**
 * A simple [Fragment] subclass.
 *
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class FrgDashboard : Fragment() {

    private var dbhelper: DBHelper? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        dbhelper = DBHelper(activity)
    }

    override fun onDetach() {
        super.onDetach()
        dbhelper?.close()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frg_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        f_dashboard_add_trans_btn.setOnClickListener {
            val fragment = FrgAddTran()
            activity!!.supportFragmentManager
                .beginTransaction().replace(R.id.frame_layout_main, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit()
        }

        f_dashboard_search.setOnClickListener {
            val fragment = FrgSearch()
            activity!!.supportFragmentManager
                .beginTransaction().replace(R.id.frame_layout_main, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit()
        }

        if (dbhelper!!.transactionsTableExists()) {

            val res0 = dbhelper?.getLast12MonthExpenses()
            last5MonthExpensesChart(res0)

            val totalExpense = dbhelper?.getCurrentMonthExpense()
            f_dashboard_total_expense.text = "£$totalExpense"

            val currentMonthExpenses = ArrayList<CurrentMonthExpense>()
            val res = dbhelper?.getCurrentMonthExpensesByCategory()

            try {
                while (res!!.moveToNext()) {
                    currentMonthExpenses.add(
                        CurrentMonthExpense(res.getString(0), "£" + res.getString(1))
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
            } finally {
                res?.close()
            }

            val layoutManager = LinearLayoutManager(activity)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            f_dashboard_current_exp_recycler_view.layoutManager = layoutManager
            f_dashboard_current_exp_recycler_view.adapter = DashboardCurrentMonthExpAdapter(context, currentMonthExpenses)
        }

        f_dashboard_current_exp_recycler_view.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    f_dashboard_search.hide()
                    f_dashboard_add_trans_btn.hide()
                } else if (dy < 0) {
                    f_dashboard_search.show()
                    f_dashboard_add_trans_btn.show()
                }
            }
        })

    }

    private fun last5MonthExpensesChart(res: Cursor?) {
        val expenseEntries = ArrayList<BarEntry>()
        try {
            var i = 0
            while (res!!.moveToNext()) {
                i++
                expenseEntries.add(BarEntry(i.toFloat(), res.getFloat(1)))
            }
        } catch (e: Exception) {
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
        } finally {
            res?.close()
        }

        val expenseDataSet = BarDataSet(expenseEntries, "Last 12 months expense trend")
        barchart_dashboard_expenses.animateY(500)
        expenseDataSet.valueTextSize = 8f
        val chartData = BarData(expenseDataSet)
        barchart_dashboard_expenses.xAxis.setDrawLabels(false)
        barchart_dashboard_expenses.xAxis.setDrawAxisLine(false)
        barchart_dashboard_expenses.xAxis.setDrawGridLines(false)
        barchart_dashboard_expenses.axisLeft.setDrawLabels(false)
        barchart_dashboard_expenses.axisLeft.setDrawAxisLine(false)
        barchart_dashboard_expenses.axisRight.setDrawLabels(false)
        barchart_dashboard_expenses.axisRight.setDrawAxisLine(false)
        barchart_dashboard_expenses.description.isEnabled = false
        barchart_dashboard_expenses.data = chartData
    }

    data class CurrentMonthExpense(var category: String, var amount: String)

}
