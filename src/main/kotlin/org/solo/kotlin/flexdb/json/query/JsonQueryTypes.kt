package org.solo.kotlin.flexdb.json.query

/**
 * All the types of queries and operations that can be performed
 * by FlexDB, from an outside query.
 */
enum class JsonQueryTypes {
    CREATE,
    SELECT,
    UPDATE,
    DELETE,
    RESET
}