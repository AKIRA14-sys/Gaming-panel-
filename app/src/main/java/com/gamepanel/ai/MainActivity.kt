package com.gamepanel.ai

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.gamepanel.ai.data.ProfileRepository
import com.gamepanel.ai.databinding.ActivityMainBinding
import com.gamepanel.ai.ui.CalibrationFragment
import com.gamepanel.ai.ui.CustomizerFragment
import com.gamepanel.ai.ui.GalleryFragment
import com.gamepanel.ai.ui.HomeFragment
import com.gamepanel.ai.ui.IntelligenceFragment
import com.gamepanel.ai.ui.PerformanceFragment
import com.gamepanel.ai.ui.PositionEditorFragment
import com.gamepanel.ai.ui.ScreenRecorderFragment
import com.gamepanel.ai.ui.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: ProfileRepository

    companion object {
        const val NAV_HOME = "home"
        const val NAV_GALLERY = "gallery"
        const val NAV_CUSTOMIZER = "customizer"
        const val NAV_POSITION_EDITOR = "position_editor"
        const val NAV_PROFILES = "profiles"
        const val NAV_INTELLIGENCE = "intelligence"
        const val NAV_CALIBRATION = "calibration"
        const val NAV_SCREEN_RECORDER = "screen_recorder"
        const val NAV_PERFORMANCE = "performance"
        const val NAV_SETTINGS = "settings"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ProfileRepository(this)

        setupBottomNav()

        if (savedInstanceState == null) {
            navigateTo(NAV_HOME, isRoot = true)
        }
    }

    private fun setupBottomNav() {
        binding.btnTabHome.setOnClickListener { navigateTo(NAV_HOME, isRoot = true) }
        binding.btnTabGallery.setOnClickListener { navigateTo(NAV_GALLERY) }
        binding.btnTabCustomizer.setOnClickListener { navigateTo(NAV_CUSTOMIZER) }
        binding.btnTabTools.setOnClickListener { navigateTo(NAV_INTELLIGENCE) }
        binding.btnTabSettings.setOnClickListener { navigateTo(NAV_SETTINGS) }
    }

    private fun updateNavSelection(activeDestination: String) {
        val cyan = ContextCompat.getColor(this, R.color.accent_cyan)
        val muted = ContextCompat.getColor(this, R.color.text_muted)

        binding.tvTabHome.setTextColor(if (activeDestination == NAV_HOME) cyan else muted)
        binding.tvTabGallery.setTextColor(if (activeDestination == NAV_GALLERY) cyan else muted)
        binding.tvTabCustomizer.setTextColor(if (activeDestination == NAV_CUSTOMIZER) cyan else muted)
        binding.tvTabTools.setTextColor(if (activeDestination == NAV_INTELLIGENCE || activeDestination == NAV_PERFORMANCE) cyan else muted)
        binding.tvTabSettings.setTextColor(if (activeDestination == NAV_SETTINGS || activeDestination == NAV_PROFILES) cyan else muted)
    }

    fun navigateTo(destination: String, bundle: Bundle? = null, isRoot: Boolean = false) {
        val fragment: Fragment = when (destination) {
            NAV_HOME -> HomeFragment()
            NAV_GALLERY -> GalleryFragment()
            NAV_CUSTOMIZER -> CustomizerFragment()
            NAV_POSITION_EDITOR -> PositionEditorFragment()
            NAV_PROFILES -> SettingsFragment()
            NAV_INTELLIGENCE -> IntelligenceFragment()
            NAV_CALIBRATION -> CalibrationFragment()
            NAV_SCREEN_RECORDER -> ScreenRecorderFragment()
            NAV_PERFORMANCE -> PerformanceFragment()
            NAV_SETTINGS -> SettingsFragment()
            else -> HomeFragment()
        }

        fragment.arguments = bundle

        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)

        if (!isRoot) {
            transaction.addToBackStack(destination)
        }

        transaction.commit()
        updateNavSelection(destination)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
