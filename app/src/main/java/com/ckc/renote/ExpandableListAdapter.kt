package com.ckc.renote

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager


open class ExpandableListAdapter(
    private val context: Context,
    private val listDataHeader: List<MenuModel>,
    private val listDataChild: HashMap<MenuModel, List<MenuModel>?>

) : BaseExpandableListAdapter() {

    private lateinit var expandableListView: ExpandableListView
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun initiateExpandableListView(view: ExpandableListView) {
        Log.i("initiateExpandableListView", "expandableListView IS BEING initialized")
        expandableListView = view
    }

    fun updateGUI() {
        Log.w("updateGUI", "Update GUI called")
        var newNotebookBackground = ContextCompat.getDrawable(context, R.drawable.new_notebook_button)
        var newSectionBackground = ContextCompat.getDrawable(context, R.drawable.new_section_button)

        for (groupPosition in 0 until groupCount) {
            //Log.w("myApp", "Group id: ".plus(groupPosition))
            val childrenCount = getChildrenCount(groupPosition)
            for (childPosition in 0 until childrenCount) {
                newSectionBackground = ContextCompat.getDrawable(context, R.drawable.new_section_button)
                //shape.setColor(ContextCompat.getColor(context, R.color.purple_200))
                //shape.setCornerRadius(32F)
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
                txtListChild?.setBackground(newSectionBackground)
            }
            newNotebookBackground = ContextCompat.getDrawable(context, R.drawable.new_notebook_button)
            //shape.setColor(ContextCompat.getColor(context, R.color.design_default_color_on_secondary))
            //shape.setCornerRadius(32F)
            val groupView: View? = getGroup(groupPosition)?.view
            val optionsButton = groupView?.findViewById<ImageView>(R.id.options_button)
            val lblListHeader = groupView?.findViewById<TextView>(R.id.lblListHeader)
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
            lblListHeader?.setBackground(newNotebookBackground)
        }
    }

    override fun getChild(groupPosition: Int, childPosition: Int): MenuModel? {
        return listDataChild[listDataHeader[groupPosition]]?.get(childPosition)
    }

    override fun getChildId(groupPosition: Int, childPosition: Int) = childPosition.toLong()

    override fun getChildView(
        groupPosition: Int, childPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup
    ): View {
        val childText = getChild(groupPosition, childPosition)?.menuName
        val infalInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val newView: View = convertView ?: infalInflater.inflate(R.layout.list_group_child, null)

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
                        dialog, id ->
                    //val child: List<String> =
                    //    laptopCollections.get(laptops.get(groupPosition))
                    //child.remove(childPosition)
                    notifyDataSetChanged()
                }
                builder.setNegativeButton("Cancel") {
                        dialog, id -> dialog.cancel()
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

    override fun getGroupView(
        groupPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup
    ): View {
        val headerTitle = getGroup(groupPosition).menuName
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val newView: View = convertView ?: layoutInflater.inflate(R.layout.list_group_header, null)
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
                        dialog, id ->
                    //val child: List<String> =
                    //    laptopCollections.get(laptops.get(groupPosition))
                    //child.remove(childPosition)
                    notifyDataSetChanged()
                }
                builder.setNegativeButton("Cancel") {
                        dialog, id -> dialog.cancel()
                }
                val alertDialog = builder.create()
                alertDialog.show()
            }
        }
        getGroup(groupPosition)?.view = newView
        updateGUI()
        return newView
    }

    override fun hasStableIds() = false

    override fun isChildSelectable(groupPosition: Int, childPosition: Int) = true

    override fun onGroupCollapsed (groupPosition: Int) {
        super.onGroupCollapsed(groupPosition)
        val groupView: View? = getGroup(groupPosition)?.view
        val lblListHeader = groupView?.findViewById<TextView>(R.id.lblListHeader)
        val actualText: String = lblListHeader?.text.toString()
        if (actualText == "+ new notebook") {
            createNewNotebook()
        }
    }

    override fun onGroupExpanded (groupPosition: Int) {
        val collapseOtherGroupsWhenGroupExpands = preferences.getBoolean("collapse", false)

        if (collapseOtherGroupsWhenGroupExpands) {
            for (gp in 0 until groupCount) {
                if (gp != groupPosition && expandableListView.isGroupExpanded(gp)) {
                    expandableListView.collapseGroup(gp) // collapse all other groups when a group gets expanded
                }
            }
        }
        super.onGroupExpanded(groupPosition)
        val groupView: View? = getGroup(groupPosition)?.view
        val lblListHeader = groupView?.findViewById<TextView>(R.id.lblListHeader)
        val actualText: String = lblListHeader?.text.toString()
        if (actualText == "+ new notebook") {
            createNewNotebook()
        }
    }

    private fun textInputDialog(message: String): String {
        var inputText = ""
        val builder = AlertDialog.Builder(context)
        builder.setTitle(message)
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)
        builder.setPositiveButton(
            "Create"
        ) { _, _ -> inputText = input.text.toString() }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, _ -> dialog.cancel() }
        builder.show()
        return inputText
    }


    fun createNewSection() {
        var sectionName = textInputDialog("Enter the section title:")
    }

    private fun createNewNotebook() {
        var notebookName = textInputDialog("Enter the notebook title:")
    }

}