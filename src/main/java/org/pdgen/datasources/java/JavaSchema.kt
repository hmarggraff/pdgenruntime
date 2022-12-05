package org.pdgen.datasources.java

import org.pdgen.data.*
import org.pdgen.env.Env

class JavaSchema(val jarFileName: String) : JoriaSchema {
    val types: HashMap<String, JoriaType>
    val rootsVector: SortedNamedVector<JoriaAccess>
    var reflection: ReflectionDelegate = StandardReflection()
    val objectType: JavaClass
    val objectArrayType: JoriaCollection
    val theClasses = ArrayList<JoriaClass>()


    init {
        Env.schemaInstance = this

        val cb = JavaClassBuilder2(jarFileName)

        objectType = cb.objectType
        objectArrayType = cb.objectArrayType
        types = cb.types
        //cb.collectionWrapperClasses.forEach { types[it.value.name] = it.value }
        types.forEach { if (it is JoriaClass) theClasses.add(it) }

        rootsVector = SortedNamedVector(cb.roots)
    }

    override fun getClasses() = theClasses

    override fun findClass(longName: String?): JoriaClass? {
        val joriaType = types[longName]
        if (joriaType is JoriaClass) return joriaType
        else return null
    }

    override fun getRoots(): SortedNamedVector<JoriaAccess> = rootsVector

    override fun reset() {
        TODO("Not yet implemented")
    }

    override fun getDatasourceName(): String = "Pojo"

    override fun findInternalType(name: String): JoriaType? = types[name]

    override fun getAllClassNames(): MutableList<String> {
        TODO("Not yet implemented")
    }

    fun getLiteralCollectionFor(literalType: JoriaType?): LiteralCollectionClass {
        val name = LiteralCollectionClass.buildName(literalType)
        val that = findInternalType(name)
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

    fun writeReplace(): Any {
        return JavaSerializableSchema(jarFileName)
    }


}