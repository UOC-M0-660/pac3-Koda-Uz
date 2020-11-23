package edu.uoc.pac3.twitch.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import edu.uoc.pac3.LaunchActivity
import edu.uoc.pac3.R
import edu.uoc.pac3.data.SessionManager
import edu.uoc.pac3.data.TwitchApiService
import edu.uoc.pac3.data.network.Network
import edu.uoc.pac3.data.oauth.UnauthorizedException
import edu.uoc.pac3.data.user.UserResponse
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {

    private val TAG = "ProfileActivity"

    private lateinit var twitchService: TwitchApiService
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Create Twitch Service
        twitchService = TwitchApiService(Network.createHttpClient(this))

        // Create SessionManager
        sessionManager = SessionManager(this)

        // Get User Data
        try {
            lifecycleScope.launch {
                val userResponse = withContext(Dispatchers.IO) { twitchService.getUser() }
                setupProfileUI(userResponse)
            }
        } catch (exception: UnauthorizedException) {
            // Token Expired
            lifecycleScope.launch {
                try {
                    //Refresh Token
                    refreshAccessToken()
                    //  Get User data again
                    val userResponse = withContext(Dispatchers.IO) { twitchService.getUser() }
                    setupProfileUI(userResponse)
                } catch (exception: UnauthorizedException) {
                // Tokens are invalid
                // Delete current tokens
                withContext(Dispatchers.IO) {
                    sessionManager.clearAccessToken()
                    sessionManager.clearRefreshToken()
                }
                // Return to LaunchActivity
                startActivity(Intent(
                        this@ProfileActivity,
                        LaunchActivity::class.java
                ))
            }

            }
        }


        updateDescriptionButton.setOnClickListener {
            updateUserDescription()
        }

        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun updateUserDescription() {
        lifecycleScope.launch {
            val description = text_input_layout.editText?.text.toString()
            Log.d(TAG, description)

            val userResponse = withContext(Dispatchers.IO) {
                twitchService.updateUserDescription(description)
            }

            setupProfileUI(userResponse)
        }
    }

    private fun setupProfileUI(userResponse: UserResponse?) {
        userResponse?.data?.get(0)?.let{
            userNameTextView.text = it.userName
            viewsText.text = it.viewCount.toString()
            userDescriptionEditText.setText(it.description)
            Glide.with(this@ProfileActivity)
                    .load(it.profileImageUrl)
                    .into(user_profile_image)
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                sessionManager.clearAccessToken()
                sessionManager.clearRefreshToken()
            }
            startActivity(Intent(this@ProfileActivity, LaunchActivity::class.java))
        }
    }

    // Refreshes Access tokens
    private suspend fun refreshAccessToken() {
        withContext(Dispatchers.IO) {
            // Get Refresh token
            val refreshToken = sessionManager.getRefreshToken()
            refreshToken?.let { token ->
                // Get new tokens
                val response = twitchService.refreshTokens(token)
                response?.let {
                    // Save new tokens
                    sessionManager.saveAccessToken(it.accessToken)
                    sessionManager.saveRefreshToken(it.refreshToken ?: "")
                }
            }
        }
    }

}