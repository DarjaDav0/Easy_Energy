package com.example.easyenergy.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.easyenergy.databinding.FragmentDataReadBinding

class DataReadFragment : Fragment() {
    private var _binding: FragmentDataReadBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDataReadBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // the binding -object allows you to access views in the layout, textviews etc.
        binding.textView.text
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun getElectrictyData() {
        val JSON_URL = "http://spotprices.energyecs.frostbit.fi/api/v1/prices"

        // Request a string response from the provided URL.
        val stringRequest: StringRequest = object : StringRequest(
            Request.Method.GET, JSON_URL,
            Response.Listener { response ->
                // print the response as a whole
                // we can use GSON to modify this response into something more usable
                Log.d("ADVTECH", response)

            },
            Response.ErrorListener {
                // typically this is a connection error
                Log.d("ADVTECH", it.toString())
            })
        {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {

                // basic headers for the data
                val headers = HashMap<String, String>()
                headers["Accept"] = "application/json"
                headers["Content-Type"] = "application/json; charset=utf-8"
                return headers
            }
        }

        // Add the request to the RequestQueue. This has to be done in both getting and sending new data.
        // if using this in an activity, use "this" instead of "context"
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(stringRequest)
    }
}

