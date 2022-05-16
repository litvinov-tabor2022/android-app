package cz.jenda.tabor2022.fragments

import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.TagActions
import cz.jenda.tabor2022.activities.TagsActivity
import cz.jenda.tabor2022.data.proto.Portal

class TagDiscoverFragment(activity: TagsActivity, private val actions: TagActions) : TagAwareFragmentBase(activity) {

    private val builder: Portal.PlayerData.Builder = Portal.PlayerData.newBuilder()
    private var name: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_tag_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // TODO disable negative values
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
            builder.strength--
            saveAndRefreshView(ChangeType.Strength, -1)
        }
        view.findViewById<ImageButton>(R.id.button_dexterity_minus).setOnClickListener {
            builder.dexterity--
            saveAndRefreshView(ChangeType.Dexterity, -1)
        }
        view.findViewById<ImageButton>(R.id.button_magic_minus).setOnClickListener {
            builder.magic--
            saveAndRefreshView(ChangeType.Magic, -1)
        }
        view.findViewById<ImageButton>(R.id.button_bonus_points_minus).setOnClickListener {
            builder.bonusPoints--
            saveAndRefreshView(ChangeType.BonusPoint, -1)
        }

        // TODO detect if tag is still connected
        activity?.runOnUiThread {
            view.findViewById<TextView>(R.id.text_name)?.text = getString(R.string.player_name_none)
            view.findViewById<TextView>(R.id.text_strength)?.text = "0"
            view.findViewById<TextView>(R.id.text_dexterity)?.text = "0"
            view.findViewById<TextView>(R.id.text_magic)?.text = "0"
            view.findViewById<TextView>(R.id.text_bonus_points)?.text = "0"
        }

        // TODO reset values on tag disconnect? or support clicking and after-write to tag? 🤔
    }

    override suspend fun onTagRead(tag: MifareClassic, tagData: Portal.PlayerData?) {
        Log.d(Constants.AppTag, "Tag read @ TagDiscoverFragment: $tagData")

        if (tagData != null) {
            // TODO check consistency

            name = PortalApp.instance.db.usersDao().getById(tagData.userId).name

            builder.strength = tagData.strength
            builder.dexterity = tagData.dexterity
            builder.magic = tagData.magic
            builder.bonusPoints = tagData.bonusPoints

            refreshView()
        }
    }

    private fun saveAndRefreshView(changeType: ChangeType, delta: Int) {
        // TODO write the change to tag!
        // TODO register transaction

        refreshView()
    }

    private fun refreshView() {
        activity?.runOnUiThread {
            view?.findViewById<TextView>(R.id.text_name)?.text = name
            view?.findViewById<TextView>(R.id.text_strength)?.text = builder.strength.toString()
            view?.findViewById<TextView>(R.id.text_dexterity)?.text = builder.dexterity.toString()
            view?.findViewById<TextView>(R.id.text_magic)?.text = builder.magic.toString()
            view?.findViewById<TextView>(R.id.text_bonus_points)?.text = builder.bonusPoints.toString()
        }
    }
}

private enum class ChangeType {
    Strength, Dexterity, Magic, BonusPoint
}