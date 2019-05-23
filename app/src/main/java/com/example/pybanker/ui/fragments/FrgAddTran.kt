package com.example.pybanker.ui.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pybanker.R
import com.example.pybanker.model.NewTransactionAdapter
import kotlinx.android.synthetic.main.frg_add_tran.*


/**
 * A simple [Fragment] subclass.
 *
 */
class FrgAddTran : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frg_add_tran, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        f_trans_viewpager.adapter = NewTransactionAdapter(childFragmentManager)
        f_trans_tabs.setupWithViewPager(f_trans_viewpager)
    }


}
