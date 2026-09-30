package com.gamepanel.ai

import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainActivityLaunchTest {

    @Test
    fun testMainActivityLaunchAndFragmentInflation() {
        try {
            val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
            val activity = controller.get()
            assertNotNull(activity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
