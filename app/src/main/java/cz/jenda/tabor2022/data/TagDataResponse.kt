package cz.jenda.tabor2022.data

import java.io.Serializable

interface TagResponse : Serializable {
    val data: ByteArray?
    val dataIsValid: Boolean
    val message: String
}

class ErrorTagResponse(override val message: String) : TagResponse {
    override val data: ByteArray?
        get() = null
    override val dataIsValid: Boolean
        get() = false
}

open class TagDataResponse(override val data: ByteArray?, override val dataIsValid: Boolean) :
    TagResponse {
    override val message: String
        get() = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TagDataResponse

        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false
        if (dataIsValid != other.dataIsValid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data?.contentHashCode() ?: 0
        result = 31 * result + dataIsValid.hashCode()
        return result
    }
}
