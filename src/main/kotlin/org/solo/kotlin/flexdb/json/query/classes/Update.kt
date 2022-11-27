package org.solo.kotlin.flexdb.json.query.classes

/**
 * Class representation of:
 * ```
 * {
 *      "table": "users",
 *      "action": "update",
 *      "payload": {
 *          "columns": {
 *              "email": "s.n@gmail.com"
 *          },
 *          "condition": "name == 'sn'"
 *      }
 * }
 * ```
 */
@Suppress("unused")
data class JsonUpdate(var tableName: String, var action: String, var payload: JsonUpdatePayload) {
    constructor() : this("", "", JsonUpdatePayload())
}


/**
 * Stores columns and condition for [JsonUpdate]
 */
data class JsonUpdatePayload(var columns: Map<String, String>, var condition: String?) {
    constructor() : this(mapOf(), null)
}
