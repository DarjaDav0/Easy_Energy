package com.example.easyenergy.fragments

import android.R
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.easyenergy.BuildConfig
import com.example.easyenergy.datatypes.ElectricityPrice
import com.github.aachartmodel.aainfographics.aachartcreator.*
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAStyle
import com.google.gson.GsonBuilder
import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.domain.Authorization
import com.influxdb.client.domain.Permission
import com.influxdb.client.domain.PermissionResource
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.channels.consumeEach
import kotlin.collections.ArrayList


class MainViewModel : ViewModel(){
    private val url = "http://localhost:8086"
    //token pitäisi saada local.properties tiedostoon ja sitten hakea sieltä
    private val token = "wRvSgV9igGmzZnHpB-pJ-l-oZQ2LtRwSoZgeazPmWTKLaP5RJhEYVhGgoh5bYVbPl8H0HSrV41KWBj94rztSkw=="
    private val org = "easyenergy"
    private val bucket = "electricity_prices"
    private val client: InfluxDBClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket)

    private var hoursList = mutableListOf<Double>()
    var allDataList = mutableListOf<ElectricityPrice>()
    var dayDataList = mutableListOf<Double>()
    var dayClassList = mutableListOf<ElectricityPrice>()
    private lateinit var testChart : AAChartModel
    val getChart get() = testChart
    var chosenYear: String = "2022"
    val timeFormat = ""
    var currentTime = ""
    private var _currentHourPrice: String= ""
    val currentHourPrice get() = _currentHourPrice

    private var _yearArray = mutableListOf<String>()
    private var selectedYear = "2023"



    //tällä hetkellä hakee getAllDatan kautta kaikki vuodet, _yearArrayhyn tallennettu getAllDatan data -> tulee vaihtaa sitten lopulliseen vuoden datan hakuun
    fun createSpinner(spinner: Spinner, context: Context)
    {
        val spinnerAdapter= ArrayAdapter(context, R.layout.simple_spinner_item, _yearArray)
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {

                selectedYear = spinner.getSelectedItem().toString()
                Log.d("Spinner", selectedYear)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    fun createDayChart()
    {
        testChart = AAChartModel()
            .chartType(AAChartType.Column)
            //.title("Hintaseuranta")
            .titleStyle(AAStyle.Companion.style("#494949", 32, AAChartFontWeightType.Bold))
            .axesTextColor("#494949")
            .backgroundColor("#f9bf05")


            .borderRadius(3)
            .title("Päivä hinta")
            .animationDuration(650)
            .animationType(AAChartAnimationType.SwingTo)

            .dataLabelsEnabled(true)
            .series(arrayOf(
                AASeriesElement()
                    .color("#494949")
                    .name("c/kwh")
                    .data(dayDataList.toTypedArray())
            )
            )
    }

    fun createMonthChart()
    {
        hoursList.clear()
        //for loop on puhtaasti tehty testausta varten
        for (i in 0 until 7)
        {
            hoursList.add(i.toDouble())
        }

        testChart = AAChartModel()
            .chartType(AAChartType.Column)
            //.title("Hintaseuranta")
            .titleStyle(AAStyle.Companion.style("#494949", 32, AAChartFontWeightType.Bold))
            .axesTextColor("#494949")
            .backgroundColor("#f9bf05")

            .borderRadius(3)
            .title("Kuukausi hinta")
            .animationDuration(650)
            .animationType(AAChartAnimationType.SwingTo)

            .dataLabelsEnabled(true)
            .series(arrayOf(
                AASeriesElement()
                    .color("#494949")
                    .name("c/kwh")
                    .data(hoursList.toTypedArray())
            )
            )
    }

    fun createYearChart()
    {
        hoursList.clear()

        //for loop on puhtaasti tehty testausta varten
        for (i in 0 until 4)
        {
            hoursList.add(i.toDouble())
        }

        testChart = AAChartModel()
            .chartType(AAChartType.Column)
            //.title("Hintaseuranta")
            .titleStyle(AAStyle.Companion.style("#494949", 32, AAChartFontWeightType.Bold))
            .axesTextColor("#494949")
            .backgroundColor("#f9bf05")


            .borderRadius(3)
            .title("Vuosi hinta")
            .animationDuration(650)
            .animationType(AAChartAnimationType.SwingTo)

            .dataLabelsEnabled(true)
            .series(arrayOf(
                AASeriesElement()
                    .color("#494949")
                    .name("c/kwh")
                    .data(hoursList.toTypedArray())
            )
            )
    }

    fun getAllData(context: Context) {
        viewModelScope.launch {
            val JSON_URL = "http://spotprices.energyecs.frostbit.fi/api/v1/prices"

            val gson = GsonBuilder().setPrettyPrinting().create()
            // Request a string response from the provided URL.
            val stringRequest: StringRequest = object : StringRequest(
                Request.Method.GET, JSON_URL,
                Response.Listener { response ->
                    var result : List<ElectricityPrice> = gson.fromJson(response, Array<ElectricityPrice>::class.java).toList()

                    //lisää atm valitun vuoden datan listaan, kevyempi myös testata vuoden datalla
                    for (item: ElectricityPrice in result)
                    {
                        val timeValSub = item.Time?.substring(0,4)


                        if (timeValSub == chosenYear)
                        {
                            allDataList.add(item)
                        }

                        //lisää vuosi arrayhyn vuoden jos sitä ei ole jo listassa, tällä tavalla välttyi duplikaateilta
                        //TODO: muokata vuoden lisäys oman tietokannan datan mukaan
                        if (_yearArray.contains(timeValSub))
                        {
                            true
                        }
                        else
                        {
                            _yearArray.add(timeValSub!!)
                        }

                    }
                    Log.d("ADVTECH", allDataList.size.toString())

                },
                Response.ErrorListener {
                    // typically this is a connection error
                    Log.d("ADVTECH", it.toString())
                }) {
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
    fun getDayData(context: Context) {
        viewModelScope.launch {
            val currentHour = SimpleDateFormat("HH", Locale.getDefault()).format(Date()).toInt()
            currentTime = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            //periaatteessa toimii kun vaihtaa 2023-03-21 kohdan currenttimeen
            //mutta backendillä oli itsellään ongelmia hakea viime aikaista dataa byday
            val JSON_URL = "http://spotprices.energyecs.frostbit.fi/api/v1/prices/byday/2023-03-21T00:00:00Z"

            val gson = GsonBuilder().setPrettyPrinting().create()
            // Request a string response from the provided URL.
            val stringRequest: StringRequest = object : StringRequest(
                Request.Method.GET, JSON_URL,
                Response.Listener { response ->
                    var result : List<ElectricityPrice> = gson.fromJson(response, Array<ElectricityPrice>::class.java).toList()



                    for (item in result)
                    {
                        val time = item.Time?.substring(12,13)
                        dayDataList.add(item.value!!)
                        hoursList.add(time!!.toDouble())
                        dayClassList.add(item)
                    }
                    // Store the list of ElectricityPrice objects in allDataList

                    Log.d("ADVTECH", dayDataList.toString())

                },
                Response.ErrorListener {
                    // typically this is a connection error
                    Log.d("ADVTECH", it.toString())
                }) {
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

    fun getDataFromInflux(context: Context){

        val influxDBClient = InfluxDBClientKotlinFactory
            .create("http://localhost:8086", "wRvSgV9igGmzZnHpB-pJ-l-oZQ2LtRwSoZgeazPmWTKLaP5RJhEYVhGgoh5bYVbPl8H0HSrV41KWBj94rztSkw==".toCharArray(), org, bucket)
        // hard coded ajat toistaiseksi
        val fluxQuery = ("from(bucket: \"electricity_prices\")\n" +
                "  |> range(start: 2023-04-20T04:00:00Z, stop: 2023-04-21T04:00:00Z)\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"my_measurement\")")


        val results = influxDBClient.getQueryKotlinApi().queryRaw(fluxQuery, org)


        Log.d("InfluxDB", results.toString())

        influxDBClient.close()
    }

    fun getThisHourData(context: Context) {
        viewModelScope.launch {
            currentTime = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val currentHour = SimpleDateFormat("HH", Locale.getDefault()).format(Date()).toInt()

            val JSON_URL = "http://spotprices.energyecs.frostbit.fi/api/v1/prices/byday/2023-03-21T${currentHour}:00:00Z"

            val gson = GsonBuilder().setPrettyPrinting().create()
            // Request a string response from the provided URL.
            val stringRequest: StringRequest = object : StringRequest(
                Request.Method.GET, JSON_URL,
                Response.Listener { response ->
                    var result: List<ElectricityPrice> = gson.fromJson(response, Array<ElectricityPrice>::class.java).toList()

                    val formatter = DecimalFormat("0.0")
                    val thisHourItem = result[currentHour]
                    val formatted = formatter.format(thisHourItem.value)
                    _currentHourPrice = "$formatted c/kwh"
                    Log.d("ThisHour", thisHourItem.value.toString())



                },
                Response.ErrorListener {
                    // typically this is a connection error
                    Log.d("ADVTECH", it.toString())
                }) {
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

    fun testDataFromInflux() {

        //TODO: ongelmana on authorization, influxd serveri vaatii edelleen tokenia pyöriessään
        //read only token omaan influxdb cloudiin, data on hieman huonossa muodossa vielä missä price on yhdessä pötkössä (esim. 2.92 2014-12-12T00:00:00Z)
        // ja timestamppina toimii timenow (ei voi tallentaa energianhintojen aikoja timestampiksi sillä cloud tallentaa vain 30 päivän ajan dataa)
        runBlocking {
            val token = "IKVYeQawX7_afsKYXDD6lBJmb85LSjCMdrLGAzdHdAwogI6VPYMN_CIinx7G39aAGkxRU6xKaSt19TZZtwWuWw==".toCharArray()
            /*
            val resource = PermissionResource()
            resource.org("Easy Energy")
            resource.type(PermissionResource.TYPE_BUCKETS)
            val read = Permission()
            read.resource(resource)
            read.action(Permission.ActionEnum.READ)
             */
            val influxDBClient = InfluxDBClientKotlinFactory
                .create(
                    "https://us-east-1-1.aws.cloud2.influxdata.com", token
                )

            // hard coded ajat toistaiseksi
            val fluxQuery = ("from(bucket: \"energy_prices\")\n" +
                    "  |> range(-10)")


            val list = mutableListOf<String>()
            val result = influxDBClient.getQueryKotlinApi().query(fluxQuery, "easyenergy")

            Log.d("InfluxDB", result.toString())



            influxDBClient.close()
        }
    }

}