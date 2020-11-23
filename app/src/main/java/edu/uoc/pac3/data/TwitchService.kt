package edu.uoc.pac3.data

import edu.uoc.pac3.data.network.Endpoints
import edu.uoc.pac3.data.oauth.OAuthConstants
import edu.uoc.pac3.data.oauth.OAuthTokensResponse
import edu.uoc.pac3.data.oauth.UnauthorizedException
import edu.uoc.pac3.data.streams.StreamsResponse
import edu.uoc.pac3.data.user.User
import edu.uoc.pac3.data.user.UserResponse
import io.ktor.client.*
import io.ktor.client.request.*
import java.lang.Exception

/**
 * Created by alex on 24/10/2020.
 */

class TwitchApiService(private val httpClient: HttpClient) {
    private val TAG = "TwitchApiService"

    /// Gets Access and Refresh Tokens on Twitch
    suspend fun getTokens(authorizationCode: String): OAuthTokensResponse? {
        return try {
            httpClient.post<OAuthTokensResponse>(Endpoints.oauthTokenUrl) {
                parameter("client_secret", OAuthConstants.clientSecret)
                parameter("code", authorizationCode)
                parameter("grant_type", "authorization_code")
                parameter("redirect_uri", OAuthConstants.redirectUri)
            }
        } catch (exception: Exception) {
            null
        }
    }

    // Refresh Access and Refresh tokens
    suspend fun refreshTokens(refreshToken: String):OAuthTokensResponse? {
        return httpClient.post<OAuthTokensResponse>(Endpoints.oauthTokenUrl) {
            parameter("client_secret", OAuthConstants.clientSecret)
            parameter("refresh_token", refreshToken)
            parameter("grant_type", "refresh_token")
        }
    }

    /// Gets Streams on Twitch
    @Throws(UnauthorizedException::class)
    suspend fun getStreams(cursor: String? = null): StreamsResponse? {
        // Get Streams from Twitch
        return try {
            if (cursor != null) {
                // Uses cursor for pagination
                httpClient.get<StreamsResponse>(Endpoints.twitchStreamsUrl) {
                    parameter("first", 20)
                    parameter("after", cursor)
                }
            } else {
                httpClient.get<StreamsResponse>(Endpoints.twitchStreamsUrl)
            }
        } catch (exception: Exception) {
            throw UnauthorizedException
        }
    }

    /// Gets Current Authorized User on Twitch
    @Throws(UnauthorizedException::class)
    suspend fun getUser(): User? {
        try {
            // HTTP Query return list of user
            val userResponse = httpClient.get<UserResponse>(Endpoints.twitchUserUrl)
            // Logged user will be the first one
            return userResponse.data?.get(0)
        } catch (exception: Exception) {
            throw UnauthorizedException
        }
    }

    /// Gets Current Authorized User on Twitch
    @Throws(UnauthorizedException::class)
    suspend fun updateUserDescription(description: String): User? {
        try {
            // HTTP Query return list of user
            val userResponse = httpClient.put<UserResponse>(Endpoints.twitchUserUrl) {
                parameter("description", description)
            }
            // Logged user will be the first one
            return userResponse.data?.get(0)
        } catch (exception: Exception) {
            throw UnauthorizedException
        }
    }
}