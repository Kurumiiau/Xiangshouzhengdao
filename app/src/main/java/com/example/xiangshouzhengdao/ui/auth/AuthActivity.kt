package com.example.xiangshouzhengdao.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.data.local.DataRepository
import com.example.xiangshouzhengdao.databinding.ActivityAuthBinding
import com.example.xiangshouzhengdao.ui.main.MainActivity
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var repository: DataRepository

    private var isLoginMode = true  // true=登录, false=注册

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = DataRepository.getInstance(this)
        initViews()
    }

    private fun initViews() {
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                isLoginMode = tab?.position == 0
                updateUI()
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })

        binding.btnSubmit.setOnClickListener {
            if (isLoginMode) {
                doLogin()
            } else {
                doRegister()
            }
        }

        binding.btnGuest.setOnClickListener {
            goToMain()
        }
    }

    private fun updateUI() {
        binding.btnSubmit.text = if (isLoginMode) getString(R.string.auth_sign_in) else getString(R.string.auth_sign_up)

        val registerVisibility = if (isLoginMode) View.GONE else View.VISIBLE
        binding.tilNickname.visibility = registerVisibility
        binding.tilConfirmPassword.visibility = registerVisibility
    }

    // 密码不要trim，注册时允许的空格在登录时也得原样参与校验。
    private fun doLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.auth_fill), Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val user = repository.login(username, password)
            if (user != null) {
                Toast.makeText(this@AuthActivity, getString(R.string.auth_login_ok), Toast.LENGTH_SHORT).show()
                goToMain()
            } else {
                Toast.makeText(this@AuthActivity, getString(R.string.auth_login_fail), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun doRegister() {
        val username = binding.etUsername.text.toString().trim()
        val nickname = binding.etNickname.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (username.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
            Toast.makeText(this, getString(R.string.auth_fill_all), Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, getString(R.string.auth_pwd_mismatch), Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, getString(R.string.auth_pwd_len), Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val userId = repository.register(username, password, nickname)
            if (userId != null) {
                Toast.makeText(this@AuthActivity, getString(R.string.auth_reg_ok), Toast.LENGTH_SHORT).show()
                repository.login(username, password)
                goToMain()
            } else {
                Toast.makeText(this@AuthActivity, getString(R.string.auth_user_exists), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}