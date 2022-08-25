package com.r42914lg.arkados.vitalk.ui

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

interface ICoreFrame {
    fun startProgressOverlay()
    fun stopProgressOverlay()
    fun showFavoriteIcon(showIfTrue: Boolean)
    fun renderMenuItems()
    fun showFab(flag: Boolean)
    fun showTabOneMenuItems(flag: Boolean)
    fun updateUI(account: GoogleSignInAccount)
    fun askRatings()
}