package org.solo.kotlin.flexdb.json.query.classes

/**
 * Class representation of:
 * ```
 * {
 *   "table": "users",
 *   "action": "delete",
 *   "payload": {
 *      "condition": "name MATCHES 's.*'"
 *    }
 * }
 * ```
 */
@Suppress("unused")
data class JsonDelete(var tableName: String, var action: String, var payload: JsonDeletePayload) {
    constructor() : this("", "", JsonDeletePayload())
}

/**
 * ```
 * {
 *   "table": "users",
 *   "action": "delete",
 *   "payload": {
 *      "condition": "name MATCHES 'Sushant.*'"
 *    }
 * }
 * ```
 * Stores condition in above `JSON` for [JsonDelete]
 */
data class JsonDeletePayload(var condition: String) {
    constructor() : this("")
}
