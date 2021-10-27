package ru.greencall.androidsuite

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.strictmode.WebViewMethodCalledOnWrongThreadViolation
import android.view.Menu
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi


import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.greencall.androidsuite.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    var networkAvailible = false
    lateinit var mWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        var url = getString(R.string.website_url)

        mWebView = findViewById(R.id.webView)
        val webSettings = mWebView.settings
        webSettings.javaScriptEnabled = true
        webSettings.setAppCacheEnabled(false)

        loadWebSite(mWebView, url, applicationContext)

        swipeRefreshLayout.setColorSchemeResources(R.color.colorRed, R.color.colorBlue, R.color.colorGreen)
        swipeRefreshLayout.apply {
            setOnRefreshListener {
                if (mWebView.url != null) url = mWebView.url!!
                loadWebSite(mWebView, url, applicationContext)
            }

            setOnChildScrollUpCallback { parent, child -> mWebView.getScrollY() > 0}
        }

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
    }

    private fun loadWebSite(mWebView: WebView, url: String, context: Context) {

        progressBar.visibility = View.VISIBLE
        networkAvailible = isNetworkAvailible(context)
        mWebView.clearCache(true)
        if (networkAvailible) {
            wvVisible(mWebView)
            mWebView.webViewClient = MyWebViewClient()
            mWebView.loadUrl(url)
        } else {
            wvGone(mWebView)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    private fun wvVisible(mWebView: WebView) {
        mWebView.visibility = View.VISIBLE
        tvCheckConnection.visibility = View.GONE
    }

    private fun wvGone(mWebView: WebView) {
        mWebView.visibility = View.GONE
        tvCheckConnection.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    @Suppress("DEPRECATION")
    private fun isNetworkAvailible(context: Context) : Boolean {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return if (Build.VERSION.SDK_INT > 22) {
                val an = cm.activeNetwork ?: return false
                val capabilities = cm.getNetworkCapabilities(an) ?: return false
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
                val a = cm.activeNetworkInfo ?: return false
                a.isConnected && (a.type == ConnectivityManager.TYPE_WIFI || a.type == ConnectivityManager.TYPE_MOBILE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }


    private fun onLoadComplete() {
        swipeRefreshLayout.isRefreshing = false
        progressBar.visibility = View.GONE
    }

    private inner class MyWebViewClient : WebViewClient() {

        @RequiresApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {

            val url = request?.url.toString()
            return urlOverride(url)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
            return urlOverride(url)
        }

        private fun urlOverride(url: String): Boolean {
            progressBar.visibility = View.VISIBLE
            networkAvailible = isNetworkAvailible(applicationContext)

            if (networkAvailible) {
                if (Uri.parse(url).host == getString(R.string.website_domain)) return false
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
                onLoadComplete()
                return true
            } else {
                wvGone(webView)
                return false
            }
        }

        @Suppress("DEPRECATION")
        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
            super.onReceivedError(view, errorCode, description, failingUrl)
            if (errorCode == 0) {
                view?.visibility = View.GONE
                tvCheckConnection.visibility = View.VISIBLE
                onLoadComplete()
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            onReceivedError(view, error!!.errorCode, error.description.toString(), request!!.url.toString())
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            onLoadComplete()
        }

    }


}