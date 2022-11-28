package org.solo.kotlin.flexdb.db.json

data class DbConfigSchemafullAndSchemaless(var engine: String)

data class DbConfig(
    var schemafull: DbConfigSchemafullAndSchemaless,
    var schemaless: DbConfigSchemafullAndSchemaless
)
