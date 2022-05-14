package cz.jenda.tabor2022.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.connection.PortalConnection

class PortalAdapter(private val context: Activity, private val data: List<PortalConnection>) :
    ArrayAdapter<String>(context, R.layout.portal_entry) {

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): String {
        return data[position].deviceId
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.portal_entry, null, true)
        val idText = rowView.findViewById(R.id.portal_id) as TextView
        val ipText = rowView.findViewById(R.id.portal_ip) as TextView
        idText.text = data[position].deviceId
        ipText.text = data[position].ip.toString()
        return rowView
    }

}