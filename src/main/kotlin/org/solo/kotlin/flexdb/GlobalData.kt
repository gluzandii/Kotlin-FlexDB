package org.solo.kotlin.flexdb

import org.solo.kotlin.flexdb.db.DB

object GlobalData {
    @JvmStatic
    var db: DB? = null

    @JvmStatic
    var pswd: String? = null
}