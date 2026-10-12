package com.ahmetakca.omnia.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.ahmetakca.omnia.adapter.QuantityChoiceAdapter
import com.ahmetakca.omnia.databinding.FragmentBottomSheetQuantityBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetQuantityFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentBottomSheetQuantityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBottomSheetQuantityBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val quantityList = (1..10).toList()
        binding.quantityRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.quantityRecyclerView.adapter =
            QuantityChoiceAdapter(quantityList) { selectedQuantity ->
                parentFragmentManager.setFragmentResult(
                    "quantity_result",
                    Bundle().apply { putInt("selected_quantity", selectedQuantity) })
                dismiss()
            }
    }
}