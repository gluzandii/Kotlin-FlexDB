package org.solo.kotlin.flexdb.internal

import org.solo.kotlin.flexdb.db.structure.schemafull.Schema
import org.solo.kotlin.flexdb.db.structure.schemafull.primitive.Column
import org.solo.kotlin.flexdb.db.structure.schemafull.primitive.DbConstraint
import org.solo.kotlin.flexdb.db.types.DbValue
import org.solo.kotlin.flexdb.json.query.classes.JsonQueryColumn
import java.util.*

/**
 * A typealias that maps the column name to the actual [JsonQueryColumn].
 *
 * It represents the `payload` of a query.
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
 * Here 'name', 'email' and 'age' are the keys of the map.
 * The [JsonQueryColumn] is the value, basically the type and the constraints are the values, a.k.a [JsonQueryColumn]
 */
typealias JsonCreatePayload = HashMap<String, JsonQueryColumn>

/**
 * Converts the current JsonCreate payload, into normal [Column].
 * The [Column] is converted to a [java.util.HashSet] which is converted to a [Schema].
 */

fun JsonCreatePayload.toSchema(): Schema {
    val set = hashSetOf<Column>()

    for ((k, v) in this) {
        val type =
            DbValue.fromClassName(v.type)
        val consts = EnumSet.noneOf(DbConstraint::class.java)!!

        for (i in v.constraints) {
            consts.add(DbConstraint.valueOf(i.uppercase()))
        }

        set.add(Column(name = k, type, consts))
    }
    return Schema(set)
}