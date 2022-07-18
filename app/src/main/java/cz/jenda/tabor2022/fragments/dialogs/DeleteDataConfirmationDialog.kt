package cz.jenda.tabor2022.fragments.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import cz.jenda.tabor2022.R

class DeleteDataConfirmationDialog : DialogFragment() {
    private lateinit var listener: DeleteDataConfirmationDialogListener

    interface DeleteDataConfirmationDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        try {
            listener = activity as DeleteDataConfirmationDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                (context.toString() + " must implement DeleteDataConfirmationDialogListener")
            )
        }

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.q_delete_data)
                .setPositiveButton(
                    R.string.ok
                ) { _, _ ->
                    listener.onDialogPositiveClick(this)
                }
                .setNegativeButton(
                    R.string.cancel
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