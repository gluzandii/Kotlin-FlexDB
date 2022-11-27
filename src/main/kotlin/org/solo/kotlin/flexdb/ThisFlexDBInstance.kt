package org.solo.kotlin.flexdb

import org.solo.kotlin.flexdb.db.DB

/**
 * Stores data relating to this instance of FlexDB.
 */
object ThisFlexDBInstance {
    /**
     * Stores the [DB] of this FlexDB instance.
     */
    @JvmStatic
    var thisInstanceDb: DB? = null
        @Synchronized get
        @Synchronized set
}