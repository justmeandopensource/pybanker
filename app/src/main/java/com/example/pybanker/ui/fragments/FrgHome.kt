package com.example.pybanker.ui.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.pybanker.R
import com.example.pybanker.model.HomeViewPagerAdapter
import kotlinx.android.synthetic.main.frg_home.*

/**
 * A simple [Fragment] subclass.
 *
 */
class FrgHome : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frg_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        f_home_viewpager.adapter = HomeViewPagerAdapter(childFragmentManager)
        f_home_tabs.setupWithViewPager(f_home_viewpager)
    }

}
