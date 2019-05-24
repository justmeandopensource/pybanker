package com.example.pybanker.model

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.pybanker.ui.fragments.FrgAccounts
import com.example.pybanker.ui.fragments.FrgDashboard

class HomeViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FrgDashboard()
            else -> FrgAccounts()
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Dashboard"
            else -> "Accounts"
        }
    }
}