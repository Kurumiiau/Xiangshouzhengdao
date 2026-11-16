package com.example.xiangshouzhengdao.ui.home

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.data.local.DataRepository
import com.example.xiangshouzhengdao.data.local.toModel
import com.example.xiangshouzhengdao.databinding.FragmentHomeBinding
import com.example.xiangshouzhengdao.ui.calendar.SolarCalendarActivity
import com.example.xiangshouzhengdao.ui.custom.CustomPrescriptionActivity
import com.example.xiangshouzhengdao.ui.herb.HerbListActivity
import com.example.xiangshouzhengdao.ui.history.HistoryActivity
import com.example.xiangshouzhengdao.ui.knowledge.BodyTypeKnowledgeActivity
import com.example.xiangshouzhengdao.ui.result.ResultActivity
import com.example.xiangshouzhengdao.ui.solar.SolarPrescriptionActivity
import com.example.xiangshouzhengdao.i18n.Loc
import com.example.xiangshouzhengdao.ui.survey.SurveyActivity
import com.example.xiangshouzhengdao.utils.SolarTermUtil
import com.example.xiangshouzhengdao.utils.WeatherUtil
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var entrancePlayed = false
    private val ambientAnimators = mutableListOf<Animator>()

    private val gridTiles: List<View>
        get() =
        listOf(
            binding.btnHerbIntro,
            binding.btnSolarPrescription,
            binding.btnBodyKnowledge,
            binding.btnBodyTest,
            binding.btnSolarCalendar,
            binding.btnCustomPrescription
        )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        loadSolarTermInfo()
        loadWeather()
        loadRecentRecord()
        playEntranceAnimations()
        startAmbientAnimations()
    }

    override fun onResume() {
        super.onResume()
        loadSolarTermInfo()
        loadRecentRecord()
    }

    private fun initViews() {
        val targets: List<Pair<View, Class<*>>> = listOf(
            binding.btnHerbIntro to HerbListActivity::class.java,
            binding.btnSolarPrescription to SolarPrescriptionActivity::class.java,
            binding.btnBodyKnowledge to BodyTypeKnowledgeActivity::class.java,
            binding.btnBodyTest to SurveyActivity::class.java,
            binding.btnSolarCalendar to SolarCalendarActivity::class.java,
            binding.btnCustomPrescription to CustomPrescriptionActivity::class.java
        )
        targets.forEach { (view, clazz) ->
            attachPressEffect(view)
            view.setOnClickListener {
                openWithTransition(Intent(requireContext(), clazz))
            }
        }

        attachPressEffect(binding.tvViewAll)
        binding.tvViewAll.setOnClickListener {
            openWithTransition(Intent(requireContext(), HistoryActivity::class.java))
        }
    }

    // 启动参数携带动画，避免使用已经弃用的overridePendingTransition。
    private fun openWithTransition(intent: Intent) {
        val options = ActivityOptions.makeCustomAnimation(requireContext(), R.anim.slide_in_right, R.anim.slide_out_left)
        startActivity(intent, options.toBundle())
    }

    private fun loadSolarTermInfo() {
        val currentTerm = SolarTermUtil.getCurrentSolarTerm()
        binding.tvSolarTerm.text = Loc.solarTerm(currentTerm)
        binding.tvHeroHint.text = Loc.heroHint(SolarTermUtil.getSolarTermTheme(currentTerm))
        binding.tvSolarDate.text = SimpleDateFormat(Loc.datePattern(), Loc.dateLocale()).format(Date())
    }

    private fun loadWeather() {
        binding.tvWeatherTemp.text = "--°C"
        binding.tvWeatherDesc.text = getString(R.string.weather_locating)
        binding.ivWeatherIcon.setImageResource(R.drawable.weather_unknown)
        binding.ivWeatherIcon.imageTintList = ColorStateList.valueOf(Color.WHITE)

        val prefs = requireContext().getSharedPreferences("location_prefs", android.content.Context.MODE_PRIVATE)
        val lat = prefs.getFloat("latitude", 0f).toDouble()
        val lon = prefs.getFloat("longitude", 0f).toDouble()

        if (lat == 0.0 && lon == 0.0) {
            binding.tvWeatherDesc.text = getString(R.string.weather_not_located)
            fetchWeatherWithLocation(39.9042, 116.4074)
            return
        }
        fetchWeatherWithLocation(lat, lon)
    }

    private fun fetchWeatherWithLocation(lat: Double, lon: Double) {
        val viewBinding = _binding ?: return
        WeatherUtil.fetchWeatherByLocation(lat, lon, requireContext()) { weatherInfo ->
            activity?.runOnUiThread {
                // 网络完成时可能已切换tab；只允许更新发起请求时的那一棵视图。
                if (_binding !== viewBinding || !isAdded) return@runOnUiThread
                binding.ivWeatherIcon.imageTintList = ColorStateList.valueOf(Color.WHITE)
                if (weatherInfo != null) {
                    binding.tvWeatherTemp.text = getString(R.string.ui_temperature_celsius, weatherInfo.temperature.toInt())
                    binding.tvWeatherDesc.text = getString(R.string.ui_weather_location, weatherInfo.cityName, weatherInfo.description)
                    // 资源ID直接引用，拼写和裁剪都能在构建时检查。
                    val iconResId = when (weatherInfo.iconName) {
                        "weather_sunny" -> R.drawable.weather_sunny
                        "weather_cloudy" -> R.drawable.weather_cloudy
                        "weather_fog" -> R.drawable.weather_fog
                        "weather_drizzle" -> R.drawable.weather_drizzle
                        "weather_sleet" -> R.drawable.weather_sleet
                        "weather_rain" -> R.drawable.weather_rain
                        "weather_snow" -> R.drawable.weather_snow
                        "weather_thunder" -> R.drawable.weather_thunder
                        else -> R.drawable.weather_unknown
                    }
                    binding.ivWeatherIcon.setImageResource(iconResId)
                    binding.ivWeatherIcon.imageTintList = ColorStateList.valueOf(Color.WHITE)
                } else {
                    binding.tvWeatherTemp.text = "--°C"
                    binding.tvWeatherDesc.text = getString(R.string.weather_failed)
                    binding.ivWeatherIcon.setImageResource(R.drawable.weather_unknown)
                }
            }
        }
    }

    private fun loadRecentRecord() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val repository = DataRepository.getInstance(requireContext())
                val records = repository.getCurrentUserRecords()
                    .map { it.toModel() }
                    .sortedByDescending { it.testDate }

                if (records.isEmpty()) {
                    binding.recentRecordContainer.removeAllViews()
                    if (binding.tvEmptyRecord.parent == null) {
                        binding.recentRecordContainer.addView(binding.tvEmptyRecord)
                    }
                    binding.tvEmptyRecord.visibility = View.VISIBLE
                    return@launch
                }

                binding.tvEmptyRecord.visibility = View.GONE
                val record = records.first()

                val recordView = layoutInflater.inflate(
                    R.layout.item_history,
                    binding.recentRecordContainer,
                    false
                )

                val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
                val dateStr = dateFormat.format(Date(record.testDate))
                val parts = dateStr.split("-")

                recordView.findViewById<TextView>(R.id.tvDay).text = parts[1]
                recordView.findViewById<TextView>(R.id.tvMonth).text = SimpleDateFormat(Loc.monthPattern(), Loc.dateLocale()).format(Date(record.testDate))
                recordView.findViewById<TextView>(R.id.tvBodyType).text = record.bodyType
                recordView.findViewById<TextView>(R.id.tvSolarTerm).text =
                    getString(R.string.ui_term_badge, Loc.solarTerm(record.solarTerm),
                        if (record.mode == "quick") Loc.quickBadge() else Loc.fullBadge())

                recordView.setOnClickListener {
                    val bodyResultJson = record.bodyResultJson
                    val intent = if (!bodyResultJson.isNullOrBlank()) {
                        Intent(requireContext(), ResultActivity::class.java).apply {
                            putExtra(ResultActivity.EXTRA_BODY_RESULT, bodyResultJson)
                            putExtra(ResultActivity.EXTRA_MODE, record.mode)
                            putExtra(ResultActivity.EXTRA_FROM_HISTORY, true)
                            putExtra(ResultActivity.EXTRA_RECORD_ID, record.recordId)
                            putExtra(ResultActivity.EXTRA_TEST_DATE, record.testDate)
                            putExtra(ResultActivity.EXTRA_ANSWERS, Gson().toJson(record.answers))
                            putExtra(ResultActivity.EXTRA_CONSTRAINTS, Gson().toJson(record.constraints))
                        }
                    } else {
                        val bodyResult = com.example.xiangshouzhengdao.data.model.BodyResult(
                            type = com.example.xiangshouzhengdao.data.model.BodyResult.ResultType.MILD,
                            title = record.bodyType,
                            description = Loc.pick("Past assessment record", "历史测评记录"),
                            scores = record.scores,
                            mainTypes = record.mainTypes.mapNotNull { record.scores[it] },
                            mildTypes = emptyList(),
                            recommendation = ""
                        )
                        Intent(requireContext(), ResultActivity::class.java).apply {
                            putExtra(ResultActivity.EXTRA_BODY_RESULT, Gson().toJson(bodyResult))
                            putExtra(ResultActivity.EXTRA_MODE, record.mode)
                            putExtra(ResultActivity.EXTRA_FROM_HISTORY, true)
                            putExtra(ResultActivity.EXTRA_RECORD_ID, record.recordId)
                            putExtra(ResultActivity.EXTRA_TEST_DATE, record.testDate)
                            putExtra(ResultActivity.EXTRA_ANSWERS, Gson().toJson(record.answers))
                            putExtra(ResultActivity.EXTRA_CONSTRAINTS, Gson().toJson(record.constraints))
                        }
                    }
                    openWithTransition(intent)
                }

                binding.recentRecordContainer.removeAllViews()
                binding.recentRecordContainer.addView(recordView)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                e.printStackTrace()
            }
        }
    }

    // 这里只观察触摸做动画并返回false，点击和无障碍performClick由View默认处理，不能重复调用。
    @SuppressLint("ClickableViewAccessibility")
    private fun attachPressEffect(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().cancel()
                    v.animate().scaleX(0.94f).scaleY(0.94f).setDuration(110).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().cancel()
                    v.animate().scaleX(1f).scaleY(1f)
                        .setDuration(190)
                        .setInterpolator(OvershootInterpolator(2f))
                        .start()
                }
            }
            false
        }
    }

    private fun playEntranceAnimations() {
        if (entrancePlayed) return
        entrancePlayed = true

        val scene = mutableListOf<View>()
        scene.add(binding.heroCard)
        scene.addAll(gridTiles)
        scene.add(binding.recentCard)

        scene.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = dp(16f)
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(50L + index * 45L)
                .setDuration(420L)
                .setInterpolator(DecelerateInterpolator(1.8f))
                .start()
        }
    }

    private fun startAmbientAnimations() {
        ObjectAnimator.ofFloat(binding.ivWeatherIcon, "alpha", 1f, 0.55f).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.also { ambientAnimators.add(it); it.start() }
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    override fun onDestroyView() {
        ambientAnimators.forEach { it.cancel() }
        ambientAnimators.clear()
        entrancePlayed = false
        super.onDestroyView()
        _binding = null
    }
}