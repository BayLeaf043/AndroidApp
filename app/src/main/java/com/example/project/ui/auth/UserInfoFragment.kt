package com.example.project.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.project.R
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import android.widget.RadioGroup
import com.google.android.material.button.MaterialButton
import java.util.Calendar
import android.app.DatePickerDialog
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.project.viewmodel.auth.AuthViewModel
import androidx.fragment.app.activityViewModels
import com.example.project.viewmodel.auth.AuthViewModelFactory
import android.widget.ImageButton





class UserInfoFragment: Fragment() {
    private val authViewModel: AuthViewModel by activityViewModels {
        AuthViewModelFactory()
    }

    private lateinit var tilPhone: TextInputLayout
    private lateinit var etPhone: TextInputEditText
    private lateinit var tilFirstName: TextInputLayout
    private lateinit var etFirstName: TextInputEditText
    private lateinit var tilLastName: TextInputLayout
    private lateinit var etLastName: TextInputEditText
    private lateinit var rgGender: RadioGroup
    private lateinit var btnSelectDate: MaterialButton
    private lateinit var btnSaveAndContinue: MaterialButton
    private lateinit var progressBar: View

    private var selectedGender: String? = null
    private var selectedBirthDate: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_info, container, false)

        tilPhone = view.findViewById(R.id.tilPhone)
        etPhone = view.findViewById(R.id.etPhone)

        tilFirstName = view.findViewById(R.id.tilFirstName)
        etFirstName = view.findViewById(R.id.etFirstName)
        tilLastName = view.findViewById(R.id.tilLastName)
        etLastName = view.findViewById(R.id.etLastName)
        rgGender = view.findViewById(R.id.rgGender)
        btnSelectDate = view.findViewById(R.id.btnSelectDate)
        btnSaveAndContinue = view.findViewById(R.id.btnSaveAndContinue)
        progressBar = view.findViewById(R.id.progressBar)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack: ImageButton = view.findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(
                R.id.action_userInfoFragment_to_phoneFragment
            )
        }
        // якщо поле пусте – підставляємо +380 і ставимо курсор в кінець
        if (etPhone.text.isNullOrBlank()) {
            etPhone.setText("+380")
            etPhone.setSelection(etPhone.text?.length ?: 4)
        }

        rgGender.setOnCheckedChangeListener { _, checkedId ->
            selectedGender = when (checkedId) {
                R.id.rbFemale -> "female"
                R.id.rbMale -> "male"
                else -> "other"
            }
        }

        btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        authViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnSaveAndContinue.isEnabled = !isLoading
        }

        authViewModel.errorMessage.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        authViewModel.openHome.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                findNavController().navigate(
                    R.id.action_userInfoFragment_to_homeFragment
                )
            }
        }

        btnSaveAndContinue.setOnClickListener {
            saveProfile()
        }
    }

    private fun saveProfile() {
        val phone = etPhone.text?.toString()?.trim() ?: ""
        val firstName = etFirstName.text?.toString()?.trim() ?: ""
        val lastName = etLastName.text?.toString()?.trim() ?: ""
        val gender = selectedGender ?: ""
        val birthDate = selectedBirthDate ?: ""

        var hasError = false

        if (phone.isEmpty() || phone == "+380") {
            tilPhone.error = "Введіть номер телефону"
            hasError = true
        } else if (!phone.startsWith("+380")) {
            tilPhone.error = "Номер має починатися з +380"
            hasError = true
        } else {
            val digitsPart = phone.removePrefix("+380")
            if (digitsPart.length != 9 || !digitsPart.all { it.isDigit() }) {
                tilPhone.error = "Введіть 9 цифр після +380"
                hasError = true
            } else {
                tilPhone.error = null
            }
        }

        if (firstName.isEmpty()) {
            tilFirstName.error = "Введіть ім’я"
            hasError = true
        } else tilFirstName.error = null

        if (lastName.isEmpty()) {
            tilLastName.error = "Введіть прізвище"
            hasError = true
        } else tilLastName.error = null

        if (gender.isEmpty()) {
            Toast.makeText(requireContext(), "Оберіть стать", Toast.LENGTH_SHORT).show()
            hasError = true
        }

        if (birthDate.isEmpty()) {
            Toast.makeText(requireContext(), "Оберіть дату народження", Toast.LENGTH_SHORT).show()
            hasError = true
        }

        if (hasError) return

        authViewModel.saveProfile(
            phone = phone,
            firstName = firstName,
            lastName = lastName,
            gender = gender,
            birthDate = birthDate
        )
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val dateStr = String.format("%02d.%02d.%04d", dayOfMonth, month + 1, year)
                selectedBirthDate = dateStr
                btnSelectDate.text = dateStr
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        dialog.show()
    }
}