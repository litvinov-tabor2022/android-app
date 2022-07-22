package cz.jenda.tabor2022.fragments.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import cz.jenda.tabor2022.R

class NumberPickDialog : DialogFragment() {
    private lateinit var listener: NumberPickDialogListener

    interface NumberPickDialogListener {
        fun onValueChosen(value: Int)
        fun onCancelled()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        try {
            listener = activity as NumberPickDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                (context.toString() + " must implement NumberPickDialogListener")
            )
        }

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val view = this.layoutInflater.inflate(R.layout.dialog_number_picker, null);
            builder.setView(view);

            val picker = view.findViewById<NumberPicker>(R.id.number_picker)

            picker.minValue = 0;
            picker.maxValue = 254;

            builder.setPositiveButton("OK") { _, _ ->
                val value = picker.value
                listener.onValueChosen(value)
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener.onCancelled()
    }
}