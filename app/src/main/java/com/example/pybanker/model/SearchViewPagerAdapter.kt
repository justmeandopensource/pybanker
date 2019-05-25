package com.example.pybanker.model

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.pybanker.ui.fragments.FrgListByCategory
import com.example.pybanker.ui.fragments.FrgSearchGeneral

class SearchViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FrgSearchGeneral()
            else -> FrgListByCategory()
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Search"
            else -> "List by Category"
        }
    }
}