package com.ckc.renote

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.text.InputType
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
        expandableListView = view
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
        val collapseOtherGroupsWhenGroupExpands = preferences.getBoolean("collapse", false)

        if (collapseOtherGroupsWhenGroupExpands) { // collapse all other groups when a group gets expanded
            for (gp in 0 until groupCount) {
                if (gp != groupPosition && expandableListView.isGroupExpanded(gp)) {
                    expandableListView.collapseGroup(gp)
                }
            }
        }
        super.onGroupExpanded(groupPosition)
        val groupView: View = getGroup(groupPosition).view
        val lblListHeader = groupView.findViewById<TextView>(R.id.lblListHeader)
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
        notifyDataSetChanged()
    }

    private fun createNewNotebook() {
        var notebookName = textInputDialog("Enter the notebook title:")
        notifyDataSetChanged()
    }

}