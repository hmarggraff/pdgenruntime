package org.pdgen.datasources.java

import org.objectweb.asm.*
import org.pdgen.data.JoriaDataException
import org.pdgen.util.Log
import java.io.FileNotFoundException
import java.util.jar.JarFile

class FindReportAnnotations(jarFile: String) {
    val roots = ArrayList<TestDataRootDef>()

    init {

        val jf = try { JarFile(jarFile)} catch (x: FileNotFoundException) {
            val msg = "adapterjar not found at: $jarFile"
            Log.ini.error(msg)
            throw JoriaDataException(msg, x)
        }

        try {
            Log.schema.info("FindReportAnnotations: $jarFile")
            val entries = jf.entries()

            entries.asIterator().forEach {
                if (it.name.endsWith(".class") && !it.name.contains('-')) { // this skips META-INF and module-info.class
                    try {
                        //val classname = it.name.substring(0, it.name.length - 6).replace('/', '.')
                        val classVisitor = RwClassVisitor()
                        ClassReader(jf.getInputStream(it)).accept(classVisitor, 0)
                        classVisitor.roots.forEach {
                            Log.schema.info(" RootAnnotation class=${it.testDataProviderClass}, method=${it.testDataMethod}")
                        }
                        roots.addAll(classVisitor.roots)
                    } catch (t: Throwable) {
                        Log.schema.error(t)
                    }
                }
            }
        } finally {
            jf.close()
        }
    }

    class RwClassVisitor : ClassVisitor(Opcodes.ASM9) {
        private val reportData = "org/pdgen/annotations/ReportData;"
        private val reportDataProvider = "org/pdgen/annotations/ReportDataProvider;"
        var isReportClass = false
        var currentclassname = ""
        var currentMethod = ""
        val roots = ArrayList<TestDataRootDef>()

        override fun visit(
            version: Int,
            access: Int,
            name: String,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            currentclassname = name
            isReportClass = false
        }

        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
            if (descriptor.contains(reportDataProvider)) {
                Log.schema.debug("reportDataProvider: $currentclassname")
                isReportClass = true
            }
            return null
        }

        override fun visitMethod(
            access: Int,
            name: String,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor? {

            if (isReportClass) {
                currentMethod = name

                return object : MethodVisitor(Opcodes.ASM9) {
                    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
                        if (descriptor.contains(reportData)) {
                            roots.add(TestDataRootDef(currentclassname.replace('/', '.'), currentMethod))
                        }
                        return null
                    }
                }
            }

            return null
        }
    }
}