package com.example.pybanker.ui.fragments


import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.pybanker.R
import com.example.pybanker.model.DBHelper
import kotlinx.android.synthetic.main.frg_transfer_transaction.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 *
 */
class FrgTransferTransaction : Fragment() {

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
        return inflater.inflate(R.layout.frg_transfer_transaction, container, false)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        f_transfer_date.text = SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { v0, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            v0.dayOfMonth // Useless code to suppress warning

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.UK)
            f_transfer_date.text = sdf.format(cal.time)

        }

        f_transfer_date.setOnClickListener {
            DatePickerDialog(context!!, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        f_transfer_calbtn.setOnClickListener {
            DatePickerDialog(context!!, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        f_transfer_addbtn.setOnClickListener {
            val opdate = f_transfer_date.text.toString()
            val description = f_transfer_description.text.toString()
            val amount = f_transfer_amount.text.toString()
            val fromAccount = f_transfer_from_account.selectedItem.toString()
            val toAccount = f_transfer_to_account.selectedItem.toString()

            when {
                fromAccount == "Choose a source account" -> {
                    Toast.makeText(context, "Select a source account", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                toAccount == "Choose a destination account" -> {
                    Toast.makeText(context, "Select a destination account", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                amount.isEmpty() -> {
                    f_transfer_amount.error = "Amount Required!"
                    return@setOnClickListener
                }
                description.isEmpty() -> {
                    f_transfer_description.error = "Description Required!"
                    return@setOnClickListener
                }
                fromAccount == toAccount -> {
                    Toast.makeText(context, "Info: Source & Destination accounts are same!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }


            dbhelper?.addTransaction(opdate, description, "TRANSFER OUT", 0.00f, amount.toFloat(), fromAccount)
            dbhelper?.updateAccountBalance(fromAccount, amount.toFloat(), "EX")
            dbhelper?.addTransaction(opdate, description, "TRANSFER IN", amount.toFloat(), 0.00f, toAccount)
            dbhelper?.updateAccountBalance(toAccount, amount.toFloat(), "IN")

            clearForm()
            Toast.makeText(context, "Funds Transferred successfully", Toast.LENGTH_SHORT).show()

        }

        val fromAccountsList = dbhelper?.listActiveAccounts()
        fromAccountsList?.add(0, "Choose a source account")
        val fromAccountsAdapter = ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item,
            fromAccountsList!!.toMutableList())

        f_transfer_from_account.adapter = fromAccountsAdapter

        val toAccountsList = dbhelper?.listActiveAccounts()
        toAccountsList?.add(0, "Choose a destination account")
        val toAccountsAdapter = ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item,
            toAccountsList!!.toMutableList())

        f_transfer_to_account.adapter = toAccountsAdapter
    }

    @SuppressLint("SimpleDateFormat")
    private fun clearForm() {
        f_transfer_from_account.setSelection(0)
        f_transfer_to_account.setSelection(0)
        f_transfer_date.text = SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
        f_transfer_amount.text.clear()
        f_transfer_description.text.clear()
    }

}
