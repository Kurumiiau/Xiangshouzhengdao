package com.example.xiangshouzhengdao.ui.custom

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.data.model.Herb
import com.example.xiangshouzhengdao.data.local.DataRepository
import com.example.xiangshouzhengdao.engine.PrescriptionEngine
import com.example.xiangshouzhengdao.i18n.Loc
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class CustomPrescriptionActivity : AppCompatActivity() {

    private lateinit var spinnerBodyType: Spinner
    private lateinit var spinnerScene: Spinner
    private lateinit var tvPrescriptionResult: TextView
    private lateinit var cardResult: CardView

    private var selectedBodyType: String = "平和质"
    private var selectedScene: String = "日常养生"
    private val bodyTypeIds = mapOf(
        "平和质" to "pinghe", "气虚质" to "qixu", "阳虚质" to "yangxu",
        "阴虚质" to "yinxu", "痰湿质" to "tanshi", "湿热质" to "shire",
        "血瘀质" to "xueyu", "气郁质" to "qiyu", "特禀质" to "tebing"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_prescription)

        initViews()
        setupSpinners()
    }

    private fun initViews() {
        findViewById<View>(R.id.toolbar)?.setOnClickListener { finish() }

        spinnerBodyType = findViewById(R.id.spinnerBodyType)
        spinnerScene = findViewById(R.id.spinnerScene)
        tvPrescriptionResult = findViewById(R.id.tvPrescriptionResult)
        cardResult = findViewById(R.id.cardResult)

        findViewById<View>(R.id.btnGenerate).setOnClickListener {
            generatePrescription()
        }
    }

    private fun setupSpinners() {
        val bodyTypeKeys = listOf("pinghe", "qixu", "yangxu", "yinxu", "tanshi", "shire", "xueyu", "qiyu", "tebing")
        val bodyTypes = arrayOf("平和质", "气虚质", "阳虚质", "阴虚质", "痰湿质", "湿热质", "血瘀质", "气郁质", "特禀质")
        val bodyTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bodyTypeKeys.map { Loc.bodyName(it) })
        bodyTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBodyType.adapter = bodyTypeAdapter
        spinnerBodyType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedBodyType = bodyTypes[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val scenes = arrayOf("日常养生", "助眠安神", "提神醒脑", "疏肝解郁", "祛湿化浊", "温阳散寒")
        val sceneAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, scenes.map { Loc.scene(it) })
        sceneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerScene.adapter = sceneAdapter
        spinnerScene.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedScene = scenes[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun generatePrescription() {
        cardResult.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val zhHerbs = loadHerbsFromAssets("herbs.json")
                val displayHerbs = loadHerbsFromAssets(if (Loc.lang == "en") "herbs_en.json" else "herbs.json")
                val userId = DataRepository.getInstance(this@CustomPrescriptionActivity).getCurrentUserId()
                val constraints = getSharedPreferences("user_constraints_$userId", MODE_PRIVATE)
                    .getStringSet("constraints", emptySet())?.toList().orEmpty()
                val candidates = filterHerbsByBodyTypeAndScene(zhHerbs, selectedBodyType, selectedScene)
                // 定制入口也走同一禁忌规则，不能绕过测评前保存的身体状况。
                val allowed = PrescriptionEngine.filterAllowedHerbs(
                    candidates, constraints, listOfNotNull(bodyTypeIds[selectedBodyType])
                ).map { it.herbId }.toSet()
                val filteredHerbs = displayHerbs.filter { it.herbId in allowed }

                if (filteredHerbs.isEmpty()) {
                    Toast.makeText(this@CustomPrescriptionActivity, getString(R.string.custom_none), Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val prescriptionText = buildPrescriptionText(filteredHerbs, selectedBodyType, selectedScene)
                tvPrescriptionResult.text = prescriptionText
                cardResult.visibility = View.VISIBLE

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@CustomPrescriptionActivity, getString(R.string.custom_fail), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadHerbsFromAssets(fileName: String): List<Herb> {
        return try {
            val json = assets.open(fileName).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Herb>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    
    private fun filterHerbsByBodyTypeAndScene(
        herbs: List<Herb>,
        bodyType: String,
        scene: String
    ): List<Herb> {
        val bodyTypeId = bodyTypeIds[bodyType] ?: return emptyList()
        return herbs.filter { herb ->
            // 性味是“辛甘温”等完整描述；体质适配以资料库的稳定编码为准。
            val matchesBodyType = bodyTypeId in herb.bodyTypes
            val isWarming = herb.nature.contains("温") || herb.nature.contains("热")

            val matchesScene = when (scene) {
                "助眠安神" -> herb.effect.contains("神") || herb.effect.contains("眠")
                "提神醒脑" -> herb.effect.contains("神") || herb.effect.contains("脑")
                "疏肝解郁" -> herb.effect.contains("肝") || herb.effect.contains("郁")
                "祛湿化浊" -> herb.effect.contains("湿") || herb.effect.contains("浊")
                "温阳散寒" -> isWarming && herb.effect.contains("寒")
                else -> true
            }

            matchesBodyType && matchesScene
        }
    }

    
    private fun buildPrescriptionText(
        herbs: List<Herb>,
        bodyType: String,
        scene: String
    ): String {
        val sb = StringBuilder()
        sb.append("【").append(Loc.customHeader()).append("】\n\n")
        val bodyKey = mapOf(
            "平和质" to "pinghe", "气虚质" to "qixu", "阳虚质" to "yangxu", "阴虚质" to "yinxu",
            "痰湿质" to "tanshi", "湿热质" to "shire", "血瘀质" to "xueyu", "气郁质" to "qiyu", "特禀质" to "tebing"
        )
        sb.append(Loc.customBodyLabel()).append(":").append(Loc.bodyName(bodyKey[bodyType] ?: "pinghe")).append("\n")
        sb.append(Loc.customSceneLabel()).append(":").append(Loc.scene(scene)).append("\n\n")
        sb.append("【").append(Loc.customJun()).append("】\n")

        val junHerbs = herbs.take(2)
        junHerbs.forEach { herb ->
            sb.append("• ${herb.name}：${herb.effect}\n")
        }

        sb.append("\n【").append(Loc.customChen()).append("】\n")
        val chenHerbs = herbs.drop(2).take(3)
        chenHerbs.forEach { herb ->
            sb.append("• ${herb.name}：${herb.effect}\n")
        }

        sb.append("\n【").append(Loc.customZuoShi()).append("】\n")
        val zuoHerbs = herbs.drop(5).take(2)
        if (zuoHerbs.isEmpty()) {
            sb.append("• ").append(Loc.customAdjust()).append("\n")
        } else {
            zuoHerbs.forEach { herb ->
                sb.append("• ${herb.name}：${herb.effect}\n")
            }
        }

        sb.append("\n【").append(Loc.customUsage()).append("】\n")
        sb.append("• ").append(Loc.customUsage1()).append("\n")
        sb.append("• ").append(Loc.customUsage2()).append("\n")
        sb.append("• ").append(Loc.customUsage3()).append("\n")
        sb.append("• ").append(Loc.customUsage4()).append("\n")

        return sb.toString()
    }
}
