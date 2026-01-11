package com.example.project.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.project.R
import com.example.project.viewmodel.auth.AuthViewModel
import com.example.project.viewmodel.auth.AuthViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment: Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels {
        AuthViewModelFactory()
    }

    private lateinit var tvGreeting: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvGreeting = view.findViewById(R.id.tvGreeting)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val composeView = view.findViewById<ComposeView>(R.id.composeCarousel)
        val fabProfile = view.findViewById<FloatingActionButton>(R.id.fabProfile)
        val cardMembership = view.findViewById<MaterialCardView>(R.id.cardMembership)
        val cardMyTrainings = view.findViewById<MaterialCardView>(R.id.cardMyTrainings)
        val cardSchedule = view.findViewById<MaterialCardView>(R.id.cardSchedule)
        val navController = findNavController()


        val cards = listOf(
            InfoCard(
                title = "Про нас",
                imageRes = R.drawable.card4,   // твоя картинка
                textRes = R.string.info_about_full    // довгий текст
            ),
            InfoCard(
                title = "Контактна інформація",
                imageRes = R.drawable.card1,   // твоя картинка
                textRes = R.string.info_contacts    // довгий текст
            ),
            InfoCard(
                title = "Правила студії",
                imageRes = R.drawable.card2,   // твоя картинка
                textRes = R.string.info_rules    // довгий текст
            )
        )

        composeView.setContent {
            MaterialTheme {
                InfoCarousel(
                    items = cards,
                    onCardClick = { card ->
                        navController.navigate(
                            R.id.action_homeFragment_to_infoDetailsFragment,
                            bundleOf(
                                "title" to card.title,
                                "imageRes" to card.imageRes,
                                "textRes" to card.textRes
                            )
                        )
                    }
                )
            }
        }


        authViewModel.currentProfile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                tvGreeting.text = "Привіт, ${profile.firstName}!"
            } else {
                tvGreeting.text = "Привіт!"
            }
        }

        fabProfile.setOnClickListener {
            showProfileDialog(navController)
        }

        cardMembership.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_membershipFragment)
        }

        cardMyTrainings.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_trainingFragment)
        }

        cardSchedule.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_scheduleFragment)
        }

    }

    private fun showProfileDialog(navController: NavController) {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_profile, null)

        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)

        val profile = authViewModel.currentProfile.value
        val firebaseEmail = authViewModel.getCurrentUserEmail()

        val fullName = when {
            !profile?.firstName.isNullOrBlank() || !profile?.lastName.isNullOrBlank() ->
                "${profile?.firstName.orEmpty()} ${profile?.lastName.orEmpty()}".trim()
            else -> "Користувач"
        }

        tvName.text = fullName
        tvEmail.text = profile?.email?.takeIf { it.isNotBlank() } ?: firebaseEmail ?: "—"

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .create()

        btnLogout.setOnClickListener {
            dialog.dismiss()

            authViewModel.logout()

            navController.popBackStack(R.id.GoogleLoginFragment, false)
        }

        dialog.show()
    }
}