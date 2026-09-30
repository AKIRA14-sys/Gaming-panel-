package com.gamepanel.ai.ui

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.gamepanel.ai.R
import com.gamepanel.ai.data.GameProfile
import com.gamepanel.ai.data.ProfileRepository
import com.gamepanel.ai.databinding.FragmentCustomizerBinding
import com.gamepanel.ai.service.CrosshairOverlayService

class CustomizerFragment : Fragment() {

    private var _binding: FragmentCustomizerBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ProfileRepository
    private lateinit var currentProfile: GameProfile

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomizerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ProfileRepository(requireContext())

        val activeGameId = repository.getActiveGameId()
        currentProfile = repository.getActiveProfileForGame(activeGameId)

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        setupOpacityControls()
        setupColorPresets()
        setupSliders()
        setupCenterDot()
        setupLock()
        updatePreview()

        binding.btnSaveCustomizer.setOnClickListener {
            repository.saveProfile(currentProfile)
            notifyOverlayService()
            parentFragmentManager.popBackStack()
        }
    }

    private fun updatePreview() {
        binding.custCrosshairPreview.setCrosshairAsset(currentProfile.crosshairAsset)
        binding.custCrosshairPreview.crosshairColor = currentProfile.color
        binding.custCrosshairPreview.crosshairSizeDp = currentProfile.sizeDp
        binding.custCrosshairPreview.crosshairOpacity = currentProfile.opacity
        binding.custCrosshairPreview.crosshairThickness = currentProfile.thickness
        binding.custCrosshairPreview.crosshairRotation = currentProfile.rotation
        binding.custCrosshairPreview.centerDotEnabled = currentProfile.centerDotEnabled
        binding.custCrosshairPreview.centerDotSizeDp = currentProfile.centerDotSizeDp
        binding.custCrosshairPreview.centerDotColor = currentProfile.centerDotColor
        binding.custCrosshairPreview.centerDotOpacity = currentProfile.centerDotOpacity
    }

    private fun notifyOverlayService() {
        if (repository.isOverlayActive()) {
            val intent = Intent(requireContext(), CrosshairOverlayService::class.java).apply {
                action = CrosshairOverlayService.ACTION_UPDATE_PROFILE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(intent)
            } else {
                requireContext().startService(intent)
            }
        }
    }

    private fun setupOpacityControls() {
        binding.sbOpacity.progress = (currentProfile.opacity * 100).toInt()
        binding.sbOpacity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentProfile.opacity = progress / 100f
                    updatePreview()
                    notifyOverlayService()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnOpVeryLight.setOnClickListener { setOpacityPreset(0.20f) }
        binding.btnOpLight.setOnClickListener { setOpacityPreset(0.35f) }
        binding.btnOpMedium.setOnClickListener { setOpacityPreset(0.55f) }
        binding.btnOpBold.setOnClickListener { setOpacityPreset(0.75f) }
        binding.btnOpVeryBold.setOnClickListener { setOpacityPreset(1.00f) }
    }

    private fun setOpacityPreset(value: Float) {
        currentProfile.opacity = value
        binding.sbOpacity.progress = (value * 100).toInt()
        updatePreview()
        notifyOverlayService()
    }

    private fun setupColorPresets() {
        val colorPairs = listOf(
            "White" to Color.WHITE,
            "Black" to Color.BLACK,
            "Red" to Color.RED,
            "Green" to Color.GREEN,
            "Blue" to Color.BLUE,
            "Cyan" to Color.CYAN,
            "Yellow" to Color.YELLOW,
            "Purple" to ContextCompat.getColor(requireContext(), R.color.accent_purple),
            "Pink" to Color.parseColor("#FFC0CB"),
            "Orange" to ContextCompat.getColor(requireContext(), R.color.accent_orange)
        )

        binding.llColorPresets.removeAllViews()
        for ((_, colorValue) in colorPairs) {
            val btn = Button(requireContext()).apply {
                layoutParams = ViewGroup.MarginLayoutParams(100, 100).apply {
                    setMargins(8, 0, 8, 0)
                }
                setBackgroundColor(colorValue)
                setOnClickListener {
                    currentProfile.color = colorValue
                    updatePreview()
                    notifyOverlayService()
                }
            }
            binding.llColorPresets.addView(btn)
        }
    }

    private fun setupSliders() {
        binding.sbSize.progress = currentProfile.sizeDp.toInt()
        binding.sbSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentProfile.sizeDp = progress.coerceAtLeast(12).toFloat()
                    updatePreview()
                    notifyOverlayService()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sbThickness.progress = (currentProfile.thickness * 10).toInt()
        binding.sbThickness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentProfile.thickness = (progress / 10f).coerceAtLeast(0.5f)
                    updatePreview()
                    notifyOverlayService()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sbRotation.progress = currentProfile.rotation.toInt()
        binding.sbRotation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentProfile.rotation = progress.toFloat()
                    updatePreview()
                    notifyOverlayService()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnResetRotation.setOnClickListener {
            currentProfile.rotation = 0f
            binding.sbRotation.progress = 0
            updatePreview()
            notifyOverlayService()
        }
    }

    private fun setupCenterDot() {
        binding.cbCenterDot.isChecked = currentProfile.centerDotEnabled
        binding.cbCenterDot.setOnCheckedChangeListener { _, isChecked ->
            currentProfile.centerDotEnabled = isChecked
            updatePreview()
            notifyOverlayService()
        }

        binding.sbCenterDotSize.progress = currentProfile.centerDotSizeDp.toInt()
        binding.sbCenterDotSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentProfile.centerDotSizeDp = progress.coerceAtLeast(1).toFloat()
                    updatePreview()
                    notifyOverlayService()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupLock() {
        binding.switchLockCrosshair.isChecked = currentProfile.isLocked
        binding.switchLockCrosshair.setOnCheckedChangeListener { _, isChecked ->
            currentProfile.isLocked = isChecked
            repository.saveProfile(currentProfile)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
