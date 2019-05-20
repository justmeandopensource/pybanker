package com.example.pybanker


import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.frg_accounts.*


/**
 * A simple [Fragment] subclass.
 *
 */
class FrgAccounts : Fragment() {

    private var dbhelper: DBHelper? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        dbhelper = DBHelper(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frg_accounts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnAddAccount.setOnClickListener{
            val fragment = FrgAddAccount()
            fragmentManager
                ?.beginTransaction()?.replace(R.id.frame_layout_main, fragment)
                ?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                ?.commit()
        }

        btnShowAccounts.setOnClickListener{
            val res = dbhelper?.getAccounts
            if (res?.count == 0) {
                Toast.makeText(activity, "No Accounts found!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val buffer = StringBuffer()
            while (res!!.moveToNext()){
                buffer.append("Name: " + res.getString(0) + "\n")
                buffer.append("Balance: " + res.getString(1) + "\n")
                buffer.append("Last Operated: " + res.getString(2) + "\n")
                buffer.append("Exclude from Stats: " + res.getString(3) + "\n")
                buffer.append("Type: " + res.getString(4) + "\n")
            }
            showDialog("List of Accounts", buffer.toString())
        }

    }

    private fun showDialog(title:String, message:String) {
        val builder = AlertDialog.Builder(activity)
        builder.setCancelable(true)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.show()
    }

}
