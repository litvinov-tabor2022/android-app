package cz.jenda.tabor2022

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.TagLostException
import android.nfc.tech.MifareClassic
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.protobuf.InvalidProtocolBufferException
import cz.jenda.tabor2022.Constants.toHex
import cz.jenda.tabor2022.data.ErrorTagResponse
import cz.jenda.tabor2022.data.TagDataResponse
import cz.jenda.tabor2022.data.TagResponse
import cz.jenda.tabor2022.data.proto.Portal
import cz.jenda.tabor2022.exception.InvalidDataOnTag
import cz.jenda.tabor2022.exception.TagCannotBeRead
import cz.jenda.tabor2022.exception.TagCannotBeWritten
import cz.jenda.tabor2022.exception.TagExceptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.coroutines.CoroutineContext

class TagActions(
    private val ctx: AppCompatActivity,
    private val onTagRead: suspend (MifareClassic, Portal.PlayerData?) -> Unit
) : CoroutineScope {
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    suspend fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            NfcAdapter.ACTION_TECH_DISCOVERED -> {
                intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.let { tag ->
                    Log.d(Constants.AppTag, "Inserted tag, reading data")

                    val (mifare, resp) = readTag(tag)
                    if (resp.dataIsValid) {
                        runCatching {
                            withContext(Dispatchers.IO) {
                                val istream = ByteArrayInputStream(resp.data)
                                val playerData = Portal.PlayerData.parseDelimitedFrom(istream)
                                Log.v(Constants.AppTag, "Loaded PlayerData: $playerData")
                                playerData
                            }
                        }.onSuccess { onTagRead(mifare, it) }.onFailure { e ->
                            Log.d(Constants.AppTag, "Inserted tag couldn't be read/parsed", e)
                            onTagRead(mifare, null)
                        }
                    } else {
                        Log.d(Constants.AppTag, "Inserted tag couldn't be read")
                        onTagRead(mifare, null)
                    }
                }
            }
            NfcAdapter.ACTION_TAG_DISCOVERED -> {
                intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.let { tag ->
                    Log.d(Constants.AppTag, "Inserted tag, reading data")

                    val (mifare, resp) = readTag(tag)
                    if (resp.dataIsValid) {
                        runCatching {
                            withContext(Dispatchers.IO) {
                                val istream = ByteArrayInputStream(resp.data)
                                val playerData = Portal.PlayerData.parseDelimitedFrom(istream)
                                Log.v(Constants.AppTag, "Loaded PlayerData: $playerData")
                                playerData
                            }
                        }.onSuccess { onTagRead(mifare, it) }.onFailure { e ->
                            Log.d(Constants.AppTag, "Inserted tag couldn't be read/parsed", e)
                            onTagRead(mifare, null)
                        }
                    } else {
                        Log.d(Constants.AppTag, "Inserted tag couldn't be read")
                        onTagRead(mifare, null)
                    }
                }
            }
        }
    }

    suspend fun writeToTag(tag: MifareClassic, data: Portal.PlayerData) {
        withContext(Dispatchers.IO) {
            val buffer = ByteArrayOutputStream()
            data.writeDelimitedTo(buffer)
            writeToTag(tag, buffer.toByteArray())
        }
    }

    private suspend fun readTag(tag: Tag): Pair<MifareClassic, TagResponse> {
        return withContext(Dispatchers.IO) {
            val mifare = MifareClassic.get(tag)
            mifare.connect()

            var dataResponse: TagResponse =
                ErrorTagResponse(ctx.getString(R.string.read_tag_possibly_invalid))
            var data: ByteArray? = null

            try {
                data = readRaw(mifare) // read everything
            } catch (e: TagExceptions) {
                dataResponse = ErrorTagResponse(ctx.getString(R.string.read_tag_generic_error))
            } catch (e: TagLostException) {
                dataResponse = ErrorTagResponse(ctx.getString(R.string.read_tag_lost_connection))
            } catch (e: Exception) {
                dataResponse = ErrorTagResponse(ctx.getString(R.string.read_tag_possibly_invalid))
            }

            if (data != null) {
                try {
                    dataResponse = TagDataResponse(data, true)
                } catch (e: InvalidProtocolBufferException) {
                    e.message?.let { Log.e(Constants.AppTag, it) }
                }
            }

            Pair(mifare, dataResponse)
        }
    }

    private fun readRaw(mifare: MifareClassic): ByteArray? {
        var blockIndex = 1
        var sectorIndex: Int = blockIndex / 4
        var bytesRead = 0
        var sectorAuthenticated: Boolean = false
        var data: ByteArray = byteArrayOf()
        while (bytesRead < Constants.TagDataSizeLimit) {
            if (blockIndex % 4 == 3) { // sector was changed -> new authentication
                blockIndex++
                sectorIndex++
                sectorAuthenticated = false
            }
            if (!sectorAuthenticated) {
                sectorAuthenticated =
                    if (mifare.authenticateSectorWithKeyA(sectorIndex, Constants.MifareKey1)) {
                        true
                    } else if (mifare.authenticateSectorWithKeyA(
                            sectorIndex,
                            Constants.MifareKey2
                        )
                    ) {
                        true
                    } else {
                        return null
                    }
            }
            try {
                data += readBlock(mifare, blockIndex) ?: throw InvalidDataOnTag("")
            } catch (e: IOException) {
                throw TagCannotBeRead("Error reading block $blockIndex")
            }
            bytesRead += if ((Constants.TagDataSizeLimit - bytesRead) < 16) (Constants.TagDataSizeLimit - bytesRead) else 16
            blockIndex++
        }

        return data
    }

    @Throws(IOException::class)
    private fun readBlock(mifare: MifareClassic, blockIndex: Int): ByteArray? {
        return mifare.readBlock(blockIndex)?.copyOf()
    }

    private suspend fun writeToTag(mifare: MifareClassic, data: ByteArray) {
        assert(data.size <= Constants.TagDataSizeLimit)
        writeRaw(mifare, data)

        // read the tag again and verify
        val reread = readRaw(mifare)?.take(data.size)?.toByteArray()
        Log.d(
            Constants.AppTag,
            "Verifying data on tag: written ${data.toHex()}, read ${reread?.toHex()}"
        )
        if (!reread.contentEquals(data)) {
            throw TagCannotBeWritten("Data written to tag couldn't be verified")
        }
    }

    private suspend fun writeRaw(mifare: MifareClassic, data: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            var blockIndex = 1
            var sectorIndex: Int = blockIndex / 4
            var bytesWritten = 0
            var sectorAuthenticated = false
            while (bytesWritten < data.size) {
                if (blockIndex % 4 == 3) { // sector was changed -> new authentication
                    blockIndex++
                    sectorIndex++
                    sectorAuthenticated = false
                }
                if (!sectorAuthenticated) {
                    if (mifare.authenticateSectorWithKeyA(sectorIndex, Constants.MifareKey1)) {
                        sectorAuthenticated = true
                    } else if (mifare.authenticateSectorWithKeyA(
                            sectorIndex,
                            Constants.MifareKey2
                        )
                    ) {
                        sectorAuthenticated = true
                    } else {
                        return@withContext false
                    }
                }
                val bytesToWrite =
                    if ((data.size - bytesWritten) < 16) (data.size - bytesWritten) else 16
                val dataToWrite = data.copyOfRange(bytesWritten, bytesWritten + bytesToWrite)
                writeBlock(mifare, blockIndex, dataToWrite)
                bytesWritten += bytesToWrite
                blockIndex++
            }

            true
        }
    }

    private fun writeBlock(mifare: MifareClassic, blockIndex: Int, data: ByteArray) {
        var blockOfData = data.copyOf()
        for (i in data.size..15) {
            blockOfData += 0x00
        }
        mifare.writeBlock(blockIndex, blockOfData)
    }
}
