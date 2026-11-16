package com.example.xiangshouzhengdao.ui.survey

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.xiangshouzhengdao.databinding.FragmentSurveyBinding

class SurveyFragment : Fragment() {

    private var _binding: FragmentSurveyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSurveyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        binding.cardQuickSurvey.setOnClickListener {
            startSurveyActivity(SurveyActivity.MODE_QUICK)
        }

        binding.cardFullSurvey.setOnClickListener {
            startSurveyActivity(SurveyActivity.MODE_FULL)
        }
    }

    // 模式要显式传，两个入口共用答题页，但题库和进度不能混。
    private fun startSurveyActivity(mode: String) {
        val intent = Intent(requireContext(), SurveyActivity::class.java).apply {
            putExtra(SurveyActivity.EXTRA_MODE, mode)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}