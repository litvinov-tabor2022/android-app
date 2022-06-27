package cz.jenda.tabor2022.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.adapters.OnItemShortClickListener
import cz.jenda.tabor2022.adapters.UserDetailsActivityPagerAdapter
import cz.jenda.tabor2022.data.model.*
import cz.jenda.tabor2022.fragments.abstractions.AbsSkillListFragment
import cz.jenda.tabor2022.fragments.dialogs.AddSkillDialog
import cz.jenda.tabor2022.viewmodel.SkillViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

class AddSkillToUserFragment(
    private val userWithSkills: UserAndSkills,
    private val adapter: UserDetailsActivityPagerAdapter
) :
    AbsSkillListFragment(userWithSkills, SkillViewModel()),
    AddSkillDialog.AddSkillDialogListener {
    var lastClickedSkill: Skill? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skillListAdapter.setOnItemClickListener(object : OnItemShortClickListener<Skill> {
            override fun itemShortClicked(item: Skill) {
                val violateLimits = checkLimitViolation(item)
                if (!violateLimits && lastClickedSkill != item) {
                    val dialog = AddSkillDialog()
                    dialog.show(childFragmentManager, "")
                } else {
                    addSkillToUser(item)
                }
                lastClickedSkill = item
            }
        })
    }

    private fun checkLimitViolation(skill: Skill): Boolean {
        val user = userWithSkills.user
        if (user.strength < skill.strength) return false
        if (user.dexterity < skill.dexterity) return false
        if (user.magic < skill.magic) return false
        return true
    }

    private fun addSkillToUser(selectedSkill: Skill) {
        val crossRef = UserSkillCrossRef(
            userId = userWithSkills.user.id,
            skillId = selectedSkill.id
        )
        launch(Dispatchers.IO) {
            adapter.pushTransaction(
                GameTransaction(
                    time = Instant.now(),
                    deviceId = Constants.AppDeviceId,
                    userId = userWithSkills.user.id,
                    strength = 0,
                    dexterity = 0,
                    magic = 0,
                    bonusPoints = 0,
                    skillId = selectedSkill.id.toInt()
                )
            )

            PortalApp.instance.db.userSkillCrossRefDao().save(crossRef)
            activity?.runOnUiThread {
                Toast.makeText(
                    context,
                    "Schopnost ${selectedSkill.name} přidána",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        lastClickedSkill?.let { addSkillToUser(it) }
        Log.i(Constants.AppTag, "Skill ${lastClickedSkill.toString()} was force added.")
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {
        Log.i(Constants.AppTag, "Force skill adding quit.")
    }


}