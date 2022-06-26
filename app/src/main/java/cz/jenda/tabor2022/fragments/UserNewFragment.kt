package cz.jenda.tabor2022.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import cz.jenda.tabor2022.Constants
import cz.jenda.tabor2022.Extras
import cz.jenda.tabor2022.PortalApp
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.activities.TagWriteActivity
import cz.jenda.tabor2022.data.model.User
import cz.jenda.tabor2022.data.proto.Portal
import kotlinx.coroutines.launch

class UserNewFragment : BasicFragment() {

    private val builder: Portal.PlayerData.Builder = Portal.PlayerData.newBuilder()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_user_new, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        builder.strength = 10
        builder.dexterity = 10
        builder.magic = 10
        builder.bonusPoints = 10

        // TODO disable negative values
        view.findViewById<ImageButton>(R.id.button_strength_plus)
            .setOnClickListener { builder.strength++; refreshView() }
        view.findViewById<ImageButton>(R.id.button_dexterity_plus)
            .setOnClickListener { builder.dexterity++; refreshView() }
        view.findViewById<ImageButton>(R.id.button_magic_plus)
            .setOnClickListener { builder.magic++; refreshView() }
        view.findViewById<ImageButton>(R.id.button_bonus_points_plus)
            .setOnClickListener { builder.bonusPoints++; refreshView() }
        view.findViewById<ImageButton>(R.id.button_strength_minus)
            .setOnClickListener { builder.strength--; refreshView() }
        view.findViewById<ImageButton>(R.id.button_dexterity_minus)
            .setOnClickListener { builder.dexterity--; refreshView() }
        view.findViewById<ImageButton>(R.id.button_magic_minus)
            .setOnClickListener { builder.magic--; refreshView() }
        view.findViewById<ImageButton>(R.id.button_bonus_points_minus)
            .setOnClickListener { builder.bonusPoints--; refreshView() }

        view.findViewById<Button>(R.id.button_save).setOnClickListener {
            if (validation()) {
                val name = view.findViewById<EditText>(R.id.text_name).text.toString()
                Log.i(Constants.AppTag, "Will create user $name")
                launch { saveNewUser(name, builder) }
            }
        }

        view.findViewById<Button>(R.id.button_save_and_init).setOnClickListener {
            if (validation()) {
                val name = view.findViewById<EditText>(R.id.text_name).text.toString()
                Log.i(Constants.AppTag, "Will create user $name")
                launch {
                    saveNewUser(name, builder)?.let {
                        builder.secret = Constants.TagSecret
                        builder.userId = it.toInt()
                        writeNewUserToTag()
                    }
                }
            }
        }
    }


    private fun validation(): Boolean {
        val name = view?.findViewById<EditText>(R.id.text_name)?.text.toString()

        return if (name.isEmpty()) {
            activity?.runOnUiThread {
                Toast.makeText(
                    activity,
                    R.string.err_missing_name,
                    Toast.LENGTH_LONG
                ).show()
            }
            false
        } else {
            true
        }
    }

    private suspend fun saveNewUser(name: String, data: Portal.PlayerData.Builder): Long? {
        var userId: Long? = null
        runCatching {
            Log.d("database", PortalApp.instance.dbPath)
            userId = PortalApp.instance.db.usersDao()
                .create(User(0, name, data.strength, data.dexterity, data.magic, data.bonusPoints))
        }
            .onSuccess {
                activity?.runOnUiThread {
                    Toast.makeText(
                        activity,
                        R.string.user_created,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.i(Constants.AppTag, "User $name created")
                return userId
            }.onFailure { e ->
                Log.w(Constants.AppTag, "User $name NOT created", e)

                if (e.message?.contains(Constants.Db.UniqueConflict) == true) {
                    Log.i(Constants.AppTag, "User $name already exists!")
                    activity?.runOnUiThread {
                        Toast.makeText(
                            activity,
                            R.string.err_user_already_exists,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(
                            activity,
                            R.string.err_user_not_created,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                return userId
            }
        return userId
    }

    private fun writeNewUserToTag() {
        val intent = Intent(view?.context, TagWriteActivity::class.java)
        intent.putExtra(Extras.DATA_TO_WRITE_ON_TAG, builder.build())
        startActivity(intent)
    }

    private fun refreshView() {
        activity?.runOnUiThread {
            view?.findViewById<TextView>(R.id.text_strength)?.text = builder.strength.toString()
            view?.findViewById<TextView>(R.id.text_dexterity)?.text = builder.dexterity.toString()
            view?.findViewById<TextView>(R.id.text_magic)?.text = builder.magic.toString()
            view?.findViewById<TextView>(R.id.text_bonus_points)?.text =
                builder.bonusPoints.toString()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshView()
    }
}