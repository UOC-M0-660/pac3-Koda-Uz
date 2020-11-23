package edu.uoc.pac3.oauth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.uoc.pac3.R
import edu.uoc.pac3.data.SessionManager
import edu.uoc.pac3.data.TwitchApiService
import edu.uoc.pac3.data.network.Endpoints
import edu.uoc.pac3.data.network.Network
import edu.uoc.pac3.data.oauth.OAuthConstants
import edu.uoc.pac3.twitch.streams.StreamsActivity
import kotlinx.android.synthetic.main.activity_oauth.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class OAuthActivity : AppCompatActivity() {

    private val TAG = "OAuthActivity"
    private val uniqueState = UUID.randomUUID().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oauth)
        launchOAuthAuthorization()
    }

    fun buildOAuthUri(): Uri {
        // Prepare URL
        return Uri.parse(Endpoints.oauthAuthorizationUrl)
            .buildUpon()
            .appendQueryParameter("client_id", OAuthConstants.clientId)
            .appendQueryParameter("redirect_uri", OAuthConstants.redirectUri)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", "user:read:email user:edit")
            .appendQueryParameter("state", uniqueState)
            .build()
    }

    private fun launchOAuthAuthorization() {
        //  Create URI
        val uri = buildOAuthUri()

        // TODO: Set webView Redirect Listener
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                request?.let {
                    // Check if this url is our OAuth redirect, otherwise ignore it
                    if (request.url.toString().startsWith(OAuthConstants.redirectUri)) {
                        // To prevent CSRF attacks, check that we got the same state value we sent, otherwise ignore it
                        val responseState = request.url.getQueryParameter("state")
                        if (responseState == uniqueState) {
                            // This is our request, obtain the code!
                            request.url.getQueryParameter("code")?.let { code ->
                                // Got it!
                                Log.d("OAuth", "Here is the authorization code! $code")
                                onAuthorizationCodeRetrieved(code)
                            } ?: run {
                                // User cancelled the login flow
                                // TODO: Handle error
                            }
                        }
                    }
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }

        // Load OAuth Uri
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(uri.toString())
    }

    // Call this method after obtaining the authorization code
    // on the WebView to obtain the tokens
    private fun onAuthorizationCodeRetrieved(authorizationCode: String) {

        // Show Loading Indicator
        progressBar.visibility = View.VISIBLE

        // Create Twitch Service
        val twitchService = TwitchApiService(Network.createHttpClient(this))

        val intent = Intent(this, StreamsActivity::class.java)

        // Get Tokens from Twitch
        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) { twitchService.getTokens(authorizationCode) }
            Log.d("OAuth", "Access Token: ${response?.accessToken}. Refresh Token: ${response?.refreshToken}")

            // Save access token and refresh token using the SessionManager class
            response?.let {
                val sessionManager = SessionManager(this@OAuthActivity)
                withContext(Dispatchers.IO) { sessionManager.saveAccessToken(it.accessToken) }
                withContext(Dispatchers.IO) { it.refreshToken?.let { it1 -> sessionManager.saveRefreshToken(it1) } }
            }

            // Start Activity
            startActivity(intent)
        }
    }
}