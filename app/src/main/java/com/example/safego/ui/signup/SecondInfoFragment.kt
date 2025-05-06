package com.example.safego.ui.signup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.example.safego.databinding.FragmentSecondInfoBinding
import com.example.safego.util.helpers.personalDataValidaion.DataValidator
import com.example.safego.util.helpers.personalDataValidaion.ErrorHints

class SecondInfoFragment : Fragment() {
    private var email = ""
    private var password =""
    private var passConfirmation =""
    private var strength =""
    private lateinit var binding: FragmentSecondInfoBinding
    private lateinit var viewModel: SignupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        //inflate
        binding = FragmentSecondInfoBinding.inflate(inflater , container ,false)
        viewModel = ViewModelProvider(this)[SignupViewModel::class.java]

        setControllers()
        return binding.root
    }
    private fun setControllers(){
        binding.email.addTextChangedListener {
            email=it.toString()
            if(DataValidator.validEmail(email)){
                binding.emailLayout.error=null
            }
        }
        binding.password.addTextChangedListener {
            password=it.toString()
            if(DataValidator.validPasswordLength(password)){
                binding.passwordLayout.error=null
            }
            binding.passwordLayout.helperText= DataValidator.validStrongPassword(password)
            strength= DataValidator.validStrongPassword(password)
        }
        binding.confirmPassword.addTextChangedListener {
            passConfirmation=it.toString()
            if(DataValidator.validPasswordMatch(password,passConfirmation)){
                binding.confirmPasswordLayout.error=null
            }
    }
}
    fun showError(){
        if(!DataValidator.validEmail(email)){
            binding.emailLayout.error= ErrorHints.showEmailError()
        }
        if(!DataValidator.validPasswordLength(password)){
            binding.passwordLayout.error= ErrorHints.showPasswordLengthError()
            binding.passwordLayout.helperText=strength
        }
        if(!DataValidator.strongPassword(password)){
            binding.passwordLayout.error= ErrorHints.showStrongPasswordError()
        }

        if(!DataValidator.validPasswordMatch(password,passConfirmation)){
            binding.confirmPasswordLayout.error= ErrorHints.showPasswordMatchError()
        }
    }
    fun getData() : Triple<String,String,String>{
        return Triple(email,password,passConfirmation)
    }
    fun allDone():Boolean{
        return (DataValidator.validEmail(email)&&
                DataValidator.validPassword(password)
                && DataValidator.validPasswordMatch(password,passConfirmation)
                )

    }
}