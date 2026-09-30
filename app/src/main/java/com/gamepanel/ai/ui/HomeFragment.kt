package com.gamepanel.ai.ui

import android.content.Intent
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
import com.gamepanel.ai.data.ProfileRepository
import com.gamepanel.ai.databinding.FragmentHomeBinding
import com.gamepanel.ai.service.CrosshairOverlayService

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ProfileRepository

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
        setupButtons()
        updatePreviewAndState()
    }

    override fun onResume() {
        super.onResume()
        updatePreviewAndState()
    }

    private fun setupGameSelector() {
        val games = ProfileRepository.SUPPORTED_GAMES
        val gameNames = games.map { it.name }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, gameNames).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerGameSelector.adapter = adapter

        val currentGameId = repository.getActiveGameId()
        val currentIdx = games.indexOfFirst { it.id == currentGameId }.coerceAtLeast(0)
        binding.spinnerGameSelector.setSelection(currentIdx)

        binding.spinnerGameSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGame = games[position]
                if (selectedGame.id != repository.getActiveGameId()) {
                    repository.setActiveGameId(selectedGame.id)
                    updatePreviewAndState()
                    if (repository.isOverlayActive()) {
                        startOverlayService()
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupButtons() {
        val mainActivity = activity as? MainActivity

        binding.btnStartOverlay.setOnClickListener {
            if (checkOverlayPermission()) {
                startOverlayService()
                updatePreviewAndState()
            } else {
                requestOverlayPermission()
            }
        }

        binding.btnStopOverlay.setOnClickListener {
            stopOverlayService()
            updatePreviewAndState()
        }

        binding.btnShowQuickPanel.setOnClickListener {
            if (checkOverlayPermission()) {
                val intent = Intent(requireContext(), CrosshairOverlayService::class.java).apply {
                    action = CrosshairOverlayService.ACTION_SHOW_PANEL
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    requireContext().startForegroundService(intent)
                } else {
                    requireContext().startService(intent)
                }
            } else {
                requestOverlayPermission()
            }
        }

        binding.navGallery.setOnClickListener { mainActivity?.navigateTo(MainActivity.NAV_GALLERY) }
        binding.navCustomizer.setOnClickListener { mainActivity?.navigateTo(MainActivity.NAV_CUSTOMIZER) }
        binding.navPositionEditor.setOnClickListener { mainActivity?.navigateTo(MainActivity.NAV_POSITION_EDITOR) }
        binding.navProfiles.setOnClickListener { mainActivity?.navigateTo(MainActivity.NAV_PROFILES) }
        binding.navIntelligence.setOnClickListener { mainActivity?.navigateTo(MainActivity.NAV_INTELLIGENCE) }
        binding.navCalibration.setOnClickListener { mainActivity?.navigateTo(MainActivity.NAV_CALIBRATION) }
        binding.navPerformance.setOnClickListener { mainActivity?.navigateTo(MainActivity.NAV_PERFORMANCE) }
        binding.navSettings.setOnClickListener { mainActivity?.navigateTo(MainActivity.NAV_SETTINGS) }
    }

    private fun updatePreviewAndState() {
        val activeGameId = repository.getActiveGameId()
        val activeProfile = repository.getActiveProfileForGame(activeGameId)

        binding.tvGameProfileInfo.text = "Profile: ${activeProfile.name}"

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
        if (isRunning) {
            binding.tvOverlayStateBadge.text = "OVERLAY ACTIVE"
            binding.tvOverlayStateBadge.setTextColor(resources.getColor(R.color.accent_green, null))
        } else {
            binding.tvOverlayStateBadge.text = "OFFLINE"
            binding.tvOverlayStateBadge.setTextColor(resources.getColor(R.color.text_muted, null))
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
