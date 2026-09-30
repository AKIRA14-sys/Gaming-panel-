package com.gamepanel.ai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.gamepanel.ai.data.GameProfile
import com.gamepanel.ai.data.ProfileRepository
import com.gamepanel.ai.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ProfileRepository
    private lateinit var activeProfile: GameProfile

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ProfileRepository(requireContext())

        val activeGameId = repository.getActiveGameId()
        activeProfile = repository.getActiveProfileForGame(activeGameId)

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        updateUI()

        binding.btnCreateProfile.setOnClickListener {
            val input = EditText(requireContext())
            input.hint = "Profile Name (e.g. COD Battle Royale)"
            AlertDialog.Builder(requireContext())
                .setTitle("Create New Profile")
                .setView(input)
                .setPositiveButton("Create") { _, _ ->
                    val name = input.text.toString().trim()
                    if (name.isNotEmpty()) {
                        activeProfile = repository.createProfile(activeGameId, name)
                        updateUI()
                        Toast.makeText(requireContext(), "Created profile: $name", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnDuplicateProfile.setOnClickListener {
            activeProfile = repository.duplicateProfile(activeProfile)
            updateUI()
            Toast.makeText(requireContext(), "Duplicated profile: ${activeProfile.name}", Toast.LENGTH_SHORT).show()
        }

        binding.btnResetProfile.setOnClickListener {
            activeProfile = repository.resetProfile(activeProfile)
            updateUI()
            Toast.makeText(requireContext(), "Reset profile: ${activeProfile.name}", Toast.LENGTH_SHORT).show()
        }

        binding.btnExportProfiles.setOnClickListener {
            val json = repository.exportAllProfilesJson()
            AlertDialog.Builder(requireContext())
                .setTitle("Exported Profiles JSON")
                .setMessage(json)
                .setPositiveButton("OK", null)
                .show()
        }

        binding.btnImportProfiles.setOnClickListener {
            val input = EditText(requireContext())
            input.hint = "Paste Profiles JSON here"
            AlertDialog.Builder(requireContext())
                .setTitle("Import Profiles")
                .setView(input)
                .setPositiveButton("Import") { _, _ ->
                    val jsonStr = input.text.toString()
                    if (repository.importAllProfilesJson(jsonStr)) {
                        activeProfile = repository.getActiveProfileForGame(activeGameId)
                        updateUI()
                        Toast.makeText(requireContext(), "Profiles imported successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Invalid JSON format", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun updateUI() {
        binding.tvActiveProfileName.text = "Current Profile: ${activeProfile.name} (${activeProfile.gameName})"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
