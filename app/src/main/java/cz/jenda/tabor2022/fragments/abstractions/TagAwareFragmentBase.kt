package cz.jenda.tabor2022.fragments.abstractions

import android.nfc.tech.MifareClassic
import cz.jenda.tabor2022.activities.BatchModeActivity
import cz.jenda.tabor2022.data.proto.Portal
import cz.jenda.tabor2022.fragments.abstractions.BasicFragment

abstract class TagAwareFragmentBase(private val activity: BatchModeActivity) : BasicFragment() {
    abstract suspend fun onTagRead(tag: MifareClassic, tagData: Portal.PlayerData?)

    override fun onResume() {
        super.onResume()
        activity.setCurrentFragment(this)
    }
}