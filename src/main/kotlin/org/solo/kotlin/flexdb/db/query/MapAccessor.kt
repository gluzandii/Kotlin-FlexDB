package org.solo.kotlin.flexdb.db.query

import org.springframework.asm.MethodVisitor
import org.springframework.asm.Opcodes
import org.springframework.expression.AccessException
import org.springframework.expression.EvaluationContext
import org.springframework.expression.TypedValue
import org.springframework.expression.spel.CodeFlow
import org.springframework.expression.spel.CompilablePropertyAccessor
import org.springframework.lang.Nullable
import org.springframework.util.Assert

/**
 * MapAccessor ported from spring.context.
 */
class MapAccessor : CompilablePropertyAccessor {
    override fun getSpecificTargetClasses(): Array<Class<*>> {
        return arrayOf(MutableMap::class.java)
    }

    @Throws(AccessException::class)
    override fun canRead(context: EvaluationContext, @Nullable target: Any?, name: String): Boolean {
        return target is Map<*, *> && target.containsKey(name)
    }

    @Throws(AccessException::class)
    override fun read(context: EvaluationContext, @Nullable target: Any?, name: String): TypedValue {
        Assert.state(target is Map<*, *>, "Target must be of type Map")
        val map = target as Map<*, *>?
        val value = map!![name]
        if (value == null && !map.containsKey(name)) {
            throw MapAccessException(name)
        }
        return TypedValue(value)
    }

    @Throws(AccessException::class)
    override fun canWrite(context: EvaluationContext, @Nullable target: Any?, name: String): Boolean {
        return true
    }

    @Throws(AccessException::class)
    override fun write(context: EvaluationContext, @Nullable target: Any?, name: String, @Nullable newValue: Any?) {
        Assert.state(target is Map<*, *>, "Target must be a Map")
        val map = target as MutableMap<Any, Any?>
        map[name] = newValue
    }

    override fun isCompilable(): Boolean {
        return true
    }

    override fun getPropertyType(): Class<*> {
        return Any::class.java
    }

    override fun generateCode(propertyName: String, mv: MethodVisitor, cf: CodeFlow) {
        val descriptor = cf.lastDescriptor()
        if (descriptor == null || descriptor != "Ljava/util/Map") {
            if (descriptor == null) {
                cf.loadTarget(mv)
            }
            CodeFlow.insertCheckCast(mv, "Ljava/util/Map")
        }
        mv.visitLdcInsn(propertyName)
        mv.visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            "java/util/Map",
            "get",
            "(Ljava/lang/Object;)Ljava/lang/Object;",
            true
        )
    }

    /**
     * Exception thrown from `read` in order to reset a cached
     * PropertyAccessor, allowing other accessors to have a try.
     */
    private class MapAccessException(private val key: String) : AccessException("") {

        override val message: String
            get() = "Map does not contain a value for key '$key'"
    }
}