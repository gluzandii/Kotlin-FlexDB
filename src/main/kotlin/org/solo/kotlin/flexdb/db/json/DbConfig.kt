package org.solo.kotlin.flexdb.db.json

data class DbConfigEngineConfig(val name: String, val rowsPerFile: Int) {
    constructor() : this("SequentialSchemafullDbEngine", 1000)
}

@Suppress("unused")
data class DbConfigSchemafullAndSchemaless(var engine: DbConfigEngineConfig) {
    constructor() : this(DbConfigEngineConfig())
}

@Suppress("unused")
data class DbConfig(
    var schemafull: DbConfigSchemafullAndSchemaless,
    var schemaless: DbConfigSchemafullAndSchemaless
) {
    constructor() : this(DbConfigSchemafullAndSchemaless(), DbConfigSchemafullAndSchemaless())
}
