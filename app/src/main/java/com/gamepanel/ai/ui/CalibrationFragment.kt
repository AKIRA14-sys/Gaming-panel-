package com.gamepanel.ai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.gamepanel.ai.data.GameProfile
import com.gamepanel.ai.data.ProfileRepository
import com.gamepanel.ai.databinding.FragmentCalibrationBinding

class CalibrationFragment : Fragment() {

    private var _binding: FragmentCalibrationBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ProfileRepository
    private lateinit var activeProfile: GameProfile

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalibrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ProfileRepository(requireContext())

        val activeGameId = repository.getActiveGameId()
        activeProfile = repository.getActiveProfileForGame(activeGameId)

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        updateCrosshairView()

        binding.btnCalCenter.setOnClickListener {
            activeProfile.offsetX = 0
            activeProfile.offsetY = 0
            repository.saveProfile(activeProfile)
            updateCrosshairView()
            Toast.makeText(requireContext(), "Center Calibration Applied", Toast.LENGTH_SHORT).show()
        }

        binding.btnCalVisual.setOnClickListener {
            Toast.makeText(requireContext(), "Target Grid active for visual alignment", Toast.LENGTH_SHORT).show()
        }

        binding.btnCalReset.setOnClickListener {
            repository.resetProfile(activeProfile)
            updateCrosshairView()
            Toast.makeText(requireContext(), "Calibration Reset", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCrosshairView() {
        binding.calCrosshairView.setCrosshairAsset(activeProfile.crosshairAsset)
        binding.calCrosshairView.crosshairColor = activeProfile.color
        binding.calCrosshairView.crosshairSizeDp = activeProfile.sizeDp
        binding.calCrosshairView.crosshairOpacity = activeProfile.opacity
        binding.calCrosshairView.crosshairThickness = activeProfile.thickness
        binding.calCrosshairView.crosshairRotation = activeProfile.rotation
        binding.calCrosshairView.centerDotEnabled = activeProfile.centerDotEnabled
        binding.calCrosshairView.centerDotSizeDp = activeProfile.centerDotSizeDp
        binding.calCrosshairView.centerDotColor = activeProfile.centerDotColor
        binding.calCrosshairView.centerDotOpacity = activeProfile.centerDotOpacity
        binding.calCrosshairView.translationX = activeProfile.offsetX.toFloat()
        binding.calCrosshairView.translationY = activeProfile.offsetY.toFloat()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
