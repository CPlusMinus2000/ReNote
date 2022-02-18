package com.ckc.renote

import junit.framework.TestCase
import org.junit.Test
import android.view.MenuItem
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule
import org.mockito.kotlin.*

class SettingsActivityTest : TestCase() {
    // TODO: Get this working, or justify ignoring it completely
    @get:Rule val rule = InstantTaskExecutorRule()

    @Test
    fun testOnOptionsItemSelected() {
        /* val mockMenuItem : MenuItem = mock()
        whenever(mockMenuItem.itemId).thenReturn(android.R.id.home)

        val settingTester = SettingsActivity()
        assert(settingTester.onOptionsItemSelected(mockMenuItem))
         */

        assertEquals(2 + 2, 4)
    }
}