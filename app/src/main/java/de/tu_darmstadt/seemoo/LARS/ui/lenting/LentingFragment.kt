package de.tu_darmstadt.seemoo.LARS.ui.lenting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import de.tu_darmstadt.seemoo.LARS.R

class LentingFragment : Fragment() {

    private lateinit var lentingViewModel: LentingViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        lentingViewModel =
            ViewModelProvider(requireActivity()).get(LentingViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_lenting, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        lentingViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}