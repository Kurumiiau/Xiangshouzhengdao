package com.example.xiangshouzhengdao.ui.solar

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.data.model.SolarPrescription
import com.example.xiangshouzhengdao.i18n.Loc
import com.example.xiangshouzhengdao.utils.SolarTermUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SolarPrescriptionActivity : AppCompatActivity() {

    private lateinit var tvCurrentSolarTerm: TextView
    private lateinit var tvCurrentTheme: TextView
    private lateinit var tvCurrentEffect: TextView
    private lateinit var junHerbsContainer: LinearLayout
    private lateinit var recyclerSolarTerms: RecyclerView

    private lateinit var adapter: SolarPrescriptionAdapter
    private var allPrescriptions: List<SolarPrescription> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solar_prescription)

        initViews()
        loadPrescriptions()
    }

    private fun initViews() {
        tvCurrentSolarTerm = findViewById(R.id.tvCurrentSolarTerm)
        tvCurrentTheme = findViewById(R.id.tvCurrentTheme)
        tvCurrentEffect = findViewById(R.id.tvCurrentEffect)
        junHerbsContainer = findViewById(R.id.junHerbsContainer)
        recyclerSolarTerms = findViewById(R.id.recyclerSolarTerms)

        adapter = SolarPrescriptionAdapter { prescription ->
            val intent = Intent(this, SolarPrescriptionDetailActivity::class.java).apply {
                putExtra("solar_term", prescription.solarTerm)
                putExtra("theme", prescription.theme)
                putExtra("effect", prescription.effect)
                putStringArrayListExtra("jun_herbs", ArrayList(prescription.junHerbs))
            }
            startActivity(intent)
        }
        recyclerSolarTerms.layoutManager = LinearLayoutManager(this)
        recyclerSolarTerms.adapter = adapter

        findViewById<View>(R.id.toolbar)?.setOnClickListener {
            finish()
        }
    }

    private fun loadPrescriptions() {
        try {
            val json = assets.open("solar_prescriptions.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<SolarPrescription>>() {}.type
            allPrescriptions = Gson().fromJson(json, type) ?: emptyList()

            displayCurrentSolarTerm()

            adapter.submitList(allPrescriptions)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.solar_load_fail), Toast.LENGTH_SHORT).show()
        }
    }

    // 节气键始终用中文原名查资产，只有显示时才翻译。
    private fun displayCurrentSolarTerm() {
        val currentTerm = SolarTermUtil.getCurrentSolarTerm()
        tvCurrentSolarTerm.text = Loc.solarTerm(currentTerm)

        val currentPrescription = allPrescriptions.find { it.solarTerm == currentTerm }

        if (currentPrescription != null) {
            tvCurrentTheme.text = Loc.solarTheme(currentPrescription.theme)
            tvCurrentEffect.text = Loc.solarEffect(currentTerm)

            junHerbsContainer.removeAllViews()
            currentPrescription.junHerbs.forEach { herbName ->
                val herbTag = createHerbTag(herbName)
                junHerbsContainer.addView(herbTag)
            }
        } else {
            tvCurrentTheme.text = Loc.seasonTheme("")
            tvCurrentEffect.text = Loc.solarEffect("")
        }
    }

    private fun createHerbTag(herbName: String): View {
        val textView = TextView(this).apply {
            text = herbName
            textSize = 13f
            setTextColor(resources.getColor(R.color.primary, theme))
            setBackgroundResource(R.drawable.bg_tag)
            setPadding(16, 8, 16, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 12
            }
        }
        return textView
    }
}