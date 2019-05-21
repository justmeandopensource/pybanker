package com.example.pybanker

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.frg_accounts_cards.view.*

class AccountsAdapter(val context: Context?, val Accounts: ArrayList<FrgAccounts.Account>) : RecyclerView.Adapter<AccountsAdapter.AccountViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.frg_accounts_cards, parent, false)
        return AccountViewHolder(view)
    }

    override fun getItemCount(): Int {
        return Accounts.size
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.name?.text = Accounts[position].accountName
        holder.lastoperated?.text = Accounts[position].lastOperated
        holder.balance?.text = Accounts[position].balance
    }

    inner class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView? = itemView.tv_account_name
        val lastoperated: TextView? = itemView.tv_account_lastoperated
        val balance: TextView? = itemView.tv_account_balance
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}