package org.pdgen.datasources.java

import org.pdgen.data.*
import org.pdgen.util.Log
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import java.awt.Image
import javax.swing.Icon
import java.util.Enumeration

class JavaClassBuilder {
    val jarFileName: String
    val classLoader: ClassLoader
    val types = HashMap<String, JoriaType>()
    val roots = ArrayList<AbstractTypedJoriaAccess>()
    val collectionWrapperClasses = HashMap<JoriaClass, CollectionWrapperClass>()
    val objectType: JavaClass
    val objectArrayType: JoriaCollection
    val objectAsInterfaceBase: Array<JoriaClass>
    val testDataRoots: List<TestDataRootDef>;


    /**
     * designer mode: Loads jar for testing and declarations and searches for annotations
     */
    constructor(jarFileName: String) {
        this.jarFileName = jarFileName
        classLoader = URLClassLoader(arrayOf(File(jarFileName).toURI().toURL()))
        val annotationFinder = FindReportAnnotations(jarFileName)
        testDataRoots = annotationFinder.roots
        testDataRoots.forEach {
            addRoot(it.testDataProviderClass, it.testDataMethod)
        }
    }

    /**
     * runtime mode: expects domain classes in classpath and loads list of roots
     */
    constructor(jarFileName: String, roots: List<Pair<String, String>>, testDataRoots: List<TestDataRootDef>) {
        this.jarFileName = jarFileName
        this.testDataRoots = testDataRoots
        classLoader  = JavaClassBuilder::class.java.classLoader
        testDataRoots.forEach {
            addRoot(it.testDataProviderClass, it.testDataMethod)
        }
        roots.forEach { if (testDataRoots.firstOrNull { tdr -> tdr.testDataMethod == it.second } == null) Log.schema.warn("Root ${it.second} not found any more.")}

    }
    init {

        val internalBuilder = BuildJavaInternals(types)
        objectType = internalBuilder.objectType
        types[objectType.name] = objectType
        objectArrayType = collectionOf(Array<Any>::class.java, objectType)
        objectAsInterfaceBase = arrayOf(objectType)
    }


    fun addRoot(cn: String, mn: String) {
        val c = classLoader.loadClass(cn)
        // sequential serach, because parameters are yet unknown
        // therefore root methods must not be overloaded
        //val method = c.getMethod(mn)

        val declaredMethods = c.declaredMethods
        val m = declaredMethods.find { it.name.equals(mn) }
        if (m != null) // can be null if the root method was deleted. Must be handled by schema check.
        {
            addRoot(m)
        }
        else {
            Log.schema.warn("Test data method $mn not found in test data provider class $cn.")
        }
    }

    fun addRoot(m: Method) {
        if (!Modifier.isStatic(m.modifiers)) {
            Log.schema.warn("SchemaBuilder.addRoot needs static method: ${m.declaringClass}.${m.name} is not.")
            return
        }
        val retType = buildReturnType(m, null)
        if (retType == null)
            return
        if (retType is JoriaCollection) {
            var collectionWrapperClass: CollectionWrapperClass? = collectionWrapperClasses[retType.elementType]
            if (collectionWrapperClass == null) {
                collectionWrapperClass = CollectionWrapperClass()
                collectionWrapperClass.initElementAccess(JavaCollectionWrapperElements(collectionWrapperClass, retType))

                collectionWrapperClasses[retType.elementType] = collectionWrapperClass
                types[collectionWrapperClass.name] = collectionWrapperClass
            }
            val root = CollectionWrapperRoot(m, collectionWrapperClass)
            roots.add(root)
        } else

            roots.add(JavaObjectRoot(m, retType))
    }

    fun buildReturnType(m: Method, genericMap: java.util.HashMap<String, String>?): JoriaType? {
        val c = m.declaringClass
        val genericType = m.genericReturnType
        val type = m.returnType
        val actualType = genericMap?.get(type.name)
        Log.schema.debug("Method=${c.typeName}.${m.name}: ${genericType.typeName} collection: ${isCollection(type)} actualType: $actualType")
        return buildJoriaClassForMember(type, genericType)
    }

    fun buildFieldType(f: Field): JoriaType? {
        val c = f.declaringClass
        val genericType = f.genericType
        val type = f.type
        Log.schema.debug(
            "SchemaBuilder.build field=${c.typeName}.${f.name}: ${genericType.typeName} collection: ${
                isCollection(
                    type
                )
            }"
        )
        return buildJoriaClassForMember(type, genericType)
    }

    private fun buildJoriaClassForMember(type: Class<*>, genericType: Type): JoriaType? {
        val seenClass = types[type.name]
        if (seenClass != null)
            return seenClass

        if (Icon::class.java.isAssignableFrom(type) || Image::class.java.isAssignableFrom((type)))
            return DefaultImageLiteral.instance()

        val collection = isCollection(type)
        if (collection) {
            if (genericType is ParameterizedType) {
                if (genericType.actualTypeArguments.size > 1) {
                    Log.schema.warn("$genericType has more than one type Parameter. Cannot process")
                    return null
                }
                val typeName = genericType.actualTypeArguments[0].typeName
                Log.schema.debug("Type Parameter ${typeName}")
                var elementType = types[typeName]
                if (elementType == null) {
                    val elementClass: Class<*> = classLoader.loadClass(typeName)

                    elementType = types[elementClass.name]
                    if (elementType == null) {
                        val joriaElementClass = JavaClass(elementClass)
                        types.put(elementClass.name, joriaElementClass)
                        processMembersForClass(joriaElementClass, null)
                        return collectionOf(type, joriaElementClass)
                    }
                }
                if (elementType is JoriaClass) {
                    return collectionOf(type, elementType)
                } else {
                    val literalCollectionClass = LiteralCollectionClass(elementType)
                    return types[literalCollectionClass.name] ?: literalCollectionClass
                }
            } else if (type.isArray) {
                val elementJavaClass = type.componentType
                val elementJoriaClass = buildReachableClasses(elementJavaClass, genericType)
                return collectionOf(type, elementJoriaClass as JoriaClass)
            } else {
                return collectionOf(type, objectType)
            }
        } else {
            return buildReachableClasses(type, genericType)
        }

    }

    protected fun collectionOf(collectionClass: Class<*>, elementType: JoriaClass): JoriaCollection {
        val collectionName = JavaList.makeCollectionName(collectionClass, elementType)
        val ret = types[collectionName]
        if (ret is JoriaCollection)
            return ret
        if (ret != null)
            throw Error("Collection Name collision $collectionName")
        val newColl = JavaList(collectionClass, elementType)
        types[collectionName] = newColl
        return newColl
    }

    private fun buildReachableClasses(klass: Class<*>, genericType: Type): JoriaType {
        if (genericType is ParameterizedType) {
            val usedType = types[genericType.typeName]
            if (usedType != null)
                return usedType
            val actualTypeArguments = genericType.actualTypeArguments
            val typeParameters = klass.typeParameters
            val genericMap = HashMap<String, String>()
            if (typeParameters.size > 0) {
                for (i in 0..typeParameters.size - 1) {
                    genericMap[typeParameters[i].name] = actualTypeArguments[i].typeName
                }
            }
            val jc = JavaClass(klass, genericType.typeName)
            types[genericType.typeName] = jc
            processMembersForClass(jc, genericMap)
            return jc
        } else {
            val javaClass = types[klass.name]
            if (javaClass == null) {
                val jc = JavaClass(klass)
                types.put(klass.name, jc)
                processMembersForClass(jc, null)

                return jc
            } else
                return javaClass
        }
    }

    private fun processMembersForClass(jc: JavaClass, genericMap: java.util.HashMap<String, String>?) {
        val memberList = ArrayList<JoriaAccess>()
        jc.myClass.methods.forEach {
            if (it.parameterCount == 0 && it.returnType != Void.TYPE && Modifier.isPublic(it.modifiers) && !Modifier.isStatic(it.modifiers) && it.name != "hashCode") {
                try {
                    val buildReturnType = buildReturnType(it, genericMap)
                    if (buildReturnType != null)
                        memberList.add(JavaMethod(jc, it, buildReturnType))
                    else
                        Log.schema.error("failed to build return type for ${it}, skipping it")

                } catch (x: Throwable) {
                    Log.schema.error(x, "failed to build return type for ${it}, skipping it")
                }
            }
        }
        //jc.myClass.fields.forEach { memberList.add(JavaMethod(jc, it, buildReturnType(it))) }
        jc.members = memberList.toTypedArray()
    }

    private fun isCollection(c: Class<*>): Boolean =
        c.isArray ||
                java.lang.Iterable::class.java.isAssignableFrom(c) ||
                Iterator::class.java.isAssignableFrom(c) ||
                Enumeration::class.java.isAssignableFrom(c)

    fun javaSchema(): JavaSchema  = JavaSchema(jarFileName, types, testDataRoots, SortedNamedVector<JoriaAccess>(roots))
}