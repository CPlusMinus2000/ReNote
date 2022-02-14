package com.ckc.renote

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.style.StyleSpan
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.ckc.renote.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            startActivity(Intent(this@MainActivity, MainActivity2::class.java))
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_bold -> {
                val editor: EditText = findViewById(R.id.edit_text1)
                val start = editor.selectionStart
                val end = editor.selectionEnd
                val ss = editor.text.getSpans(start, end, StyleSpan::class.java)
                for (span in ss) {
                    if (span.style == Typeface.BOLD) {
                        editor.text.removeSpan(span)
                        return true
                    }
                }
                editor.text.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                true
            }
            R.id.action_italic -> {
                val editor: EditText = findViewById(R.id.edit_text1)
                val start = editor.selectionStart
                val end = editor.selectionEnd
                val ss = editor.text.getSpans(start, end, StyleSpan::class.java)
                for (span in ss) {
                    if (span.style == Typeface.ITALIC) {
                        editor.text.removeSpan(span)
                        return true
                    }
                }
                editor.text.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                true
            }
            R.id.action_save -> { // TODO: save to file rather than printing to stdout
                val editor: EditText = findViewById(R.id.edit_text1)
                val text = Html.toHtml(editor.text, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
                editor.setText(Html.fromHtml("$text<p><u>haha</u></p>", Html.FROM_HTML_MODE_COMPACT))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}