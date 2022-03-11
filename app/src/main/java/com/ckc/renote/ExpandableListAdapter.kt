package com.ckc.renote

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
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
            //shape.setColor(ContextCompat.getColor(context, R.color.design_default_color_on_secondary))
            //shape.setCornerRadius(32F)
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
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Do you wish to delete ".plus(actualText).plus("?\n" +
                        "This action cannot be undone."))
                builder.setCancelable(false)
                builder.setPositiveButton("Delete") {
                        _, _ ->
                    //val child: List<String> =
                    //    laptopCollections.get(laptops.get(groupPosition))
                    //child.remove(childPosition)
                    val section: Note = noteDao.findByName(actualText)
                    noteDao.delete(section)
                    mainActivity.prepareMenuData()
                    notifyDataSetChanged()
                }
                builder.setNegativeButton("Cancel") {
                        dialog, _ -> dialog.cancel()
                }
                val alertDialog = builder.create()
                alertDialog.show()
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
        /*
        if (headerTitle == "+ new notebook") {
            newView = convertView ?: layoutInflater.inflate(R.layout.list_group_header_new_notebook, null)
        } else {
            newView = convertView ?: layoutInflater.inflate(R.layout.list_group_header, null)
        }**/
        val lblListHeader = newView.findViewById<TextView>(R.id.lblListHeader)
        lblListHeader.setTypeface(null, Typeface.BOLD)
        lblListHeader.text = headerTitle

        val optionsButton = newView.findViewById<ImageView>(R.id.options_button)
        optionsButton.setOnClickListener {
            val actualText: String = lblListHeader.text.toString()
            if (actualText != "+ new notebook") {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Do you wish to delete ".plus(actualText).plus("?\n" +
                        "This action cannot be undone."))
                builder.setCancelable(false)
                builder.setPositiveButton("Delete") {
                        _, _ ->
                    val notebook: Notebook = noteDao.findNotebookByName(actualText)
                    val sections: List<Note> = noteDao.loadNotesInOrder(notebook.name)
                    val sectionIterator = sections.iterator()
                    while (sectionIterator.hasNext()) {
                        val section: Note = sectionIterator.next()
                        noteDao.delete(section)
                    }
                    noteDao.deleteNotebook(notebook)
                    mainActivity.prepareMenuData()
                    notifyDataSetChanged()
                }
                builder.setNegativeButton("Cancel") {
                        dialog, _ -> dialog.cancel()
                }
                val alertDialog = builder.create()
                alertDialog.show()
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

    private fun collapseAllNotebooks() {
        for (gp in 0 until groupCount) {
            expandableListView.collapseGroup(gp)
        }
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
            // Here you get get input text from the Edittext
            val notebookName = input.text.toString()
            val order = noteDao.getMaxNotebookOrder() + 1
            val newNotebook = Notebook(
                notebookName,
                order
            )
            noteDao.insertNotebook(newNotebook)
            mainActivity.prepareMenuData()
            notifyDataSetChanged()
        }
        builder.setNegativeButton("Cancel") {dialog, _ ->
            mainActivity.hideKeyboard()
            dialog.cancel()
        }
        builder.show()
    }

    fun createNewSection(groupPosition: Int) {
        Log.i("New section", "Checkpoint 0")
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Enter the section title:")

        // Set up the input
        val input = EditText(context)
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.hint = ""
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        Log.i("New section", "Checkpoint 2")

        // Set up the buttons
        builder.setPositiveButton("Create") { _, _ ->
            Log.i("New section", "Checkpoint 1")
            val groupView: View = getGroup(groupPosition).view
            val lblListHeader = groupView.findViewById<TextView>(R.id.lblListHeader)
            val notebookName: String = lblListHeader?.text.toString()

            val contents = ""
            val creationTime = System.currentTimeMillis()
            val customOrder = noteDao.getMaxCustomOrder() + 1
            val lastEdited = System.currentTimeMillis()
            val name = input.text.toString()
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
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            mainActivity.hideKeyboard()
            dialog.cancel()}
        builder.show()
        Log.i("New section", "Checkpoint 3")
    }


    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}