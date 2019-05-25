package com.example.pybanker.ui.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pybanker.R
import com.example.pybanker.model.SearchViewPagerAdapter
import kotlinx.android.synthetic.main.frg_search.*

/**
 * A simple [Fragment] subclass.
 *
 */
class FrgSearch : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frg_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        f_search_viewpager.adapter = SearchViewPagerAdapter(childFragmentManager)
        f_search_tabs.setupWithViewPager(f_search_viewpager)
    }

}
