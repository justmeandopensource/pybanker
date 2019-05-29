package com.example.pybanker.ui.fragments


import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.pybanker.R
import com.example.pybanker.model.AccountsTransactionsAdapter
import com.example.pybanker.model.DBHelper
import kotlinx.android.synthetic.main.frg_account_details.*

/**
 * A simple [Fragment] subclass.
 *
 */
class FrgAccountDetails : Fragment() {

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
        return inflater.inflate(R.layout.frg_account_details, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        val accountTransactions  = ArrayList<AccountTransaction>()
        val res = dbhelper?.getAccountLast100(arguments?.getString("accountName"))

        try {
            while (res!!.moveToNext()) {
                val credit = res.getString(3)
                val debit = res.getString(4)

                val amount = if (credit.isNullOrEmpty() || credit == "0.00") {
                    "£$debit"
                } else {
                    "£$credit"
                }

                val type = if (credit.isNullOrEmpty() || credit == "0.00") {
                    Color.RED
                } else {
                    Color.parseColor("#008000")
                }

                accountTransactions.add(
                    AccountTransaction(
                        res.getString(0),
                        res.getString(1),
                        res.getString(2),
                        amount,
                        type
                    )
                )
            }
        } catch (e: Exception) {
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
        } finally {
            res?.close()
        }

        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rv_account_transactions.layoutManager = layoutManager
        rv_account_transactions.adapter = AccountsTransactionsAdapter(context, accountTransactions)

        super.onActivityCreated(savedInstanceState)
    }

    data class AccountTransaction(var opdate: String,
                                  var description: String,
                                  var category: String,
                                  var amount: String,
                                  var type: Int)

}
