package edu.uoc.pac3.twitch.streams

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.uoc.pac3.R
import edu.uoc.pac3.data.SessionManager
import edu.uoc.pac3.data.TwitchApiService
import edu.uoc.pac3.data.network.Network
import edu.uoc.pac3.data.oauth.UnauthorizedException
import edu.uoc.pac3.data.streams.StreamsResponse
import edu.uoc.pac3.oauth.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class StreamsActivity : AppCompatActivity() {

    private val TAG = "StreamsActivity"
    private lateinit var twitchService: TwitchApiService
    private lateinit var adapter: StreamsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streams)
        // Init RecyclerView
        initRecyclerView()
        // Get Streams
        twitchService = TwitchApiService(Network.createHttpClient(this))
        lifecycleScope.launch {
            try {
                val streams = withContext(Dispatchers.IO) { twitchService.getStreams() }
                streams?.data?.let { adapter.addStreams(it) }
            } catch (exception: UnauthorizedException) {
                exception.printStackTrace()
                Log.d(TAG, exception.message)

                //Refresh
                refreshAccessToken()

                val streams = withContext(Dispatchers.IO) { twitchService.getStreams() }
                streams?.data?.let { adapter.addStreams(it) }
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