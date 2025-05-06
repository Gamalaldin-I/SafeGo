package com.example.safego.ui.signup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.example.safego.databinding.FragmentFirstInfoBinding
import com.example.safego.util.helpers.personalDataValidaion.DataValidator
import com.example.safego.util.helpers.personalDataValidaion.ErrorHints

class FirstInfoFragment : Fragment() {
    //the variables of this fragment
    private var name = ""
    private var ssn = ""
    private var phone = ""
    private lateinit var viewModel: SignupViewModel
    private lateinit var binding: FragmentFirstInfoBinding

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
        binding = FragmentFirstInfoBinding.inflate(inflater , container ,false)
        viewModel = ViewModelProvider(this)[SignupViewModel::class.java]
        setControllers()
        return binding.root
    }

    private fun setControllers(){
        binding.username.addTextChangedListener {
            name=it.toString()
            if(DataValidator.isValidName(name)){
                binding.userNameLayout.error=null
            }
        }
        binding.ssn.addTextChangedListener {
            ssn=it.toString()
            if(DataValidator.validSSN(ssn)){
                binding.ssnLayout.error=null
            }
        }
        binding.phone.addTextChangedListener {
            phone=it.toString()
            if(DataValidator.validPhone(phone)){
                binding.phoneLayout.error=null
            }
        }
    }

    fun showErrors() {
        if (!DataValidator.isValidName(name)) {
            binding.userNameLayout.error = ErrorHints.showNameError()
        }
        if (!DataValidator.validSSN(ssn)) {
            binding.ssnLayout.error = ErrorHints.showSSNError()
        }
        if (!DataValidator.validPhone(phone)) {
            binding.phoneLayout.error = ErrorHints.showPhoneError()
        }
    }

    fun allDone():Boolean{
        return (DataValidator.isValidName(name)&&
                DataValidator.validSSN(ssn)
                && DataValidator.validPhone(phone))
    }
    fun getData() : Triple<String,String,String>{
        return Triple(name,ssn,phone)
    }
}