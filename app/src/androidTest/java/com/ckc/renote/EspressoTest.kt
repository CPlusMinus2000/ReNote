package com.ckc.renote

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.AllOf.allOf
import org.junit.Test

fun withIndex(matcher: Matcher<View?>, index: Int): Matcher<View?>? {
    return object : TypeSafeMatcher<View?>() {
        var currentIndex = 0
        override fun describeTo(description: Description) {
            description.appendText("with index: ")
            description.appendValue(index)
            matcher.describeTo(description)
        }

        override fun matchesSafely(view: View?): Boolean {
            return matcher.matches(view) && currentIndex++ == index
        }
    }
}

class EspressoTest {
    @Test
    fun testSettingsButton() {
        Intents.init()
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.action_settings)).perform(click())
        intended(hasComponent(SettingsActivity::class.java.name))
        Intents.release()
    }

    @Test
    fun testEnteringText() {
        Intents.init()
        ActivityScenario.launch(MainActivity::class.java)
        onView(allOf(withId(R.id.edit_text1), isDisplayed()))
            .perform(typeText("Does this work?"))
            .check(matches(withText("Does this work?")))
        Intents.release()
    }

    // Test to see if opening the settings menu affects written text
    @Test
    fun testSettingsPreservesText() {
        Intents.init()
        ActivityScenario.launch(MainActivity::class.java)
        onView(allOf(withId(R.id.edit_text1), isDisplayed()))
            .perform(typeText("This text should be preserved"))

        onView(withId(R.id.action_settings)).perform(click())
        Espresso.pressBack()
        onView(allOf(withId(R.id.edit_text1), isDisplayed()))
            .check(matches(withText("This text should be preserved")))

        Intents.release()
    }
}