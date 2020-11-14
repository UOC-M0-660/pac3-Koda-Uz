package edu.uoc.pac3.data.oauth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by alex on 07/09/2020.
 */
@Serializable
data class OAuthTokensResponse(
    @SerialName(value = "access_token") val accessToken: String,
    @SerialName(value = "refresh_token") val refreshToken: String? = null,
)
