// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data

import org.pdgen.env.Settings

enum class Log(var level: Int = 1) {
    ini(level("Log.ini")),
    schema(level("Log.schema")),
    ui(level("Log.ui")),
    run(level("Log.run"));

    fun warn(msg: String): Unit {
        if (level >= levelDebug)
            out.println("$blanks${this.name} $msg")
    }

    fun info(msg: String): Unit {
        if (level >= levelInfo)
            out.println("$blanks${this.name} $msg")
    }

    fun debug(msg: String): Unit {
        if (level >= levelDebug)
            out.println("$blanks${this.name} $msg")
    }

    fun debug(condition: Boolean, msg: String): Unit {
        if (condition && level >= levelDebug)
            out.println("$blanks${this.name} $msg")
    }

    fun error(msg: String): Unit {
        out.println("$blanks${this.name} $msg")
    }

    fun error(ex: Throwable): Unit {
        out.println("$blanks${this.name} ${ex.message}")
        ex.printStackTrace(out)
    }

    fun error(ex: Throwable, msg:String): Unit {
        out.println("$blanks${this.name} ${ex.message}: $msg")
        ex.printStackTrace(out)
    }

    public fun indent() {
        indent++;
        blanks = blanks + "  "
    }

    public fun undent() {
        if (indent <= 0) return
        indent--;
        blanks = blanks.substring(2)
    }

}

fun level(module: String) = Settings.get(module)?.toInt() ?: 3
var blanks: String = ""
var indent = 0;


val out = System.out
val levelError = 1
val levelWarn = 2
val levelInfo = 3
val levelDebug = 4
