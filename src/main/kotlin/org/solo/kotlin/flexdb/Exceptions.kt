package org.solo.kotlin.flexdb

class MismatchedSchemaException(msg: String) : Exception(msg)
class NullUsedInNonNullColumnException(msg: String) : Exception(msg)
class MismatchedTypeException(msg: String) : Exception(msg)
class InvalidColumnProvidedException(msg: String) : Exception(msg)
class DuplicatesInUniqueColumnException(msg: String) : Exception(msg)
class InvalidRowException(msg: String) : Exception(msg)
class InvalidPasswordProvidedException(msg: String) : Exception(msg)
class TableAlreadyExistsException(msg: String) : Exception(msg)