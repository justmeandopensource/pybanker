package com.example.pybanker

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class Act01Main : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLogin.setOnClickListener {
            if (etAccessKey.text.toString() == "8989") { // todo Get access key from Database and don't hardcode
                val intent = Intent(this, Act02NavDrawer::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Invalid Access Key", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
