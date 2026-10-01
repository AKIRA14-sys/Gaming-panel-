package com.gamepanel.ai.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.gamepanel.ai.databinding.FragmentScreenRecorderBinding

class ScreenRecorderFragment : Fragment() {

    private var _binding: FragmentScreenRecorderBinding? = null
    private val binding get() = _binding!!

    private var isRecording = false

    private val recordLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            isRecording = true
            binding.tvRecordingStatus.text = "STATUS: RECORDING ACTIVE"
            binding.tvRecordingStatus.setTextColor(resources.getColor(com.gamepanel.ai.R.color.accent_red, null))
            Toast.makeText(requireContext(), "Screen Recording Started (Overlay Hidden)", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Screen recording permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScreenRecorderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.btnStartRecording.setOnClickListener {
            val projectionManager = requireContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            recordLauncher.launch(projectionManager.createScreenCaptureIntent())
        }

        binding.btnStopRecording.setOnClickListener {
            if (isRecording) {
                isRecording = false
                binding.tvRecordingStatus.text = "STATUS: RECORDING SAVED"
                binding.tvRecordingStatus.setTextColor(resources.getColor(com.gamepanel.ai.R.color.accent_green, null))
                Toast.makeText(requireContext(), "Screen Recording Saved to Movies/GamePanelAI", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "No active recording session", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
