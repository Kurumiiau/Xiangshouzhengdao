package com.example.xiangshouzhengdao.ui.knowledge

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.xiangshouzhengdao.R

class JunChenZuoShiActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jun_chen_zuo_shi)

        initViews()
    }

    // 说明正文放在资源里，切语言时跟着系统资源一起切。
    private fun initViews() {
        findViewById<View>(R.id.toolbar)?.setOnClickListener { finish() }
    }
}