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
        editor.setText("Does this work?")
        sleep(500);
        assertThat(editor.getText(), `is`(equalTo("Does this work?")))
    }

    @Test
    fun testSettingsPreservesText() {
        val editor: UiObject = device.findObject(UiSelector().className("android.widget.EditText"))
        editor.setText("This text should be preserved")

        val settings: UiObject = device.findObject(UiSelector().resourceId("com.ckc.renote:id/action_settings"))
        settings.click()

        device.pressBack()
        assertThat(editor.getText(), `is`(equalTo("This text should be preserved")))
    }
}
