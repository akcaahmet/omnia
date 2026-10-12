package com.ahmetakca.omnia.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.ahmetakca.omnia.NetworkClient
import com.ahmetakca.omnia.R
import com.ahmetakca.omnia.databinding.FragmentLoginBinding
import com.ahmetakca.omnia.model.ActivateRequest
import com.ahmetakca.omnia.model.LoginRequest
import com.ahmetakca.omnia.service.ApiService
import com.ahmetakca.omnia.util.TokenManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val compositeDisposable = CompositeDisposable()

    //    private val apiService = NetworkClient.createApiService(requireContext())
    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: ApiService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenManager = TokenManager(requireContext())
        apiService = NetworkClient.createApiService(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            binding.loginButton.visibility = View.INVISIBLE
            binding.loginProgressBar.visibility = View.VISIBLE
            val request = LoginRequest(email, password)
            compositeDisposable.add(
                apiService.login(request)
                    .subscribeOn(Schedulers.io()).observeOn(
                        AndroidSchedulers.mainThread()
                    ).subscribe({ response ->
                        // Hesap aktif edilmemişse bir dialog penceresi gösterilecek
                        // Bu pencere kayıt işlemi sonrasındakiyle aynı olacak
                        // Hesap kaydedilsin mi kontrolü yapılacak ve sonrasında sharedPreferences'a eklenecek
                        tokenManager.saveTokens(
                            response.accessToken,
                            response.refreshToken
                        )
                        tokenManager.saveRememberMe(binding.saveAccountCheckBox.isChecked)
                        println("${response.accessToken}\n${response.refreshToken}")
                        goToHome(view)
                    }, { error ->
                        binding.loginProgressBar.visibility = View.GONE
                        binding.loginButton.visibility = View.VISIBLE
                        if (error is HttpException) {
                            val errorMessage =
                                JSONObject(
                                    error.response()?.errorBody()?.string()!!
                                ).getString("detail")
                            if (error.code() == 422) {
                                Toast.makeText(
                                    context,
                                    "Mail istenen formatta değil",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@subscribe
                            } else if (error.code() == 403) {
                                parentFragmentManager.setFragmentResultListener(
                                    "verification_result",
                                    viewLifecycleOwner
                                ) { _, bundle ->
                                    val verified = bundle.getBoolean("verified")
                                    if (verified) {
                                        val action =
                                            LoginFragmentDirections.actionLoginFragmentToHomeFragment()
                                        Navigation.findNavController(view).navigate(action)
                                    }
                                }
                                val dialog = VerificationDialogFragment().apply {
                                    arguments = Bundle().apply {
                                        putString("email", email)
                                        putString("source", "login")
                                    }
                                }
                                dialog.show(parentFragmentManager, "VerificationDialog")
                            }
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Bir hata meydana geldi",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    })
            )
        }
        binding.registerTextView.setOnClickListener {
            goToRegister(it)
        }
    }

    private fun goToRegister(view: View) {
        val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
        Navigation.findNavController(view).navigate(action)
    }

    private fun goToHome(view: View) {
        val action =
            LoginFragmentDirections.actionLoginFragmentToHomeFragment()
        Navigation.findNavController(view).navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }
}