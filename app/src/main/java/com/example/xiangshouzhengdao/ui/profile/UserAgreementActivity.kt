package com.example.xiangshouzhengdao.ui.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.xiangshouzhengdao.databinding.ActivityUserAgreementBinding

class UserAgreementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserAgreementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserAgreementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    // 协议正文走资源文件，改文案不用动页面逻辑。
    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
}