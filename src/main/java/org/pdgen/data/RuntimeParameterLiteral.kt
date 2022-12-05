// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data

import org.pdgen.data.view.RuntimeParameter
import org.pdgen.model.run.RunEnv
import java.util.*

open class RuntimeParameterLiteral(name: String, typ: JoriaType?) : AbstractTypedJoriaAccess(name, typ), RuntimeParameter {
    private val serialVersionUID = 7L

    override fun getValue(from: DBData?, asView: JoriaAccess?, env: RunEnv?): DBData? {
        val value = env!!.getRuntimeParameterValue(this)
        return value
        /*
        //TODO this must done, when RuntimeParameters are received through the API
        if (value == null)
            return null

        if (type.isStringLiteral) return DBStringImpl(this, value.toString())
        else if (type.isIntegerLiteral)  {
            if (value is Long || value is Int || value is Byte || value is Short) return DBIntImpl(this, (value as Number).toLong())
            else if (value is String) {
                try {
                    return DBIntImpl(this, value.toLong())
                } catch (ex: NumberFormatException) {
                    throw JoriaUserException(Res.msg("ParameterMustBeNot", name, value.javaClass.getName(), Res.str("integral_Number_Integer_Long_Short_Byte")))
                }
            }
            else throw JoriaUserException(Res.msg("ParameterMustBeNot", name, value.javaClass.getName(), Res.str("integral_Number_Integer_Long_Short_Byte")))
        } else if (type.isRealLiteral) {
                if (value is Float || value is Double) return DBRealImpl(this, (value as Number).toDouble())
                else if (value is String) {
                    try {
                        return DBRealImpl(this, value.toDouble())
                    } catch (ex: NumberFormatException) {
                        throw JoriaUserException(Res.msg("ParameterMustBeNot", name, value.javaClass.getName(), Res.str("floating_point_number_Double_Float")))
                    }
                } else throw JoriaUserException(Res.msg("ParameterMustBeNot", name, value.javaClass.getName(), Res.str("floating_point_number_Double_Float")))

        } else if (type.isBooleanLiteral) {
            if (value is Boolean) return DBBooleanImpl(this, value)
            else if (value is String) return DBBooleanImpl(this, value.toBoolean())
            else throw JoriaUserException(Res.msg("ParameterMustBeNot", name, value.javaClass.getName(), Res.str("Boolean")))
        }
        throw JoriaInternalError("Unexpected Type for Runtime Parameter: $type")

         */
    }

    constructor(name: String) : this(name, null)

    override fun setName(newName: String?) {
        name = newName
        makeLongName()
    }

    override fun getUsedAccessors(accessSet: MutableSet<JoriaAccess>) {
        accessSet.add(this)
    }

    override fun collectVariables(runtimeParameterSet: MutableSet<RuntimeParameter>, seen: MutableSet<Any>?) {
        runtimeParameterSet.add(this)
    }

    override fun getOqlEvaluator(): Any? = null
    override fun collectI18nKeys2(localizables: HashMap<String, MutableList<I18nKeyHolder>>?) {/* nothing to do */
    }

    override fun collectI18nKeys2(s: HashMap<String, MutableList<I18nKeyHolder>>?, seen: MutableSet<Any>?) {
        // nothing to do
    }

    override fun collectVisiblePickersInScope(
        collection: MutableList<Array<JoriaAccess>>?,
        visible: MutableSet<RuntimeParameter>?,
        pathStack: Stack<JoriaAccess>?,
        seen: MutableSet<Any>?
    ) {
        // nothing to do
    }
}