package com.example.project.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.project.R
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.example.project.viewmodel.auth.AuthViewModel
import androidx.fragment.app.activityViewModels
import com.example.project.viewmodel.auth.AuthViewModelFactory
import android.widget.ProgressBar
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class GoogleLoginFragment : Fragment() {

    private val authViewModel: AuthViewModel by activityViewModels {
        AuthViewModelFactory()
    }

    private lateinit var btnContinue: MaterialButton
    private lateinit var progressBar: ProgressBar

    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    authViewModel.signInWithGoogle(idToken)
                } else {
                    Toast.makeText(requireContext(), "Немає idToken", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Помилка Google Sign-In", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_google_login, container, false)
        btnContinue = view.findViewById(R.id.btnContinue)
        progressBar = view.findViewById(R.id.progressBar)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGoogleClient()
        observeViewModel()

        // перевіряємо, чи користувач вже залогінений
        authViewModel.checkCurrentUser()

        btnContinue.setOnClickListener {
            startGoogleSignIn()
        }
    }

    private fun observeViewModel() {

        authViewModel.loading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            btnContinue.isEnabled = !loading
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        authViewModel.openUserInfo.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    R.id.action_phoneFragment_to_userInfoFragment
                )
            }
        }

        authViewModel.openHome.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    R.id.action_phoneFragment_to_homeFragment
                )
            }
        }
    }

    private fun setupGoogleClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    private fun startGoogleSignIn() {
        googleSignInClient.signOut().addOnCompleteListener {
            googleLauncher.launch(googleSignInClient.signInIntent)
        }
    }

}