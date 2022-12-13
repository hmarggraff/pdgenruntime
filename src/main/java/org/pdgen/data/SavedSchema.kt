package org.pdgen.data

import java.io.Serializable

interface SavedSchema: Serializable {
    fun buildSchema(forDesigner: Boolean): JoriaSchema
}