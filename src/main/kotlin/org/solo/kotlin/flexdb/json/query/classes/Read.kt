package org.solo.kotlin.flexdb.json.query.classes

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

data class JsonSelectQuery(var tableName: String, var action: String, var payload: JsonSelectPayload) {
    constructor() : this("", "", JsonSelectPayload())
}

/**
 * Stores the columns and condition for [JsonSelectQuery]
 */
data class JsonSelectPayload(var columns: Set<String>, var condition: String?) {
    constructor() : this(setOf(), null)
}
