package org.pdgen.datasources.java

import java.io.Serializable

class JavaSerializableSchema(val jarFileName: String) : Serializable {

    fun readResolve(): Any {
        println("readResolve JavaSerializableSchema")
        val cb = JavaClassBuilder(jarFileName)
        return cb.javaSchema()
    }

}