package org.solo.kotlin.flexdb.json.query.classes

import org.solo.kotlin.flexdb.internal.JsonCreatePayload

/**
 * Class representation of:
 * ```
 * "payload": {
 *      "name": {
 *          "type": "String",
 *          "constraints": [
 *              "NotNull",
 *              "Immutable"
 *          ]
 *      },
 *      "email": {
 *          "type": "Email",
 *          "constraints": [
 *              "Unique"
 *          ]
 *      },
 *      "age": {
 *          "type": "Number",
 *          "constraints": [
 *              "NotNull"
 *          ]
 *      }
 * }
 * ```
 */

data class JsonQueryColumn(var type: String, var constraints: Set<String>) {
    constructor() : this("", setOf())
}

/**
 * The query that is sent to the server for creating a table.
 *
 * It contains a payload like:
 * ```
 * "payload": {
 *      "name": {
 *          "type": "String",
 *          "constraints": [
 *              "NotNull",
 *              "Immutable"
 *          ]
 *      },
 *      "email": {
 *          "type": "Email",
 *          "constraints": [
 *              "Unique"
 *          ]
 *      },
 *      "age": {
 *          "type": "Number",
 *          "constraints": [
 *              "NotNull"
 *          ]
 *      }
 * }
 * ```
 *
 * along with table and action.
 */
@Suppress("unused")
data class JsonCreateQuery(var table: String, var action: String, var payload: JsonCreatePayload) {
    constructor() : this("", "", JsonCreatePayload())
}
