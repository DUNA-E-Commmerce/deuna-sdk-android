package com.deuna.maven.shared

enum class ElementUrl (var url: String) {
    DEVELOPMENT("https://element.dev.deuna.io"),
    STAGING("https://element.stg.deuna.io"),
    PRODUCTION("https://element.deuna.io"),
    SANDBOX("https://element.sbx.deuna.io");
}