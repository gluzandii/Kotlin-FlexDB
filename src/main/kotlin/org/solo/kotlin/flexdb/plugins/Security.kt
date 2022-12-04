package org.solo.kotlin.flexdb.plugins

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.solo.kotlin.flexdb.db
import org.solo.kotlin.flexdb.db.query.SortingType
import org.solo.kotlin.flexdb.db.structure.schemafull.primitive.Row
import org.solo.kotlin.flexdb.json.JsonUtil
import org.solo.kotlin.flexdb.json.query.JsonQueryTypes
import org.solo.kotlin.flexdb.json.query.JsonQueryUtil
import org.solo.kotlin.flexdb.json.query.classes.*

suspend inline fun handleError(ex: Throwable, call: ApplicationCall) {
    val msg: String

    when (ex) {
        is JsonParseException -> {
            msg = "Couldn't parse query: ${ex.message}"
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = mapOf(
                    "status" to "could not parse query",
                    "message" to ex.message,
                    "stack trace" to ex.stackTraceToString()
                )
            )
        }

        is MismatchedInputException -> {
            msg = "Invalid query: ${ex.message}"
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = mapOf(
                    "status" to "bad query",
                    "message" to ex.message,
                    "stack trace" to ex.stackTraceToString()
                )
            )
        }

        else -> {
            msg = "An error occurred: ${ex.message}"
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = mapOf(
                    "status" to "error",
                    "message" to ex.message,
                    "stack trace" to ex.stackTraceToString()
                )
            )
        }
    }

    System.err.println(msg)
    ex.printStackTrace()
}

fun Application.configureSecurity() {
    install(Authentication) {
        basic(name = "flexdb-query") {
            realm = "flexdb"
            validate { credentials ->
                if (credentials.name == "admin" && credentials.password == "admin") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }

    routing {
        authenticate("flexdb-query") {
            post("/query") {
                // Structure of queries has been put in the "when" statement
                // It will be implemented at the end.

                try {
                    val body = call.receiveText()
                    val mapper = JsonUtil.newObjectMapper()
                    val engine = db.schemafullEngine()

                    @Suppress("UNUSED_VARIABLE")
                    when (JsonQueryUtil.getQueryType(body)) {
                        JsonQueryTypes.CREATE -> {
                            val query = mapper.readValue(body, JsonCreateQuery::class.java)!!

                            engine.query(
                                command = query.action,
                                tableName = query.table,
                                where = null,
                                columns = query.payload,
                                sortingType = SortingType.NONE to null,
                            )

                            call.respond(HttpStatusCode.OK, "Table: ${query.table} created")
                            return@post
                        }

                        JsonQueryTypes.SELECT -> {
                            val query = mapper.readValue(body, JsonSelectQuery::class.java)!!

                            // Impl sorting in select query l8r
                            @Suppress("UNCHECKED_CAST") val list: List<Row> = engine.query(
                                command = query.action,
                                tableName = query.table,
                                where = query.payload.condition,
                                columns = query.payload.toJsonCreatePayload(),
                                sortingType = Pair(query.sorting.toEnum(), query.sorting.column),
                            ) as List<Row>

                            // Conert the list of rows to something that can be serialized
                            // and sent over the wire

                            return@post
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
                } catch (ex: Throwable) {
                    handleError(ex, call)
                }
            }
        }
    }
}
