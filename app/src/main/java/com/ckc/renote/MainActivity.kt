package com.ckc.renote

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.text.Spannable
import android.text.style.StyleSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.ExpandableListView.OnGroupClickListener
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.io.File


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var expandableListAdapter: ExpandableListAdapter? = null
    private var expandableListView: ExpandableListView? = null
    var headerList: MutableList<MenuModel> = ArrayList()
    var childList = HashMap<MenuModel, List<MenuModel>?>()
    var handler: Handler = Handler() // used for autosave looper
    var runnable: Runnable? = null // used for autosave looper
    private var delay = 10000 // used for autosave looper: 10000 = 10 seconds
    private var openSection = "data_structures"
    private var fileType = ".txt"

    private fun loadFromFile(sectionName: String) {
        openSection = sectionName
        var contents = this.openFileInput(openSection.plus(fileType)).bufferedReader().useLines { lines ->
            lines.fold("") { some, text ->
                "$some\n$text"
            }
        }
        val editor: EditText = findViewById(R.id.edit_text1)
        editor.setText(Html.fromHtml("$contents", Html.FROM_HTML_MODE_COMPACT))
        //editor.setText(contents)
    }

    private fun saveFile() {
        val editor: EditText = findViewById(R.id.edit_text1)
        val text = Html.toHtml(editor.text, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
        //editor.setText(Html.fromHtml("$text<p><u>haha</u></p>", Html.FROM_HTML_MODE_COMPACT))
        val filename = openSection.plus(fileType)
        var file = File(this.filesDir, filename)
        file.writeText(text, Charsets.UTF_8)
    }

    private fun createFileIfDoesntExist(sectionName: String) {
        val filename = sectionName.plus(fileType)
        var file = File(this.filesDir, filename)
        if (!file.exists()) {
            file.writeText("", Charsets.UTF_8)
        }
    }

    private fun createMissingFiles() {
        // this method is temporary
        // its only needed to make sure that the Friday's demo can run on any device
        createFileIfDoesntExist("data_structures")
        createFileIfDoesntExist("functional_programming")
        createFileIfDoesntExist("object_oriented_programming")
        createFileIfDoesntExist("selection_interview")
        createFileIfDoesntExist("information_gathering_interview")
    }

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

        drawer.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                Log.i("drawer", "onDrawerSlide");
            }

            override fun onDrawerOpened(drawerView: View) {
                Log.i("drawer", "onDrawerOpened")
            }

            override fun onDrawerClosed(drawerView: View) {
                Log.i("drawer", "onDrawerClosed")
            }

            override fun onDrawerStateChanged(newState: Int) {
                expandableListAdapter?.updateGUI()
                Log.i("drawer", "onDrawerStateChanged");
            }
        })

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
        createMissingFiles()
        loadFromFile(openSection)
        updatePageScrollView()

        Log.i("onCreate", "before assigning view")

        expandableListView?.let { expandableListAdapter?.initiateExpandableListView(it) }

        Log.i("onCreate", "after assigning view")
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
            R.id.action_save -> {
                saveFile()
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
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        var menuModel = MenuModel(
            "Physics",
            true,
            true,
            "physics",
            drawer
        ) //Menu of Android Tutorial. No sub menus
        headerList.add(menuModel)
        var childModelsList: MutableList<MenuModel> = ArrayList()
        var childModel = MenuModel(
            "+ new section",
            false,
            false,
            "information_gathering_interview",
            drawer
        )
        childModelsList.add(childModel)
        if (!menuModel.hasChildren) {
            childList[menuModel] = null
        }
        childModelsList = ArrayList()
        menuModel = MenuModel("Computer Science", true, true, "", drawer) //Menu of Java Tutorials
        headerList.add(menuModel)
        childModel = MenuModel(
            "Data Structures",
            false,
            false,
            "data_structures",
            drawer
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "Functional Programming",
            false,
            false,
            "functional_programming",
            drawer
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "Object-Oriented Programming",
            false,
            false,
            "object_oriented_programming",
            drawer
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "+ new section",
            false,
            false,
            "information_gathering_interview",
            drawer
        )
        childModelsList.add(childModel)
        if (menuModel.hasChildren) {
            childList[menuModel] = childModelsList
        }
        childModelsList = ArrayList()
        menuModel = MenuModel("Public Speaking", true, true, "", drawer) //Menu of Python Tutorials
        headerList.add(menuModel)
        childModel = MenuModel(
            "Selection Interview",
            false,
            false,
            "selection_interview",
            drawer
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "Information-Gathering Interview",
            false,
            false,
            "information_gathering_interview",
            drawer
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "+ new section",
            false,
            false,
            "information_gathering_interview",
            drawer
        )
        childModelsList.add(childModel)
        if (menuModel.hasChildren) {
            childList[menuModel] = childModelsList
        }
        menuModel = MenuModel(
            "+ new notebook",
            true,
            false,
            "physics",
            drawer
        ) //Menu of Android Tutorial. No sub menus
        headerList.add(menuModel)
        if (!menuModel.hasChildren) {
            childList[menuModel] = null
        }
    }

    private fun populateExpandableList() {
        expandableListAdapter = ExpandableListAdapter(this, headerList, childList)
        expandableListView!!.setAdapter(expandableListAdapter)
        expandableListView!!.setOnGroupClickListener(OnGroupClickListener { parent, v, groupPosition, id ->
            if (headerList[groupPosition].isGroup) {
                if (!headerList[groupPosition].hasChildren) {

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

        expandableListView!!.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            if (childList[headerList[groupPosition]] != null) {
                Log.w("myApp", "small tap");
                saveFile()
                val model = childList[headerList[groupPosition]]!![childPosition]
                loadFromFile(model.url);
                // Close the navigation drawer
                val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
                drawer.closeDrawer(GravityCompat.START)

            }
            false
        }

    }

    private fun updatePageScrollView() {
        //val linLayout = findViewById<LinearLayout>(R.id.drawer_layout)

        //val stub = findViewById<View>(R.id.layout_stub) as ViewStub
        //val edittext = EditText(this)
        ///edittext.id = "page0".toInt()

        //linLayout.addView(edittext)


    }

    override fun onResume() {
        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay.toLong())
            saveFile() // autosave
        }.also { runnable = it }, delay.toLong())
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable!!)
    }



}
