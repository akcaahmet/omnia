package com.ahmetakca.omnia.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.ahmetakca.omnia.NetworkClient
import com.ahmetakca.omnia.R
import com.ahmetakca.omnia.adapter.HistoryListAdapter
import com.ahmetakca.omnia.databinding.FragmentHistoryBinding
import com.ahmetakca.omnia.model.OrderResponse
import com.ahmetakca.omnia.service.ApiService
import com.ahmetakca.omnia.util.TokenManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding
    private val compositeDisposable = CompositeDisposable()
    private lateinit var apiService: ApiService
    private lateinit var historyListAdapter: HistoryListAdapter
    private val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private lateinit var tokenManager: TokenManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenManager = TokenManager(requireContext())
        apiService = NetworkClient.createApiService(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getFromWeb()
    }

    private fun getFromWeb() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        val accessToken = tokenManager.getAccessToken() ?: return
        compositeDisposable.add(
            apiService.order("Bearer $accessToken").subscribeOn(Schedulers.io()).observeOn(
                AndroidSchedulers.mainThread()
            ).subscribe({ response ->
                handleResponse(response)
                binding.loadingProgressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }, { error ->
                binding.loadingProgressBar.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                error.printStackTrace()
            })
        )
    }

    private fun handleResponse(historyList: List<OrderResponse>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyListAdapter = HistoryListAdapter(
            historyList = historyList,
            onDetailClick = { order -> onDetailClick(order) },
            formatter = formatter
        )
        binding.recyclerView.adapter = historyListAdapter
    }

    private fun onDetailClick(order: OrderResponse) {
        val action =
            HistoryFragmentDirections.actionHistoryFragmentToOrderDetailFragment(order)
        Navigation.findNavController(requireView()).navigate(action)
    }
}