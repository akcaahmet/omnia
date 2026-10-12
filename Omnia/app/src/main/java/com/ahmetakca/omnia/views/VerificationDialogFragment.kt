package com.ahmetakca.omnia.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.ahmetakca.omnia.NetworkClient
import com.ahmetakca.omnia.databinding.FragmentVerificationDialogBinding
import com.ahmetakca.omnia.model.ActivateRequest
import com.ahmetakca.omnia.model.ResendRequest
import com.ahmetakca.omnia.service.ApiService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException

class VerificationDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentVerificationDialogBinding
    var compositeDisposable = CompositeDisposable()
    private lateinit var apiService: ApiService
    private var email = ""
    private var source = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiService = NetworkClient.createApiService(requireContext())
        email = requireArguments().getString("email")!!
        source = requireArguments().getString("source")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVerificationDialogBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var codeValid = false
        var userId: Int
        if (source.equals("login")) {
            binding.resendButton.visibility = View.VISIBLE
        }
        binding.resendButton.setOnClickListener {
            val request = ResendRequest(email)
            compositeDisposable.add(
                apiService.resendCode(request)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { response ->
                            println("X")
                            userId = response.id
                        },
                        { error ->
                            if (error is HttpException) {
                                val errorMessage =
                                    JSONObject(error.response()?.errorBody()?.string()!!).getString(
                                        "detail"
                                    )
                                if (error.code() == 422) {
                                    Toast.makeText(
                                        context,
                                        "Mail istenen formatta değil",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Bir hata meydana geldi",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
            )
        }
        binding.veritificationCodeEditText.doAfterTextChanged { text ->
            val code = text.toString().trim()
            if (code.length != 6) {
                binding.veritificationCodeEditText.error = "Doğrulama kodu 6 haneli olmalı"
                codeValid = false
            } else {
                binding.veritificationCodeEditText.error = null
                codeValid = true
            }
        }
        binding.verifyButton.setOnClickListener {
            val code = binding.veritificationCodeEditText.text.toString().trim()
            val request = ActivateRequest(email, code.toInt())
            if (code.isEmpty()) {
                Toast.makeText(context, "Kod alanı boş olamaz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (codeValid) {
                compositeDisposable.add(
                    apiService.activate(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ response ->
                            // Kodlar kıyaslanacak ve eşleşiyorsa giriş ekranına yönlendirilecek, eşleşmiyorsa toast mesaj gösterilecek
                            parentFragmentManager.setFragmentResult(
                                "verification_result",
                                Bundle().apply { putBoolean("verified", true) })
                            dismiss()
                            Toast.makeText(context, "Hesabınız oluşturuldu", Toast.LENGTH_SHORT)
                                .show()
                        }, { error ->
                            if (error is HttpException) {
                                val errorMessage =
                                    JSONObject(error.response()?.errorBody()?.string()!!).getString(
                                        "detail"
                                    )
                                if (error.code() == 422) {
                                    Toast.makeText(
                                        context,
                                        "Mail istenen formatta değil",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@subscribe
                                }
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
                Toast.makeText(context, "Doğrulama kodunu kontrol edin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }
    }
}