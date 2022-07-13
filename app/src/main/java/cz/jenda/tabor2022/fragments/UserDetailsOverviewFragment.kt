package cz.jenda.tabor2022.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.adapters.UserDetailsActivityPagerAdapter
import cz.jenda.tabor2022.data.Helpers.toPlayerData
import cz.jenda.tabor2022.databinding.FragmentUserDetailsBinding
import cz.jenda.tabor2022.fragments.abstractions.BasicFragment
import kotlinx.coroutines.launch

class UserDetailsOverviewFragment(
    private val adapter: UserDetailsActivityPagerAdapter
) : BasicFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUserDetailsBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        adapter.data.observe(viewLifecycleOwner) { pd -> binding.builder = pd }
        binding.userWithSkills = adapter.userWithGroup
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.button_strength_plus).setOnClickListener {
            val updatedPlayerData = adapter.data.value
            updatedPlayerData?.strength = updatedPlayerData?.strength?.plus(1)!!
            adapter.data.postValue(updatedPlayerData)
        }
        view.findViewById<ImageButton>(R.id.button_dexterity_plus).setOnClickListener {
            val updatedPlayerData = adapter.data.value
            updatedPlayerData?.dexterity = updatedPlayerData?.dexterity?.plus(1)!!
            adapter.data.postValue(updatedPlayerData)
            Log.i(Constants.AppTag, "Dexterity plus $updatedPlayerData")
        }
        view.findViewById<ImageButton>(R.id.button_magic_plus).setOnClickListener {
            val updatedPlayerData = adapter.data.value
            updatedPlayerData?.magic = updatedPlayerData?.magic?.plus(1)!!
            adapter.data.postValue(updatedPlayerData)
        }
        view.findViewById<ImageButton>(R.id.button_bonus_points_plus).setOnClickListener {
            val updatedPlayerData = adapter.data.value
            updatedPlayerData?.bonusPoints = updatedPlayerData?.bonusPoints?.plus(1)!!
            adapter.data.postValue(updatedPlayerData)
        }
        view.findViewById<ImageButton>(R.id.button_strength_minus).setOnClickListener {
            val updatedPlayerData = adapter.data.value
            if ((updatedPlayerData?.strength ?: 0) > 0) {
                updatedPlayerData?.strength = updatedPlayerData?.strength?.minus(1) ?: 0
                adapter.data.postValue(updatedPlayerData)
            }
        }
        view.findViewById<ImageButton>(R.id.button_dexterity_minus).setOnClickListener {
            val updatedPlayerData = adapter.data.value
            if ((updatedPlayerData?.dexterity ?: 0) > 0) {
                updatedPlayerData?.dexterity = updatedPlayerData?.dexterity?.minus(1) ?: 0
                adapter.data.postValue(updatedPlayerData)
            }
        }
        view.findViewById<ImageButton>(R.id.button_magic_minus).setOnClickListener {
            val updatedPlayerData = adapter.data.value
            if ((updatedPlayerData?.magic ?: 0) > 0) {
                updatedPlayerData?.magic = updatedPlayerData?.magic?.minus(1) ?: 0
                adapter.data.postValue(updatedPlayerData)
            }
        }
        view.findViewById<ImageButton>(R.id.button_bonus_points_minus).setOnClickListener {
            val updatedPlayerData = adapter.data.value
            if ((updatedPlayerData?.bonusPoints ?: 0) > 0) {
                updatedPlayerData?.bonusPoints = updatedPlayerData?.bonusPoints?.minus(1) ?: 0
                adapter.data.postValue(updatedPlayerData)
            }
        }
//        adapter.transactionsBuffer.observe(viewLifecycleOwner) { transactions ->
//            val color = context?.let {
//                if (transactions.isNullOrEmpty())
//                    ContextCompat.getColor(it, R.color.green) else ContextCompat.getColor(
//                    it,
//                    R.color.design_default_color_error
//                )
//            }
//            color?.let { view.findViewById<TextView>(R.id.text_name).setTextColor(it) }
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launch {
            val skillsFragment =
                adapter.data.value?.build()
                    ?.let { UserTagSkillsFragment(adapter.userWithGroup, it) }
            skillsFragment?.let {
                childFragmentManager
                    .beginTransaction()
                    .add(R.id.skills_view, it)
                    .commit()
            }
        }
    }

}