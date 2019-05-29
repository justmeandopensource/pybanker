package com.example.pybanker.ui.fragments


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.pybanker.model.DBHelper
import com.example.pybanker.R
import kotlinx.android.synthetic.main.frg_add_account.*


/**
 * A simple [Fragment] subclass.
 *
 */
class FrgAddAccount : Fragment() {

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
        return inflater.inflate(R.layout.frg_add_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AddAccSubmit.setOnClickListener{

            val name = AddAccETAccountName?.text.toString()
            var balance = AddAccETBalance?.text.toString()
            val excludetotal = if (AddAccExclude.isChecked) "yes" else "no"
            val type = when {
                AddAccTypeCurrent.isChecked -> "Current"
                AddAccTypeSavings.isChecked -> "Savings"
                else -> "Credit Card"
            }

            if (name.isEmpty()) {
                AddAccETAccountName.error = "Name Required"
                return@setOnClickListener
            }
            if (balance.isEmpty()) {
                balance = "0.00"
            }
            try {
                dbhelper?.addAccount(name, balance.toFloat(), excludetotal, type)
                Toast.makeText(activity, "Account added successfully", Toast.LENGTH_SHORT).show()
                clearForm()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(activity, e.message.toString(), Toast.LENGTH_SHORT).show()
            } finally {
                val frgAccounts = FrgAccounts()
                activity!!.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout_main, frgAccounts)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun clearForm() {
        AddAccETAccountName.setText("")
        AddAccETBalance.setText("")
        AddAccExclude.isChecked = false
        AddAccType.check(R.id.AddAccTypeCurrent)
    }

}
