package org.solo.kotlin.flexdb.db.query

import org.solo.kotlin.flexdb.db.query.clause.Where
import org.solo.kotlin.flexdb.db.structure.primitive.Column

data class Query(val table: String, val columns: Set<Column>, val wheres: Set<Where>)
