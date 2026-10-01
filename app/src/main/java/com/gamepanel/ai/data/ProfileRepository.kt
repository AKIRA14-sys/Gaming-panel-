package com.gamepanel.ai.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import org.json.JSONArray
import org.json.JSONObject

class ProfileRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("gamepanel_ai_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_ACTIVE_GAME_ID = "active_game_id"
        const val KEY_PROFILES_PREFIX = "profiles_game_"
        const val KEY_ACTIVE_PROFILE_PREFIX = "active_profile_id_"
        const val KEY_OVERLAY_ACTIVE = "overlay_active"

        val DEFAULT_SUPPORTED_GAMES = listOf(
            GameItem("free_fire", "Free Fire", "com.dts.freefireth"),
            GameItem("free_fire_max", "Free Fire MAX", "com.dts.freefiremax"),
            GameItem("cod_mobile", "Call of Duty: Mobile", "com.activision.callofduty.shooter"),
            GameItem("blood_strike", "Blood Strike", "com.mobi.bloodstrike"),
            GameItem("pubg_mobile", "PUBG Mobile", "com.tencent.ig"),
            GameItem("other_games", "Other Games", null)
        )
    }

    fun getInstalledAndDefaultGames(): List<GameItem> {
        val gameList = mutableListOf<GameItem>()
        gameList.addAll(DEFAULT_SUPPORTED_GAMES.filter { it.id != "other_games" })

        try {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val apps = pm.queryIntentActivities(intent, 0)
            for (app in apps) {
                val pkgName = app.activityInfo.packageName
                val appName = app.loadLabel(pm).toString()

                // Check if package looks like a game or not already added
                if (!gameList.any { it.packageName == pkgName }) {
                    val lower = appName.lowercase()
                    if (lower.contains("game") or lower.contains("fire") or lower.contains("duty") or
                        lower.contains("pubg") or lower.contains("strike") or lower.contains("legend") or
                        lower.contains("arena") or lower.contains("clash") or lower.contains("shadow") or
                        lower.contains("apex") or lower.contains("fortnite") or lower.contains("roblox") or
                        lower.contains("minecraft") or lower.contains("brawl")) {
                        val gameId = "pkg_${pkgName.replace('.', '_')}"
                        gameList.add(GameItem(gameId, appName, pkgName))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        gameList.add(DEFAULT_SUPPORTED_GAMES.last()) // Other Games
        return gameList
    }

    fun getActiveGameId(): String {
        return prefs.getString(KEY_ACTIVE_GAME_ID, "free_fire") ?: "free_fire"
    }

    fun setActiveGameId(gameId: String) {
        prefs.edit().putString(KEY_ACTIVE_GAME_ID, gameId).apply()
    }

    fun getActiveGameItem(): GameItem {
        val gameId = getActiveGameId()
        val allGames = getInstalledAndDefaultGames()
        return allGames.find { it.id == gameId } ?: DEFAULT_SUPPORTED_GAMES.first()
    }

    fun getActiveProfileForGame(gameId: String): GameProfile {
        val profiles = getProfilesForGame(gameId)
        val activeProfileId = prefs.getString(KEY_ACTIVE_PROFILE_PREFIX + gameId, null)
        return profiles.find { it.id == activeProfileId } ?: profiles.firstOrNull() ?: createDefaultProfileForGame(gameId)
    }

    fun setActiveProfileForGame(gameId: String, profileId: String) {
        prefs.edit().putString(KEY_ACTIVE_PROFILE_PREFIX + gameId, profileId).apply()
    }

    fun getProfilesForGame(gameId: String): MutableList<GameProfile> {
        val jsonStr = prefs.getString(KEY_PROFILES_PREFIX + gameId, null)
        if (jsonStr.isNullOrEmpty()) {
            val defaultProfile = createDefaultProfileForGame(gameId)
            val list = mutableListOf(defaultProfile)
            saveProfilesForGame(gameId, list)
            return list
        }

        val result = mutableListOf<GameProfile>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                result.add(GameProfile.jsonToProfile(obj))
            }
        } catch (e: Exception) {
            val defaultProfile = createDefaultProfileForGame(gameId)
            result.add(defaultProfile)
        }
        if (result.isEmpty()) {
            result.add(createDefaultProfileForGame(gameId))
        }
        return result
    }

    fun saveProfilesForGame(gameId: String, profiles: List<GameProfile>) {
        val jsonArray = JSONArray()
        for (p in profiles) {
            jsonArray.put(p.toJson())
        }
        prefs.edit().putString(KEY_PROFILES_PREFIX + gameId, jsonArray.toString()).apply()
    }

    fun saveProfile(profile: GameProfile) {
        val list = getProfilesForGame(profile.gameId)
        val idx = list.indexOfFirst { it.id == profile.id }
        if (idx >= 0) {
            list[idx] = profile
        } else {
            list.add(profile)
        }
        saveProfilesForGame(profile.gameId, list)
    }

    fun createProfile(gameId: String, name: String): GameProfile {
        val gameItem = getInstalledAndDefaultGames().find { it.id == gameId } ?: DEFAULT_SUPPORTED_GAMES.last()
        val newId = "profile_${gameId}_${System.currentTimeMillis()}"
        val newProfile = GameProfile(
            id = newId,
            name = name,
            gameId = gameId,
            gameName = gameItem.name,
            crosshairAsset = "crosshairs/crosshair_1.png",
            color = 0xFF00E5FF.toInt()
        )
        val list = getProfilesForGame(gameId)
        list.add(newProfile)
        saveProfilesForGame(gameId, list)
        setActiveProfileForGame(gameId, newId)
        return newProfile
    }

    fun duplicateProfile(profile: GameProfile): GameProfile {
        val newId = "profile_${profile.gameId}_${System.currentTimeMillis()}"
        val dup = profile.copy(id = newId, name = "${profile.name} (Copy)")
        val list = getProfilesForGame(profile.gameId)
        list.add(dup)
        saveProfilesForGame(profile.gameId, list)
        return dup
    }

    fun deleteProfile(profile: GameProfile): Boolean {
        val list = getProfilesForGame(profile.gameId)
        if (list.size <= 1) return false
        list.removeAll { it.id == profile.id }
        saveProfilesForGame(profile.gameId, list)
        if (getActiveProfileForGame(profile.gameId).id == profile.id) {
            setActiveProfileForGame(profile.gameId, list.first().id)
        }
        return true
    }

    fun resetProfile(profile: GameProfile): GameProfile {
        val default = createDefaultProfileForGame(profile.gameId)
        profile.crosshairAsset = default.crosshairAsset
        profile.offsetX = 0
        profile.offsetY = 0
        profile.sizeDp = default.sizeDp
        profile.opacity = default.opacity
        profile.color = default.color
        profile.thickness = default.thickness
        profile.rotation = 0f
        profile.centerDotEnabled = false
        profile.centerDotSizeDp = 4f
        profile.centerDotColor = 0xFFFF0000.toInt()
        profile.centerDotOpacity = 1.0f
        profile.isLocked = false
        saveProfile(profile)
        return profile
    }

    fun isOverlayActive(): Boolean {
        return prefs.getBoolean(KEY_OVERLAY_ACTIVE, false)
    }

    fun setOverlayActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_OVERLAY_ACTIVE, active).apply()
    }

    fun exportAllProfilesJson(): String {
        val root = JSONObject()
        for (game in getInstalledAndDefaultGames()) {
            val profiles = getProfilesForGame(game.id)
            val arr = JSONArray()
            for (p in profiles) arr.put(p.toJson())
            root.put(game.id, arr)
        }
        return root.toString(2)
    }

    fun importAllProfilesJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            val games = getInstalledAndDefaultGames()
            for (game in games) {
                if (root.has(game.id)) {
                    val arr = root.getJSONArray(game.id)
                    val list = mutableListOf<GameProfile>()
                    for (i in 0 until arr.length()) {
                        list.add(GameProfile.jsonToProfile(arr.getJSONObject(i)))
                    }
                    if (list.isNotEmpty()) {
                        saveProfilesForGame(game.id, list)
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun createDefaultProfileForGame(gameId: String): GameProfile {
        val gameItem = getInstalledAndDefaultGames().find { it.id == gameId } ?: DEFAULT_SUPPORTED_GAMES.last()
        val defaultAsset = when (gameId) {
            "free_fire" -> "crosshairs/crosshair_1.png"
            "free_fire_max" -> "crosshairs/crosshair_2.png"
            "cod_mobile" -> "crosshairs/crosshair_10.png"
            "blood_strike" -> "crosshairs/crosshair_15.png"
            "pubg_mobile" -> "crosshairs/crosshair_20.png"
            else -> "crosshairs/crosshair_1.png"
        }
        val defaultColor = when (gameId) {
            "free_fire" -> 0xFF00E5FF.toInt()
            "free_fire_max" -> 0xFF00E676.toInt()
            "cod_mobile" -> 0xFFFFEA00.toInt()
            "blood_strike" -> 0xFFFF1744.toInt()
            "pubg_mobile" -> 0xFFFFFFFF.toInt()
            else -> 0xFF00E5FF.toInt()
        }
        return GameProfile.createDefault(
            id = "profile_${gameId}_default",
            name = "${gameItem.name} Main",
            gameId = gameId,
            gameName = gameItem.name,
            asset = defaultAsset,
            color = defaultColor
        )
    }
}
