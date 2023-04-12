package com.ckc.renote

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager


open class ExpandableListAdapter(
    private val context: Context,
    private val listDataHeader: List<MenuModel>,
    private val listDataChild: HashMap<MenuModel, List<MenuModel>?>

) : BaseExpandableListAdapter() {

    private lateinit var expandableListView: ExpandableListView
    private lateinit var noteDao: NoteDao
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private lateinit var mainActivity: MainActivity

    fun initiateExpandableListView(view: ExpandableListView) {
        expandableListView = view
    }

    fun initiateDao(dao: NoteDao) {
        noteDao = dao
    }

    fun initiateMainActivity(activity: MainActivity) {
        mainActivity = activity
    }

    fun updateGUI() {
        for (groupPosition in 0 until groupCount) {
            val childrenCount = getChildrenCount(groupPosition)
            for (childPosition in 0 until childrenCount) {
                val newSectionBackground = ContextCompat.getDrawable(context, R.drawable.new_section_button)
                val childView: View? = getChild(groupPosition, childPosition)?.view
                val optionsButton = childView?.findViewById<ImageView>(R.id.options_button)
                val txtListChild = childView?.findViewById<TextView>(R.id.lblListItem)
                val actualText: String = txtListChild?.text.toString()
                if (actualText == "+ new section") {
                    if (newSectionBackground != null) {
                        newSectionBackground.alpha = 127
                    } // make large button visible
                    optionsButton?.alpha = 0f // make options button transparent
                } else {
                    if (newSectionBackground != null) {
                        newSectionBackground.alpha = 0
                    } // make large button transparent
                    optionsButton?.alpha = 1f // make options button visible
                }
                txtListChild?.background = newSectionBackground
            }
            val newNotebookBackground = ContextCompat.getDrawable(context, R.drawable.new_notebook_button)
            val groupView: View = getGroup(groupPosition).view
            val optionsButton = groupView.findViewById<ImageView>(R.id.options_button)
            val lblListHeader = groupView.findViewById<TextView>(R.id.lblListHeader)
            val actualText: String = lblListHeader?.text.toString()
            if (actualText == "+ new notebook") {
                if (newNotebookBackground != null) {
                    newNotebookBackground.alpha = 255
                } // make large button visible
                optionsButton?.alpha = 0f // make options button transparent
            } else {
                if (newNotebookBackground != null) {
                    newNotebookBackground.alpha = 0
                } // make large button transparent
                optionsButton?.alpha = 1f // make options button visible
            }
            lblListHeader?.background = newNotebookBackground
        }
    }

    override fun getChild(groupPosition: Int, childPosition: Int): MenuModel? {
        return listDataChild[listDataHeader[groupPosition]]?.get(childPosition)
    }

    override fun getChildId(groupPosition: Int, childPosition: Int) = childPosition.toLong()

    @SuppressLint("InflateParams")
    override fun getChildView(
        groupPosition: Int, childPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup
    ): View {
        val childText = getChild(groupPosition, childPosition)?.menuName
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val newView: View = convertView ?: inflater.inflate(R.layout.list_group_child, null)

        val txtListChild = newView.findViewById<TextView>(R.id.lblListItem)
        txtListChild.text = childText

        val optionsButton = newView.findViewById<ImageView>(R.id.options_button)
        optionsButton.setOnClickListener {
            val actualText: String = txtListChild.text.toString()
            if (actualText != "+ new section") {
                showSectionOptions(optionsButton, actualText)
            }
        }
        getChild(groupPosition, childPosition)?.view = newView
        updateGUI()
        return newView
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return if (listDataChild[listDataHeader[groupPosition]] == null) 0
               else listDataChild[listDataHeader[groupPosition]]!!.size
    }

    override fun getGroup(groupPosition: Int) = listDataHeader[groupPosition]

    override fun getGroupCount() = listDataHeader.size

    override fun getGroupId(groupPosition: Int) = groupPosition.toLong()

    @SuppressLint("InflateParams")
    override fun getGroupView(
        groupPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup
    ): View {
        val headerTitle = getGroup(groupPosition).menuName
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val newView: View = convertView ?: inflater.inflate(R.layout.list_group_header, null)
        val lblListHeader = newView.findViewById<TextView>(R.id.lblListHeader)
        lblListHeader.setTypeface(null, Typeface.BOLD)
        lblListHeader.text = headerTitle

        val optionsButton = newView.findViewById<ImageView>(R.id.options_button)
        optionsButton.setOnClickListener {
            val actualText: String = lblListHeader.text.toString()
            if (actualText != "+ new notebook") {
                showNotebookOptions(optionsButton, actualText)
            }
        }
        getGroup(groupPosition).view = newView
        updateGUI()
        return newView
    }

    override fun hasStableIds() = false

    override fun isChildSelectable(groupPosition: Int, childPosition: Int) = true

    override fun onGroupCollapsed (groupPosition: Int) {
        super.onGroupCollapsed(groupPosition)
        val groupView: View = getGroup(groupPosition).view
        val lblListHeader = groupView.findViewById<TextView>(R.id.lblListHeader)
        val actualText: String = lblListHeader?.text.toString()
        if (actualText == "+ new notebook") {
            createNewNotebook()
        }
    }

    override fun onGroupExpanded (groupPosition: Int) {
        val groupView: View = getGroup(groupPosition).view
        val lblListHeader = groupView.findViewById<TextView>(R.id.lblListHeader)
        val actualText: String = lblListHeader?.text.toString()
        if (actualText == "+ new notebook") {
            createNewNotebook()
        } else {
            val collapseOtherGroupsWhenGroupExpands = preferences.getBoolean("collapse", false)
            if (collapseOtherGroupsWhenGroupExpands) { // collapse all other groups when a group gets expanded
                for (gp in 0 until groupCount) {
                    if (gp != groupPosition && expandableListView.isGroupExpanded(gp)) {
                        expandableListView.collapseGroup(gp)
                    }
                }
            }
        }
        super.onGroupExpanded(groupPosition)
    }

    private fun createNewNotebook() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Enter the notebook title:")

        // Set up the input
        val input = EditText(context)
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.hint = ""
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("Create") { _, _ ->
            // Here you get input text from the Edittext
            val notebookName = input.text.toString()
            if (notebookName != "+ new notebook") {
                val order = noteDao.getMaxNotebookOrder() + 1
                val newNotebook = Notebook(
                    notebookName,
                    order,
                    System.currentTimeMillis(),
                    System.currentTimeMillis()
                )
                noteDao.insertNotebook(newNotebook)
                mainActivity.prepareMenuData()
                notifyDataSetChanged()
            }
        }
        builder.setNegativeButton("Cancel") {dialog, _ ->
            mainActivity.hideKeyboard()
            dialog.cancel()
        }
        builder.show()
    }

    fun createNewSection(groupPosition: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Enter the section title:")
        val input = EditText(context)
        input.hint = ""
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)
        builder.setPositiveButton("Create") { _, _ ->
            val name = input.text.toString()
            if (name != "+ new section") {
                val groupView: View = getGroup(groupPosition).view
                val lblListHeader = groupView.findViewById<TextView>(R.id.lblListHeader)
                val notebookName: String = lblListHeader?.text.toString()
                val contents = ""
                val creationTime = System.currentTimeMillis()
                val customOrder = noteDao.getMaxCustomOrder() + 1
                val lastEdited = System.currentTimeMillis()
                val newSection = Note(
                    contents,
                    name,
                    creationTime,
                    lastEdited,
                    customOrder,
                    notebookName
                )
                noteDao.insert(newSection)
                mainActivity.prepareMenuData()
                notifyDataSetChanged()
                mainActivity.saveFile()
                mainActivity.loadFromDatabase(name)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            mainActivity.hideKeyboard()
            dialog.cancel()}
        builder.show()
    }


    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun renameSection(actualText: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Enter the new title:")
        val input = EditText(context)
        input.hint = ""
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(actualText)
        builder.setView(input)
        builder.setPositiveButton("Rename") { _, _ ->
            val section: Note = noteDao.findByName(actualText)
            val contents = section.contents
            val creationTime = section.creationTime
            val customOrder = section.customOrder
            val lastEdited = section.lastEdited
            val name = input.text.toString()
            val notebookName =section.notebookName
            val newSection = Note(
                contents,
                name,
                creationTime,
                lastEdited,
                customOrder,
                notebookName
            )
            noteDao.delete(section)
            noteDao.insert(newSection)
            mainActivity.prepareMenuData()
            notifyDataSetChanged()
            mainActivity.loadFromDatabase(name)
            mainActivity.hideKeyboard()
            mainActivity.hideKeyboard()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            mainActivity.hideKeyboard()
            dialog.cancel()
        }
        builder.show()
    }

    private fun moveUpSection(actualText: String) {
        val sortBy = preferences.getString("sort_by", "custom_order")
        if (sortBy == "custom_order") {
            val section: Note = noteDao.findByName(actualText)
            val anotherSection = noteDao.loadPreviousNoteInOrder(section.notebookName, section.customOrder)
            Log.i("reorder", "method called")
            if (anotherSection != null) { // section exists with smaller order
                Log.i("reorder", "not null")
                val newSection = Note(
                    section.contents,
                    section.name,
                    section.creationTime,
                    section.lastEdited,
                    anotherSection.customOrder,
                    section.notebookName
                )
                val newAnotherSection = Note(
                    anotherSection.contents,
                    anotherSection.name,
                    anotherSection.creationTime,
                    anotherSection.lastEdited,
                    section.customOrder,
                    anotherSection.notebookName
                )
                noteDao.delete(section)
                noteDao.delete(anotherSection)
                noteDao.insert(newSection)
                noteDao.insert(newAnotherSection)
                mainActivity.prepareMenuData()
                notifyDataSetChanged()
            }
        } else {
            openDialogCannotChangeOrder()
        }
    }

    private fun moveDownSection(actualText: String) {
        val sortBy = preferences.getString("sort_by", "custom_order")
        if (sortBy == "custom_order") {
            val section: Note = noteDao.findByName(actualText)
            val anotherSection = noteDao.loadNextNoteInOrder(section.notebookName, section.customOrder)
            if (anotherSection != null) { // section exists with larger order
                val newSection = Note(
                    section.contents,
                    section.name,
                    section.creationTime,
                    section.lastEdited,
                    anotherSection.customOrder,
                    section.notebookName
                )
                val newAnotherSection = Note(
                    anotherSection.contents,
                    anotherSection.name,
                    anotherSection.creationTime,
                    anotherSection.lastEdited,
                    section.customOrder,
                    anotherSection.notebookName
                )
                noteDao.delete(section)
                noteDao.delete(anotherSection)
                noteDao.insert(newSection)
                noteDao.insert(newAnotherSection)
                mainActivity.prepareMenuData()
                notifyDataSetChanged()
            }
        } else {
            openDialogCannotChangeOrder()
        }
    }

    private fun deleteSection(actualText: String) {
        Log.i("note count", "Note Count: ".plus(noteDao.noteCount()))
        if (noteDao.noteCount() == 1) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Section cannot be deleted because it is the last section left.")
            builder.setNegativeButton("OK") {
                    dialog, _ -> dialog.cancel()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        } else {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Do you wish to delete ".plus(actualText).plus("?\n" +
                    "This action cannot be undone."))
            builder.setPositiveButton("Delete") {
                    _, _ ->
                val section: Note = noteDao.findByName(actualText)
                noteDao.delete(section)
                mainActivity.prepareMenuData()
                notifyDataSetChanged()
                // Close section if it is open
                if (actualText == mainActivity.openSection) {
                    // TO IMPLEMENT
                    // if smaller order exist in notebook, switch to it
                    // otherwise, switch to any section
                    // else {
                    val notebooks: List<Notebook> = noteDao.loadNotebooksInOrder()
                    val notebookIterator = notebooks.iterator()
                    var breakAll = false
                    while (notebookIterator.hasNext() && !breakAll) {
                        val notebook: Notebook = notebookIterator.next()
                        val sections: List<Note> = noteDao.loadNotesInOrder(notebook.name)
                        val sectionIterator = sections.iterator()
                        while (sectionIterator.hasNext()) {
                            val section: Note = sectionIterator.next()
                            mainActivity.loadFromDatabase(section.name)
                            breakAll = true
                            break
                        }
                    }
                }
            }
            builder.setNegativeButton("Cancel") {
                    dialog, _ -> dialog.cancel()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

    private fun showSectionOptions(view: View, actualText: String) {
        val popup = PopupMenu(context, view)
        popup.inflate(R.menu.section_drop_down)
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.rename -> {
                    renameSection(actualText)
                }
                R.id.move_up -> {
                    moveUpSection(actualText)
                }
                R.id.move_down -> {
                    moveDownSection(actualText)
                }
                R.id.delete -> {
                    deleteSection(actualText)
                }
            }
            true
        })
        popup.show()
    }

    private fun renameNotebook(actualText: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Enter the new title:")
        val input = EditText(context)
        input.hint = ""
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(actualText)
        builder.setView(input)
        builder.setPositiveButton("Rename") { _, _ ->
            val notebook: Notebook = noteDao.findNotebookByName(actualText)
            val notebookName = input.text.toString()
            val order = notebook.order
            val createdAt = notebook.createdAt
            val lastModified = notebook.lastModified
            val newNotebook = Notebook(
                notebookName,
                order,
                createdAt,
                lastModified
            )
            val sections: List<Note> = noteDao.loadNotesInOrder(notebook.name)
            val sectionIterator = sections.iterator()
            while (sectionIterator.hasNext()) {
                val section: Note = sectionIterator.next()
                val contents = section.contents
                val creationTime = section.creationTime
                val customOrder = section.customOrder
                val lastEdited = section.lastEdited
                val name = section.name
                val notebookName =notebookName
                val newSection = Note(
                    contents,
                    name,
                    creationTime,
                    lastEdited,
                    customOrder,
                    notebookName
                )
                noteDao.delete(section)
                noteDao.insert(newSection)
            }
            noteDao.deleteNotebook(notebook)
            noteDao.insertNotebook(newNotebook)
            mainActivity.prepareMenuData()
            notifyDataSetChanged()
            mainActivity.hideKeyboard()
            mainActivity.hideKeyboard()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            mainActivity.hideKeyboard()
            dialog.cancel()}
        builder.show()
    }

    private fun moveUpNotebook(actualText: String) {
        val sortBy = preferences.getString("sort_by", "custom_order")
        if (sortBy == "custom_order") {
            val notebook: Notebook = noteDao.findNotebookByName(actualText)
            val anotherNotebook = noteDao.loadPreviousNotebookInOrder(notebook.order)
            if (anotherNotebook != null) { // notebook exists with smaller order
                val newNotebook = Notebook(
                    notebook.name,
                    anotherNotebook.order,
                    notebook.createdAt,
                    notebook.lastModified
                )
                val newAnotherNotebook = Notebook(
                    anotherNotebook.name,
                    notebook.order,
                    anotherNotebook.createdAt,
                    anotherNotebook.lastModified
                )
                noteDao.deleteNotebook(notebook)
                noteDao.deleteNotebook(anotherNotebook)
                noteDao.insertNotebook(newNotebook)
                noteDao.insertNotebook(newAnotherNotebook)
                mainActivity.prepareMenuData()
                notifyDataSetChanged()
            }
        } else {
            openDialogCannotChangeOrder()
        }
    }

    private fun moveDownNotebook(actualText: String) {
        val sortBy = preferences.getString("sort_by", "custom_order")
        if (sortBy == "custom_order") {
            val notebook: Notebook = noteDao.findNotebookByName(actualText)
            val anotherNotebook = noteDao.loadNextNotebookInOrder(notebook.order)
            if (anotherNotebook != null) { // notebook exists with larger order
                val newNotebook = Notebook(
                    notebook.name,
                    anotherNotebook.order,
                    notebook.createdAt,
                    notebook.lastModified
                )
                val newAnotherNotebook = Notebook(
                    anotherNotebook.name,
                    notebook.order,
                    anotherNotebook.createdAt,
                    anotherNotebook.lastModified
                )
                noteDao.deleteNotebook(notebook)
                noteDao.deleteNotebook(anotherNotebook)
                noteDao.insertNotebook(newNotebook)
                noteDao.insertNotebook(newAnotherNotebook)
                mainActivity.prepareMenuData()
                notifyDataSetChanged()
            }
        } else {
            openDialogCannotChangeOrder()
        }
    }

    private fun deleteNotebook(actualText: String) {
        if (noteDao.notebookCount() == 1) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Notebook cannot be deleted because it is the last notebook left.")
            builder.setNegativeButton("OK") {
                    dialog, _ -> dialog.cancel()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        } else {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Do you wish to delete ".plus(actualText).plus("?\n" +
                    "This action cannot be undone."))
            builder.setCancelable(false)
            builder.setPositiveButton("Delete") {
                    _, _ ->
                val notebook: Notebook = noteDao.findNotebookByName(actualText)
                val sections: List<Note> = noteDao.loadNotesInOrder(notebook.name)
                val sectionIterator = sections.iterator()
                var sectionOpenInNotebook = false
                while (sectionIterator.hasNext()) {
                    val section: Note = sectionIterator.next()
                    if (section.name == mainActivity.openSection) {
                        sectionOpenInNotebook = true
                    }
                    noteDao.delete(section)
                }
                noteDao.deleteNotebook(notebook)
                mainActivity.prepareMenuData()
                notifyDataSetChanged()
                // Close section if it is open in the deleted notebook
                if (sectionOpenInNotebook) {
                    val notebooks: List<Notebook> = noteDao.loadNotebooksInOrder()
                    val notebookIterator = notebooks.iterator()
                    var breakAll = false
                    while (notebookIterator.hasNext() && !breakAll) {
                        val notebook: Notebook = notebookIterator.next()
                        val sections: List<Note> = noteDao.loadNotesInOrder(notebook.name)
                        val sectionIterator = sections.iterator()
                        while (sectionIterator.hasNext()) {
                            val section: Note = sectionIterator.next()
                            mainActivity.loadFromDatabase(section.name)
                            breakAll = true
                            break
                        }
                    }
                }
            }
            builder.setNegativeButton("Cancel") {
                    dialog, _ -> dialog.cancel()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

    private fun showNotebookOptions(view: View, actualText: String) {
        val popup = PopupMenu(context, view)
        popup.inflate(R.menu.notebook_drop_down)
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.rename -> {
                    renameNotebook(actualText)
                }
                R.id.move_up -> {
                    moveUpNotebook(actualText)
                }
                R.id.move_down -> {
                    moveDownNotebook(actualText)
                }
                R.id.delete -> {
                    deleteNotebook(actualText)
                }
            }
            true
        })
        popup.show()
    }

    private fun openDialogCannotChangeOrder() {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Change the order to custom order in order to change the order.")
        builder.setPositiveButton("Go to settings") {
                _, _ ->
            mainActivity.openSettings()
        }
        builder.setNegativeButton("Cancel") {
                dialog, _ -> dialog.cancel()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }
}