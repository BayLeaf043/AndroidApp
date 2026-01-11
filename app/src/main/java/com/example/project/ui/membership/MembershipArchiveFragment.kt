package com.example.project.ui.membership

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.project.R
import com.example.project.viewmodel.membership.MyMembershipViewModel
import com.example.project.viewmodel.membership.MyMembershipViewModelFactory
import kotlin.getValue
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Toast
import com.example.project.data.remote.FirebaseProvider

class MembershipArchiveFragment: Fragment() {

    private val vm: MyMembershipViewModel by viewModels { MyMembershipViewModelFactory() }
    private lateinit var adapter: MembershipArchiveAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_membership_archive, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val rv = view.findViewById<RecyclerView>(R.id.rvMembershipsArchive)
        val progress = view.findViewById<ProgressBar>(R.id.progressBar)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        btnBack.setOnClickListener { findNavController().navigateUp() }

        adapter = MembershipArchiveAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        vm.loading.observe(viewLifecycleOwner) { isLoading ->
            progress.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        vm.error.observe(viewLifecycleOwner) { err ->
            if (!err.isNullOrBlank()) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
            }
        }

        vm.items.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        val uid = FirebaseProvider.auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Користувач не авторизований", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        vm.loadArchive(uid)
    }
}