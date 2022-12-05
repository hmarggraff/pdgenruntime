package org.pdgen.datasources.java

import org.objectweb.asm.*
import org.pdgen.data.Log
import java.io.File
import java.util.jar.JarFile

class FindReportAnnotations(val jarFile: File) {
    val roots = ArrayList<Pair<String, String>>()

    init {
        val jf = JarFile(jarFile)
        try {
            val entries = jf.entries()
            Log.schema.info("FindReportAnnotations: $jarFile")

            entries.asIterator().forEach {
                if (it.name.endsWith(".class") && !it.name.contains('-')) { // this skips META-INF and module-info.class
                    try {
                        //val classname = it.name.substring(0, it.name.length - 6).replace('/', '.')
                        val classVisitor = RwClassVisitor()
                        ClassReader(jf.getInputStream(it)).accept(classVisitor, 0)
                        classVisitor.roots.forEach {
                            Log.schema.info(" RootAnnotation class=${it.first}, method=${it.second}")
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
        private val reportData = "io/pdgen/annotations/ReportData;"
        private val reportDataProvider = "io/pdgen/annotations/ReportDataProvider;"
        var isReportClass = false
        var currentclassname = ""
        var currentMethod = ""
        val roots = ArrayList<Pair<String, String>>()

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
                            roots.add(Pair(currentclassname.replace('/', '.'), currentMethod))
                        }
                        return null
                    }
                }
            }

            return null
        }
    }
}