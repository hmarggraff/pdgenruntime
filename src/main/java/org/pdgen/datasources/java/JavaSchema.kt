package org.pdgen.datasources.java

import org.pdgen.data.*
import org.pdgen.env.Env

class JavaSchema(val jarFileName: String, val types: HashMap<String, JoriaType>, val testDataRoots: List<TestDataRootDef>, val rootsVector: SortedNamedVector<JoriaAccess>) : JoriaSchema {
    val reflection: ReflectionDelegate = StandardReflection()
    val objectType: JavaClass  = types["Object"] as JavaClass
    val objectArrayType: JoriaCollection = types["Array<Object>"] as JoriaCollection

    init {
        Env.schemaInstance = this
    }

    override fun findClass(longName: String): JoriaClass? {
        val joriaType = types[longName]
        if (joriaType is JoriaClass) return joriaType
        else return null
    }

    override fun getRoots(): SortedNamedVector<JoriaAccess> = rootsVector
    override fun getTypes(): MutableMap<String, JoriaType> = types

    fun getLiteralCollectionFor(literalType: JoriaType?): LiteralCollectionClass {
        val name = LiteralCollectionClass.buildName(literalType)
        val that = types[name]
        if (that == null) {
            val ret = LiteralCollectionClass(literalType)
            types[name] = ret
            return ret
        }

        if (that is LiteralCollectionClass) return that
        throw Error("Name clash LiteralCollectionClass $name and ${that.javaClass.name}")
    }

    fun findWrapper(forType: JoriaCollection): CollectionWrapperClass {
        val internalType = types[forType.elementType.name]
        if (internalType is CollectionWrapperClass) return internalType
        throw Error("Name clash")
    }

    fun getReflectionDelegate() = reflection

    fun findClassOrType(c: Class<*>): JoriaType {
        val type = c.typeName
        val seenClass = types[type]
        if (seenClass != null)
            return seenClass
        val internal = types[type]
        if (internal != null)
            return internal
        return objectType
    }

    override fun getSchemaForSave() = JavaSavedSchema(this)
}