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

data class JsonResetQuery(var tableName: String, var action: String, var payload: JsonResetPayload) {
    constructor() : this("", "", JsonResetPayload())
}

/**
 * Stores condition for [JsonResetQuery]
 */
data class JsonResetPayload(var condition: String) {
    constructor() : this("")
}
