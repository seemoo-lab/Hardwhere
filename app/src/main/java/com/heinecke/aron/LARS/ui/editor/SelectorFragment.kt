package com.heinecke.aron.LARS.ui.editor

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heinecke.aron.LARS.R
import com.heinecke.aron.LARS.data.model.Selectable


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [SelectorFragment.OnListFragmentInteractionListener] interface.
 */
class SelectorFragment : Fragment(),
    MySelectorRecyclerViewAdapter.OnListFragmentInteractionListener {

    // currently selected item or null
    private var selectable: Selectable? = null
    private lateinit var viewModel: SelectorViewModel
    private var returnCode: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(requireParentFragment())[SelectorViewModel::class.java]
        arguments?.let {
            selectable = it.getParcelable(ARG_SELECTABLE)
            returnCode = it.getInt(ARG_RETURN_CODE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_selector_list, container, false)
        setHasOptionsMenu(true)
        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = MySelectorRecyclerViewAdapter(this@SelectorFragment)
            }
        }
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.select,menu)
    }

    class SelectorData(
        /** Item that is selected **/
        val item: Selectable,
        /** InputID that got passed, can be used by caller for input identification **/
        val inputID: Int)

    companion object {
        const val ARG_SELECTABLE = "selectable"
        const val ARG_RETURN_CODE = "returnCode"

        /**
         * Get new pair of fragment id,args for spawn on NavController
         */
        @JvmStatic
        fun newInstancePair(selectable: Selectable?, returnCode: Int) : Pair<Int,Bundle> {
            val args = Bundle().apply {
                putParcelable(ARG_SELECTABLE,selectable)
                putInt(ARG_RETURN_CODE,returnCode)
            }
            return Pair(R.id.selector_fragment,args)
        }
    }

    override fun onListFragmentInteraction(item: Selectable) {
        viewModel.setSelected(SelectorData(item,returnCode))
    }
}
