package edu.uoc.pac3.twitch.streams

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.uoc.pac3.LaunchActivity
import edu.uoc.pac3.R
import edu.uoc.pac3.data.SessionManager
import edu.uoc.pac3.data.TwitchApiService
import edu.uoc.pac3.data.network.Network
import edu.uoc.pac3.data.oauth.UnauthorizedException
import edu.uoc.pac3.twitch.profile.ProfileActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class StreamsActivity : AppCompatActivity() {

    private val TAG = "StreamsActivity"
    private lateinit var twitchService: TwitchApiService
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: StreamsListAdapter

    private var cursor: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streams)
        // Init RecyclerView
        initRecyclerView()
        // Init SessionManager
        sessionManager = SessionManager(this)
        // Init TwitchService
        twitchService = TwitchApiService(Network.createHttpClient(this))

        // Get Streams
        lifecycleScope.launch {
            try {
                getStreams()
            } catch (exception: UnauthorizedException) {
                try {
                    //Refresh Access Tokens
                    refreshAccessToken()
                    // Try to get Streams again
                    getStreams()
                } catch (exception: UnauthorizedException) {
                    // Tokens are invalid
                    // Delete current tokens
                    withContext(Dispatchers.IO) {
                        sessionManager.clearAccessToken()
                        sessionManager.clearRefreshToken()
                    }
                    // Return to LaunchActivity
                    startActivity(Intent(
                            this@StreamsActivity,
                            LaunchActivity::class.java
                    ))
                }
            }
        }
    }

    // Create options menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.user_menu, menu)
        return true
    }

    // Menu item selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Load user profile activity
            R.id.user_profile_menu_item -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        // Set Layout Manager
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        // Init Adapter
        adapter = StreamsListAdapter()
        recyclerView.adapter = adapter

        // Add Scroll Listener, it detects when user reaches the bottom of the RecyclerView
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // True when user reaches end of the RecyclerView
                if (!recyclerView.canScrollVertically(1)
                        && newState == RecyclerView.SCROLL_STATE_IDLE) {

                    // Gets new Streams
                    lifecycleScope.launch {
                        getStreams()
                    }
                }
            }
        })
    }

    // Retrieves Streams
    private suspend fun getStreams() {
        // Gets Streams from Twitch
        val streams = withContext(Dispatchers.IO) { twitchService.getStreams(cursor) }
        // Add streams to RecyclerView
        streams?.data?.let { adapter.addStreams(it) }
        // Save cursor for pagination
        cursor = streams?.pagination?.cursor
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