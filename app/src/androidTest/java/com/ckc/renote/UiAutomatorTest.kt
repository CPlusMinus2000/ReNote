package com.ckc.renote

import android.content.Context
import android.content.Intent
import android.os.SystemClock.sleep
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UiAutomatorTest {

    private lateinit var device: UiDevice
    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressHome()
        device.wait(Until.hasObject(By.pkg(device.launcherPackageName).depth(0)), 500)

        // Launch the app
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage("com.ckc.renote")?.apply {
            // Clear out any previous instances
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        device.wait(Until.hasObject(By.pkg("com.ckc.renote").depth(0)), 500)
    }

    @Test
    fun testEnteringText() {
        val editor: UiObject = device.findObject(UiSelector().className("android.widget.EditText"))
        editor.text = "Does this work?"
        sleep(500)
        assertThat(editor.text, `is`(equalTo("Does this work?")))
    }

    @Test
    fun testSettingsPreservesText() {
        val editor: UiObject = device.findObject(UiSelector().className("android.widget.EditText"))
        editor.text = "This text should be preserved"

        val settings: UiObject = device.findObject(UiSelector().resourceId("com.ckc.renote:id/action_settings"))
        settings.click()

        device.pressBack()
        assertThat(editor.text, `is`(equalTo("This text should be preserved")))
    }

    @Test
    fun happyPathTimer() {
        val startTime = System.currentTimeMillis()
        val editor: UiObject = device.findObject(UiSelector().className("android.widget.EditText"))
        editor.text = "This is a test that measures how much time certain actions take to complete."

        val bold: UiObject = device.findObject(UiSelector().resourceId("com.ckc.renote:id/action_bold"))
        bold.click()

        editor.text += " This is some more text. It is bolded."
        bold.click()

        editor.text += " This is yet more text. It is not bolded."

        val save: UiObject = device.findObject(UiSelector().resourceId("com.ckc.renote:id/action_save"))
        val load: UiObject = device.findObject(UiSelector().resourceId("com.ckc.renote:id/action_load"))
        save.click()

        editor.text += " Some more text. I hope it doesn't get deleted..."
        load.click()

        val endTime = System.currentTimeMillis()
        val timeTaken = endTime - startTime
        println("Time taken: $timeTaken")
    }
}
