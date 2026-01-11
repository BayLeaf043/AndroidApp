package com.example.project.ui.membership

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.project.R
import com.example.project.data.model.MyMembershipUi
import com.example.project.viewmodel.membership.MyMembershipViewModel
import com.example.project.viewmodel.membership.MyMembershipViewModelFactory
import com.google.android.material.button.MaterialButton
import com.example.project.data.remote.FirebaseProvider

class MembershipFragment: Fragment() {

    private val vm: MyMembershipViewModel by viewModels { MyMembershipViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_membership, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = FirebaseProvider.auth.currentUser?.uid

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnOpenCatalog = view.findViewById<MaterialButton>(R.id.btnOpenCatalog)
        val btnOpenArchive = view.findViewById<MaterialButton>(R.id.btnOpenArchive)
        val cardNoMembership = view.findViewById<View>(R.id.cardNoMembership)
        val compose = view.findViewById<ComposeView>(R.id.composeMemberships)


        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        btnOpenCatalog.setOnClickListener {
            findNavController().navigate(
                R.id.action_membershipFragment_to_membershipCatalogFragment
            )
        }

        btnOpenArchive.setOnClickListener {
            findNavController().navigate(R.id.action_membershipFragment_to_membershipArchiveFragment)
        }

        vm.error.observe(viewLifecycleOwner) { err ->
            if (!err.isNullOrBlank()) Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
        }

        fun render(items: List<MyMembershipUi>, activatingId: String?) {
            val hasItems = items.isNotEmpty()
            cardNoMembership.visibility = if (hasItems) View.GONE else View.VISIBLE
            compose.visibility = if (hasItems) View.VISIBLE else View.GONE

            if (hasItems) {
                compose.setContent {
                    MaterialTheme {
                        MembershipPager(
                            items = items,
                            activatingId = activatingId,
                            onActivateToday = { docId ->
                                uid?.let {
                                    vm.activateToday(docId, 30, it)
                                }
                            }
                        )
                    }
                }
            }
        }

        var lastItems: List<MyMembershipUi> = emptyList()
        var lastActivating: String? = null

        vm.items.observe(viewLifecycleOwner) {
            lastItems = it
            render(lastItems, lastActivating)
        }
        vm.activatingId.observe(viewLifecycleOwner) {
            lastActivating = it
            render(lastItems, lastActivating)
        }

        if (uid != null) {
            vm.load(uid) // тут уже String
        } else {
            Toast.makeText(requireContext(), "Користувач не авторизований", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }
}