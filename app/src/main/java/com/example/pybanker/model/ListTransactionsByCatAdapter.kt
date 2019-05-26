package com.example.pybanker.model

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.pybanker.R
import com.example.pybanker.ui.fragments.FrgListByCategory
import kotlinx.android.synthetic.main.frg_list_by_category_cards.view.*

class ListTransactionsByCatAdapter (val context: Context?, val listTransByCat: ArrayList<FrgListByCategory.ListTransByCat>)
    : RecyclerView.Adapter<ListTransactionsByCatAdapter.TransactionsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.frg_list_by_category_cards, parent, false)
        return TransactionsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listTransByCat.size
    }

    override fun onBindViewHolder(holder: TransactionsViewHolder, position: Int) {
        holder.opdate?.text = listTransByCat[position].opdate
        holder.description?.text = listTransByCat[position].description
        holder.account?.text = listTransByCat[position].account
        holder.amount?.text = listTransByCat[position].amount

        holder.amount?.setTextColor(listTransByCat[position].type)
    }

    inner class TransactionsViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        val opdate: TextView? = itemView.f_list_bycat_card_opdate
        val description: TextView? = itemView.f_list_bycat_card_description
        val amount: TextView? = itemView.f_list_bycat_card_amount
        val account: TextView? = itemView.f_list_bycat_card_account
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}