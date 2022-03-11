package com.ckc.renote

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ExpandableListView
import android.widget.ExpandableListView.OnGroupClickListener
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var expandableListAdapter: ExpandableListAdapter? = null
    private var expandableListView: ExpandableListView? = null
    private var headerList: MutableList<MenuModel> = ArrayList()
    private var childList = HashMap<MenuModel, List<MenuModel>?>()
    private var handler: Handler = Handler() // used for autosave looper
    private var runnable: Runnable? = null // used for autosave looper
    private var delay = 10000 // used for autosave looper: 10000 = 10 seconds
    private lateinit var currNote: Note
    private lateinit var db: NoteRoomDatabase
    private lateinit var noteDao: NoteDao
    private var recording: Boolean = false
    private lateinit var editor: Editor
    private var openSection = "data_structures"
    private var fileType = ".json"

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadFromFile(sectionName: String) {
        openSection = sectionName
        val contents = this.openFileInput(openSection.plus(fileType)).bufferedReader().useLines { lines ->
            lines.fold("") { some, text -> "$some\n$text" }
        }
        currNote = Json.decodeFromString(contents)
        editor.load(currNote.contents)

    }

    private fun loadFromDatabase(sectionName: String) {
        Log.d("loadFromDatabase", db.toString())
        currNote = noteDao.findByName(sectionName)
        Log.d("loadFromDatabase", currNote.toString())
        editor.load(currNote.contents)
        supportActionBar?.title = sectionName
    }

    private fun saveFile() {
        Log.d("EditorAddress", editor.toString())
        Log.d("currNoteAddress", currNote.toString())
        editor.save(currNote)
    }

    private suspend fun createFileIfDoesntExist(sectionName: String) {
        if (noteDao.noteExists(sectionName) == 0) {
            val currTime = System.currentTimeMillis()
            val note = Note("", sectionName, currTime, currTime, noteDao.getMaxCustomOrder() + 1, "")
            noteDao.insertAll(note)
        }
    }

    private fun createMissingFiles() {
        // this method is temporary
        // its only needed to make sure that the Friday's demo can run on any device
        runBlocking {
            launch {
                createFileIfDoesntExist("data_structures")
                createFileIfDoesntExist("functional_programming")
                createFileIfDoesntExist("object_oriented_programming")
                createFileIfDoesntExist("selection_interview")
                createFileIfDoesntExist("information_gathering_interview")
            }
        }
    }

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            //db.clearAllTables()
            startActivity(Intent(this@MainActivity, MainActivity2::class.java))
        }

        this.db = NoteRoomDatabase.getDatabase(applicationContext)
        this.noteDao = db.noteDao()

        createMissingFiles()

        expandableListView = findViewById(R.id.expandableListView)
        prepareMenuData()
        populateExpandableList()
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)

        drawer.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                Log.i("drawer", "onDrawerSlide")
            }

            override fun onDrawerOpened(drawerView: View) {
                Log.i("drawer", "onDrawerOpened")
                hideKeyboard()
            }

            override fun onDrawerClosed(drawerView: View) {
                Log.i("drawer", "onDrawerClosed")
            }

            override fun onDrawerStateChanged(newState: Int) {
                expandableListAdapter?.updateGUI()
                Log.i("drawer", "onDrawerStateChanged")
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

        editor = Editor(findViewById(R.id.editor), db.noteDao())
        loadFromDatabase(openSection)
        updatePageScrollView()
        expandableListView?.let { expandableListAdapter?.initiateExpandableListView(it) }
        expandableListAdapter?.initiateDao(noteDao)
        expandableListAdapter?.initiateMainActivity(this)
        supportActionBar?.title = "" // hides an app name in the toolbar
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_record -> {
                if (!recording) {
                    editor.startRecording()
                    item.title = "Stop"
                    recording = true
                } else {
                    editor.stopRecording()
                    item.title = "Record"
                    recording = false
                }
            }
            R.id.action_play -> editor.play()
            R.id.action_undo -> editor.undo()
            R.id.action_redo -> editor.redo()
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

    fun prepareMenuData() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout

        headerList.clear()
        childList.clear()

        val notebooks: List<Notebook> = noteDao.loadNotebooksInOrder()

        Log.i("db", "Notebooks length:: ".plus(notebooks.size))

        val notebookIterator = notebooks.iterator()
        while (notebookIterator.hasNext()) {
            val notebook: Notebook = notebookIterator.next()
            Log.i("db", "Notebook title: ".plus(notebook.name))
            var menuModel = MenuModel(
                notebook.name,
                isGroup = true,
                hasChildren = true,
                "",
                drawer
            )
            val childModelsList: MutableList<MenuModel> = ArrayList()
            headerList.add(menuModel)

            val sections: List<Note> = noteDao.loadNotesInOrder(notebook.name)
            val sectionInterator = sections.iterator()
            while (sectionInterator.hasNext()) {
                val section: Note = sectionInterator.next()
                val childModel = MenuModel(
                    section.name,
                    isGroup = false,
                    hasChildren = false,
                    "",
                    drawer
                )
                childModelsList.add(childModel)
            }
            val childModel = MenuModel(
                "+ new section",
                isGroup = false,
                hasChildren = false,
                "",
                drawer
            )
            childModelsList.add(childModel)
            childList[menuModel] = childModelsList
        }
        var menuModel = MenuModel(
            "+ new notebook",
            isGroup = false,
            hasChildren = false,
            "",
            drawer
        )
        headerList.add(menuModel)

        /*
        var menuModel = MenuModel(
            "Physics",
            isGroup = true,
            hasChildren = true,
            "physics",
            drawer
        )
        headerList.add(menuModel)
        var childModelsList: MutableList<MenuModel> = ArrayList()
        var childModel = MenuModel(
            "+ new section",
            isGroup = false,
            hasChildren = false,
            "information_gathering_interview",
            drawer
        )
        childModelsList.add(childModel)
        if (!menuModel.hasChildren) {
            childList[menuModel] = null
        }
        menuModel = MenuModel("Computer Science", isGroup = true, hasChildren = true, "", drawer) //Menu of Java Tutorials
        childModelsList = ArrayList()
        headerList.add(menuModel)
        childModel = MenuModel(
            "Data Structures",
            isGroup = false,
            hasChildren = false,
            "data_structures",
            drawer
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "Functional Programming",
            isGroup = false,
            hasChildren = false,
            "functional_programming",
            drawer
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "Object-Oriented Programming",
            isGroup = false,
            hasChildren = false,
            "object_oriented_programming",
            drawer
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "+ new section",
            isGroup = false,
            hasChildren = false,
            "information_gathering_interview",
            drawer
        )
        childModelsList.add(childModel)
        if (menuModel.hasChildren) {
            childList[menuModel] = childModelsList
        }
        childModelsList = ArrayList()
        menuModel = MenuModel("Public Speaking", isGroup = true, hasChildren = true, "", drawer) //Menu of Python Tutorials
        headerList.add(menuModel)
        childModel = MenuModel(
            "Selection Interview",
            isGroup = false,
            hasChildren = false,
            "selection_interview",
            drawer
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "Information-Gathering Interview",
            isGroup = false,
            hasChildren = false,
            "information_gathering_interview",
            drawer
        )
        childModelsList.add(childModel)
        childModel = MenuModel(
            "+ new section",
            isGroup = false,
            hasChildren = false,
            "information_gathering_interview",
            drawer
        )
        childModelsList.add(childModel)
        if (menuModel.hasChildren) {
            childList[menuModel] = childModelsList
        }
        menuModel = MenuModel(
            "+ new notebook",
            isGroup = true,
            hasChildren = false,
            "physics",
            drawer
        ) //Menu of Android Tutorial. No sub menus
        headerList.add(menuModel)
        if (!menuModel.hasChildren) {
            childList[menuModel] = null
        }**/
    }

    private fun populateExpandableList() {
        expandableListAdapter = ExpandableListAdapter(this, headerList, childList)
        expandableListView!!.setAdapter(expandableListAdapter)
        expandableListView!!.setOnGroupClickListener(OnGroupClickListener { _, _, groupPosition, _ ->
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

        expandableListView!!.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            if (childList[headerList[groupPosition]] != null) {
                val childView: View? = expandableListAdapter!!.getChild(groupPosition, childPosition)?.view
                val txtListChild = childView?.findViewById<TextView>(R.id.lblListItem)
                val actualText: String = txtListChild?.text.toString()
                if (actualText == "+ new section") {
                    expandableListAdapter!!.createNewSection(groupPosition)
                } else {
                    saveFile()
                    val model = childList[headerList[groupPosition]]!![childPosition]
                    loadFromDatabase(model.menuName)
                    // Close the navigation drawer
                    val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
                    drawer.closeDrawer(GravityCompat.START)
                }
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
            Log.d("Editor", editor.toString())
            saveFile() // autosave
            // Toast.makeText(this@MainActivity, "This method will run every 10 seconds", Toast.LENGTH_SHORT).show()
        }.also { runnable = it }, delay.toLong())
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable!!)
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
