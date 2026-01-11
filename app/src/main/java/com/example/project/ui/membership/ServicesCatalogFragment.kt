package com.example.project.ui.membership

import androidx.fragment.app.Fragment
import com.example.project.viewmodel.service.ServicesCatalogViewModel
import androidx.fragment.app.viewModels
import com.example.project.viewmodel.service.ServicesCatalogViewModelFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import com.example.project.R
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import androidx.core.widget.doOnTextChanged
import androidx.core.os.bundleOf
import com.google.android.material.button.MaterialButton


class ServicesCatalogFragment:Fragment() {

    private val viewModel: ServicesCatalogViewModel by viewModels {
        ServicesCatalogViewModelFactory()
    }

    private lateinit var adapter: ServiceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_services_catalog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val rvMemberships = view.findViewById<RecyclerView>(R.id.rvMemberships)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)
        val etSearch = view.findViewById<TextInputEditText>(R.id.etSearch)

        val btnAll = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAll)
        val btnBasic = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnBasic)
        val btnPro = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPro)

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        adapter = ServiceAdapter(emptyList()) { service ->
            val bundle = bundleOf("serviceId" to service.id)
            findNavController().navigate(
                R.id.action_membershipCatalog_to_membershipPurchase,
                bundle
            )
        }
        rvMemberships.adapter = adapter
        rvMemberships.layoutManager = LinearLayoutManager(requireContext())

        etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.setSearchQuery(text?.toString())
        }

        fun setActiveButton(active: MaterialButton) {
            btnAll.isChecked = false
            btnBasic.isChecked = false
            btnPro.isChecked = false
            active.isChecked = true
        }

        btnAll.setOnClickListener {
            viewModel.setLevelFilter(null) // всі
            setActiveButton(btnAll)
        }
        btnBasic.setOnClickListener {
            viewModel.setLevelFilter("basic")
            setActiveButton(btnBasic)
        }
        btnPro.setOnClickListener {
            viewModel.setLevelFilter("pro")
            setActiveButton(btnPro)
        }

        // за замовчуванням: всі
        setActiveButton(btnAll)
        viewModel.setLevelFilter(null)

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.memberships.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        // 🔥 Event
        viewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        // Стартове завантаження
        if (viewModel.memberships.value.isNullOrEmpty()) {
            viewModel.loadMemberships()
        }
    }
}