package com.example.xiangshouzhengdao.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.data.local.DataRepository
import com.example.xiangshouzhengdao.databinding.ActivityAboutBinding
import com.example.xiangshouzhengdao.BuildConfig
import kotlinx.coroutines.launch

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        loadVersionInfo()
    }

    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnUserAgreement.setOnClickListener {
            startActivity(Intent(this, UserAgreementActivity::class.java))
        }

        binding.btnPrivacyPolicy.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }

    }

    private fun loadVersionInfo() {
        // 使用应用自身的版本，图表依赖也有同名 BuildConfig。
        val versionName = BuildConfig.VERSION_NAME
        binding.tvVersion.text = getString(R.string.about_version, versionName)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
