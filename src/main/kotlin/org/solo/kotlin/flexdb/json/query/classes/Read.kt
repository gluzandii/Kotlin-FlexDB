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

data class JsonRead(var tableName: String, var action: String, var payload: JsonReadPayload) {
    constructor() : this("", "", JsonReadPayload())
}

/**
 * Stores the columns and condition for [JsonRead]
 */
data class JsonReadPayload(var columns: Set<String>, var condition: String?) {
    constructor() : this(setOf(), null)
}
