package com.vincensaregina.p3lproject

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    var handler: Handler? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        ///////ActionBar
        //Remove shadow
        supportActionBar?.elevation = 0F
        //Remove default title in toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        handler = Handler()
        handler!!.postDelayed({
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
        }, 1500)
    }
}