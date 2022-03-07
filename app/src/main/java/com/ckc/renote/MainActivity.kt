package com.ckc.renote

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ExpandableListView
import android.widget.ExpandableListView.OnGroupClickListener
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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
    var delay = 10000 // used for autosave looper: 10000 = 10 seconds
    private lateinit var editor: Editor
    private var openSection = "data_structures"
    private var fileType = ".txt"

    private fun loadFromFile(sectionName: String) {
        openSection = sectionName
        val contents = this.openFileInput(openSection.plus(fileType)).bufferedReader().useLines { lines ->
            lines.fold("") { some, text ->
                "$some\n$text"
            }
        }
        editor.load(contents)
    }

    private fun saveFile() {
        val filename = openSection.plus(fileType)
        val file = File(this.filesDir, filename)
        editor.save(file)
    }

    private fun createFileIfDoesntExist(sectionName: String) {
        val filename = sectionName.plus(fileType)
        val file = File(this.filesDir, filename)
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
        editor = Editor(findViewById(R.id.editor))
        loadFromFile(openSection)
        updatePageScrollView()

        //window.statusBarColor = ContextCompat.getColor(this, R.color.dark_red)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_bold -> editor.bold()
            R.id.action_italic -> editor.italic()
            R.id.action_underline -> editor.underline()
            R.id.action_strikethrough -> editor.strikeThrough()
            R.id.action_increase_font_size -> editor.increaseFontSize()
            R.id.action_decrease_font_size -> editor.decreaseFontSize()
            R.id.action_save -> saveFile()
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
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
            "Physics",
            true,
            true,
            "physics"
        ) //Menu of Android Tutorial. No sub menus
        headerList.add(menuModel)
        var childModelsList: MutableList<MenuModel> = ArrayList()
        var childModel = MenuModel(
            "+ new section",
            false,
            false,
            "information_gathering_interview"
        )
        childModelsList.add(childModel)
        if (!menuModel.hasChildren) {
            childList[menuModel] = null
        }
        childModelsList = ArrayList()
        menuModel = MenuModel("Computer Science", true, true, "") //Menu of Java Tutorials
        headerList.add(menuModel)
        childModel = MenuModel(
            "Data Structures",
            false,
            false,
            "data_structures"
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "Functional Programming",
            false,
            false,
            "functional_programming"
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "Object-Oriented Programming",
            false,
            false,
            "object_oriented_programming"
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "+ new section",
            false,
            false,
            "information_gathering_interview"
        )
        childModelsList.add(childModel)
        if (menuModel.hasChildren) {
            //Log.d("API123","here");
            childList[menuModel] = childModelsList
        }
        childModelsList = ArrayList()
        menuModel = MenuModel("Public Speaking", true, true, "") //Menu of Python Tutorials
        headerList.add(menuModel)
        childModel = MenuModel(
            "Selection Interview",
            false,
            false,
            "selection_interview"
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "Information-Gathering Interview",
            false,
            false,
            "information_gathering_interview"
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "+ new section",
            false,
            false,
            "information_gathering_interview"
        )
        childModelsList.add(childModel)
        if (menuModel.hasChildren) {
            childList[menuModel] = childModelsList
        }
        menuModel = MenuModel(
            "+ new textbook",
            true,
            false,
            "physics"
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
            //Toast.makeText(this@MainActivity, "This method will run every 10 seconds", Toast.LENGTH_SHORT).show()
        }.also { runnable = it }, delay.toLong())
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable!!)
    }

}
