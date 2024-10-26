package com.example.easyenergy.datatypes

import com.google.gson.annotations.SerializedName


data class ElectricityPrice (

  @SerializedName("_time" ) var Time  : String? = null,
  @SerializedName("value" ) var value : Double? = null

)