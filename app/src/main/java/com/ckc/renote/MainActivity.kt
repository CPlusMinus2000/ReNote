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
import com.google.android.material.navigation.NavigationView

import android.widget.ExpandableListView
import android.util.Log
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.ActionBarDrawerToggle
import android.widget.ExpandableListView.OnGroupClickListener
import android.widget.ExpandableListView.OnChildClickListener
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import java.util.ArrayList
import java.util.HashMap

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var expandableListAdapter: ExpandableListAdapter? = null
    private var expandableListView: ExpandableListView? = null
    var headerList: MutableList<MenuModel> = ArrayList()
    var childList = HashMap<MenuModel, List<MenuModel>?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { _ ->
            startActivity(Intent(this@MainActivity, MainActivity2::class.java))
        }

        expandableListView = findViewById(R.id.expandableListView)
        prepareMenuData()
        populateExpandableList()
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        /**
         * // Passing each menu ID as a set of Ids because each
         * // menu should be considered as top level destinations.
         * mAppBarConfiguration = new AppBarConfiguration.Builder(
         * R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
         * .setOpenableLayout(drawer)
         * .build();
         * NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
         * NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
         * NavigationUI.setupWithNavController(navigationView, navController); */
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
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        /**
         * if (id == R.id.nav_camera) {
         * // Handle the camera action
         * } else if (id == R.id.nav_gallery) {
         *
         * } else if (id == R.id.nav_slideshow) {
         *
         * } else if (id == R.id.nav_manage) {
         *
         * } else if (id == R.id.nav_share) {
         *
         * } else if (id == R.id.nav_send) {
         *
         * } */
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun prepareMenuData() {
        var menuModel = MenuModel(
            "Fermentation Sciences",
            true,
            false,
            "https://www.journaldev.com/9333/android-webview-example-tutorial"
        ) //Menu of Android Tutorial. No sub menus
        headerList.add(menuModel)
        if (!menuModel.hasChildren) {
            childList[menuModel] = null
        }
        menuModel = MenuModel("Astrology", true, true, "") //Menu of Java Tutorials
        headerList.add(menuModel)
        var childModelsList: MutableList<MenuModel> = ArrayList()
        var childModel = MenuModel(
            "Moon cycles",
            false,
            false,
            "https://www.journaldev.com/7153/core-java-tutorial"
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "Most influential people",
            false,
            false,
            "https://www.journaldev.com/19187/java-fileinputstream"
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "Synchronising periods with moon cycles",
            false,
            false,
            "https://www.journaldev.com/19115/java-filereader"
        )
        childModelsList.add(childModel)
        if (menuModel.hasChildren) {
            //Log.d("API123","here");
            childList[menuModel] = childModelsList
        }
        childModelsList = ArrayList()
        menuModel = MenuModel("Animal Studies", true, true, "")
        headerList.add(menuModel)
        childModel = MenuModel(
            "How milk heals infants",
            false,
            false,
            "https://www.journaldev.com/19243/python-ast-abstract-syntax-tree"
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "Health benefits of cockroach ingestion",
            false,
            false,
            "https://www.journaldev.com/19226/python-fractions"
        )
        childModelsList.add(childModel)
        if (menuModel.hasChildren) {
            childList[menuModel] = childModelsList
        }
    }

    private fun populateExpandableList() {
        expandableListAdapter = ExpandableListAdapter(this, headerList, childList)
        expandableListView!!.setAdapter(expandableListAdapter)
        expandableListView!!.setOnGroupClickListener(OnGroupClickListener { parent, v, groupPosition, id ->
            if (headerList[groupPosition].isGroup) {
                if (!headerList[groupPosition].hasChildren) {
                    Log.d("TAG", "Do something")
                    // Act
                    /**
                     * WebView webView = findViewById(R.id.webView);
                     * webView.loadUrl(headerList.get(groupPosition).url);
                     * onBackPressed(); */
                    /**
                     * WebView webView = findViewById(R.id.webView);
                     * webView.loadUrl(headerList.get(groupPosition).url);
                     * onBackPressed(); */
                    /**
                     * WebView webView = findViewById(R.id.webView);
                     * webView.loadUrl(headerList.get(groupPosition).url);
                     * onBackPressed(); */
                    /**
                     * WebView webView = findViewById(R.id.webView);
                     * webView.loadUrl(headerList.get(groupPosition).url);
                     * onBackPressed(); */
                }
            }
            false
        })
        expandableListView!!.setOnChildClickListener(object : OnChildClickListener {
            override fun onChildClick(
                parent: ExpandableListView,
                v: View,
                groupPosition: Int,
                childPosition: Int,
                id: Long
            ): Boolean {
                if (childList[headerList[groupPosition]] != null) {
                    Log.d("TAG", "Do something")
                    // Act
                    /**
                     * MenuModel model = childList.get(headerList.get(groupPosition)).get(childPosition);
                     * if (model.url.length() > 0) {
                     * WebView webView = findViewById(R.id.webView);
                     * webView.loadUrl(model.url);
                     * onBackPressed();
                     * } */
                }
                return false
            }
        })
    }
}