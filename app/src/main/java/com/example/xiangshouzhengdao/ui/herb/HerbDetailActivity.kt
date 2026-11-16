package com.example.xiangshouzhengdao.ui.herb

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.i18n.Loc
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

class HerbDetailActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tvHerbName: TextView
    private lateinit var tvNature: TextView
    private lateinit var tvMeridian: TextView
    private lateinit var tvEffect: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvBaikeContent: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorLayout: View
    private lateinit var btnRetry: TextView
    private lateinit var tvRetryHint: TextView

    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    private var retryCount = 0
    private val maxAutoRetry = 10
    private val retryDelayMs = 2000L // 每次重试间隔 2 秒

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_herb_detail)

        initViews()
        loadHerbInfo()
        loadBaikeContent()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        tvHerbName = findViewById(R.id.tvHerbName)
        tvNature = findViewById(R.id.tvNature)
        tvMeridian = findViewById(R.id.tvMeridian)
        tvEffect = findViewById(R.id.tvEffect)
        tvRole = findViewById(R.id.tvRole)
        tvBaikeContent = findViewById(R.id.tvBaikeContent)
        progressBar = findViewById(R.id.progressBar)
        errorLayout = findViewById(R.id.errorLayout)
        btnRetry = findViewById(R.id.btnRetry)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        btnRetry.setOnClickListener {
            retryCount = 0
            loadBaikeContent()
        }
    }

    private fun loadHerbInfo() {
        val name = intent.getStringExtra("herb_name") ?: Loc.pick("Unknown", "未知")
        val nature = intent.getStringExtra("herb_nature") ?: ""
        val meridian = intent.getStringExtra("herb_meridian") ?: ""
        val effect = intent.getStringExtra("herb_effect") ?: ""
        val role = intent.getStringExtra("herb_role") ?: ""

        tvHerbName.text = name
        tvNature.text = nature
        tvMeridian.text = meridian
        tvEffect.text = effect
        tvRole.text = Loc.roleName(role)
    }

    private fun loadBaikeContent() {
        val herbName = intent.getStringExtra("herb_name") ?: return

        progressBar.visibility = View.VISIBLE
        tvBaikeContent.visibility = View.GONE
        errorLayout.visibility = View.GONE

        executor.execute {
            try {
                val content = fetchBaikeSummary(herbName)
                handler.post {
                    if (isDestroyed || isFinishing) return@post
                    progressBar.visibility = View.GONE
                    if (content != null && content.isNotBlank()) {
                        retryCount = 0
                        tvBaikeContent.text = content
                        tvBaikeContent.visibility = View.VISIBLE
                    } else {
                        handleLoadFailure()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                handler.post {
                    if (isDestroyed || isFinishing) return@post
                    progressBar.visibility = View.GONE
                    handleLoadFailure()
                }
            }
        }
    }

    private fun handleLoadFailure() {
        retryCount++

        if (retryCount <= maxAutoRetry) {
            errorLayout.visibility = View.VISIBLE
            btnRetry.text = getString(R.string.herb_auto_retry, retryCount, maxAutoRetry)
            btnRetry.isEnabled = false

            handler.postDelayed({
                if (!isDestroyed && !isFinishing) {
                    loadBaikeContent()
                }
            }, retryDelayMs)
        } else {
            // 超过10次，变为手动重试（第11次及以后）
            errorLayout.visibility = View.VISIBLE
            btnRetry.text = getString(R.string.herb_retry)
            btnRetry.isEnabled = true
        }
    }

    private fun fetchBaikeSummary(keyword: String): String? {
        return try {
            val encodedKeyword = URLEncoder.encode(keyword, "UTF-8")
            val url = URL("https://baike.baidu.com/api/openapi/BaikeLemmaCardApi?scope=103&format=json&appid=379020&bk_key=$encodedKeyword&bk_length=600")

            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                connectTimeout = 10000
                readTimeout = 10000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10)")
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                parseBaikeResponse(response)
            } else {
                getLocalDescription(keyword)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getLocalDescription(keyword)
        }
    }

    private fun parseBaikeResponse(json: String): String? {
        return try {
            val root = JSONObject(json)
            when {
                root.has("abstract") -> root.getString("abstract")
                root.has("desc") -> root.getString("desc")
                root.has("content") -> root.getString("content")
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    // 百科请求失败时先用已有功效兜底，详情页不能只剩一个重试按钮。
    private fun getLocalDescription(herbName: String): String? {
        return intent.getStringExtra("herb_effect")
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        executor.shutdownNow()
    }
}