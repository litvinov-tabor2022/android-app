package cz.jenda.tabor2022.fragments.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import cz.jenda.tabor2022.R

class WriteToTagDialog : DialogFragment() {
        private lateinit var listener: WriteToTagDialogListener

        interface WriteToTagDialogListener {
            fun onDialogPositiveClick(dialog: DialogFragment)
            fun onDialogNegativeClick(dialog: DialogFragment)
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            try {
                listener = if(parentFragment != null)
                    parentFragment as WriteToTagDialogListener
                else
                    activity as WriteToTagDialogListener
            } catch (e: ClassCastException) {
                throw ClassCastException(
                    (context.toString() + " must implement AddSkillDialogListener")
                )
            }

            return activity?.let {
                val builder = AlertDialog.Builder(it)
                builder.setMessage(R.string.dialog_rewrite_tag)
                    .setPositiveButton(
                        R.string.yes
                    ) { _, _ ->
                        listener.onDialogPositiveClick(this)
                    }
                    .setNegativeButton(
                        R.string.no
                    ) { _, _ ->
                        listener.onDialogNegativeClick(this)
                    }
                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")
        }

        override fun onCancel(dialog: DialogInterface) {
            super.onCancel(dialog)
            listener.onDialogNegativeClick(this)
        }
    }
