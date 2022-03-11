package com.ckc.renote

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms
import androidx.test.espresso.web.webdriver.DriverAtoms.*
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.matchers.JUnitMatchers.containsString
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebviewTest {
    @get:Rule
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    @Test
    fun testWebViewInteraction() {
        onWebView().forceJavascriptEnabled()
    }

    @Test
    fun typeTextInInput() {
        // Create an intent that displays a web form.
        // val noteIntent = Intent()
        val testText = "Hello World!"

        // Lazily launch the Activity with a custom start Intent per test.
        // ActivityScenario.launch<MainActivity>(noteIntent)

        // Selects the WebView in your layout. If you have multiple WebView
        // objects, you can also use a matcher to select a given WebView,
        // onWebView(withId(R.id.web_view)).
        onWebView()
            // Find the input element by ID.
            .withElement(findElement(Locator.ID, "editor"))

            // Clear previous input and enter new text into the input element.
            .perform(clearElement())
            .perform(DriverAtoms.webKeys(testText))

            // Find the response element by ID, and verify that it contains the
            // entered text.
            .withElement(findElement(Locator.ID, "editor"))
            .check(webMatches(getText(), containsString(testText)))
    }
}