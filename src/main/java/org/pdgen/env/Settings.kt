// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env

import org.pdgen.data.Trace
import java.io.File
import java.io.FileInputStream
import java.lang.System.getProperty
import java.lang.System.getenv
import java.math.RoundingMode
import java.util.*
import java.util.prefs.Preferences

object Settings {
    val props = Properties()

    init {
        val logFile = getenv("RW.LOGFILE") ?: getProperty("rw.logfile")
        if (logFile != null)
            Trace.setLogStream(logFile)
        Trace.setTraceLevel(3)
        Trace.addModule(Trace.baseInit)
    }

    @JvmStatic
    public val installHome = InstallHome.findInstallHome()
    @JvmStatic
    public val userHomeDir = getenv("USERPROFILE")
    @JvmStatic
    public val userdir = getProperty("user.dir")
    @JvmStatic
    public val username = getProperty("user.name")


    init {
        loadSettingCascade()
        initLogging()
    }

    val screenShotDir: File = File(get("screenShotDir", "$userHomeDir/.pdgen"))
    val showDebugColors = booleanProperty("showDebugColors")
    @JvmStatic
    public val roundingMode = roundingMode()


    @JvmStatic
    public fun booleanProperty(key: String): Boolean {
        val p = get(key)
        return p != null && p.equals("true")
    }

    private fun initLogging() {
        val logLevelStr = props.getProperty("LogLevel")
        if (logLevelStr != null) {
            Trace.log(Trace.baseInit, "Switching to loglevel=$logLevelStr")
            val logLevel = Integer.valueOf(logLevelStr)
            Trace.setTraceLevel(logLevel)
        }
        val logModules = props.getProperty("LogModules")
        if (logModules != null) {
            val modules = logModules.split(',')
            modules.forEach { Trace.addModule(it) }
        }
        Trace.logDebug(Trace.baseInit, "Proprties count=${props.size}")
        props.stringPropertyNames().forEach {
            Trace.logDebug(Trace.baseInit, "  propkey=$it val=${props.getProperty(it)}")
        }
    }

    private fun loadSettingCascade() {
        mergeProps(File(userHomeDir, "pdgen.properties"), props)
        mergeProps(File(userdir, "pdgen.properties"), props)
        val configfileName = getenv("RW.CONFIG") ?: getProperty("rw.config")
        if (configfileName != null) {
            mergeProps(configfileName, props)
        } else {
            Trace.log(
                Trace.baseInit,
                "No config file specified in env as RW.CONFIG or as system property rw.config"
            )
        }
        val pref = Preferences.userNodeForPackage(Settings::class.java)
        pref.keys().forEach {
            props.put(it, pref.get(it, "novalue"))
        }

        val getenv = getenv()
        getenv.forEach { k, v -> props[k] = v }

        val sysProps = System.getProperties()
        sysProps.stringPropertyNames().forEach {
            Trace.logDebug(Trace.baseInit, "Sysprop key=$it, val=${sysProps.getProperty(it)}")
            props.setProperty(it, sysProps.getProperty(it))
        }
    }

    private fun mergeProps(from: String, props: Properties) = mergeProps(File(from), props)

    private fun mergeProps(configFile: File, props: Properties) {
        if (configFile.exists()) {
            Trace.log(Trace.baseInit, "Config found=true, file=${configFile.absolutePath}")

            props.load(FileInputStream(configFile))
        } else
            Trace.log(Trace.init,"Config found=false, file=${configFile.absolutePath}")
    }

    fun roundingMode(): RoundingMode {
        val roundingText = get("DecimalFormatRoundingMode")

        return if (roundingText != null) RoundingMode.valueOf(roundingText) else RoundingMode.HALF_EVEN
    }

    @JvmStatic
    public fun get(key: String): String? = props.getProperty(key)

    @JvmStatic
    public fun get(key: String, defaultValue: String): String = props.getProperty(key) ?: defaultValue

    @JvmStatic
    public fun getPropertyNames(): MutableSet<String> = props.stringPropertyNames()!!

    @JvmStatic
    fun savePref(key: String, value: String) {
        val pref = Preferences.userNodeForPackage(Settings::class.java)
        pref.put(key, value)

    }
}