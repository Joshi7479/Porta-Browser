package com.example.jbrowsev1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Welcome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome)
        val ready = findViewById<Button>(R.id.ready)
        ready.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) } }
    }
