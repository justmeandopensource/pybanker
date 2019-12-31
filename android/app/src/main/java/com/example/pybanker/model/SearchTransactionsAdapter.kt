package com.example.pybanker.model

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.pybanker.R
import com.example.pybanker.ui.fragments.FrgSearchGeneral
import kotlinx.android.synthetic.main.frg_search_general_cards.view.*

class SearchTransactionsAdapter (val context: Context?,
                                 val SearchTransactions: ArrayList<FrgSearchGeneral.SearchTransaction>)
    : RecyclerView.Adapter<SearchTransactionsAdapter.TransactionsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.frg_search_general_cards, parent, false)
        return TransactionsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return SearchTransactions.size
    }

    override fun onBindViewHolder(holder: TransactionsViewHolder, position: Int) {
        holder.opdate?.text = SearchTransactions[position].opdate
        holder.description?.text = SearchTransactions[position].description
        holder.category?.text = SearchTransactions[position].category
        holder.account?.text = SearchTransactions[position].account
        holder.amount?.text = SearchTransactions[position].amount

        holder.amount?.setTextColor(SearchTransactions[position].type)
    }

    inner class TransactionsViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        val opdate: TextView? = itemView.f_search_general_opdate
        val description: TextView? = itemView.f_search_general_description
        val category: TextView? = itemView.f_search_general_category
        val amount: TextView? = itemView.f_search_general_amount
        val account: TextView? = itemView.f_search_general_account
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}