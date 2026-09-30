package com.gamepanel.ai.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gamepanel.ai.MainActivity
import com.gamepanel.ai.R
import com.gamepanel.ai.data.ProfileRepository
import com.gamepanel.ai.databinding.FragmentGalleryBinding
import com.gamepanel.ai.view.CrosshairView

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ProfileRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = ProfileRepository(requireContext())

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val assetList = mutableListOf<String>()
        try {
            val files = requireContext().assets.list("crosshairs") ?: emptyArray()
            val sorted = files.filter { it.endsWith(".png") }.sortedBy {
                it.replace("crosshair_", "").replace(".png", "").toIntOrNull() ?: 0
            }
            for (f in sorted) {
                assetList.add("crosshairs/$f")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val activeGameId = repository.getActiveGameId()
        val profile = repository.getActiveProfileForGame(activeGameId)

        binding.rvCrosshairGallery.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvCrosshairGallery.adapter = GalleryAdapter(assetList, profile.crosshairAsset) { selectedAsset ->
            profile.crosshairAsset = selectedAsset
            repository.saveProfile(profile)
            (activity as? MainActivity)?.navigateTo(MainActivity.NAV_CUSTOMIZER)
        }
    }

    class GalleryAdapter(
        private val items: List<String>,
        private val selectedAsset: String,
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val container: FrameLayout = view.findViewById(R.id.itemContainer)
            val crosshairView: CrosshairView = view.findViewById(R.id.itemCrosshairView)
            val tvIndex: TextView = view.findViewById(R.id.tvItemIndex)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_crosshair, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val assetPath = items[position]
            holder.crosshairView.setCrosshairAsset(assetPath)
            holder.crosshairView.crosshairColor = Color.CYAN
            holder.crosshairView.crosshairSizeDp = 28f
            holder.crosshairView.crosshairOpacity = 1.0f

            val indexName = assetPath.substringAfter("crosshair_").substringBefore(".png")
            holder.tvIndex.text = "#$indexName"

            if (assetPath == selectedAsset) {
                holder.container.setBackgroundColor(0xFF2A3040.toInt())
            } else {
                holder.container.setBackgroundColor(0x00000000)
            }

            holder.itemView.setOnClickListener {
                onItemClick(assetPath)
            }
        }

        override fun getItemCount() = items.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
