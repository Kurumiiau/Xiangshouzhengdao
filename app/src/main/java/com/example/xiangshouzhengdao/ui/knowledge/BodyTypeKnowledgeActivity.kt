package com.example.xiangshouzhengdao.ui.knowledge

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.xiangshouzhengdao.R

class BodyTypeKnowledgeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_body_type_knowledge)

        initViews()
    }

    // 两张入口卡分别去体质介绍和配伍说明，返回键仍交给各自页面。
    private fun initViews() {
        findViewById<View>(R.id.toolbar)?.setOnClickListener {
            finish()
        }

        findViewById<CardView>(R.id.cardNineBodyTypes)?.setOnClickListener {
            startActivity(Intent(this, BodyTypeListActivity::class.java))
        }

        findViewById<CardView>(R.id.cardJunChenZuoShi)?.setOnClickListener {
            startActivity(Intent(this, JunChenZuoShiActivity::class.java))
        }
    }
}