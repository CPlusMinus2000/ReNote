package com.ckc.renote

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.util.Log
import android.view.*
import android.view.animation.ScaleAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.ExpandableListView.OnGroupClickListener
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONException
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

const val SERVER_ADDRESS = "http://54.188.105.181:8080"

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var expandableListAdapter: ExpandableListAdapter? = null
    private var expandableListView: ExpandableListView? = null
    private var headerList: MutableList<MenuModel> = ArrayList()
    private var childList = HashMap<MenuModel, List<MenuModel>?>()
    private var handler: Handler = Handler() // used  for autosave looper
    private var runnable: Runnable? = null // used for autosave looper
    private var delay = 10000 // used for autosave looper: 10000 = 10 seconds
    private lateinit var currNote: Note
    private lateinit var db: NoteRoomDatabase
    private lateinit var noteDao: NoteDao
    private var recording: Boolean = false
    private lateinit var editor: Editor
    lateinit var openSection: String
    private var mScale = 1f
    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private var gestureDetector: GestureDetector? = null
    private var searchData = SearchData()
    private lateinit var alertDialogGlobal: AlertDialog
    private lateinit var recordButton: MenuItem
    private val requestRecordAudioPermission = 200
    private val requestImagePermission = 400
    private val requestOpenGalleryIntent = 444
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    @OptIn(ExperimentalSerializationApi::class)
    fun loadFromDatabase(sectionName: String) {
        Log.d("loadFromDatabase", db.toString())
        currNote = noteDao.findByName(sectionName)
        editor.load(currNote.contents)
        supportActionBar?.title = sectionName
        openSection = sectionName
        hideKeyboard()
    }

    fun saveFile() {
        // update last modified time
        noteDao.update(openSection, System.currentTimeMillis())
        Log.d("EditorAddress", editor.toString())
        Log.d("currNoteAddress", currNote.toString())
        editor.save(currNote)
    }

    private fun checkNetworkConnection(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo?.isConnected() ?: false
    }

    @Throws(IOException::class, JSONException::class)
    private suspend fun httpPost(myUrl: String): String {
        val result = withContext(Dispatchers.IO) {
            val url = URL(myUrl)
            // 1. create HttpURLConnection
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")

            // 3. add JSON content to POST request body
            setPostRequestContent(conn, Json.encodeToString(currNote))

            // 4. make POST request to the given URL
            conn.connect()

            Log.d("httpPost", "Response Code: ${conn.responseCode}")
            Log.d("httpPost", "Response Message: ${conn.responseMessage}")
            // Log.d("httpPost", "Error Message: ${conn.errorStream.bufferedReader().use { it.readText() }}")

            val response = conn.inputStream.bufferedReader().use { it.readText() }

            // 5. return response message
            response + ""
        }

        Log.d("httpPost", result)
        return result
    }

    @Throws(IOException::class, JSONException::class)
    private suspend fun httpGet(myUrl: String): String {
        val result = withContext(Dispatchers.IO) {
            val url = URL(myUrl)
            // 1. create HttpURLConnection
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")

            // 4. make GET request to the given URL
            conn.connect()
            val response = conn.inputStream.bufferedReader().use { it.readText() }

            // 5. return response message
            response + ""
        }

        Log.d("httpGet", result)
        return result
    }

    @Throws(IOException::class)
    private fun setPostRequestContent(conn: HttpURLConnection, jsonObject: String) {
        val os = conn.outputStream
        val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
        writer.write(jsonObject)
        writer.flush()
        writer.close()
        os.close()
    }

    private fun saveToServer() {
        if (checkNetworkConnection()) {
            val url = SERVER_ADDRESS
            var saved = false
            runBlocking{
                launch {
                    try {
                        val result = httpPost(url)
                        Log.d("saveToServer", result)
                        currNote = Json.decodeFromString(result)
                        saved = true
                    } catch (e: Exception) {
                        Log.d("saveToServer", e.toString())
                    }
                }
            }

            if (saved) {
                Toast.makeText(this, "Saved to server", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to save to server", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d("saveToServer", "No network connection available")
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFromServer() {
        if (checkNetworkConnection()) {
            val url = SERVER_ADDRESS
            var loaded = false
            runBlocking{
                launch {
                    try {
                        val result = httpGet(url)
                        val noteList: List<Note> = Json.decodeFromString(result)
                        if (currNote.serverId != null) {
                            for (note in noteList) {
                                if (note.serverId == currNote.serverId) {
                                    currNote = note
                                    loaded = true
                                    editor.load(currNote.contents)
                                    break
                                }
                            }
                        }

                        if (!loaded) {
                            for (note in noteList) {
                                if (note.name == currNote.name && note.notebookName == currNote.notebookName) {
                                    currNote = note
                                    loaded = true
                                    editor.load(currNote.contents)
                                    break
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("loadFromServer", e.toString())
                    }
                }
            }

            if (!loaded) {
                Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d("loadFromServer", "No network connection available")
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createMissingFiles() {
        // If there is not at least 1 Notebook and 1 Section, create them
        val notebookCount = noteDao.notebookCount()
        val sectionCount = noteDao.noteCount()
        if (notebookCount == 0) {
            val newNotebook = Notebook(
                "New Notebook",
                1,
                System.currentTimeMillis(),
                System.currentTimeMillis()
            )
            noteDao.insertNotebook(newNotebook)
        }
        if (sectionCount == 0) {
            val notebooks: List<Notebook> = noteDao.loadNotebooksInOrder()
            val notebookIterator = notebooks.iterator()
            while (notebookIterator.hasNext()) {
                val notebook: Notebook = notebookIterator.next()
                val section = Note(
                    "",
                    "New Section",
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    1,
                    notebook.name
                )
                noteDao.insert(section)
                break
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
            //startActivity(Intent(this@MainActivity, MainActivity2::class.java))
            val builder = AlertDialog.Builder(this)
            builder.setMessage("I wish your sleeves to be always wet.")
            builder.setCancelable(false)
            builder.setPositiveButton("Hope for the best") { _, _ ->

            }
            builder.setNegativeButton("Accept curse") { dialog, _ ->
                dialog.cancel()
            }
            val alertDialog = builder.create()
            alertDialog.show()
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
                saveFile()
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
         * NavController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
         * NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
         * NavigationUI.setupWithNavController(navigationView, navController); */
        editor = Editor(findViewById(R.id.editor), db.noteDao())
        handler.postDelayed(Runnable {
            val saved = noteDao.getMostRecentlyModifiedNote() ?: "data_structures"
            currNote = noteDao.findByName(saved)
            editor.setInitContent(currNote.contents)
            supportActionBar?.title = saved
            openSection = saved
        }, 100)

        expandableListView?.let { expandableListAdapter?.initiateExpandableListView(it) }
        expandableListAdapter?.initiateDao(noteDao)
        expandableListAdapter?.initiateMainActivity(this)

        gestureDetector = GestureDetector(this, GestureListener())

        mScaleGestureDetector = ScaleGestureDetector(
            this,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val scale = 1 - detector.scaleFactor
                    val prevScale = mScale
                    mScale += scale
                    if (mScale > 10f) mScale = 10f
                    val scaleAnimation = ScaleAnimation(
                        1f / prevScale,
                        1f / mScale,
                        1f / prevScale,
                        1f / mScale,
                        detector.focusX,
                        detector.focusY
                    )
                    scaleAnimation.duration = 0
                    scaleAnimation.fillAfter = true
                    val pageView = findViewById<View>(R.id.pageLayout)
                    pageView.startAnimation(scaleAnimation)
                    return true
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                saveFile()
                showSearchAlertDialog()
            }
            R.id.action_rewind_options -> showRewindAlertDialog()
            R.id.action_record -> {
                recordButton = item
                if (!recording) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.RECORD_AUDIO
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            permissions,
                            requestRecordAudioPermission
                        )
                    } else {
                        editor.startRecording("${externalCacheDir?.absolutePath}/audiorecordtest.3gp")
                        recordButton.title = "Stop"
                        recording = true
                    }
                } else {
                    editor.stopRecording()
                    recordButton.title = "Record"
                    recording = false
                }
            }
            R.id.action_image -> {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), requestImagePermission)
                } else {
                    openGalleryForImage()
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
            R.id.action_save -> saveToServer()
            R.id.action_load -> loadFromServer()
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
        val notebookIterator = notebooks.iterator()
        while (notebookIterator.hasNext()) {
            val notebook: Notebook = notebookIterator.next()
            val menuModel = MenuModel(
                notebook.name,
                isGroup = true,
                hasChildren = true,
                "",
                drawer
            )
            val childModelsList: MutableList<MenuModel> = ArrayList()
            headerList.add(menuModel)
            val sections: List<Note> = noteDao.loadNotesInOrder(notebook.name)
            val sectionIterator = sections.iterator()
            while (sectionIterator.hasNext()) {
                val section: Note = sectionIterator.next()
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
        val menuModel = MenuModel(
            "+ new notebook",
            isGroup = false,
            hasChildren = false,
            "",
            drawer
        )
        headerList.add(menuModel)
    }

    private fun populateExpandableList() {
        expandableListAdapter = ExpandableListAdapter(this, headerList, childList)
        expandableListView!!.setAdapter(expandableListAdapter)
        expandableListView!!.setOnGroupClickListener(OnGroupClickListener { _, _, groupPosition, _ ->
            if (headerList[groupPosition].isGroup) {
                if (!headerList[groupPosition].hasChildren) {

                    /**
                     * WebView = findViewById(R.id.webView);
                     * webView.loadUrl(headerList.get(groupPosition).url);
                     * onBackPressed(); */
                    /**
                     * WebView = findViewById(R.id.webView);
                     * webView.loadUrl(headerList.get(groupPosition).url);
                     * onBackPressed(); */
                    /**
                     * WebView = findViewById(R.id.webView);
                     * webView.loadUrl(headerList.get(groupPosition).url);
                     * onBackPressed(); */
                    /**
                     * WebView = findViewById(R.id.webView);
                     * webView.loadUrl(headerList.get(groupPosition).url);
                     * onBackPressed(); */
                }
            }
            false
        })

        expandableListView!!.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            if (childList[headerList[groupPosition]] != null) {
                val childView: View? =
                    expandableListAdapter!!.getChild(groupPosition, childPosition)?.view
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

    override fun onStop() {
        super.onStop()
        saveFile()
        //db.clearAllTables() // temporary
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        super.dispatchTouchEvent(event)
        mScaleGestureDetector!!.onTouchEvent(event)
        gestureDetector!!.onTouchEvent(event)
        return gestureDetector!!.onTouchEvent(event)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            return true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestRecordAudioPermission) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                editor.startRecording("${externalCacheDir?.absolutePath}/audiorecordtest.3gp")
                recordButton.title = "Stop"
                recording = true
            } else {
                Toast.makeText(
                    applicationContext,
                    "Please allow microphone access for audio recording!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        else if (requestCode == requestImagePermission) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGalleryForImage()
            } else {
                Toast.makeText(applicationContext, "Please allow media access for image insertion!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, requestOpenGalleryIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == requestOpenGalleryIntent){
            Log.d("thegallery data?.data", data?.data.toString())
            editor.addImage(data?.data.toString())
        }
    }

    private fun hasText(section: Note): Boolean {
        if (searchData.textInput == "") {
            return false
        }
        // true if section contains searchData.textInput, false otherwise
        var target = section.contents
        target = target.replace("<(.*?)>".toRegex(), "")
        target = target.replace("\\\\u003C(.*?)>".toRegex(),"")
        target = target.replace("&nbsp;", "")
        target = target.replace("&amp;", "")
        return target.contains(searchData.textInput, ignoreCase = true)
    }

    private fun updateSearchResults() {
        searchData.sectionNames.clear()
        // iterate over all notebooks and sections in order
        val notebooks: List<Notebook> = noteDao.loadNotebooksInOrder()
        val notebookIterator = notebooks.iterator()
        while (notebookIterator.hasNext()) {
            val notebook: Notebook = notebookIterator.next()
            val sections: List<Note> = noteDao.loadNotesInOrder(notebook.name)
            val sectionIterator = sections.iterator()
            while (sectionIterator.hasNext()) {
                val section: Note = sectionIterator.next()
                if (hasText(section)) {
                    searchData.sectionNames += section.name
                }
            }
        }
    }

    private fun updateSearchInterface(linearLayout: LinearLayout) {
        // delete all children
        linearLayout.removeAllViews()
        // go through the list, create new children
        for (sectionName in searchData.sectionNames) {
            val childLayout: View = layoutInflater.inflate(
                R.layout.search_item,
                null
            )
            val notebookName = noteDao.findByName(sectionName).notebookName
            val textView = childLayout.findViewById<TextView>(R.id.search_child_text_view)
            val sourceString = notebookName.plus(": ").plus(sectionName)
            val spannableString = SpannableString(sourceString)
            val boldSpan = StyleSpan(Typeface.BOLD)
            spannableString.setSpan(
                boldSpan,
                0,
                notebookName.length + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textView.text = spannableString
            childLayout.setOnClickListener {
                //val actualText: String = textView.text.toString()
                saveFile()
                loadFromDatabase(sectionName)
                alertDialogGlobal.cancel()
            }
            linearLayout.addView(childLayout)
        }
    }

    @SuppressLint("ResourceType")
    private fun showSearchAlertDialog() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.search_alert_dialog, null)
        builder.setView(view)
        builder.setMessage("Search all notes for matching text")
        val wrapper = view.findViewById<LinearLayout>(R.id.search_wrapper_linear_layout)
        val searchHeader = wrapper.findViewById<ConstraintLayout>(R.id.search_header)
        val editText = searchHeader.findViewById<EditText>(R.id.search_edit_text)
        val scrollView = wrapper.findViewById<ScrollView>(R.id.search_scroll_view)
        val linearLayout = scrollView.findViewById<LinearLayout>(R.id.search_linear_layout)
        editText.setText(searchData.textInput)
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {}
            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                val searchInput = editText.text.toString()
                searchData.textInput = searchInput
                Log.i("search", "Search result:".plus(searchInput))

                updateSearchResults()
                updateSearchInterface(linearLayout)
            }
        })
        val alertDialog = builder.create()
        alertDialog.show()
        alertDialogGlobal = alertDialog
        updateSearchResults()
        updateSearchInterface(linearLayout)
    }

    @SuppressLint("ResourceType")
    private fun showRewindAlertDialog() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.rewind_alert_dialog, null)
        builder.setView(view)
        builder.setMessage("Rewind controls")
        //val wrapper = view.findViewById<LinearLayout>(R.id.search_wrapper_linear_layout)
        //val searchHeader = wrapper.findViewById<ConstraintLayout>(R.id.search_header)
        //val editText = searchHeader.findViewById<EditText>(R.id.search_edit_text)
        //val scrollView = wrapper.findViewById<ScrollView>(R.id.search_scroll_view)
        //val linearLayout = scrollView.findViewById<LinearLayout>(R.id.search_linear_layout)
        /*
        editText.setText(searchData.textInput)
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {}
            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                val searchInput = editText.text.toString()
                searchData.textInput = searchInput
                Log.i("search", "Search result:".plus(searchInput))

                updateSearchResults()
                updateSearchInterface(linearLayout)
            }
        })*/
        val alertDialog = builder.create()
        alertDialog.show()
    }
}