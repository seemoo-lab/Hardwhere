package de.tu_darmstadt.seemoo.LARS.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import de.tu_darmstadt.seemoo.LARS.R

class AboutFragment : Fragment() {

    private lateinit var aboutViewModel: AboutViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        aboutViewModel =
            ViewModelProvider(requireActivity()).get(AboutViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_checkout, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        aboutViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}