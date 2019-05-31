package com.example.pybanker.ui.fragments


import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.pybanker.R
import com.example.pybanker.model.DBHelper
import com.example.pybanker.model.ListTransactionsByCatAdapter
import kotlinx.android.synthetic.main.frg_list_by_category.*

/**
 * A simple [Fragment] subclass.
 *
 */
class FrgListByCategory : Fragment() {

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
        return inflater.inflate(R.layout.frg_list_by_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val monthList = ArrayList<String>()
        monthList.add("Pick a month")
        monthList.add("January")
        monthList.add("February")
        monthList.add("March")
        monthList.add("April")
        monthList.add("May")
        monthList.add("June")
        monthList.add("July")
        monthList.add("August")
        monthList.add("September")
        monthList.add("October")
        monthList.add("November")
        monthList.add("December")
        val monthsAdapter = ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item,
            monthList.toMutableList())
        f_list_bycat_month.adapter = monthsAdapter

        val categoriesList = dbhelper?.listCategories()
        categoriesList?.add(0,"Choose a category")
        val categoriesAdapter = ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item,
            categoriesList!!.toMutableList())
        f_list_bycat_category.adapter = categoriesAdapter

        val yearList = dbhelper?.getDistinctYear()
        yearList?.add(0, "Pick a year")
        val yearAdapter = ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item,
            yearList!!.toMutableList())
        f_list_bycat_year.adapter = yearAdapter

        f_list_bycat_listbtn.setOnClickListener {
            val category = f_list_bycat_category.selectedItem.toString()
            var month = f_list_bycat_month.selectedItem.toString()
            var year = f_list_bycat_year.selectedItem.toString()

            clearForm()

            if (category == "Choose a category") {
                Toast.makeText(context, "Please choose a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (month == "Pick a month" || year == "Pick a year") {
                month = ""
                year = ""
            }

            val res = dbhelper?.listTransactionsByCategory(category, month, year)
            val listTransByCat = ArrayList<ListTransByCat>()

            try {
                while (res!!.moveToNext()) {
                    val credit = res.getString(2)
                    val debit = res.getString(3)

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

                    listTransByCat.add(
                        ListTransByCat(
                            res.getString(0),
                            res.getString(1),
                            amount,
                            type,
                            res.getString(4)
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
            f_list_bycat_recycler_view.layoutManager = layoutManager
            f_list_bycat_recycler_view.adapter = ListTransactionsByCatAdapter(context, listTransByCat)

        }

    }

    private fun clearForm() {
        f_list_bycat_category.setSelection(0)
        f_list_bycat_month.setSelection(0)
        f_list_bycat_year.setSelection(0)
    }

    data class ListTransByCat(var opdate: String,
                                 var description: String,
                                 var amount: String,
                                 var type: Int,
                                 var account: String)

}
