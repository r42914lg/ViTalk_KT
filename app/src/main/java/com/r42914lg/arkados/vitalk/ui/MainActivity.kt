package com.r42914lg.arkados.vitalk.ui

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.navigateUp
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.r42914lg.arkados.vitalk.R
import com.r42914lg.arkados.vitalk.ViTalkApp
import com.r42914lg.arkados.vitalk.ViTalkConstants
import com.r42914lg.arkados.vitalk.controller.MainController
import com.r42914lg.arkados.vitalk.databinding.ActivityMainBinding
import com.r42914lg.arkados.vitalk.graph.ActivityComponent
import com.r42914lg.arkados.vitalk.graph.DaggerActivityComponent
import com.r42914lg.arkados.vitalk.graph.MyViewModelFactory
import com.r42914lg.arkados.vitalk.model.ViTalkVM
import com.r42914lg.arkados.vitalk.utils.NetworkTracker
import com.r42914lg.arkados.vitalk.utils.PermissionsHelper
import javax.inject.Inject


class MainActivity : AppCompatActivity(), ICoreFrame {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var progressOverlay: View
    private lateinit var checkableMenuItem: MenuItem
    private lateinit var signinMenuItem: MenuItem
    private lateinit var viTalkVM: ViTalkVM
    private lateinit var controller: MainController
    private lateinit var activityComponent: ActivityComponent
    private var showFavoritesFlag = false
    private var favoritesWasNull = false
    private var menuItemsReady = false

    @Inject
    lateinit var myViewModelFactory: MyViewModelFactory

    @Inject
    lateinit var permissionsHelper: PermissionsHelper

    @Inject
    lateinit var networkTracker: NetworkTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityComponent = DaggerActivityComponent.factory()
            .create((application as ViTalkApp).appComponent, this)

        activityComponent.inject(this)

        viTalkVM = ViewModelProvider(this, myViewModelFactory)[ViTalkVM::class.java]
        controller = MainController(this, viTalkVM)
        controller.initMainActivity(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navController = findNavController(this, R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration.Builder(navController.graph).build()
        setupActionBarWithNavController(this, navController, appBarConfiguration)

        progressOverlay = findViewById(R.id.progress_overlay)

        binding.fab.setOnClickListener { navController.navigate(R.id.action_FirstFragment_to_ThirdFragment) }

        initStrictMode()
    }

    fun getActivityComponent(): ActivityComponent {
        return activityComponent
    }

    override fun onResume() {
        super.onResume()
        controller.handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        controller.handleIntent(intent)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        checkableMenuItem = menu.findItem(R.id.favorite)
        signinMenuItem = menu.findItem(R.id.sign_in)

        if (favoritesWasNull) {
            showFavoriteIcon(showFavoritesFlag)
        }
        menuItemsReady = true

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        renderMenuItems()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.favorite) {
            item.isChecked = !item.isChecked
            item.setIcon(if (item.isChecked) R.drawable.ic_baseline_favorite_24 else R.drawable.ic_baseline_favorite_border_24)
            viTalkVM.setFavoritesChecked(item.isChecked)
        }
        if (item.itemId == R.id.sign_in) {
            controller.doGoogleSignIn()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun startProgressOverlay() {
        AnimatorHelper.animateView(progressOverlay, View.VISIBLE, 0.6f, 200)
    }

    override fun stopProgressOverlay() {
        AnimatorHelper.animateView(progressOverlay, View.GONE, 0f, 200)
    }

    override fun showFavoriteIcon(showIfTrue: Boolean) {
        showFavoritesFlag = showIfTrue
        if (!menuItemsReady) {
            favoritesWasNull = true
            return
        }
        if (!showIfTrue) {
            checkableMenuItem.isVisible = false
        } else {
            checkableMenuItem.isVisible = true
            renderMenuItems()
        }
    }

    override fun renderMenuItems() {
        if (!menuItemsReady)
            return

        val event = viTalkVM.favoritesLiveData.value
        event?.let {
            checkableMenuItem.isEnabled = event.enableFavorites
            checkableMenuItem.isChecked = event.favoritesChecked
            checkableMenuItem.setIcon(if (checkableMenuItem.isChecked) R.drawable.ic_baseline_favorite_24 else R.drawable.ic_baseline_favorite_border_24)
            signinMenuItem.isVisible = controller.noGoogleSignIn()
        }
    }

    override fun showFab(flag: Boolean) {
        binding.fab.visibility = if (flag) View.VISIBLE else View.INVISIBLE
    }

    override fun showTabOneMenuItems(flag: Boolean) {
        if (!menuItemsReady)
            return

        checkableMenuItem.isVisible = flag
        signinMenuItem.isVisible = flag && controller.noGoogleSignIn()
    }

    override fun updateUI(account: GoogleSignInAccount) {
        if (!menuItemsReady)
            return

        signinMenuItem.isVisible
    }

    override fun askRatings() {
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task: Task<ReviewInfo?> ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = reviewInfo?.let { manager.launchReviewFlow(this@MainActivity, it) }
                flow?.addOnCompleteListener {}
            }
        }
    }

    private fun initStrictMode() {
        if (ViTalkConstants.STRICT_MODE) {
            StrictMode.setThreadPolicy(
                ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDialog()
                    .build()
            )
            StrictMode.setVmPolicy(
                VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog() //.penaltyDeath()
                    .build()
            )
        }
    }
}