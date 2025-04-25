package org.pdgen.datasources.java

import org.pdgen.data.JoriaSchema
import org.pdgen.data.SavedSchema
import org.pdgen.util.Log
import java.io.File
import kotlin.io.path.Path

class JavaSavedSchema(base: JavaSchema, absolutePath: String) : SavedSchema {
    companion object { @JvmStatic private val serialVersionUID = 7L }
    var jarFileName: String = relativePath(absolutePath, base.jarFileName)
    val roots: List<Pair<String, String>> = base.roots.data.map { Pair<String, String>(it.type.name, it.name) }
    val testDataRoots: List<TestDataRootDef> = base.testDataRoots

    override fun buildSchema(forDesigner: Boolean, templateFilePath: String): JoriaSchema {
        val envLocation = System.getenv()["PDGADAPTERJAR"]
        if (envLocation != null )
        {
            Log.ini.info("PDGADAPTERJAR=$envLocation")
            jarFileName = envLocation
        }
        else {
            Log.ini.info("adapterjar=$jarFileName")
            val jarFilePath = Path(jarFileName)
            if (!jarFilePath.isAbsolute) {
                val templatePath = Path(templateFilePath).toAbsolutePath().parent
                val f = File(templatePath.toFile(), jarFileName).absolutePath
                jarFileName = f
            }
        }
        val cb = if (forDesigner) JavaClassBuilder(jarFileName) else JavaClassBuilder(jarFileName, roots, testDataRoots)
        return cb.javaSchema()
    }
    fun relativePath(from:String, to: String): String {
        val fromPath = Path(from).parent
        val toPath = Path(to)
        val relativeTo = fromPath.relativize(toPath)
        return relativeTo.toString();
    }
}