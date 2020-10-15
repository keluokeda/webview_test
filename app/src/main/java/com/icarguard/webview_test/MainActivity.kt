package com.icarguard.webview_test

import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.lzyzsd.jsbridge.BridgeWebViewClient
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val defaultUrl = "https://h5.icarguard.com/test/testAll"


        web_view.settings.apply {

            javaScriptEnabled = true
            allowFileAccess = true
            databaseEnabled = true
            setAppCacheEnabled(true)
            setGeolocationEnabled(true)
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true

//            userAgentString = web_view.settings.userAgentString.toString() + " Ichebao/V2"
        }



        web_view.webViewClient = object : BridgeWebViewClient(web_view) {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

                if (url.startsWith("https://wx.tenpay.com")) {
                    //H5微信支付需要加上这个头部 不然会报 商家参数格式有误，请联系商家解决 的错误
                    val header = mapOf("Referer" to "https://h5.icarguard.com")
                    web_view.loadUrl(url, header)
                    return true
                } else if (url.startsWith("weixin://")) {
                    val intent = Intent()
                    intent.action = Intent.ACTION_VIEW
                    intent.data = Uri.parse(url)
                    try {
                        //没有安装微信会出异常
                        startActivity(intent)
                    } catch (e: Exception) {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("提示")
                            .setMessage("未检测到微信客户端，请安装后重试。")
                            .setPositiveButton("知道了", null).show()
                    }
                    return true
                }

//                view.loadUrl(url)

                return super.shouldOverrideUrlLoading(view, url)
            }
        }

        //获取经纬度
        web_view.registerHandler("getLocationFromAppBridge") { data, callback ->

            val json = JSONObject()

            json.put("longitude", .0)
            json.put("latitude", .0)
            json.put("status", true)
            callback.onCallBack(json.toString())
        }

        //导航
        web_view.registerHandler("openNavigatorFromAppBridge") { data, callback ->
            val json = JSONObject(data)
            val name = json.optString("gasStationName")
            val latitude = json.optDouble("latitude")
            val longitude = json.optDouble("longitude")

            val result = JSONObject()
            result.put("status", true)
            callback.onCallBack(result.toString())


        }



        web_view.loadUrl(defaultUrl)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(1, 1, 1, "手动输入地址")

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) {
            val editText = EditText(this)

            AlertDialog.Builder(this)
                .setTitle("请输入地址")
                .setView(editText)
                .setPositiveButton("前往") { _, _ ->
                    val url = editText.text.toString()

                    if (url.isNotEmpty()) {
                        web_view.loadUrl(url)
                    }

                }.show()

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (web_view.canGoBack()) {
            web_view.goBack()
            return
        }
        super.onBackPressed()
    }
}