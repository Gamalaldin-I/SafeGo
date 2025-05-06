package com.example.safego.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.example.safego.databinding.ActivityLoginBinding
import com.example.safego.util.helpers.singlton.Animator
import com.example.safego.ui.signup.SignupActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private var email:String=""
    private var password:String=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        setContentView(binding.root)
        setControllers()


    }
    private fun setControllers(){
        binding.loginBtn.setOnClickListener{
            if(!viewModel.validEmail(email)){
                binding.emailLayout.error=viewModel.showEmailError()}

            if(!viewModel.validPassword(password)) {
                binding.passwordLayout.error = viewModel.showPasswordError()
            }

            viewModel.login(email,password,this)
        }

        binding.email.addTextChangedListener{
            email=it.toString()
            if(viewModel.validEmail(email)){
                binding.emailLayout.error=null
            }
        }
        binding.password.addTextChangedListener{
            password=it.toString()
            if(viewModel.validPassword(password)){
                binding.passwordLayout.error=null
            }
        }
        binding.signup.setOnClickListener{
            Animator.animateTxt(binding.signup){
            startActivity(Intent(this, SignupActivity::class.java))
            finish()}
        }
    }
}
