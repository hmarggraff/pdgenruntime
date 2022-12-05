package org.pdgen.datasources.java

import org.joda.time.DateTime
import org.pdgen.data.*
import java.awt.Image
import java.io.InputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URI
import java.sql.Time
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.swing.Icon
import kotlin.reflect.KClass

class BuildJavaInternals(val internals: HashMap<String, JoriaType>) {

    val objectType: JavaClass
    init {
        putPrimitive(Boolean::class, DefaultBooleanLiteral.instance())
        putPrimitive(java.lang.Boolean::class, DefaultBooleanLiteral.instance())
        putPrimitive(Byte::class, DefaultIntLiteral.instance())
        putPrimitive(java.lang.Byte::class, DefaultIntLiteral.instance())
        putPrimitive(Char::class, DefaultCharacterLiteral.instance())
        putPrimitive(java.lang.Character::class, DefaultCharacterLiteral.instance())
        putPrimitive(Short::class, DefaultIntLiteral.instance())
        putPrimitive(java.lang.Short::class, DefaultIntLiteral.instance())
        putPrimitive(Int::class, DefaultIntLiteral.instance())
        putPrimitive(java.lang.Integer::class, DefaultIntLiteral.instance())
        putPrimitive(Long::class, DefaultIntLiteral.instance())
        putPrimitive(java.lang.Long::class, DefaultIntLiteral.instance())
        putPrimitive(Float::class, DefaultRealLiteral.instance())
        putPrimitive(java.lang.Float::class, DefaultRealLiteral.instance())
        putPrimitive(Double::class, DefaultRealLiteral.instance())
        putPrimitive(java.lang.Double::class, DefaultRealLiteral.instance())

        putClass(String::class, DefaultStringLiteral.instance())
        putClass(Class::class, JavaClassAsLiteral.instance())
        objectType = buildObjectClass()

        putClass(java.util.Date::class, JoriaDateTime.instance())
        putClass(LocalDate::class, JoriaDateTime.instance())
        putClass(LocalDateTime::class, JoriaDateTime.instance())
        putClass(DateTime::class, JoriaDateTime.instance())
        putClass(org.joda.time.LocalDate::class, JoriaDateTime.instance())
        putClass(Calendar::class, JoriaDateTime.instance())
        putClass(java.sql.Date::class, JoriaSqlDate.instance())
        putClass(Time::class, JoriaSqlTime.instance())
        putClass(Timestamp::class, JoriaDateTime.instance())
        putClass(Image::class, DefaultImageLiteral.instance())
        putClass(Icon::class, DefaultImageLiteral.instance())
        putClass(BigInteger::class, DefaultIntLiteral.instance())
        putClass(BigDecimal::class, DefaultRealLiteral.instance())

        putClass(Array<String>::class, LiteralCollectionClass(DefaultStringLiteral.instance()))
        val numberType = buildNumberClass()
        putClass(Number::class, numberType)
        putClass(Map.Entry::class, buildMapeEntryClass())
        putClass(URI::class, buildUriClass())
        putClass(InputStream::class, buildStreamClass())
        registerDerivedClassesFromObject(numberType)
    }

    fun putPrimitive(kc: KClass<*>, jc: JoriaType) {
        val name = kc.java.name
        internals[name] = jc
    }
    fun putClass(kc: KClass<*>, jc: JoriaType) {
        val name = kc.java.name
        internals[name] = jc }

    private fun buildNumberClass(): JavaClass {
        val numberClass = Number::class.java
        val numberType = JavaClass(numberClass)
        numberType.setInternal(true)
        numberType.setName("Number")
        try {
            val members = arrayOf(
                JavaMethod(numberType, numberClass.getMethod("longValue"), DefaultIntLiteral.instance()),
                JavaMethod(numberType, numberClass.getMethod("doubleValue"), DefaultRealLiteral.instance())
            )
            numberType.setMembers(members)
        } catch (ex: NoSuchMethodException) {
            throw JoriaAssertionError("init java.lang.Long method not found")
        }
        return numberType
    }

    private fun buildObjectClass(): JavaClass {
        val objectClass = Object::class.java
        val objectType = JavaClass(objectClass)
        objectType.setInternal(true)
        objectType.setName("Object")
        try {
            val members = arrayOf(
                JavaMethod(objectType, objectClass.getMethod("toString"), DefaultStringLiteral.instance()),
                JavaMethod(objectType, objectClass.getMethod("getClass"), JavaClassAsLiteral.instance())
            )
            objectType.setMembers(members)
        } catch (ex: NoSuchMethodException) {
            throw JoriaAssertionError("init java.lang.Object method not found")
        }
        return objectType
    }

    private fun buildStreamClass(): JavaClass {
        val streamClass = InputStream::class.java
        val streamType = JavaClass(streamClass)
        streamType.setInternal(true)
        streamType.setName("java.io.InputStream")
        try {
            val members = arrayOf(
                JavaMethod(streamType, streamClass.getMethod("toString"), DefaultStringLiteral.instance()),
                JavaAttachedMethod(
                    streamType,
                    JavaInternalExtensionMethods::streamIntoImage,
                    DefaultImageLiteral.instance(),
                    "streamToImage"
                )
            )

            streamType.setMembers(members)
        } catch (ex: NoSuchMethodException) {
            throw JoriaAssertionError("init java.lang.Object method not found")
        }
        return streamType
    }

    private fun buildUriClass(): JavaClass {
        val klass = URI::class.java
        val type = JavaClass(klass)
        type.setInternal(true)
        type.setName("java.net.URI")
        try {
            val members = arrayOf(
                JavaMethod(type, klass.getMethod("getFragment"), DefaultStringLiteral.instance()),
                JavaMethod(type, klass.getMethod("getHost"), DefaultStringLiteral.instance()),
                JavaMethod(type, klass.getMethod("getPath"), DefaultStringLiteral.instance()),
                JavaMethod(type, klass.getMethod("getPort"), DefaultStringLiteral.instance()),
                JavaMethod(type, klass.getMethod("getScheme"), DefaultStringLiteral.instance()),
                JavaMethod(type, klass.getMethod("getScheme"), DefaultStringLiteral.instance()),
                JavaMethod(type, klass.getMethod("toString"), DefaultStringLiteral.instance()),
                JavaMethod(type, klass.getMethod("getQuery"), DefaultStringLiteral.instance())
            )
            type.setMembers(members)
        } catch (ex: NoSuchMethodException) {
            throw JoriaAssertionError("init java.lang.Object method not found")
        }
        return type
    }

    fun buildMapeEntryClass(): JavaClass {
        val mapEntryClass = Map.Entry::class.java
        val mapEntryType = JavaClass(mapEntryClass)
        mapEntryType.setInternal(true)
        mapEntryType.setName("Map.Entry")
        try {
            val members = arrayOf(
                JavaMethod(mapEntryType, mapEntryClass.getMethod("getKey"), objectType),
                JavaMethod(mapEntryType, mapEntryClass.getMethod("getValue"), objectType)
            )
            mapEntryType.setMembers(members)
        } catch (ex: NoSuchMethodException) {
            throw JoriaAssertionError("init java.util.Map.Entry methods not found")
        }
        return mapEntryType
    }

    fun registerDerivedClassesFromObject(numberType: JavaClass) {
        var oder = objectType.derivedClasses
        if (oder == null) {
            oder = ArrayList()
            objectType.derivedClasses = oder
        }
        oder.add(numberType)
    }

}