package com.example.xiangshouzhengdao.ui.solar

import androidx.core.graphics.toColorInt
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.i18n.Loc

class SolarPrescriptionDetailActivity : AppCompatActivity() {

    private lateinit var tvSolarTermName: TextView
    private lateinit var tvTheme: TextView
    private lateinit var tvEffect: TextView
    private lateinit var junHerbsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solar_prescription_detail)

        initViews()
        loadData()
    }

    private fun initViews() {
        tvSolarTermName = findViewById(R.id.tvSolarTermName)
        tvTheme = findViewById(R.id.tvTheme)
        tvEffect = findViewById(R.id.tvEffect)
        junHerbsContainer = findViewById(R.id.junHerbsContainer)

        findViewById<View>(R.id.toolbar)?.setOnClickListener {
            finish()
        }
    }

    // 君药从列表原样传过来；翻译只影响标题，不改药材名。
    private fun loadData() {
        val solarTerm = intent.getStringExtra("solar_term") ?: Loc.pick("Unknown", "未知")
        val theme = intent.getStringExtra("theme") ?: ""
        val effect = intent.getStringExtra("effect") ?: ""
        val junHerbs = intent.getStringArrayListExtra("jun_herbs") ?: arrayListOf()

        tvSolarTermName.text = Loc.solarTerm(solarTerm)
        tvTheme.text = Loc.solarTheme(theme)
        tvEffect.text = Loc.solarEffect(solarTerm)

        junHerbsContainer.removeAllViews()
        if (junHerbs.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = getString(R.string.solar_nodata)
                textSize = 14f
                setTextColor("#999999".toColorInt())
            }
            junHerbsContainer.addView(emptyText)
        } else {
            junHerbs.forEach { herbName ->
                val herbTag = createHerbTag(herbName)
                junHerbsContainer.addView(herbTag)
            }
        }
    }

    private fun createHerbTag(herbName: String): View {
        val textView = TextView(this).apply {
            text = herbName
            textSize = 14f
            setTextColor("#8B4513".toColorInt())
            setBackgroundResource(R.drawable.bg_tag)
            setPadding(24, 12, 24, 12)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 16
            }
        }
        return textView
    }
}