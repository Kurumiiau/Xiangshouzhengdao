package com.example.xiangshouzhengdao.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.data.local.DataRepository
import com.example.xiangshouzhengdao.data.model.PrescriptionHerb
import com.example.xiangshouzhengdao.i18n.Loc
import com.example.xiangshouzhengdao.databinding.ActivityPrescriptionDetailBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class PrescriptionDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrescriptionDetailBinding
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrescriptionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        displayPrescriptionDetails()
    }

    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnDeleteFavorite.setOnClickListener {
            deleteFavorite()
        }
    }

    // 收藏保存的是当时的配方，这里直接展示快照，不按今天的节气重算。
    private fun displayPrescriptionDetails() {
        val bodyType = intent.getStringExtra("body_type") ?: ""
        val solarTerm = intent.getStringExtra("solar_term") ?: ""
        val totalEffect = intent.getStringExtra("total_effect") ?: ""
        val herbsJson = intent.getStringExtra("herbs_json") ?: "[]"
        val productsJson = intent.getStringExtra("products_json") ?: "[]"
        val contraindications = intent.getStringExtra("contraindications") ?: ""
        val createdAt = intent.getLongExtra("created_at", 0)

        binding.tvTitle.text = bodyType
        binding.tvSubtitle.text = getString(R.string.ui_term_badge, Loc.solarTerm(solarTerm), Loc.signatureBlend())
        binding.tvTotalEffect.text = totalEffect

        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(createdAt))
        binding.tvDate.text = getString(R.string.ui_saved_at_datetime, dateStr)

        val herbType = object : TypeToken<List<PrescriptionHerb>>() {}.type
        val herbs: List<PrescriptionHerb> = gson.fromJson(herbsJson, herbType) ?: emptyList()

        binding.herbListContainer.removeAllViews()
        herbs.filter { it.ratio >= 1 }.forEach { herb ->
            val herbView = layoutInflater.inflate(
                com.example.xiangshouzhengdao.R.layout.item_herb,
                binding.herbListContainer,
                false
            )
            herbView.findViewById<TextView>(com.example.xiangshouzhengdao.R.id.tvHerbRole).apply {
                text = Loc.roleName(herb.role)
                setTextColor(herb.roleColor.toColorInt())
            }
            herbView.findViewById<TextView>(com.example.xiangshouzhengdao.R.id.tvHerbName).text = herb.name
            herbView.findViewById<TextView>(com.example.xiangshouzhengdao.R.id.tvHerbRatio).text = getString(R.string.ui_ratio_percent, herb.ratio)
            binding.herbListContainer.addView(herbView)
        }

        val productType = object : TypeToken<List<String>>() {}.type
        val products: List<String> = gson.fromJson(productsJson, productType) ?: emptyList()

        binding.productsContainer.removeAllViews()
        products.forEach { product ->
            val productView = TextView(this).apply {
                text = Loc.product(product)
                textSize = 14f
                setTextColor(getColor(com.example.xiangshouzhengdao.R.color.text_primary))
                setBackgroundResource(com.example.xiangshouzhengdao.R.drawable.bg_product_tag)
                setPadding(16, 12, 16, 12)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    openTaobaoSearch(product)
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = 12 }
            }
            binding.productsContainer.addView(productView)
        }

        if (contraindications.isNotBlank()) {
            binding.tvContraindications.text = contraindications
            binding.tvContraindications.visibility = View.VISIBLE
        } else {
            binding.tvContraindications.visibility = View.GONE
        }
    }

    private fun deleteFavorite() {
        val favoriteId = intent.getStringExtra("favorite_id") ?: return

        lifecycleScope.launch {
            val repository = DataRepository.getInstance(this@PrescriptionDetailActivity)
            repository.deleteFavoritePrescription(favoriteId)
            Toast.makeText(this@PrescriptionDetailActivity, getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    
    private fun openTaobaoSearch(query: String) {
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        val url = "https://s.taobao.com/search?q=$encodedQuery"
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }
}