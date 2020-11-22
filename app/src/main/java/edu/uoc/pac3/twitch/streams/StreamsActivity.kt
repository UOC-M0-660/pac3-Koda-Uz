package edu.uoc.pac3.twitch.streams

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.uoc.pac3.R
import edu.uoc.pac3.data.SessionManager
import edu.uoc.pac3.data.TwitchApiService
import edu.uoc.pac3.data.network.Network
import edu.uoc.pac3.data.oauth.UnauthorizedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class StreamsActivity : AppCompatActivity() {

    private val TAG = "StreamsActivity"
    private lateinit var twitchService: TwitchApiService
    private lateinit var adapter: StreamsListAdapter

    private var cursor: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streams)
        // Init RecyclerView
        initRecyclerView()
        // Get Streams
        twitchService = TwitchApiService(Network.createHttpClient(this))
        lifecycleScope.launch {
            try {
                getStreams()
            } catch (exception: UnauthorizedException) {
                //Refresh Access Tokens
                refreshAccessToken()

                getStreams()
            }
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

    private suspend fun getStreams() {
        val streams = withContext(Dispatchers.IO) { twitchService.getStreams(cursor) }
        streams?.data?.let { adapter.addStreams(it) }
        cursor = streams?.pagination?.cursor
    }

    private suspend fun refreshAccessToken() {
        val sessionManager = SessionManager(this@StreamsActivity)
        val refreshToken = withContext(Dispatchers.IO) {
            sessionManager.getRefreshToken()
        }
        val response = withContext(Dispatchers.IO) {
            twitchService.refreshTokens(refreshToken!!)
        }
        withContext(Dispatchers.IO) {
            sessionManager.saveAccessToken(response?.accessToken!!)
            sessionManager.saveRefreshToken(response?.refreshToken!!)
        }
    }

}