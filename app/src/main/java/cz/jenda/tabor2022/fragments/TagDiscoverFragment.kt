package cz.jenda.tabor2022.fragments

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.Extras.DATA_TO_WRITE_ON_TAG
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.TagActions
import cz.jenda.tabor2022.activities.TagWriteActivity
import cz.jenda.tabor2022.activities.TagsActivity
import cz.jenda.tabor2022.data.Helpers
import cz.jenda.tabor2022.data.User
import cz.jenda.tabor2022.data.proto.Portal
import java.lang.Compiler.enable


class TagDiscoverFragment(activity: TagsActivity, private val actions: TagActions) :
    TagAwareFragmentBase(activity) {

    private var readTagData: Portal.PlayerData? = null
    private val builder: Portal.PlayerData.Builder = Portal.PlayerData.newBuilder()
    private var user: User? = null

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                readTagData = null
                user = null
                builder.clear()
            }
            refreshView()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_tag_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<ImageButton>(R.id.button_strength_plus).setOnClickListener {
            builder.strength++
            saveAndRefreshView(ChangeType.Strength, 1)
        }
        view.findViewById<ImageButton>(R.id.button_dexterity_plus).setOnClickListener {
            builder.dexterity++
            saveAndRefreshView(ChangeType.Dexterity, 1)
        }
        view.findViewById<ImageButton>(R.id.button_magic_plus).setOnClickListener {
            builder.magic++
            saveAndRefreshView(ChangeType.Magic, 1)
        }
        view.findViewById<ImageButton>(R.id.button_bonus_points_plus).setOnClickListener {
            builder.bonusPoints++
            saveAndRefreshView(ChangeType.BonusPoint, 1)
        }
        view.findViewById<ImageButton>(R.id.button_strength_minus).setOnClickListener {
            if (builder.strength > 0) {
                builder.strength--
                saveAndRefreshView(ChangeType.Strength, -1)
            }
        }
        view.findViewById<ImageButton>(R.id.button_dexterity_minus).setOnClickListener {
            if (builder.dexterity > 0) {
                builder.dexterity--
                saveAndRefreshView(ChangeType.Dexterity, -1)
            }
        }
        view.findViewById<ImageButton>(R.id.button_magic_minus).setOnClickListener {
            if (builder.magic > 0) {
                builder.magic--
                saveAndRefreshView(ChangeType.Magic, -1)
            }
        }
        view.findViewById<ImageButton>(R.id.button_bonus_points_minus).setOnClickListener {
            if (builder.bonusPoints > 0) {
                builder.bonusPoints--
                saveAndRefreshView(ChangeType.BonusPoint, -1)
            }
        }

        view.findViewById<Button>(R.id.button_revert_changes).setOnClickListener {
            builder.secret = readTagData?.secret ?: ""
            builder.userId = readTagData?.userId ?: 0
            builder.strength = readTagData?.strength ?: 0
            builder.dexterity = readTagData?.dexterity ?: 0
            builder.magic = readTagData?.magic ?: 0
            builder.bonusPoints = readTagData?.bonusPoints ?: 0

            refreshView()
        }

        view.findViewById<Button>(R.id.button_request_chage_persist).setOnClickListener {
            val intent = Intent(view.context, TagWriteActivity::class.java)
            intent.putExtra(DATA_TO_WRITE_ON_TAG, builder.build())
            startForResult.launch(intent)
        }

        activity?.runOnUiThread {
            view.findViewById<TextView>(R.id.text_name)?.text = getString(R.string.player_name_none)
            view.findViewById<TextView>(R.id.text_strength)?.text = "0"
            view.findViewById<TextView>(R.id.text_dexterity)?.text = "0"
            view.findViewById<TextView>(R.id.text_magic)?.text = "0"
            view.findViewById<TextView>(R.id.text_bonus_points)?.text = "0"
        }

        refreshView()
    }

    override suspend fun onTagRead(tag: MifareClassic, tagData: Portal.PlayerData?) {
        Log.d(Constants.AppTag, "Tag read @ TagDiscoverFragment: $tagData")

        if (tagData != null) {
            readTagData = tagData

            user = PortalApp.instance.db.usersDao().getById(tagData.userId)
            val isConsistent = if (user != null) {
                Helpers.compare(user!!, tagData);
            } else false;

            if (!isConsistent) {
                activity?.runOnUiThread {
                    view?.findViewById<TextView>(R.id.text_tag_read_error)?.text =
                        getString(R.string.inconsistend_data)
                }
                return
            }

            builder.secret = tagData.secret
            builder.userId = tagData.userId
            builder.strength = tagData.strength
            builder.dexterity = tagData.dexterity
            builder.magic = tagData.magic
            builder.bonusPoints = tagData.bonusPoints

            refreshView()
        } else {
            activity?.runOnUiThread {
                view?.findViewById<TextView>(R.id.text_tag_read_error)?.text =
                    getString(R.string.inconsistend_data)
            }
        }
    }

    private fun saveAndRefreshView(changeType: ChangeType, delta: Int) {
        // TODO write the change to tag!
        // TODO register transaction
        refreshView()
    }

    private fun disableEnableControls(state: Boolean, view: ViewGroup) {
        view.children.let {
            for (child in it) {
                child.isEnabled = state
                if (child is ViewGroup) {
                    disableEnableControls(state, child)
                }
            }
        }
    }

    private fun refreshView() {
        activity?.runOnUiThread {
//            view?.findViewById<ConstraintLayout>(R.id.layout_tag_discovered)?.let {
//                disableEnableControls(readTagData != null , it)
//            }
            view?.findViewById<TextView>(R.id.text_name)?.text = user?.name
            view?.findViewById<TextView>(R.id.text_strength)?.text = builder.strength.toString()
            view?.findViewById<TextView>(R.id.text_dexterity)?.text = builder.dexterity.toString()
            view?.findViewById<TextView>(R.id.text_magic)?.text = builder.magic.toString()
            view?.findViewById<TextView>(R.id.text_bonus_points)?.text =
                builder.bonusPoints.toString()
            view?.findViewById<ConstraintLayout>(R.id.layout_footer)?.visibility = Button.INVISIBLE;
            readTagData?.let {
                view?.findViewById<ConstraintLayout>(R.id.layout_footer)?.visibility =
                    if (builder.build() != it) Button.VISIBLE else Button.INVISIBLE;
            }
        }
    }
}

private enum class ChangeType {
    Strength, Dexterity, Magic, BonusPoint
}