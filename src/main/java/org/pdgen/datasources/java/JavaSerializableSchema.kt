package org.pdgen.datasources.java

import java.io.Serializable

class JavaSerializableSchema(val jarFileName: String) : Serializable {

    fun readResolve(): Any {
        println("readResolve JavaSerializableSchema")
        return JavaSchema(jarFileName)
    }

}