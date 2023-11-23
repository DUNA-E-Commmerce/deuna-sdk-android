package com.deuna.maven.shared

enum class ApiGatewayUrl(val url: String)  {
    DEVELOPMENT("https://api.dev.deuna.io"),
    STAGING("https://api.stg.deuna.io"),
    PRODUCTION("https://api.deuna.io"),
    SANDBOX("https://api.sbx.deuna.io");
}