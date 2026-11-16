package com.example.xiangshouzhengdao

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.content.edit
import com.example.xiangshouzhengdao.i18n.Loc

class App : Application() {

    companion object {
        const val PREFS_LANG = "app_settings"
        const val KEY_LANG = "app_lang"

        @Volatile
        var currentLang: String = "zh"
            private set

        fun setLang(context: Context, lang: String) {
            currentLang = lang
            Loc.lang = lang
            context.getSharedPreferences(PREFS_LANG, Context.MODE_PRIVATE)
                .edit { putString(KEY_LANG, lang) }
        }

        fun applyAppLocale(lang: String) {
            AppCompatDelegate.setApplicationLocales(
                if (lang == "en") LocaleListCompat.forLanguageTags("en")
                // 清空 Locale 会重新跟随系统，英文系统上选择“中文”也必须明确指定。
                else LocaleListCompat.forLanguageTags("zh-CN")
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        val saved = getSharedPreferences(PREFS_LANG, MODE_PRIVATE)
            .getString(KEY_LANG, "zh") ?: "zh"
        currentLang = saved
        Loc.lang = saved
        applyAppLocale(saved)
    }
}
