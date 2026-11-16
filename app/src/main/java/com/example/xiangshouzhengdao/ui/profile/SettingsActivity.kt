package com.example.xiangshouzhengdao.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.xiangshouzhengdao.App
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.databinding.ActivitySettingsBinding
import com.example.xiangshouzhengdao.ui.main.MainActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        refreshLanguageLabel()

        binding.btnLanguage.setOnClickListener {
            showLanguageDialog()
        }
    }

    private fun refreshLanguageLabel() {
        binding.tvLanguageValue.text =
            if (App.currentLang == "en") getString(R.string.lang_en) else getString(R.string.lang_zh)
    }

    private fun showLanguageDialog() {
        val options = arrayOf(getString(R.string.lang_zh), getString(R.string.lang_en))
        val values = arrayOf("zh", "en")
        val current = if (App.currentLang == "en") 1 else 0

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.settings_language))
            .setSingleChoiceItems(options, current) { dialog, which ->
                dialog.dismiss()
                val target = values[which]
                if (target == App.currentLang) return@setSingleChoiceItems
                // 先写偏好再切Locale，页面重建时才能读到同一份语言设置。
                App.setLang(this, target)
                App.applyAppLocale(target)
                Toast.makeText(this, getString(R.string.lang_restart_hint), Toast.LENGTH_LONG).show()
                // 重启到主页,全局应用新语言
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}