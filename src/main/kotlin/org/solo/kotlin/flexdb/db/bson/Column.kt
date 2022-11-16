package org.solo.kotlin.flexdb.db.bson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.solo.kotlin.flexdb.json.query.classes.JsonColumns
import org.solo.kotlin.flexdb.json.query.classes.deserializeJsonColumn

@JsonDeserialize(using = BsonDbColumnDeserializer::class)
data class DbColumn(val columns: JsonColumns)

class BsonDbColumnDeserializer : JsonDeserializer<DbColumn>() {
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): DbColumn {
        val node = parser.codec.readTree<JsonNode>(parser)!!
        val p = node["columns"]!!

        val columns = JsonColumns()
        deserializeJsonColumn(p, columns)

        return DbColumn(columns)
    }
}