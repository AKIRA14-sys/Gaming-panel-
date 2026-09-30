package com.gamepanel.ai.data

import org.json.JSONObject

data class GameProfile(
    val id: String,
    var name: String,
    val gameId: String,
    val gameName: String,
    var crosshairAsset: String = "crosshairs/crosshair_1.png",
    var offsetX: Int = 0,
    var offsetY: Int = 0,
    var sizeDp: Float = 36f,
    var opacity: Float = 0.75f,
    var color: Int = 0xFF00E5FF.toInt(),
    var thickness: Float = 1.0f,
    var rotation: Float = 0f,
    var centerDotEnabled: Boolean = false,
    var centerDotSizeDp: Float = 4f,
    var centerDotColor: Int = 0xFFFF0000.toInt(),
    var centerDotOpacity: Float = 1.0f,
    var isLocked: Boolean = false
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("gameId", gameId)
            put("gameName", gameName)
            put("crosshairAsset", crosshairAsset)
            put("offsetX", offsetX)
            put("offsetY", offsetY)
            put("sizeDp", sizeDp.toDouble())
            put("opacity", opacity.toDouble())
            put("color", color)
            put("thickness", thickness.toDouble())
            put("rotation", rotation.toDouble())
            put("centerDotEnabled", centerDotEnabled)
            put("centerDotSizeDp", centerDotSizeDp.toDouble())
            put("centerDotColor", centerDotColor)
            put("centerDotOpacity", centerDotOpacity.toDouble())
            put("isLocked", isLocked)
        }
    }

    companion object {
        fun createDefault(id: String, name: String, gameId: String, gameName: String, asset: String, color: Int): GameProfile {
            return GameProfile(
                id = id,
                name = name,
                gameId = gameId,
                gameName = gameName,
                crosshairAsset = asset,
                color = color
            )
        }

        fun jsonToProfile(json: JSONObject): GameProfile {
            return GameProfile(
                id = json.optString("id", "profile_default"),
                name = json.optString("name", "Default Profile"),
                gameId = json.optString("gameId", "other"),
                gameName = json.optString("gameName", "Other Games"),
                crosshairAsset = json.optString("crosshairAsset", "crosshairs/crosshair_1.png"),
                offsetX = json.optInt("offsetX", 0),
                offsetY = json.optInt("offsetY", 0),
                sizeDp = json.optDouble("sizeDp", 36.0).toFloat(),
                opacity = json.optDouble("opacity", 0.75).toFloat(),
                color = json.optInt("color", 0xFF00E5FF.toInt()),
                thickness = json.optDouble("thickness", 1.0).toFloat(),
                rotation = json.optDouble("rotation", 0.0).toFloat(),
                centerDotEnabled = json.optBoolean("centerDotEnabled", false),
                centerDotSizeDp = json.optDouble("centerDotSizeDp", 4.0).toFloat(),
                centerDotColor = json.optInt("centerDotColor", 0xFFFF0000.toInt()),
                centerDotOpacity = json.optDouble("centerDotOpacity", 1.0).toFloat(),
                isLocked = json.optBoolean("isLocked", false)
            )
        }
    }
}

data class GameItem(
    val id: String,
    val name: String,
    val packageName: String? = null
)
