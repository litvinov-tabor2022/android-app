package cz.jenda.tabor2022.fragments

import android.database.sqlite.SQLiteConstraintException
import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.activities.BatchModeActivity
import cz.jenda.tabor2022.data.Helpers.execute
import cz.jenda.tabor2022.data.model.GameTransaction
import cz.jenda.tabor2022.data.proto.Portal
import cz.jenda.tabor2022.databinding.FragmentBatchModeBinding
import cz.jenda.tabor2022.fragments.abstractions.TagAwareFragmentBase
import kotlinx.datetime.toKotlinInstant
import java.time.Instant

class BatchModeFragment(private val batchModeActivity: BatchModeActivity) : TagAwareFragmentBase(batchModeActivity) {
    private val builder: MutableLiveData<Portal.PlayerData.Builder> =
        MutableLiveData(Portal.PlayerData.newBuilder())

    override suspend fun onTagRead(tag: MifareClassic, tagData: Portal.PlayerData?) {
        tagData?.let {
            val playerData = it.toBuilder()

            val deltaStrength = builder.value?.strength ?: 0
            val deltaDexterity = builder.value?.dexterity ?: 0
            val deltaMagic = builder.value?.magic ?: 0
            val deltaBonusPoints = builder.value?.bonusPoints ?: 0

            playerData.strength = playerData.strength + deltaStrength
            playerData.dexterity = playerData.dexterity + deltaDexterity
            playerData.magic = playerData.magic + deltaMagic
            playerData.bonusPoints = playerData.bonusPoints + deltaBonusPoints

            val transactions = if (deltaStrength != 0 || deltaDexterity != 0 || deltaMagic != 0 || deltaBonusPoints != 0) {
                mutableListOf(
                    GameTransaction(
                        time = Instant.now().toKotlinInstant(),
                        userId = it.userId.toLong(),
                        deviceId = Constants.AppDeviceId,
                        strength = deltaStrength,
                        dexterity = deltaDexterity,
                        magic = deltaMagic,
                        bonusPoints = deltaBonusPoints,
                        skillId = 0
                    )
                )
            } else {
                mutableListOf()
            }

            runCatching {
                batchModeActivity.writeToTag(tag, playerData.build())
            }.onSuccess {
                transactions.forEach {
                    runCatching {
                        it.execute()
                    }.onFailure { e ->
                        when (e) {
                            is SQLiteConstraintException -> {
                                if (e.message?.contains(Constants.Db.UniqueConflict) == true) {
                                    Log.v(Constants.AppTag, "Transaction $it is already imported!")
                                    // rethrow all different errors!
                                }
                            }
                        }
                        throw e
                    }
                }
                activity?.runOnUiThread {
                    Toast.makeText(activity, R.string.writing_data_ok, Toast.LENGTH_SHORT).show()
                }
            }.onFailure {
                activity?.runOnUiThread {
                    Toast.makeText(activity, R.string.writing_data_failed, Toast.LENGTH_SHORT).show()
                }
            }
        } ?: activity?.runOnUiThread {
            Toast.makeText(this.context, R.string.read_tag_generic_error, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentBatchModeBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        builder.observe(viewLifecycleOwner) { pd -> binding.builder = pd }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val builderVal = builder.value
        builderVal?.strength = 0
        builderVal?.dexterity = 0
        builderVal?.magic = 0
        builderVal?.bonusPoints = 0
        builder.postValue(builderVal)
        // TODO disable negative values
        view.findViewById<ImageButton>(R.id.button_strength_plus)
            .setOnClickListener {
                builder.postValue(
                    builder.value?.strength?.plus(1)
                        ?.let { builder.value?.setStrength(it) }); refreshView()
            }
        view.findViewById<ImageButton>(R.id.button_dexterity_plus)
            .setOnClickListener {
                builder.postValue(
                    builder.value?.dexterity?.plus(1)
                        ?.let { builder.value?.setDexterity(it) }); refreshView()
            }
        view.findViewById<ImageButton>(R.id.button_magic_plus)
            .setOnClickListener {
                builder.postValue(
                    builder.value?.magic?.plus(1)
                        ?.let { builder.value?.setMagic(it) }); refreshView()
            }
        view.findViewById<ImageButton>(R.id.button_bonus_points_plus)
            .setOnClickListener {
                builder.postValue(
                    builder.value?.bonusPoints?.plus(1)
                        ?.let { builder.value?.setBonusPoints(it) }); refreshView()
            }
        view.findViewById<ImageButton>(R.id.button_strength_minus)
            .setOnClickListener {
                builder.postValue(
                    builder.value?.strength?.minus(1)
                        ?.let { builder.value?.setStrength(it) }); refreshView()
            }
        view.findViewById<ImageButton>(R.id.button_dexterity_minus)
            .setOnClickListener {
                builder.postValue(
                    builder.value?.dexterity?.minus(1)
                        ?.let { builder.value?.setDexterity(it) }); refreshView()
            }
        view.findViewById<ImageButton>(R.id.button_magic_minus)
            .setOnClickListener {
                builder.postValue(
                    builder.value?.magic?.minus(1)
                        ?.let { builder.value?.setMagic(it) }); refreshView()
            }
        view.findViewById<ImageButton>(R.id.button_bonus_points_minus)
            .setOnClickListener {
                builder.postValue(
                    builder.value?.bonusPoints?.minus(1)
                        ?.let { builder.value?.setBonusPoints(it) }); refreshView()
            }
    }

    private fun refreshView() {
    }
}