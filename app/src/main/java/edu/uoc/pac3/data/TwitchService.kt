package edu.uoc.pac3.data

import edu.uoc.pac3.data.network.Endpoints
import edu.uoc.pac3.data.oauth.OAuthConstants
import edu.uoc.pac3.data.oauth.OAuthTokensResponse
import edu.uoc.pac3.data.oauth.UnauthorizedException
import edu.uoc.pac3.data.streams.StreamsResponse
import edu.uoc.pac3.data.user.User
import io.ktor.client.*
import io.ktor.client.request.*

/**
 * Created by alex on 24/10/2020.
 */

class TwitchApiService(private val httpClient: HttpClient) {
    private val TAG = "TwitchApiService"

    /// Gets Access and Refresh Tokens on Twitch
    suspend fun getTokens(authorizationCode: String): OAuthTokensResponse? {
        return httpClient.post<OAuthTokensResponse>(Endpoints.oauthTokenUrl) {
            parameter("client_id", OAuthConstants.clientId)
            parameter("client_secret", OAuthConstants.clientSecret)
            parameter("code", authorizationCode)
            parameter("grant_type", "authorization_code")
            parameter("redirect_uri", OAuthConstants.redirectUri)
        }
    }

    /// Gets Streams on Twitch
    @Throws(UnauthorizedException::class)
    suspend fun getStreams(cursor: String? = null): StreamsResponse? {
        TODO("Get Streams from Twitch")
        TODO("Support Pagination")
    }

    /// Gets Current Authorized User on Twitch
    @Throws(UnauthorizedException::class)
    suspend fun getUser(): User? {
        TODO("Get User from Twitch")
    }

    /// Gets Current Authorized User on Twitch
    @Throws(UnauthorizedException::class)
    suspend fun updateUserDescription(description: String): User? {
        TODO("Update User Description on Twitch")
    }
}