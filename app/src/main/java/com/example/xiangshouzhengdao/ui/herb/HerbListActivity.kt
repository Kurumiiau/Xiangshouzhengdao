package com.example.xiangshouzhengdao.ui.herb

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.data.model.Herb
import com.example.xiangshouzhengdao.i18n.Loc
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HerbListActivity : AppCompatActivity() {

    private lateinit var recyclerHerbs: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var btnClearSearch: ImageView
    private lateinit var btnCategory: View
    private lateinit var tvCategoryLabel: TextView
    private lateinit var tvCurrentCategory: TextView
    private lateinit var emptyState: LinearLayout

    private lateinit var adapter: HerbAdapter
    private var allHerbs: List<Herb> = emptyList()
    private var currentCategory: String = "全部"
    private var currentSearch: String = ""

    private val categories = listOf(
        "全部",
        "木质定香类",
        "温阳散寒类",
        "健脾化湿类",
        "疏肝安神类",
        "清润滋阴类",
        "通鼻抗过敏类",
        "活血通络类",
        "补气固本类",
        "清热燥湿类",
        "定香粘合剂",
        "理气开郁类",
        "滋阴降火类",
        "祛风散寒类",
        "开窍醒神类",
        "祛风解表类"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_herb_list)

        initViews()
        loadHerbs()
    }

    private fun initViews() {
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.setNavigationOnClickListener { finish() }

        recyclerHerbs = findViewById(R.id.recyclerHerbs)
        etSearch = findViewById(R.id.etSearch)
        btnClearSearch = findViewById(R.id.btnClearSearch)
        btnCategory = findViewById(R.id.btnCategory)
        tvCategoryLabel = findViewById(R.id.tvCategoryLabel)
        tvCurrentCategory = findViewById(R.id.tvCurrentCategory)
        emptyState = findViewById(R.id.emptyState)

        adapter = HerbAdapter { herb ->
            val intent = Intent(this, HerbDetailActivity::class.java).apply {
                putExtra("herb_name", herb.name)
                putExtra("herb_nature", herb.nature)
                putExtra("herb_meridian", herb.meridian)
                putExtra("herb_effect", herb.effect)
                putExtra("herb_role", herb.role)
                putExtra("herb_category", herb.category)
            }
            startActivity(intent)
        }
        recyclerHerbs.layoutManager = LinearLayoutManager(this)
        recyclerHerbs.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearch = s?.toString()?.trim() ?: ""
                btnClearSearch.visibility = if (currentSearch.isNotEmpty()) View.VISIBLE else View.GONE
                filterHerbs()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnClearSearch.setOnClickListener {
            etSearch.setText("")
            currentSearch = ""
            filterHerbs()
        }

        btnCategory.setOnClickListener {
            showCategoryDialog()
        }
    }

    private fun loadHerbs() {
        try {
            val jsonFile = if (Loc.lang == "en") "herbs_en.json" else "herbs.json"
            val json = assets.open(jsonFile).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Herb>>() {}.type
            allHerbs = Gson().fromJson(json, type) ?: emptyList()
            filterHerbs()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.herb_load_fail), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCategoryDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.herb_category_dialog))
            .setItems(categories.map { Loc.herbCategory(it) }.toTypedArray()) { _, which ->
                currentCategory = categories[which]
                tvCategoryLabel.text = if (currentCategory == "全部") getString(R.string.herb_category) else Loc.herbCategory(currentCategory)
                tvCurrentCategory.text = Loc.herbCategory(currentCategory)
                tvCurrentCategory.visibility = if (currentCategory == "全部") View.GONE else View.VISIBLE
                filterHerbs()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun filterHerbs() {
        val filtered = allHerbs.filter { herb ->
            val categoryMatch = currentCategory == "全部" || herb.category == currentCategory
            val searchMatch = currentSearch.isEmpty() ||
                    herb.name.contains(currentSearch) ||
                    herb.pinyin.contains(currentSearch, ignoreCase = true) ||
                    herb.effect.contains(currentSearch)

            categoryMatch && searchMatch
        }

        adapter.submitList(filtered)

        if (filtered.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerHerbs.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerHerbs.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        // 线程池归列表页面所有，离开后不能让旧图片请求继续占着线程和 View。
        recyclerHerbs.adapter = null
        adapter.close()
        super.onDestroy()
    }
}
