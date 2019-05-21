package com.example.pybanker.ui.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pybanker.R
import kotlinx.android.synthetic.main.frg_dashboard.*
import java.io.File


/**
 * A simple [Fragment] subclass.
 *
 */
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class FrgDashboard : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frg_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        testID.setOnClickListener{
            checkDB()
        }
    }

    private fun checkDB() {

        val db: File = context!!.getDatabasePath("pybanker")

        if (db.exists()) {
            testTV.text = "Sooper. Database found"
        } else {
            testTV.text = "Database not found. Sorry"
        }

    }


}
