package org.solo.kotlin.flexdb

import org.solo.kotlin.flexdb.db.DB
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object GlobalData {
    @JvmStatic
    var db: DB? = null
        @Synchronized get
        @Synchronized set

    @JvmStatic
    var pswd: String? = null
        @Synchronized get
        @Synchronized set

    @JvmStatic
    val cachedExecutor: ExecutorService = Executors.newCachedThreadPool()
}