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
import kotlinx.android.synthetic.main.frg_new_in_ex_transaction.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 *
 */
class FrgNewInExTransaction : Fragment() {

    // TODO Check accounts before allowing transactions or fund transfers

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
        return inflater.inflate(R.layout.frg_new_in_ex_transaction, container, false)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        f_new_inex_trans_date.text = SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { v0, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            v0.dayOfMonth // Useless code to suppress warning

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.UK)
            f_new_inex_trans_date.text = sdf.format(cal.time)

        }

        f_new_inex_trans_date.setOnClickListener {
            DatePickerDialog(context!!, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        f_new_inex_trans_calbtn.setOnClickListener {
            DatePickerDialog(context!!, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        f_new_inex_trans_addbtn.setOnClickListener {
            val opdate = f_new_inex_trans_date.text.toString()
            val description = f_new_inex_trans_description.text.toString()
            val category = f_new_inex_trans_category.selectedItem.toString()
            val amount = f_new_inex_trans_amount.text.toString()
            val account = f_new_inex_trans_accounts.selectedItem.toString()

            when {
                account == "Choose an account" -> {
                    Toast.makeText(context, "Select an account", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                category == "Choose a category" -> {
                    Toast.makeText(context, "Select a category", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                amount.isEmpty() -> {
                    f_new_inex_trans_amount.error = "Amount Required!"
                    return@setOnClickListener
                }
                description.isEmpty() -> {
                    f_new_inex_trans_description.error = "Description Required!"
                    return@setOnClickListener
                }
            }

            var credit = "0.00"
            var debit = "0.00"
            val transType = dbhelper!!.getCategoryType(category)
            when (transType) {
                "IN" -> credit = amount
                else -> debit = amount
            }

            dbhelper?.addTransaction(opdate, description, category, credit.toFloat(), debit.toFloat(), account)
            dbhelper?.updateAccountBalance(account,amount.toFloat(), transType)

            clearForm()

            Toast.makeText(context, "New transaction added", Toast.LENGTH_SHORT).show()

        }
        val categoriesList = dbhelper?.listCategories()
        categoriesList?.add(0,"Choose a category")
        val categoriesAdapter = ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item,
                                             categoriesList!!.toMutableList())
        f_new_inex_trans_category.adapter = categoriesAdapter

        val accountsList = dbhelper?.listActiveAccounts()
        accountsList?.add(0, "Choose an account")
        val accountsAdapter = ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item,
                                             accountsList!!.toMutableList())

        f_new_inex_trans_accounts.adapter = accountsAdapter

    }

    @SuppressLint("SimpleDateFormat")
    private fun clearForm() {
        f_new_inex_trans_accounts.setSelection(0)
        f_new_inex_trans_category.setSelection(0)
        f_new_inex_trans_date.text = SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
        f_new_inex_trans_amount.text.clear()
        f_new_inex_trans_description.text.clear()
    }


}
