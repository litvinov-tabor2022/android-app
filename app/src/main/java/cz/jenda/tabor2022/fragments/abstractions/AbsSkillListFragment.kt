package cz.jenda.tabor2022.fragments.abstractions

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.adapters.SkillListAdapter
import cz.jenda.tabor2022.data.model.Skill
import cz.jenda.tabor2022.data.model.UserWithGroup
import cz.jenda.tabor2022.fragments.SearchableListFragment
import cz.jenda.tabor2022.viewmodel.AbsSkillViewModel

abstract class AbsSkillListFragment(
    private val user: UserWithGroup?,
    private val skillViewModel: AbsSkillViewModel,
) :
    SearchableListFragment<Skill>(R.id.search_bar) {
    lateinit var skillListAdapter: SkillListAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skillListAdapter = SkillListAdapter(user)

        listView.adapter = skillListAdapter
        listView.layoutManager = LinearLayoutManager(context)

        skillViewModel.skills.observe(viewLifecycleOwner) { skills ->
            data = skills
            skills?.let { skillListAdapter.submitList(it.toMutableList()) }
        }

        filteredData.observe(viewLifecycleOwner) { skills ->
            skills?.let { skillListAdapter.submitList(it.toMutableList()) }
        }

        registerForContextMenu(listView)
    }

    override fun filter(text: CharSequence, data: List<Skill>): List<Skill> {
        return data.filter { skill ->
            text.let { skill.name.contains(it, ignoreCase = true) }
        }
    }

}