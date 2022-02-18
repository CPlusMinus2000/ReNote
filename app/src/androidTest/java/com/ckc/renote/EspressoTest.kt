package com.ckc.renote

import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Test

class EspressoTest {
    @Test
    fun testSettingsButton() {
        Intents.init()
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.action_settings)).perform(click())
        intended(hasComponent(SettingsActivity::class.java.name))
        Intents.release()
    }
}