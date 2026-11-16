package com.example.xiangshouzhengdao.ui.result

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.data.local.DataRepository
import com.example.xiangshouzhengdao.data.local.FavoritePrescriptionEntity
import com.example.xiangshouzhengdao.data.local.toEntity
import com.example.xiangshouzhengdao.data.local.toModel
import com.example.xiangshouzhengdao.data.model.BodyResult
import com.example.xiangshouzhengdao.data.model.Prescription
import com.example.xiangshouzhengdao.data.model.UserRecord
import com.example.xiangshouzhengdao.databinding.ActivityResultBinding
import com.example.xiangshouzhengdao.engine.PrescriptionEngine
import com.example.xiangshouzhengdao.i18n.Loc
import com.example.xiangshouzhengdao.ui.survey.SurveyActivity
import com.example.xiangshouzhengdao.utils.SolarTermUtil
import com.google.gson.Gson
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import java.util.UUID
import java.security.MessageDigest

class ResultActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BODY_RESULT = "extra_body_result"
        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_CONSTRAINTS = "extra_constraints"
        const val EXTRA_FROM_HISTORY = "extra_from_history"
        const val EXTRA_ANSWERS = "extra_answers"
        const val EXTRA_RECORD_ID = "extra_record_id"
        const val EXTRA_TEST_DATE = "extra_test_date"
    }

    private lateinit var binding: ActivityResultBinding
    private lateinit var bodyResult: BodyResult
    private var currentPrescription: Prescription? = null
    private var isFavorited = false
    private var currentFavoriteId: String = ""
    private var recordId: String = ""
    private var testDate: Long = 0L
    private var fromHistory = false
    private var favoriteBusy = false
    private val savePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) saveReportAsImage() else Toast.makeText(this, getString(R.string.storage_permission_toast), Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bodyResultJson = intent.getStringExtra(EXTRA_BODY_RESULT) ?: return finish()
        bodyResult = Gson().fromJson(bodyResultJson, BodyResult::class.java)

        // 同一份答卷的ID来自提交事件，并随重建恢复，旋转不能再创建一条历史。
        recordId = savedInstanceState?.getString(EXTRA_RECORD_ID)
            ?: intent.getStringExtra(EXTRA_RECORD_ID) ?: UUID.randomUUID().toString()
        testDate = savedInstanceState?.getLong(EXTRA_TEST_DATE)
            ?: intent.getLongExtra(EXTRA_TEST_DATE, System.currentTimeMillis())
        fromHistory = intent.getBooleanExtra(EXTRA_FROM_HISTORY, false)
        initViews()
        displayResult()
    }

    private fun initViews() {
        binding.btnShare.isEnabled = false
        binding.btnFavorite.isEnabled = false
        binding.btnShare.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                savePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else saveReportAsImage()
        }

        binding.btnRetest.setOnClickListener {
            startActivity(Intent(this, SurveyActivity::class.java))
            finish()
        }

        binding.btnFavorite.setOnClickListener {
            toggleFavorite()
        }
    }

    private fun displayResult() {
        binding.tvResultTitle.text = bodyResult.title
        binding.tvResultDesc.text = bodyResult.description

        val troublesText = if (bodyResult.troubles.isNotBlank()) {
            bodyResult.troubles
        } else {
            bodyResult.description
        }
        binding.tvTroubles.text = troublesText

        setupRadarChart(binding.radarChart)

        lifecycleScope.launch {
            try {
                val repository = DataRepository.getInstance(this@ResultActivity)
                check(repository.initializeDatabase()) { "Database initialization failed" }
                val bodyPrescriptions = repository.getAllBodyPrescriptions().map { it.toModel() }
                val solarPrescriptions = repository.getAllSolarPrescriptions().map { it.toModel() }
                val prescriptionEngine = PrescriptionEngine(bodyPrescriptions, solarPrescriptions,
                    herbs = repository.getAllHerbs().map { it.toModel() })
                val currentSolarTerm = SolarTermUtil.getCurrentSolarTerm()
                val userId = repository.getCurrentUserId()
                val constraintsType = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
                val passedConstraints: List<String> = Gson().fromJson(
                    intent.getStringExtra(EXTRA_CONSTRAINTS) ?: "[]", constraintsType) ?: emptyList()
                val legacyConstraints = getSharedPreferences("user_constraints_$userId", MODE_PRIVATE)
                    .getStringSet("constraints", emptySet())?.toList().orEmpty()
                // 历史按当前节气更新，但仍保留当次声明；旧记录无声明时补当前用户已保存的条件。
                val constraints = if (fromHistory && passedConstraints.isEmpty()) legacyConstraints else passedConstraints
                val prescription = prescriptionEngine.generate(bodyResult, currentSolarTerm, constraints)
                currentPrescription = prescription
                currentFavoriteId = generateFavoriteId(userId, prescription)
                checkFavoriteStatus()
                displayPrescription(prescription)
                displayProducts(prescription)
                displayContraindications(prescription)
                displayPrescriptionTroubles(prescription)
                displayTotalEffect(prescription)
                binding.btnShare.isEnabled = true
                binding.btnFavorite.isEnabled = true
                if (!fromHistory) autoSaveRecord(repository, prescription, constraints)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                e.printStackTrace()
                Toast.makeText(this@ResultActivity, getString(R.string.solar_load_fail), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupRadarChart(chart: RadarChart) {
        // 清除默认的 "No chart data available" 提示
        chart.setNoDataText("")
        chart.setNoDataTextColor(Color.TRANSPARENT)

        val scoreValues = bodyResult.scores.values.toList()
        if (scoreValues.isEmpty()) {
            chart.clear()
            chart.invalidate()
            return
        }

        val entries = scoreValues.map {
            RadarEntry(it.transformScore.toFloat().coerceIn(0f, 100f))
        }

        val dataSet = RadarDataSet(entries, getString(R.string.result_radar_label)).apply {
            color = ContextCompat.getColor(this@ResultActivity, R.color.primary)
            fillColor = ContextCompat.getColor(this@ResultActivity, R.color.primary_light)
            fillAlpha = 64
            setDrawFilled(true)
            lineWidth = 2f
            valueTextSize = 10f
        }

        val data = RadarData(dataSet)
        chart.data = data

        val labels = scoreValues.map { Loc.bodyShort(it.bodyTypeId) }
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.textSize = 9f
        chart.xAxis.textColor = "#7A7A7A".toColorInt()

        chart.yAxis.apply {
            axisMinimum = 0f
            axisMaximum = 100f
            labelCount = 5
        }
        chart.legend.isEnabled = false
        chart.description.isEnabled = false

        chart.notifyDataSetChanged()
        chart.invalidate()
    }

    private fun displayPrescription(prescription: Prescription) {
        binding.herbListContainer.removeAllViews()

        // 过滤掉权重 < 1% 的药材
        val filteredHerbs = prescription.herbs.filter { it.ratio >= 1 }

        filteredHerbs.forEach { herb ->
            val herbView = layoutInflater.inflate(
                R.layout.item_herb,
                binding.herbListContainer,
                false
            )

            herbView.findViewById<TextView>(R.id.tvHerbRole).apply {
                text = Loc.roleName(herb.role)
                setTextColor(herb.roleColor.toColorInt())
            }
            herbView.findViewById<TextView>(R.id.tvHerbName).text = herb.name
            herbView.findViewById<TextView>(R.id.tvHerbRatio).text = getString(R.string.lint_engine_herb_ratio, herb.ratio)

            binding.herbListContainer.addView(herbView)
        }
    }

    private fun displayProducts(prescription: Prescription) {
        binding.productsContainer.removeAllViews()

        prescription.products.forEach { product ->
            val productView = TextView(this).apply {
                text = Loc.product(product)
                textSize = 14f
                setTextColor(getColor(R.color.text_primary))
                setBackgroundResource(R.drawable.bg_product_tag)
                setPadding(16, 12, 16, 12)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    openTaobaoSearch(product)
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 12
                }
            }
            binding.productsContainer.addView(productView)
        }
    }

    private fun displayContraindications(prescription: Prescription) {
        val tvContraindications = binding.root.findViewById<TextView>(R.id.tvContraindications)
        if (tvContraindications != null && prescription.contraindications.isNotBlank()) {
            tvContraindications.text = prescription.contraindications
            tvContraindications.visibility = View.VISIBLE
        } else if (tvContraindications != null) {
            tvContraindications.visibility = View.GONE
        }
    }

    private fun displayPrescriptionTroubles(prescription: Prescription) {
        // 香方卡片内已删除 tvPrescriptionTroubles，此函数保留为空实现以避免编译错误
    }

    private fun displayTotalEffect(prescription: Prescription) {
        val tvTotalEffect = binding.root.findViewById<TextView>(R.id.tvTotalEffect)
        if (tvTotalEffect != null && prescription.totalEffect.isNotBlank()) {
            tvTotalEffect.text = prescription.totalEffect
            tvTotalEffect.visibility = View.VISIBLE
        } else if (tvTotalEffect != null) {
            tvTotalEffect.visibility = View.GONE
        }
    }

    private fun openTaobaoSearch(query: String) {
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        val url = "https://s.taobao.com/search?q=$encodedQuery"
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }

    private fun generateFavoriteId(userId: String, prescription: Prescription): String {
        // 用稳定的配方内容做键，不把每次打开页面的时间或翻译文案当成新配方。
        val content = prescription.solarTerm + prescription.herbs.sortedBy { it.name }
            .joinToString("|") { "${it.name}:${it.role}:${it.ratio}" }
        val hash = MessageDigest.getInstance("SHA-256").digest(content.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return "${userId}_$hash"
    }

    private fun checkFavoriteStatus() {
        lifecycleScope.launch {
            val repository = DataRepository.getInstance(this@ResultActivity)
            isFavorited = repository.isFavoritePrescriptionExists(currentFavoriteId)
            updateFavoriteButtonUI()
        }
    }

    private fun updateFavoriteButtonUI() {
        if (isFavorited) {
            binding.btnFavorite.text = getString(R.string.result_favorited)
            binding.btnFavorite.setTextColor(ContextCompat.getColor(this, R.color.primary))
        } else {
            binding.btnFavorite.text = getString(R.string.result_favorite)
            binding.btnFavorite.setTextColor(ContextCompat.getColor(this, R.color.text_hint))
        }
    }

    private fun toggleFavorite() {
        if (favoriteBusy || currentPrescription == null || currentFavoriteId.isEmpty()) return
        favoriteBusy = true
        binding.btnFavorite.isEnabled = false
        lifecycleScope.launch {
            try {
                val repository = DataRepository.getInstance(this@ResultActivity)
                val prescription = currentPrescription ?: return@launch
                // 以数据库状态为准，避免初始状态查询和连续点击互相覆盖。
                if (repository.isFavoritePrescriptionExists(currentFavoriteId)) {
                    repository.deleteFavoritePrescription(currentFavoriteId)
                    isFavorited = false
                    Toast.makeText(this@ResultActivity, getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show()
                } else {
                    repository.saveFavoritePrescription(FavoritePrescriptionEntity(
                        favoriteId = currentFavoriteId,
                        userId = repository.getCurrentUserId(),
                        bodyType = bodyResult.title,
                        solarTerm = prescription.solarTerm,
                        totalEffect = prescription.totalEffect,
                        herbsJson = Gson().toJson(prescription.herbs),
                        productsJson = Gson().toJson(prescription.products),
                        contraindications = prescription.contraindications,
                        createdAt = System.currentTimeMillis()
                    ))
                    isFavorited = true
                    Toast.makeText(this@ResultActivity, getString(R.string.favorite_added), Toast.LENGTH_SHORT).show()
                }
                updateFavoriteButtonUI()
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Toast.makeText(this@ResultActivity, getString(R.string.save_fail), Toast.LENGTH_SHORT).show()
            } finally {
                favoriteBusy = false
                binding.btnFavorite.isEnabled = true
            }
        }
    }

    private suspend fun autoSaveRecord(repository: DataRepository, prescription: Prescription, constraints: List<String>) {
        if (repository.getUserRecordById(recordId) != null) return
        val answerType = object : com.google.gson.reflect.TypeToken<Map<Int, Int>>() {}.type
        val answers: Map<Int, Int> = Gson().fromJson(intent.getStringExtra(EXTRA_ANSWERS) ?: "{}", answerType) ?: emptyMap()
        val record = UserRecord(
            recordId = recordId,
            testDate = testDate,
            mode = intent.getStringExtra(EXTRA_MODE) ?: "full",
            answers = answers,
            scores = bodyResult.scores,
            bodyType = bodyResult.title,
            mainTypes = bodyResult.mainTypes.map { it.bodyTypeId },
            solarTerm = prescription.solarTerm,
            prescription = prescription,
            bodyResultJson = Gson().toJson(bodyResult),
            constraints = constraints
        )
        repository.saveUserRecord(record.toEntity(repository.getCurrentUserId(), record.bodyResultJson))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(EXTRA_RECORD_ID, recordId)
        outState.putLong(EXTRA_TEST_DATE, testDate)
        super.onSaveInstanceState(outState)
    }

    private fun saveReportAsImage() {
        try {
            val contentView = binding.root.getChildAt(0) as LinearLayout

            contentView.measure(
                View.MeasureSpec.makeMeasureSpec(binding.root.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            contentView.layout(0, 0, contentView.measuredWidth, contentView.measuredHeight)

            val bitmap = createBitmap(
                contentView.measuredWidth,
                contentView.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            contentView.draw(canvas)

            val filename = "香守正道体质报告_${System.currentTimeMillis()}.png"
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/香守正道")
                }
            }

            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                try {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream))
                    } ?: error("Cannot open gallery entry")
                } catch (e: Exception) {
                    contentResolver.delete(uri, null, null)
                    throw e
                }
                Toast.makeText(this, getString(R.string.result_saved_toast), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, getString(R.string.result_save_fail), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.result_save_fail) + ": " + e.message, Toast.LENGTH_SHORT).show()
        }
    }
}
