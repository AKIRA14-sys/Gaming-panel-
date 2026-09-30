package com.gamepanel.ai.ui

import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.gamepanel.ai.data.GameProfile
import com.gamepanel.ai.data.ProfileRepository
import com.gamepanel.ai.databinding.FragmentIntelligenceBinding

class IntelligenceFragment : Fragment() {

    private var _binding: FragmentIntelligenceBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ProfileRepository
    private lateinit var activeProfile: GameProfile

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntelligenceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ProfileRepository(requireContext())

        val activeGameId = repository.getActiveGameId()
        activeProfile = repository.getActiveProfileForGame(activeGameId)

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        val dm: DisplayMetrics = resources.displayMetrics
        binding.tvIntelGameName.text = "Target Game: ${activeProfile.gameName}"
        binding.tvIntelDisplayInfo.text = "Display: ${dm.widthPixels}x${dm.heightPixels} (${dm.densityDpi} dpi)"

        val recommendedAsset = when (activeProfile.gameId) {
            "free_fire", "free_fire_max" -> "crosshairs/crosshair_1.png"
            "cod_mobile" -> "crosshairs/crosshair_10.png"
            "blood_strike" -> "crosshairs/crosshair_15.png"
            "pubg_mobile" -> "crosshairs/crosshair_20.png"
            else -> "crosshairs/crosshair_5.png"
        }

        val recommendedColor = when (activeProfile.gameId) {
            "free_fire" -> 0xFF00E5FF.toInt()
            "free_fire_max" -> 0xFF00E676.toInt()
            "cod_mobile" -> 0xFFFFEA00.toInt()
            "blood_strike" -> 0xFFFF1744.toInt()
            else -> 0xFF00E5FF.toInt()
        }

        val recommendedOpacity = 0.55f
        val recommendedSize = 32f
        val recommendedThickness = 1.0f

        binding.intelCrosshairPreview.setCrosshairAsset(recommendedAsset)
        binding.intelCrosshairPreview.crosshairColor = recommendedColor
        binding.intelCrosshairPreview.crosshairOpacity = recommendedOpacity
        binding.intelCrosshairPreview.crosshairSizeDp = recommendedSize
        binding.intelCrosshairPreview.crosshairThickness = recommendedThickness

        binding.tvRecDetails.text = "Crosshair Design: Recommended #${recommendedAsset.substringAfter("crosshair_").substringBefore(".png")}\nOpacity: ${(recommendedOpacity * 100).toInt()}%\nSize: ${recommendedSize.toInt()} dp\nThickness: Thin (1.0x)\nPosition: Screen Center Reference"

        binding.btnApplyIntelligence.setOnClickListener {
            activeProfile.crosshairAsset = recommendedAsset
            activeProfile.color = recommendedColor
            activeProfile.opacity = recommendedOpacity
            activeProfile.sizeDp = recommendedSize
            activeProfile.thickness = recommendedThickness
            activeProfile.offsetX = 0
            activeProfile.offsetY = 0

            repository.saveProfile(activeProfile)
            Toast.makeText(requireContext(), "Intelligence recommendations applied to ${activeProfile.name}", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
