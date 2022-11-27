package org.solo.kotlin.flexdb.json.query.classes

/**
 * Class representation of:
 * ```
 * {
 *      "table": "users",
 *      "action": "read",
 *      "payload": {
 *          "columns": [
 *              "name",
 *              "email"
 *          ],
 *          "condition": "name == 'sn' && email == 'ns27@gmail.com'"
 *      }
 * }
 * ```
 */
@Suppress("unused")
data class JsonReset(var tableName: String, var action: String, var payload: JsonResetPayload) {
    constructor() : this("", "", JsonResetPayload())
}

/**
 * Stores condition for [JsonReset]
 */
data class JsonResetPayload(var condition: String) {
    constructor() : this("")
}
