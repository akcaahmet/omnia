package com.ahmetakca.omnia.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.Navigation
import com.ahmetakca.omnia.NetworkClient
import com.ahmetakca.omnia.databinding.FragmentRegisterBinding
import com.ahmetakca.omnia.model.RegisterRequest
import com.ahmetakca.omnia.service.ApiService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val compositeDisposable = CompositeDisposable()
    private lateinit var apiService: ApiService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiService = NetworkClient.createApiService(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var usernameValid = false
        var emailValid = false
        var passwordValid = false
        var passwordCheckValid = false
        binding.usernameEditText.doAfterTextChanged { text ->
            val username = text.toString().trim()
            usernameValid = username.length >= 5
            if (!usernameValid) {
                binding.usernameEditText.error = "Kullanıcı adı en az 5 karakter olmalı"
            } else {
                binding.usernameEditText.error = null
            }
        }
        binding.emailEditText.doAfterTextChanged { text ->
            val email = text.toString().trim()
            emailValid = email.contains("@") && email.contains(".")
            if (!emailValid) {
                binding.emailEditText.error = "Mail uygun formatta değil"
            } else {
                binding.emailEditText.error = null
            }
        }
        binding.passwordEditText.doAfterTextChanged { text ->
            val password = text.toString().trim()
            passwordValid = password.length >= 7
            if (!passwordValid) {
                binding.passwordEditText.error = "Şifre en az 7 karakter olmalı"
            } else {
                binding.passwordEditText.error = null
            }
        }
        binding.passwordCheckEditText.doAfterTextChanged { text ->
            val passwordCheck = text.toString().trim()
            passwordCheckValid = passwordCheck == binding.passwordEditText.text.toString().trim()
            if (!passwordCheckValid) {
                binding.passwordCheckEditText.error = "Girilen şifreler uyuşmuyor"
            } else {
                binding.passwordCheckEditText.error = null
            }
        }
        binding.registerButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val passwordCheck = binding.passwordCheckEditText.text.toString().trim()
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || passwordCheck.isEmpty()) {
                Toast.makeText(context, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (usernameValid && emailValid && passwordValid && passwordCheckValid) {
                val request = RegisterRequest(username, email, password)
                compositeDisposable.add(
                    apiService.register(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            // ActionBar kullanılacak ve doğrulama kodu alınacak
                            parentFragmentManager.setFragmentResultListener(
                                "verification_result",
                                viewLifecycleOwner
                            ) { _, bundle ->
                                val verified = bundle.getBoolean("verified")
                                if (verified) {
                                    val action =
                                        RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
                                    Navigation.findNavController(view).navigate(action)
                                }
                            }
                            val dialog = VerificationDialogFragment().apply {
                                arguments = Bundle().apply {
                                    putString("email", response.email)
                                    putString("source", "register")
                                }
                            }
                            dialog.show(parentFragmentManager, "VerificationDialog")
                        }, { error ->
                            if (error is HttpException) {
                                if (error.code() == 422) {
                                    Toast.makeText(
                                        context,
                                        "Mail istenen formatta değil",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@subscribe
                                }
                                val errorMessage =
                                    JSONObject(error.response()?.errorBody()?.string()).getString(
                                        "detail"
                                    )
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Bir hata meydana geldi",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                )
            } else {
                Toast.makeText(context, "Bilgilerinizi kontrol edin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }
}