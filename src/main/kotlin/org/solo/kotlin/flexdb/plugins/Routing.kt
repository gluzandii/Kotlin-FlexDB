package org.solo.kotlin.flexdb.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.solo.kotlin.flexdb.json.JsonUtil
import org.solo.kotlin.flexdb.json.query.JsonQueryTypes
import org.solo.kotlin.flexdb.json.query.JsonQueryUtil
import org.solo.kotlin.flexdb.json.query.classes.*


fun Application.configureRouting() {

    routing {
        get("/") {
            call.respond(mapOf("status" to "OK"))
        }

        authenticate("query") {
            post("/query") {
                // Structure of queries has been put in the "when" statement
                // It will be implemented at the end.

                try {
                    val body = call.receiveText()
                    val mapper = JsonUtil.newObjectMapper()

                    @Suppress("UNUSED_VARIABLE")
                    when (JsonQueryUtil.getQueryType(body)) {
                        JsonQueryTypes.CREATE -> {
                            val query = mapper.readValue(body, JsonCreateQuery::class.java)!!
                        }

                        JsonQueryTypes.SELECT -> {
                            val query = mapper.readValue(body, JsonSelectQuery::class.java)!!
                        }

                        JsonQueryTypes.UPDATE -> {
                            val query = mapper.readValue(body, JsonUpdateQuery::class.java)!!
                        }

                        JsonQueryTypes.DELETE -> {
                            val query = mapper.readValue(body, JsonDeleteQuery::class.java)!!
                        }

                        JsonQueryTypes.RESET -> {
                            val query = mapper.readValue(body, JsonResetQuery::class.java)!!
                        }
                    }
                } catch (ex: Exception) {
                    System.err.println("An error occurred:")
                    ex.printStackTrace()
                }
            }
        }
    }
}
