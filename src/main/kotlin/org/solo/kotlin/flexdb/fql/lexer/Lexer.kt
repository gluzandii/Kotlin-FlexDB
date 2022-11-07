package org.solo.kotlin.flexdb.fql.lexer

class Lexer(private var input: String) {
    private val statements: Set<String>

    init {
        if (input.isEmpty()) {
            error("The input provided is empty.")
        }

        statements = input.split(statementPattern)
            .map {
                return@map it.trim()
            }.toSet()
    }

    companion object {
        @JvmStatic
        val statementPattern = Regex("(;|;[\r\n])")
    }
}