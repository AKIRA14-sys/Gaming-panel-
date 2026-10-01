package com.gamepanel.ai.service

import android.content.Intent
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CrosshairOverlayServiceTest {

    @Test
    fun testOverlayServiceStartAndPanelShow() {
        val controller = Robolectric.buildService(CrosshairOverlayService::class.java)
        val service = controller.create().get()
        assertNotNull(service)

        val intent = Intent(service, CrosshairOverlayService::class.java).apply {
            action = CrosshairOverlayService.ACTION_SHOW_PANEL
        }
        service.onStartCommand(intent, 0, 1)
    }
}
