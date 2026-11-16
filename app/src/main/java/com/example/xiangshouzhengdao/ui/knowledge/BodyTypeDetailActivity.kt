package com.example.xiangshouzhengdao.ui.knowledge

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.i18n.Loc

class BodyTypeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_body_type_detail)

        findViewById<View>(R.id.toolbar)?.setOnClickListener {
            finish()
        }

        val name = intent.getStringExtra("body_type_name") ?: Loc.pick("Unknown Constitution", "未知体质")
        val desc = intent.getStringExtra("body_type_desc") ?: ""

        findViewById<TextView>(R.id.tvBodyTypeName).text = name
        findViewById<TextView>(R.id.tvBodyTypeDesc).text = desc

        // 页面传的是展示名，转回稳定ID后再取长文；中英文都走同一个入口。
        val key = Loc.keyOfName(name)
        findViewById<TextView>(R.id.tvDetailContent).text = if (key != null) Loc.bodyArticle(key) else name
    }
}
