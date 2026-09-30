package com.gamepanel.ai.data

import org.junit.Assert.*
import org.junit.Test

class ProfileRepositoryTest {

    @Test
    fun testDefaultProfileCreation() {
        val cyanColor = 0xFF00E5FF.toInt()
        val profile = GameProfile.createDefault(
            id = "p_def",
            name = "Default",
            gameId = "cod_mobile",
            gameName = "Call of Duty: Mobile",
            asset = "crosshairs/crosshair_10.png",
            color = cyanColor
        )

        assertEquals("p_def", profile.id)
        assertEquals("cod_mobile", profile.gameId)
        assertEquals("crosshairs/crosshair_10.png", profile.crosshairAsset)
        assertEquals(cyanColor, profile.color)
        assertFalse(profile.isLocked)
    }
}
