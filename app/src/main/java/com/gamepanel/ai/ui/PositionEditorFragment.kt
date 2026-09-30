package com.gamepanel.ai.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.gamepanel.ai.data.GameProfile
import com.gamepanel.ai.data.ProfileRepository
import com.gamepanel.ai.databinding.FragmentPositionEditorBinding
import com.gamepanel.ai.service.CrosshairOverlayService

class PositionEditorFragment : Fragment() {

    private var _binding: FragmentPositionEditorBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ProfileRepository
    private lateinit var currentProfile: GameProfile

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPositionEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ProfileRepository(requireContext())

        val activeGameId = repository.getActiveGameId()
        currentProfile = repository.getActiveProfileForGame(activeGameId)

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        setupTouchDrag()
        setupSeekBars()
        updateUI()

        binding.btnCenterPos.setOnClickListener {
            currentProfile.offsetX = 0
            currentProfile.offsetY = 0
            updateUI()
            notifyOverlayService()
        }

        binding.btnResetPos.setOnClickListener {
            currentProfile.offsetX = 0
            currentProfile.offsetY = 0
            updateUI()
            notifyOverlayService()
        }

        binding.btnSavePosition.setOnClickListener {
            repository.saveProfile(currentProfile)
            notifyOverlayService()
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupTouchDrag() {
        var startX = 0f
        var startY = 0f
        var startOffsetX = 0
        var startOffsetY = 0

        binding.phonePreviewArea.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX
                    startY = event.rawY
                    startOffsetX = currentProfile.offsetX
                    startOffsetY = currentProfile.offsetY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - startX).toInt()
                    val dy = (event.rawY - startY).toInt()
                    currentProfile.offsetX = (startOffsetX + dx).coerceIn(-200, 200)
                    currentProfile.offsetY = (startOffsetY + dy).coerceIn(-200, 200)
                    updateUI()
                    notifyOverlayService()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupSeekBars() {
        binding.sbPosX.progress = currentProfile.offsetX + 200
        binding.sbPosX.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentProfile.offsetX = progress - 200
                    updateUI()
                    notifyOverlayService()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sbPosY.progress = currentProfile.offsetY + 200
        binding.sbPosY.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentProfile.offsetY = progress - 200
                    updateUI()
                    notifyOverlayService()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateUI() {
        binding.tvPosCoordinates.text = "X: ${currentProfile.offsetX}, Y: ${currentProfile.offsetY}"
        binding.sbPosX.progress = currentProfile.offsetX + 200
        binding.sbPosY.progress = currentProfile.offsetY + 200

        binding.posCrosshairView.setCrosshairAsset(currentProfile.crosshairAsset)
        binding.posCrosshairView.crosshairColor = currentProfile.color
        binding.posCrosshairView.crosshairSizeDp = currentProfile.sizeDp
        binding.posCrosshairView.crosshairOpacity = currentProfile.opacity
        binding.posCrosshairView.crosshairThickness = currentProfile.thickness
        binding.posCrosshairView.crosshairRotation = currentProfile.rotation
        binding.posCrosshairView.centerDotEnabled = currentProfile.centerDotEnabled
        binding.posCrosshairView.centerDotSizeDp = currentProfile.centerDotSizeDp
        binding.posCrosshairView.centerDotColor = currentProfile.centerDotColor
        binding.posCrosshairView.centerDotOpacity = currentProfile.centerDotOpacity

        binding.posCrosshairView.translationX = currentProfile.offsetX.toFloat()
        binding.posCrosshairView.translationY = currentProfile.offsetY.toFloat()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
