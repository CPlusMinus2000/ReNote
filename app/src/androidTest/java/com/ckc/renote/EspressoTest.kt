package com.ckc.renote

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.core.AllOf.allOf
import org.junit.Test
import java.lang.Thread.sleep

class EspressoTest {
    @Test
    fun testSettingsButton() {
        Intents.init()
        ActivityScenario.launch(MainActivity::class.java)
        sleep(1000)
        onView(withId(R.id.action_settings)).perform(click())
        intended(hasComponent(SettingsActivity::class.java.name))
        Intents.release()
    }

    @Test
    fun testEnteringText() {
        Intents.init()
        ActivityScenario.launch(MainActivity::class.java)
        sleep(500)
        Espresso.closeSoftKeyboard()
        val storeId = "111\n"
        sleep(5000)
        onView(allOf(withId(R.id.edit_text1), isDisplayed()))
            .perform(scrollTo())
            .perform(typeText("Does this work?"))
            .check(matches(withText("Does this work?")))
        Intents.release()
    }

    // Test to see if opening the settings menu affects written text
    @Test
    fun testSettingsPreservesText() {
        Intents.init()
        ActivityScenario.launch(MainActivity::class.java)
        sleep(500)
        Espresso.closeSoftKeyboard()
        sleep(500)
        onView(allOf(withId(R.id.edit_text1), isDisplayed()))
            .perform(scrollTo())
            .perform(typeText("This text should be preserved"))

        onView(withId(R.id.action_settings)).perform(click())
        Espresso.pressBack()
        onView(allOf(withId(R.id.edit_text1), isDisplayed()))
            .check(matches(withText("This text should be preserved")))

        Intents.release()
    }

    // Basic test to check saving and loading
    @Test
    fun testSavingAndLoading() {
        Intents.init()
        ActivityScenario.launch(MainActivity::class.java)
        Espresso.closeSoftKeyboard()
        onView(allOf(withId(R.id.edit_text1), isDisplayed()))
            .perform(scrollTo())
            .perform(typeText("Let's see if this saves properly"))

        // Click on the three bars
        // then switch to another file
        // and then back to the original one
        // and check if the text is still there
        // I just can't find the damn IDs, sadge

        onView(allOf(withId(R.id.edit_text1), isDisplayed()))
            .check(matches(withText("Let's see if this saves properly")))

        Intents.release()
    }
}