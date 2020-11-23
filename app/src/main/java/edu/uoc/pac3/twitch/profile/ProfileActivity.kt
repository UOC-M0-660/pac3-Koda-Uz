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
import edu.uoc.pac3.data.user.UserResponse
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {

    private val TAG = "ProfileActivity"

    private lateinit var twitchService: TwitchApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Create Twitch Service
        twitchService = TwitchApiService(Network.createHttpClient(this))

        // Get User Data
        lifecycleScope.launch {
            val userResponse = withContext(Dispatchers.IO) { twitchService.getUser() }
            setupProfileUI(userResponse)
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
        val sessionManager = SessionManager(this)
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                sessionManager.clearAccessToken()
                sessionManager.clearRefreshToken()
            }
            startActivity(Intent(this@ProfileActivity, LaunchActivity::class.java))
        }
    }

}