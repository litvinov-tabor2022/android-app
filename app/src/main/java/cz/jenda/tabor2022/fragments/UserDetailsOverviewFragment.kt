package cz.jenda.tabor2022.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.adapters.UserDetailsActivityPagerAdapter
import cz.jenda.tabor2022.data.model.UserAndSkills
import cz.jenda.tabor2022.databinding.FragmentUserDetailsBinding
import cz.jenda.tabor2022.fragments.abstractions.BasicFragment
import kotlinx.coroutines.launch

class UserDetailsOverviewFragment(
    private var userWithSkills: UserAndSkills,
    adapter: UserDetailsActivityPagerAdapter
) : BasicFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUserDetailsBinding.inflate(layoutInflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.userWithSkills = userWithSkills
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launch {
            val skillsFragment = UserSkillsFragment(userWithSkills)
            childFragmentManager
                .beginTransaction()
                .add(R.id.skills_view, skillsFragment)
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
    }
}