package com.ckc.renote

import android.view.View

class MenuModel(
    var menuName: String,
    var isGroup: Boolean,
    var hasChildren: Boolean,
    var url: String,
    var view: View
)