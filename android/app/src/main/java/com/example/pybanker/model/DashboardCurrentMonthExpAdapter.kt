package com.example.pybanker.model

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.pybanker.R
import com.example.pybanker.ui.fragments.FrgDashboard
import kotlinx.android.synthetic.main.frg_dashboard_current_exp_cards.view.*

class DashboardCurrentMonthExpAdapter (val context: Context?, val currentMonthExpense: ArrayList<FrgDashboard.CurrentMonthExpense>)
    : RecyclerView.Adapter<DashboardCurrentMonthExpAdapter.TransactionsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.frg_dashboard_current_exp_cards, parent, false)
        return TransactionsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return currentMonthExpense.size
    }

    override fun onBindViewHolder(holder: TransactionsViewHolder, position: Int) {
        holder.category?.text = currentMonthExpense[position].category
        holder.amount?.text = currentMonthExpense[position].amount
    }


    inner class TransactionsViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        val category: TextView? = itemView.f_dashboard_current_exp_category
        val amount: TextView? = itemView.f_dashboard_current_exp_amount
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}