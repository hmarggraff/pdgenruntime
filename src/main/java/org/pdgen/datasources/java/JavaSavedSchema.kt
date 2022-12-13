package org.pdgen.datasources.java

import org.pdgen.data.JoriaSchema
import org.pdgen.data.SavedSchema
import java.io.IOException
import java.io.ObjectInputStream
import java.io.Serializable


class JavaSavedSchema(base: JavaSchema) : SavedSchema {
    companion object { @JvmStatic private val serialVersionUID = 7L }
    var jarFileName: String = base.jarFileName
    val roots: List<Pair<String, String>> = base.roots.data.map { Pair<String, String>(it.type.name, it.name) }
    val testDataRoots: List<TestDataRootDef> = base.testDataRoots

    override fun buildSchema(forDesigner: Boolean): JoriaSchema {
        val cb = JavaClassBuilder(jarFileName, roots, testDataRoots, forDesigner)
        return cb.javaSchema()
    }
}