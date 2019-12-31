package com.example.pybanker.model

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.pybanker.R
import com.example.pybanker.ui.fragments.FrgAccountDetails
import kotlinx.android.synthetic.main.frg_account_transactsion_cards.view.*

class AccountsTransactionsAdapter(val context: Context?,
                                  val AccountTransactions: ArrayList<FrgAccountDetails.AccountTransaction>)
    : RecyclerView.Adapter<AccountsTransactionsAdapter.TransactionsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.frg_account_transactsion_cards, parent, false)
        return TransactionsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return AccountTransactions.size
    }

    override fun onBindViewHolder(holder: TransactionsViewHolder, position: Int) {
        holder.opdate?.text = AccountTransactions[position].opdate
        holder.description?.text = AccountTransactions[position].description
        holder.category?.text = AccountTransactions[position].category
        holder.amount?.text = AccountTransactions[position].amount

        holder.amount?.setTextColor(AccountTransactions[position].type)

    }

    inner class TransactionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val opdate: TextView? = itemView.f_account_details_opdate
        val description: TextView? = itemView.f_account_details_description
        val category: TextView? = itemView.f_account_details_category
        val amount: TextView? = itemView.f_account_details_amount
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}