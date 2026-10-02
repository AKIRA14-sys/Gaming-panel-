package com.gamepanel.ai.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.gamepanel.ai.MainActivity
import com.gamepanel.ai.R
import com.gamepanel.ai.data.GameItem
import com.gamepanel.ai.data.ProfileRepository
import com.gamepanel.ai.databinding.FragmentHomeBinding
import com.gamepanel.ai.service.CrosshairOverlayService

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ProfileRepository
    private var gamesList: List<GameItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ProfileRepository(requireContext())

        setupGameSelector()
        setupMasterSwitches()
        setupPresets()
        setupButtons()
        updatePreviewAndState()
    }

    override fun onResume() {
        super.onResume()
        updatePreviewAndState()
    }

    private fun setupGameSelector() {
        gamesList = repository.getInstalledAndDefaultGames()
        val gameNames = gamesList.map { it.name }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, gameNames).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerGameSelector.adapter = adapter

        val currentGameId = repository.getActiveGameId()
        val currentIdx = gamesList.indexOfFirst { it.id == currentGameId }.coerceAtLeast(0)
        binding.spinnerGameSelector.setSelection(currentIdx)

        binding.spinnerGameSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position in gamesList.indices) {
                    val selectedGame = gamesList[position]
                    if (selectedGame.id != repository.getActiveGameId()) {
                        repository.setActiveGameId(selectedGame.id)
                        updatePreviewAndState()
                        if (repository.isOverlayActive()) {
                            sendServiceAction(CrosshairOverlayService.ACTION_UPDATE_PROFILE)
                        }
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupMasterSwitches() {
        binding.switchCrosshairMaster.isChecked = repository.isOverlayActive()
        binding.switchPanelMaster.isChecked = false

        binding.switchCrosshairMaster.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (checkOverlayPermission()) {
                    startOverlayService()
                } else {
                    binding.switchCrosshairMaster.isChecked = false
                    requestOverlayPermission()
                }
            } else {
                stopOverlayService()
            }
        }

        binding.switchPanelMaster.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (checkOverlayPermission()) {
                    sendServiceAction(CrosshairOverlayService.ACTION_SHOW_PANEL)
                } else {
                    binding.switchPanelMaster.isChecked = false
                    requestOverlayPermission()
                }
            } else {
                sendServiceAction(CrosshairOverlayService.ACTION_HIDE_PANEL)
            }
        }
    }

    private fun setupPresets() {
        binding.btnPresetDot.setOnClickListener {
            val profile = repository.getActiveProfileForGame(repository.getActiveGameId())
            profile.crosshairAsset = "crosshairs/crosshair_1.png"
            profile.sizeDp = 20f
            profile.opacity = 1.0f
            profile.color = Color.CYAN
            profile.centerDotEnabled = true
            profile.centerDotSizeDp = 6f
            repository.saveProfile(profile)
            updatePreviewAndState()
            sendServiceAction(CrosshairOverlayService.ACTION_UPDATE_PROFILE)
            Toast.makeText(requireContext(), "Applied Precision Dot Sight", Toast.LENGTH_SHORT).show()
        }

        binding.btnPresetCircle.setOnClickListener {
            val profile = repository.getActiveProfileForGame(repository.getActiveGameId())
            profile.crosshairAsset = "crosshairs/crosshair_25.png"
            profile.sizeDp = 35f
            profile.opacity = 0.85f
            profile.color = Color.GREEN
            repository.saveProfile(profile)
            updatePreviewAndState()
            sendServiceAction(CrosshairOverlayService.ACTION_UPDATE_PROFILE)
            Toast.makeText(requireContext(), "Applied Sniper Circle Sight", Toast.LENGTH_SHORT).show()
        }

        binding.btnPresetCross.setOnClickListener {
            val profile = repository.getActiveProfileForGame(repository.getActiveGameId())
            profile.crosshairAsset = "crosshairs/crosshair_50.png"
            profile.sizeDp = 32f
            profile.opacity = 0.90f
            profile.color = Color.YELLOW
            repository.saveProfile(profile)
            updatePreviewAndState()
            sendServiceAction(CrosshairOverlayService.ACTION_UPDATE_PROFILE)
            Toast.makeText(requireContext(), "Applied Tactical Cross Sight", Toast.LENGTH_SHORT).show()
        }

        binding.btnPresetRing.setOnClickListener {
            val profile = repository.getActiveProfileForGame(repository.getActiveGameId())
            profile.crosshairAsset = "crosshairs/crosshair_100.png"
            profile.sizeDp = 42f
            profile.opacity = 0.80f
            profile.color = Color.RED
            repository.saveProfile(profile)
            updatePreviewAndState()
            sendServiceAction(CrosshairOverlayService.ACTION_UPDATE_PROFILE)
            Toast.makeText(requireContext(), "Applied Shotgun Ring Sight", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButtons() {
        val mainActivity = activity as? MainActivity

        binding.navGallery.setOnClickListener { mainActivity?.navigateTo(MainActivity.NAV_GALLERY) }
        binding.navCustomizer.setOnClickListener { mainActivity?.navigateTo(MainActivity.NAV_CUSTOMIZER) }
        binding.navPositionEditor.setOnClickListener { mainActivity?.navigateTo(MainActivity.NAV_POSITION_EDITOR) }
        binding.navProfiles.setOnClickListener { mainActivity?.navigateTo(MainActivity.NAV_PROFILES) }
        binding.navIntelligence.setOnClickListener { mainActivity?.navigateTo(MainActivity.NAV_INTELLIGENCE) }
        binding.navCalibration.setOnClickListener { mainActivity?.navigateTo(MainActivity.NAV_CALIBRATION) }
        binding.navScreenRecorder.setOnClickListener { mainActivity?.navigateTo(MainActivity.NAV_SCREEN_RECORDER) }
        binding.navPerformance.setOnClickListener { mainActivity?.navigateTo(MainActivity.NAV_PERFORMANCE) }
        binding.navSettings.setOnClickListener { mainActivity?.navigateTo(MainActivity.NAV_SETTINGS) }
    }

    private fun updatePreviewAndState() {
        val activeGameId = repository.getActiveGameId()
        val activeProfile = repository.getActiveProfileForGame(activeGameId)

        binding.tvGameProfileInfo.text = "ACTIVE PROFILE: ${activeProfile.name}"

        binding.homeCrosshairPreview.setCrosshairAsset(activeProfile.crosshairAsset)
        binding.homeCrosshairPreview.crosshairColor = activeProfile.color
        binding.homeCrosshairPreview.crosshairSizeDp = activeProfile.sizeDp
        binding.homeCrosshairPreview.crosshairOpacity = activeProfile.opacity
        binding.homeCrosshairPreview.crosshairThickness = activeProfile.thickness
        binding.homeCrosshairPreview.crosshairRotation = activeProfile.rotation
        binding.homeCrosshairPreview.centerDotEnabled = activeProfile.centerDotEnabled
        binding.homeCrosshairPreview.centerDotSizeDp = activeProfile.centerDotSizeDp
        binding.homeCrosshairPreview.centerDotColor = activeProfile.centerDotColor
        binding.homeCrosshairPreview.centerDotOpacity = activeProfile.centerDotOpacity

        val isRunning = repository.isOverlayActive()
        binding.switchCrosshairMaster.isChecked = isRunning
    }

    private fun checkOverlayPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(requireContext())
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(requireContext(), "Please grant 'Display over other apps' permission", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${requireContext().packageName}")
            )
            startActivity(intent)
        }
    }

    private fun startOverlayService() {
        val intent = Intent(requireContext(), CrosshairOverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent)
        } else {
            requireContext().startService(intent)
        }
        repository.setOverlayActive(true)
    }

    private fun stopOverlayService() {
        val intent = Intent(requireContext(), CrosshairOverlayService::class.java).apply {
            action = CrosshairOverlayService.ACTION_STOP
        }
        requireContext().startService(intent)
        repository.setOverlayActive(false)
    }

    private fun sendServiceAction(actionName: String) {
        val intent = Intent(requireContext(), CrosshairOverlayService::class.java).apply {
            action = actionName
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent)
        } else {
            requireContext().startService(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
