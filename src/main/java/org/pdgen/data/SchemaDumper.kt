// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data

import org.pdgen.env.Env
import org.pdgen.util.StringUtils
import java.io.PrintStream
import java.util.*

class SchemaDumper {
    var markupChars = ",:<>{}[]#!@;"


    fun dumpSchema(p: PrintStream, it: JoriaSchema, internals: Iterable<JoriaType>) {
        p.println("YAML: Pdgen Schema Dump")
        p.print("Created: '")
        p.print(Date())
        p.println("'")
        p.print("TemplateFile: '")
        p.print(Env.instance().currentFile)
        p.println("'")
        p.print("SchemaClass: ")
        p.println(it.javaClass.name)
        p.println("Internal_Types:")
        for (t in internals) {
            if (!t.isClass) dumpTypeHead(p, t)
        }
        p.println()
        p.println("Internal_Classes:")
        for (t in internals) {
            if (t.isClass) dumpClass(p, t)
        }
        p.println()
        p.println("Classes: ")
        for (t in it.classes) {
            dumpClass(p, t)
        }
        p.println()
        p.println("Roots: ")
        for (root in it.roots.data) dumpMember(p, root)
    }

    protected fun dumpClass(p: PrintStream, t: JoriaType) {
        dumpTypeHead(p, t)
        p.print("   members: ")
        val jc = t as JoriaClass
        val members = jc.members
        if (members == null || members.size == 0) {
            p.println(" {}")
            return
        }
        p.println()
        for (m in members) {
            dumpMember(p, m)
        }
    }

    private fun dumpMember(p: PrintStream, m: JoriaAccess) {
        p.print("    ")
        p.print(m.name)
        p.print(": ")
        dumpQuoted(p, m.type.name)
        p.println()
    }

    protected fun dumpTypeHead(p: PrintStream, t: JoriaType) {
        p.print(" - name: ")
        val tn = t.name
        dumpQuoted(p, tn)
        p.println()
        p.print("   impl: ")
        p.println(t.javaClass.name)
        if (t.isClass) return
        p.print("   kind: ")
        if (t.isBlob) p.println("blob") else if (t.isBooleanLiteral) p.println("boolean") else if (t.isCharacterLiteral) p.println(
            "char"
        ) else if (t.isClass) p.println("class") else if (t.isDictionary) {
            p.println("dictionary")
            val d = t as JoriaDictionary
            p.print("   keys: ")
            p.println(d.keyMatchType)
            p.print("   elements: ")
            p.println(d.elementType.name)
        } else if (t.isCollection) {
            p.println("collection")
            p.print("   elements: ")
            p.println(t.asParent.name)
        } else if (t.isDate) p.println("date") else if (t.isImage) p.println("image") else if (t.isIntegerLiteral) p.println(
            "int"
        ) else if (t.isLiteralCollection) {
            p.println("literal_collection")
            val literalColl = t as JoriaLiteralCollection
            p.print("   elements: ")
            p.println(literalColl.elementLiteralType.name)
        } else if (t.isRealLiteral) p.println("float")
        else if (t.isStringLiteral) p.println("String")
        else if (t.isUnknown) p.println("unknown")
        else if (t.isVoid) p.println("void") else p.println("unspecified")
    }

    private fun dumpQuoted(p: PrintStream, tn: String) {
        val containsMarkup = StringUtils.containsChar(tn, markupChars)
        if (containsMarkup) p.print('\'')
        p.print(tn)
        if (containsMarkup) p.print('\'')
    }

}