package com.example.pybanker.ui

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.MenuItem
import android.support.v4.widget.DrawerLayout
import android.support.design.widget.NavigationView
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import com.example.pybanker.R
import com.example.pybanker.ui.fragments.*

class ActNavDrawer : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var frgHome: FrgHome

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav_drawer)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        frgHome = FrgHome()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout_main, frgHome)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack(null)
            .commit()

    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.nav_drawer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout_main, FrgSettings())
            .addToBackStack(null)
            .commit()
        return true
    }


    private fun displayFragments(id: Int) {
        val fragment = when (id) {
            R.id.nav_home -> {
                FrgHome()
            }
            R.id.nav_add_transaction -> {
                FrgAddTran()
            }
            R.id.nav_search -> {
                FrgSearch()
            }
            R.id.nav_inextrend -> {
                FrgInExTrend()
            }
            R.id.nav_category_stats -> {
                FrgCategoryStats()
            }
            else -> {
                FrgHome()
            }
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout_main, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        displayFragments(item.itemId)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
