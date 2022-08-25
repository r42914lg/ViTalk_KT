package com.r42914lg.arkados.vitalk.model

class FavoritesEvent(
    var enableFavorites: Boolean = false,
    var favoritesChecked: Boolean = false
) {
    val checkedAndEnabled: Boolean
        get() = enableFavorites && favoritesChecked
}

class TerminateDialogEvent(
    val title: String,
    val text: String
    )

class RetryDialogEvent(
    val title: String,
    val text: String
    )

