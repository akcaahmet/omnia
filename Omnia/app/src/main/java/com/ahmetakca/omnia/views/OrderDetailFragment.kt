package com.ahmetakca.omnia.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahmetakca.omnia.NetworkClient
import com.ahmetakca.omnia.R
import com.ahmetakca.omnia.adapter.OrderItemListAdapter
import com.ahmetakca.omnia.databinding.FragmentOrderDetailBinding
import com.ahmetakca.omnia.model.OrderResponse
import com.ahmetakca.omnia.service.ApiService
import com.ahmetakca.omnia.util.TokenManager
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

class OrderDetailFragment : Fragment() {
    private lateinit var binding: FragmentOrderDetailBinding
    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: ApiService
    private lateinit var orderDetail: OrderResponse
    private lateinit var orderItemListAdapter: OrderItemListAdapter
    private val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenManager = TokenManager(requireContext())
        apiService = NetworkClient.createApiService(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = OrderDetailFragmentArgs.fromBundle(requireArguments())
        orderDetail = args.orderDetails
        val orderSize = orderDetail.items.size
        binding.orderNumberTextView.text = orderDetail.id.toString()
        binding.orderDateTextView.text = formatter.format(orderDetail.createdAt)
        binding.orderSummaryTextView.text = "$orderSize Teslimat, $orderSize Ürün"
        binding.orderDetailTextView.text = "$orderSize Ürün Teslim Edildi"
        val totalPrice = orderDetail.items.map { it.unitPrice * it.quantity }.sum()
        binding.totalPriceTextView.text = totalPrice.toString()
        handleResponse(orderDetail)
    }

    private fun handleResponse(orderItemList: OrderResponse) {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        orderItemListAdapter = OrderItemListAdapter(orderItemList, formatter)
        binding.recyclerView.adapter = orderItemListAdapter
    }
}