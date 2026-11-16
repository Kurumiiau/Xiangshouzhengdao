package com.example.xiangshouzhengdao.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.data.local.DataRepository
import com.example.xiangshouzhengdao.data.local.FavoritePrescriptionEntity
import com.example.xiangshouzhengdao.i18n.Loc
import com.example.xiangshouzhengdao.databinding.ActivityMyPrescriptionsBinding
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class MyPrescriptionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyPrescriptionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPrescriptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        loadFavorites()
    }

    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadFavorites() {
        lifecycleScope.launch {
            val repository = DataRepository.getInstance(this@MyPrescriptionsActivity)
            val favorites = repository.getCurrentUserFavorites()

            if (favorites.isEmpty()) {
                showEmptyState()
            } else {
                showFavoritesList(favorites)
            }
        }
    }

    private fun showEmptyState() {
        binding.recyclerFavorites.visibility = View.GONE
        binding.emptyState.visibility = View.VISIBLE
    }

    private fun showFavoritesList(favorites: List<FavoritePrescriptionEntity>) {
        binding.emptyState.visibility = View.GONE
        binding.recyclerFavorites.visibility = View.VISIBLE
        binding.recyclerFavorites.layoutManager = LinearLayoutManager(this)
        binding.recyclerFavorites.adapter = FavoriteAdapter(favorites) { favorite ->
            val intent = Intent(this, PrescriptionDetailActivity::class.java).apply {
                putExtra("favorite_id", favorite.favoriteId)
                putExtra("body_type", favorite.bodyType)
                putExtra("solar_term", favorite.solarTerm)
                putExtra("total_effect", favorite.totalEffect)
                putExtra("herbs_json", favorite.herbsJson)
                putExtra("products_json", favorite.productsJson)
                putExtra("contraindications", favorite.contraindications)
                putExtra("created_at", favorite.createdAt)
            }
            startActivity(intent)
        }
    }

    // 详情页可以取消收藏，回来后重新取一次，别继续显示旧列表。
    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    inner class FavoriteAdapter(
        private val favorites: List<FavoritePrescriptionEntity>,
        private val onClick: (FavoritePrescriptionEntity) -> Unit
    ) : RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

        inner class ViewHolder(val cardView: MaterialCardView) : RecyclerView.ViewHolder(cardView) {
            init {
                cardView.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onClick(favorites[position])
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val cardView = android.view.LayoutInflater.from(parent.context)
                .inflate(com.example.xiangshouzhengdao.R.layout.item_favorite_prescription, parent, false) as MaterialCardView
            return ViewHolder(cardView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val favorite = favorites[position]
            val cardView = holder.cardView

            cardView.findViewById<android.widget.TextView>(
                com.example.xiangshouzhengdao.R.id.tvBodyType
            ).text = favorite.bodyType

            cardView.findViewById<android.widget.TextView>(
                com.example.xiangshouzhengdao.R.id.tvSolarTerm
            ).text = getString(R.string.ui_term_badge, Loc.solarTerm(favorite.solarTerm), Loc.signatureBlend())

            cardView.findViewById<android.widget.TextView>(
                com.example.xiangshouzhengdao.R.id.tvEffect
            ).text = favorite.totalEffect

            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date(favorite.createdAt))
            cardView.findViewById<android.widget.TextView>(
                com.example.xiangshouzhengdao.R.id.tvDate
            ).text = getString(R.string.ui_saved_on_date, dateStr)
        }

        override fun getItemCount() = favorites.size
    }
}