package de.tu_darmstadt.seemoo.LARS.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.tu_darmstadt.seemoo.LARS.R


class AboutFragment : Fragment(), AboutViewAdapter.OnListInteractionListener {

    private lateinit var aboutViewModel: AboutViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: AboutViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        aboutViewModel =
            ViewModelProvider(requireActivity()).get(AboutViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_about, container, false)
        val textView: TextView = root.findViewById(R.id.text_about)
        aboutViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewAdapter = AboutViewAdapter(this, arrayListOf())
        val lm = LinearLayoutManager(context)
        recyclerView = view.findViewById<RecyclerView>(R.id.frag_about_recycler).apply {
            layoutManager = lm
            adapter = viewAdapter

            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }

        aboutViewModel.libList.observe(viewLifecycleOwner, {
            it?.run {
                viewAdapter.replaceElements(this)
            }
        })

        aboutViewModel.currentLicenseText.observe(viewLifecycleOwner, {
            it?.run {
                val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
                alertDialogBuilder.setMessage(this)
                alertDialogBuilder.setCancelable(true)

                alertDialogBuilder.setPositiveButton(
                    getString(android.R.string.ok)
                ) { dialog, _ ->
                    dialog.cancel()
                }

                val alertDialog: AlertDialog = alertDialogBuilder.create()
                alertDialog.show()
                aboutViewModel.resetLicenseText()
            }
        })
        aboutViewModel.loadData(requireContext())
    }

    data class About(val name: String, val resource: Int)

    override fun onListItemClicked(item: About) {
        aboutViewModel.loadLicense(requireContext(),item.resource)
    }
}