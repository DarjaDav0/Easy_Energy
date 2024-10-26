package com.example.easyenergy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView
import com.example.easyenergy.databinding.ItemElectricityPriceBinding
import com.example.easyenergy.datatypes.ElectricityPrice


class ElectricityPriceAdapter(private val prices: List<ElectricityPrice>) :
    RecyclerView.Adapter<ElectricityPriceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemElectricityPriceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(prices[position])
    }

    override fun getItemCount(): Int {
        return prices.size
    }

    inner class ViewHolder(private val binding: ItemElectricityPriceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(price: ElectricityPrice) {
            binding.apply {
                dateTextView.text = price.Time
                priceTextView.text = price.value.toString()
            }
        }
    }
}
