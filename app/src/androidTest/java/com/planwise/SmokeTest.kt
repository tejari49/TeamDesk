package com.planwise

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmokeTest {
    @Test
    fun app_starts() {
        ActivityScenario.launch(MainActivity::class.java).use { }
    }
}
