package com.example.xiangshouzhengdao.ui.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.xiangshouzhengdao.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    // 长文留在strings里，页面只处理返回，避免两套语言各写一份逻辑。
    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
}