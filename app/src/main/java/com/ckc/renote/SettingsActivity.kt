package com.ckc.renote

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {
    private lateinit var mainActivity: MainActivity

    fun initiateMainActivity(activity: MainActivity) {
        mainActivity = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings" // changes text displayed in the toolbar
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        lateinit var preferenceChangeListener: OnSharedPreferenceChangeListener

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            preferenceChangeListener =
                OnSharedPreferenceChangeListener { sharedPreference, key ->
                    if (key.equals("theme")) {
                        //val preference: Preference? = findPreference(key)


                        Log.i("preferences", "Selected option:".plus(sharedPreference.getString(key, "")))

                        if (sharedPreference.getString(key, "") == "light_theme") {
                            Log.i("preferences", "Light selected")
                            //(activity as SettingsActivity?)?.lightTheme()
                        } else if (sharedPreference.getString(key, "") == "dark_theme") {
                            Log.i("preferences", "Dark selected")
                            //(activity as SettingsActivity?)?.darkTheme()
                        } else {
                            Log.i("preferences", "Auto selected")
                            //(activity as SettingsActivity?)?.automaticTheme()
                        }

                    }
                }

        }

        override fun onResume() {
            super.onResume()
            preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    fun lightTheme() {
        var mainActivity = MainActivity()
        mainActivity.lightTheme()
    }

    fun darkTheme() {
        Log.i("preferences", "Hello from dark")
        var mainActivity = MainActivity()
        mainActivity.darkTheme()
    }

    fun automaticTheme() {
        Log.i("preferences", "Hello from automatic")
        var mainActivity = MainActivity()
        mainActivity.automaticTheme()
    }

}

