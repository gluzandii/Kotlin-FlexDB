package org.solo.kotlin.flexdb.json.query.classes

import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.internal.JsonCreatePayload

/**
 * Class representation of:
 * ```
 * {
 *    "table": "users",
 *    "action": "read",
 *    "payload": {
 *        "columns": [
 *            "name",
 *            "email"
 *        ],
 *      "condition": "name == 'sn' && email == 'ns27@gmail.com'"
 *    }
 * }
 * ```
 */
@Suppress("unused")
data class JsonSelectQuery(
    var table: String,
    var action: String,
    var sorting: JsonSelectSort,
    var payload: JsonSelectPayload,
) {
    constructor() : this("", "", JsonSelectSort(), JsonSelectPayload())
}

data class JsonSelectSort(var column: String, var sortingType: String) {
    constructor() : this("", "")

    fun toEnum(): SortingType {
        return when (sortingType) {
            "ASCENDING" -> SortingType.ASCENDING
            "DESCENDING" -> SortingType.DESCENDING
            else -> SortingType.NONE
        }
    }
}

/**
 * Stores the columns and condition for [JsonSelectQuery]
 */
data class JsonSelectPayload(var columns: Set<String>, var condition: String?) {
    constructor() : this(setOf(), null)

    fun toJsonCreatePayload(): JsonCreatePayload {
        return JsonCreatePayload()
    }
}
