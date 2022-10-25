package org.solo.kotlin.flexdb

class MismatchedSchemaException(msg: String) : Exception(msg)
class NullUsedInNonNullColumnException(msg: String) : Exception(msg)
class MismatchedTypeException(msg: String) : Exception(msg)
class InvalidColumnProvidedException(msg: String) : Exception(msg)
