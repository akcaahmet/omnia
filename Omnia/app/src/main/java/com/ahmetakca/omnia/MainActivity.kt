package com.ahmetakca.omnia

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.ahmetakca.omnia.databinding.ActivityMainBinding
import com.ahmetakca.omnia.util.TokenManager
import com.ahmetakca.omnia.views.CartFragment
import com.ahmetakca.omnia.views.FavoriteFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var tokenManager: TokenManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenManager = TokenManager(this)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        val token = tokenManager.getAccessToken()
        val rememberMe = tokenManager.getRememberMe()
        if (token != null && rememberMe) {
            navController.navigate(R.id.action_loginFragment_to_homeFragment)
        } else {
            tokenManager.clearTokens()
        }
        binding.bottomNavigation.setupWithNavController(navController)
        binding.fabFavorites.setOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.action_homeFragment_to_favoriteFragment)
                }

                R.id.cartFragment -> {
                    navController.navigate(R.id.action_cartFragment_to_favoriteFragment)
                }

                R.id.favoriteFragment -> {
                    navController.popBackStack()
                }
            }
        }
        binding.clearScreenButton.setOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.cartFragment -> {
                    val cartFragment =
                        navHostFragment.childFragmentManager.fragments
                            .firstOrNull { it is CartFragment } as? CartFragment
                    cartFragment?.clearCart()
                }

                R.id.favoriteFragment -> {
                    val favoriteFragment =
                        navHostFragment.childFragmentManager.fragments
                            .firstOrNull { it is FavoriteFragment } as? FavoriteFragment
                    favoriteFragment?.clearFavorites()
                }
            }
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.fabFavorites.visibility = View.VISIBLE
                    binding.bottomNavigation.menu.findItem(R.id.homeFragment).isChecked = true
                    binding.toolbarTitle.visibility = View.GONE
                    binding.fabFavorites.setImageResource(R.drawable.favorite_24px)
                }

                R.id.cartFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.fabFavorites.visibility = View.GONE
                    binding.bottomNavigation.menu.findItem(R.id.cartFragment).isChecked = true
                    binding.toolbarTitle.visibility = View.VISIBLE
                    binding.fabFavorites.setImageResource(R.drawable.favorite_24px)
                    binding.pageTitleTextView.text = "Sepetim"
                }

                R.id.favoriteFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                    binding.fabFavorites.visibility = View.VISIBLE
                    binding.fabFavorites.setImageResource(R.drawable.home_24px)
                    binding.toolbarTitle.visibility = View.VISIBLE
                    binding.pageTitleTextView.text = "Favoriler"
                }

                R.id.historyFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.fabFavorites.visibility = View.GONE
                    binding.bottomNavigation.menu.findItem(R.id.historyFragment).isChecked = true
                    binding.toolbarTitle.visibility = View.GONE
                }

                else -> {
                    binding.bottomNavigation.visibility = View.GONE
                    binding.fabFavorites.visibility = View.GONE
                    binding.toolbarTitle.visibility = View.GONE
                }
            }
        }
    }

    fun showClearButtonNavigation(show: Boolean) {
        binding.clearScreenButton.visibility = if (show) View.VISIBLE else View.GONE
    }
}