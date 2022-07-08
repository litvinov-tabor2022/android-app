package cz.jenda.tabor2022.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cz.jenda.tabor2022.R
import cz.jenda.tabor2022.fragments.abstractions.BasicFragment
import kotlinx.coroutines.launch

abstract class SearchableListFragment<T>(private val search_bar: Int) : BasicFragment(),
    Searchable<T> {
    override var data: List<T> = emptyList()
    override val filteredData: MutableLiveData<List<T>> by lazy {
        MutableLiveData<List<T>>()
    }
    override lateinit var searchBar: EditText
    override lateinit var listView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_searchable_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        searchBar = view.findViewById(search_bar)
        listView = view.findViewById(R.id.list)
        searchBar.addTextChangedListener(textWatcher)

        val refreshLayout = view.findViewById(R.id.refresh_layout) as ConstraintLayout

        registerForContextMenu(listView)
    }

    override fun onResume() {
        super.onResume()
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            filteredData.value = p0?.let { filter(it, data) } ?: data
        }

        override fun afterTextChanged(p0: Editable?) {
        }

    }

    abstract override fun filter(text: CharSequence, data: List<T>): List<T>
}

interface Searchable<T> {
    var data: List<T>
    val filteredData: MutableLiveData<List<T>>
    var searchBar: EditText
    var listView: RecyclerView

    fun onViewCreated(view: View, savedInstanceState: Bundle?)

    fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View

    fun onResume()

    fun filter(text: CharSequence, data: List<T>): List<T>
}