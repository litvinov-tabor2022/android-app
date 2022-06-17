package cz.jenda.tabor2022.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.data.User

class UserAdapter(private val context: Activity, private val data: List<User>) :
    ArrayAdapter<User>(context, R.layout.user_entry) {

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): User {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.user_entry, null, true)
        val idText = rowView.findViewById(R.id.portal_id) as TextView
        idText.text = data[position].name
        return rowView
    }

}