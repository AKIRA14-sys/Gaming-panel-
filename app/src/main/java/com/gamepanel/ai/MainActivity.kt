package com.gamepanel.ai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        const val NAV_PERFORMANCE = "performance"
        const val NAV_SETTINGS = "settings"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ProfileRepository(this)

        if (savedInstanceState == null) {
            navigateTo(NAV_HOME)
        }
    }

    fun navigateTo(destination: String, bundle: Bundle? = null) {
        val fragment: Fragment = when (destination) {
            NAV_HOME -> HomeFragment()
            NAV_GALLERY -> GalleryFragment()
            NAV_CUSTOMIZER -> CustomizerFragment()
            NAV_POSITION_EDITOR -> PositionEditorFragment()
            NAV_PROFILES -> SettingsFragment()
            NAV_INTELLIGENCE -> IntelligenceFragment()
            NAV_CALIBRATION -> CalibrationFragment()
            NAV_PERFORMANCE -> PerformanceFragment()
            NAV_SETTINGS -> SettingsFragment()
            else -> HomeFragment()
        }

        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(destination)
            .commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
