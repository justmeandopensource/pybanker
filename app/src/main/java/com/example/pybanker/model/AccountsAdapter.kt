package com.example.pybanker.model

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.pybanker.R
import com.example.pybanker.ui.fragments.FrgAccountDetails
import com.example.pybanker.ui.fragments.FrgAccounts
import kotlinx.android.synthetic.main.frg_accounts_cards.view.*

class AccountsAdapter(val context: Context?, val Accounts: ArrayList<FrgAccounts.Account>, val fragmentManager: FragmentManager?)
    : RecyclerView.Adapter<AccountsAdapter.AccountViewHolder>() {
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
        holder.type?.text = Accounts[position].type

        val status = if (Accounts[position].status == "Active") {
            ""
        } else {
            "[" + Accounts[position].status + "]"
        }

        holder.status?.text = status

        var bgColor: Int = when (Accounts[position].type) {
            "Savings"       -> Color.parseColor("#F0FFF0")
            "Current"       -> Color.parseColor("#F0F8FF")
            "Credit Card"   -> Color.parseColor("#FFF5EE")
            else            -> Color.parseColor("#DCDCDC")
        }

        if (Accounts[position].status == "Closed") {
            bgColor = Color.parseColor("#DCDCDC")
        }

        holder.card?.setBackgroundColor(bgColor)
    }

    inner class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView? = itemView.tv_account_name
        val lastoperated: TextView? = itemView.tv_account_lastoperated
        val balance: TextView? = itemView.tv_account_balance
        val type: TextView? = itemView.f_accounts_type
        val status: TextView? = itemView.f_accounts_status
        val card: CardView? = itemView.accounts_card_view

        init {
            itemView.setOnClickListener {
                val frgAccountDetails = FrgAccountDetails()
                val bundle = Bundle()
                bundle.putString("accountName", name?.text.toString())
                frgAccountDetails.arguments = bundle
                fragmentManager!!
                    .beginTransaction()
                    .replace(R.id.frame_layout_main, frgAccountDetails)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}