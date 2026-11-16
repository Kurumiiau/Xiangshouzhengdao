package com.example.xiangshouzhengdao.ui.herb

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.data.model.Herb
import com.example.xiangshouzhengdao.i18n.Loc
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.Future

class HerbAdapter(
    private val onItemClick: (Herb) -> Unit
) : ListAdapter<Herb, HerbAdapter.HerbViewHolder>(HerbDiffCallback()) {

    private val executor = Executors.newFixedThreadPool(4)
    private val holders = mutableSetOf<HerbViewHolder>()
    private var closed = false

    fun close() {
        closed = true
        holders.forEach { it.recycle() }
        holders.clear()
        executor.shutdownNow()
    }

    override fun onViewRecycled(holder: HerbViewHolder) {
        holder.recycle()
        holders.remove(holder)
        super.onViewRecycled(holder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HerbViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_herb_card, parent, false)
        return HerbViewHolder(view, onItemClick, executor) { closed }
    }

    override fun onBindViewHolder(holder: HerbViewHolder, position: Int) {
        holders.add(holder)
        holder.bind(getItem(position))
    }

    class HerbViewHolder(
        itemView: View,
        private val onItemClick: (Herb) -> Unit,
        private val executor: java.util.concurrent.ExecutorService,
        private val isClosed: () -> Boolean
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivHerbImage: ImageView = itemView.findViewById(R.id.ivHerbImage)
        private val tvHerbName: TextView = itemView.findViewById(R.id.tvHerbName)
        private val tvHerbCategory: TextView = itemView.findViewById(R.id.tvHerbCategory)
        private val tvHerbEffect: TextView = itemView.findViewById(R.id.tvHerbEffect)
        private val tvHerbRole: TextView = itemView.findViewById(R.id.tvHerbRole)
        private val tvHerbNature: TextView = itemView.findViewById(R.id.tvHerbNature)

        private var currentHerb: Herb? = null
        private var imageRequest: Future<*>? = null
        private var requestGeneration = 0

        fun recycle() {
            requestGeneration++
            imageRequest?.cancel(true)
            imageRequest = null
            currentHerb = null
        }

        init {
            itemView.setOnClickListener {
                currentHerb?.let { onItemClick(it) }
            }
        }

        fun bind(herb: Herb) {
            recycle()
            currentHerb = herb
            tvHerbName.text = herb.name
            tvHerbCategory.text = Loc.herbCategory(herb.category)
            tvHerbEffect.text = herb.effect
            tvHerbRole.text = Loc.roleName(herb.role)
            tvHerbNature.text = herb.nature

            ivHerbImage.setImageResource(R.drawable.ic_herb)

            loadHerbImage(herb)
        }

        private fun loadHerbImage(herb: Herb) {
            if (isClosed()) return
            val generation = requestGeneration
            imageRequest = executor.submit {
                var connection: HttpURLConnection? = null
                try {
                    val encodedName = java.net.URLEncoder.encode(herb.name, "UTF-8")
                    val url = URL("https://tse1.mm.bing.net/th?q=$encodedName+中药材")
                    val request = url.openConnection() as HttpURLConnection
                    connection = request
                    request.apply {
                        connectTimeout = 5000
                        readTimeout = 5000
                        requestMethod = "GET"
                        setRequestProperty("User-Agent", "Mozilla/5.0")
                    }

                    val responseCode = request.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val bitmap = request.inputStream.use { BitmapFactory.decodeStream(it) }
                        bitmap?.let {
                            itemView.post {
                                // 请求返回时这个格子可能已经显示另一味药，取消任务并不足以阻止旧回调。
                                if (!isClosed() && requestGeneration == generation && currentHerb?.herbId == herb.herbId) {
                                    ivHerbImage.setImageBitmap(it)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    connection?.disconnect()
                }
            }
        }
    }

    class HerbDiffCallback : DiffUtil.ItemCallback<Herb>() {
        override fun areItemsTheSame(oldItem: Herb, newItem: Herb): Boolean {
            return oldItem.herbId == newItem.herbId
        }

        override fun areContentsTheSame(oldItem: Herb, newItem: Herb): Boolean {
            return oldItem == newItem
        }
    }
}
