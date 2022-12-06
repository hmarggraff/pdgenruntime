// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.styledtext.model

import org.pdgen.util.ArrayUtils
import java.awt.font.FontRenderContext
import java.io.Serializable
import java.text.AttributedCharacterIterator

class StyledParagraphList : Serializable {
    var paragraphs: Array<StyledParagraph?>

    constructor(text: String?) {
        paragraphs = arrayOf(StyledParagraph(text))
    }

    constructor(text: StyledParagraph) {
        paragraphs = arrayOf(text)
    }

    constructor() {
        paragraphs = emptyArray()
    }

    constructor(list: Array<StyledParagraph?>) {
        paragraphs = list
    }

    private constructor(from: StyledParagraphList) {
        paragraphs = from.paragraphs.copyOf()
        for (i in paragraphs.indices) {
            paragraphs[i] = StyledParagraph(from.paragraphs[i])
        }
    }

    fun setInitialAttributes(fontfamily: String?, size: Float?, bold: Boolean, italic: Boolean, underlined: Boolean) {
        val paragraph = paragraphs[0]
        // if (paragraph.length() > 0)
        paragraph!!.setInitialAttributes(fontfamily, size, bold, italic, underlined)
    }

    val initialAttributes: Map<AttributedCharacterIterator.Attribute, Any>
        get() = paragraphs[0]!!.iterator.attributes
    val asString: String
        get() {
            val retVal = StringBuffer()
            for (i in paragraphs.indices) {
                if (i != 0) retVal.append('\n')
                val styledParagraph = paragraphs[i]
                retVal.append(styledParagraph.toString())
            }
            return retVal.toString()
        }

    fun unbrokenWidth(fontRenderContext: FontRenderContext?): FloatDim {
        var w = 0f
        var h = 0f
        var j = 0
        while (j < paragraphs.size) {
            val styledParagraph = paragraphs[j]
            if (styledParagraph!!.length == 0) {
                j++
                continue
            }
            val dim = styledParagraph.unbrokenWidth(fontRenderContext)
            w = Math.max(w, dim.width)
            h += dim.height
            j++
        }
        return FloatDim(w, h)
    }

    fun length(): Int {
        return paragraphs.size
    }

    operator fun get(ix: Int): StyledParagraph? {
        return paragraphs[ix]
    }

    fun splitParagraphs(pos: Int, parNo: Int) {
        val newPars = arrayOfNulls<StyledParagraph>(paragraphs.size + 1)
        System.arraycopy(paragraphs, 0, newPars, 0, parNo + 1)
        if (parNo < paragraphs.size - 1) System.arraycopy(paragraphs, parNo + 1, newPars, parNo + 2, paragraphs.size - parNo - 1)
        val leftPar = paragraphs[parNo]
        val newPar = leftPar!!.split(pos)
        val a1 = leftPar.iterator.attributes
        assert(a1.size > 0)
        val a2 = newPar.iterator.attributes
        assert(a2.size > 0)
        newPars[parNo + 1] = newPar
        paragraphs = newPars
    }

    val charCount: Int
        get() {
            var ret = 0
            for (paragraph in paragraphs) {
                ret += paragraph!!.length
            }
            return ret
        }

    fun mergeParagraphs(parIx: Int) {
        if (paragraphs.size == parIx + 1) return
        val p1 = paragraphs[parIx]
        p1!!.merge(paragraphs[parIx + 1])
        paragraphs = ArrayUtils.remove<StyledParagraph>(paragraphs, parIx + 1)
    }

    fun copyForEditing(): StyledParagraphList {
        return StyledParagraphList(this)
    }

    val noEditing: StyledParagraphList
        get() = StyledParagraphList(this)

    fun removeParagraphs(from: Int, to: Int) {
        paragraphs = ArrayUtils.remove<StyledParagraph>(paragraphs, from, to)
    }

    fun insertTextsAsParagraphs(texts: Array<StyledParagraph?>?, at: Int) {
        paragraphs = ArrayUtils.addArray<StyledParagraph>(paragraphs, at, texts)
    }

    fun hasText(text: String?): Boolean {
        for (p in paragraphs) {
            if (p!!.hasText(text)) return true
        }
        return false
    }

    fun removeAttribute(attribute: AttributedCharacterIterator.Attribute?) {
        for (p in paragraphs) {
            p!!.removeAttribute(attribute)
        }
    }

    companion object {
        private const val serialVersionUID = 7L
    }
}