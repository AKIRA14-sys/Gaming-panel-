package com.gamepanel.ai.ui

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gamepanel.ai.databinding.FragmentPerformanceBinding

class PerformanceFragment : Fragment() {

    private var _binding: FragmentPerformanceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerformanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        loadPerformanceInfo()
        loadDisplayInfo()
    }

    private fun loadPerformanceInfo() {
        val am = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memoryInfo)

        val totalRamMb = memoryInfo.totalMem / (1024 * 1024)
        val availRamMb = memoryInfo.availMem / (1024 * 1024)

        binding.tvPerfRam.text = "RAM: ${availRamMb}MB available / ${totalRamMb}MB total"
        binding.tvPerfCpu.text = "CPU Architecture: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"}"
        binding.tvPerfAndroidVersion.text = "Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            requireContext().registerReceiver(null, filter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else 0

        binding.tvPerfBattery.text = "Battery Level: $batteryPct%"
    }

    private fun loadDisplayInfo() {
        val dm: DisplayMetrics = resources.displayMetrics
        binding.tvDispResolution.text = "Resolution: ${dm.widthPixels} x ${dm.heightPixels} pixels"
        binding.tvDispDensity.text = "Density: ${dm.densityDpi} dpi (${dm.density}x)"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display = activity?.display
            val refreshRate = display?.refreshRate ?: 60f
            binding.tvDispRefreshRate.text = "Refresh Rate: ${refreshRate.toInt()} Hz"
        } else {
            binding.tvDispRefreshRate.text = "Refresh Rate: 60 Hz"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
