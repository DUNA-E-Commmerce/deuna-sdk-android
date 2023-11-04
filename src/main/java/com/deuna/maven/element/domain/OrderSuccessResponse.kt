package com.deuna.maven.element.domain

import org.json.JSONObject

data class ElementSuccessResponse(
    val user: User,
    val metadata: Metadata
) {
    data class Metadata(
        val errorCode: String,
        val errorMessage: String
    )

    data class User(
        val id: String,
        val email: String,
        val firstName: String,
        val lastName: String,
    )

    companion object {
        fun fromJson(json: JSONObject): ElementSuccessResponse {
            val metadata = json.optJSONObject("metadata")
            val userData = json.getJSONObject("user")
            val meta= Metadata(
                metadata.getString("errorCode"),
                metadata.getString("errorMessage")
            )
            val user = User(
                userData.getString("id"),
                userData.getString("first_name"),
                userData.getString("last_name"),
                userData.getString("email"),
            )
            return ElementSuccessResponse(user, meta)
        }
    }
}
