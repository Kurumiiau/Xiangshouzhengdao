package com.example.xiangshouzhengdao.ui.knowledge

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.i18n.Loc

class BodyTypeListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_body_type_list)

        initViews()
    }

    private fun initViews() {
        findViewById<View>(R.id.toolbar)?.setOnClickListener {
            finish()
        }

        val bodyTypes = listOf(
            Triple(R.id.cardPinghe, "pinghe", "ph"),
            Triple(R.id.cardQixu, "qixu", "qx"),
            Triple(R.id.cardYangxu, "yangxu", "yx"),
            Triple(R.id.cardYinxu, "yinxu", "yin"),
            Triple(R.id.cardTanshi, "tanshi", "ts"),
            Triple(R.id.cardShire, "shire", "sr"),
            Triple(R.id.cardXueyu, "xueyu", "xy"),
            Triple(R.id.cardQiyu, "qiyu", "qy"),
            Triple(R.id.cardTebing, "tebing", "tb")
        )

        // 点击时按当前语言取文案，别把翻译后的名字存成体质标识。
        bodyTypes.forEach { (cardId, key, _) ->
            findViewById<CardView>(cardId)?.setOnClickListener {
                showBodyTypeDetail(Loc.bodyName(key), Loc.bodyBrief(key))
            }
        }
    }

    private fun showBodyTypeDetail(name: String, desc: String) {
        val detailIntent = Intent(this, BodyTypeDetailActivity::class.java).apply {
            putExtra("body_type_name", name)
            putExtra("body_type_desc", desc)
        }
        startActivity(detailIntent)
    }
}