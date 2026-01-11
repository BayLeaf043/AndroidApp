package com.example.project.ui.membership

import androidx.fragment.app.Fragment
import com.example.project.viewmodel.membership.PurchaseViewModel
import androidx.fragment.app.viewModels
import com.example.project.viewmodel.membership.PurchaseViewModelFactory
import android.widget.TextView
import android.widget.Button
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.example.project.R
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import android.widget.RadioGroup

class PurchaseMembershipFragment:Fragment() {
    private val vm: PurchaseViewModel by viewModels { PurchaseViewModelFactory() }

    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvTotal: TextView

    private lateinit var rgAge: RadioGroup
    private lateinit var tvCompatibleGroups: TextView

    private lateinit var btnBuy: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_membership_purchase, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val serviceId = requireArguments().getString("serviceId")
        if (serviceId.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Не знайдено послугу", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        val card = view.findViewById<View>(R.id.cardMembership)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

        tvTitle = card.findViewById(R.id.tvTitle)
        tvSubtitle = card.findViewById(R.id.tvSubtitle)
        tvPrice = card.findViewById(R.id.tvPrice)

        tvTotal = view.findViewById(R.id.tvTotal)
        btnBuy = view.findViewById(R.id.btnBuy)

        rgAge = view.findViewById(R.id.rgAgeGroup)
        rgAge.check(R.id.rbAge18plus)

        tvCompatibleGroups = view.findViewById(R.id.tvCompatibleGroups)

        btnBack.setOnClickListener { findNavController().navigateUp() }
        btnBuy.setOnClickListener { vm.buy() }

        setupAgeGroupSelector()
        observe()

        vm.load(serviceId)
    }

    private fun setupAgeGroupSelector() {
        rgAge.setOnCheckedChangeListener { _, checkedId ->
            val age = when (checkedId) {
                R.id.rbAge5_9 -> "5-9"
                R.id.rbAge10_17 -> "10-17"
                else -> "18+"
            }
            vm.setAgeGroup(age)
        }
    }

    private fun observe() {
        vm.service.observe(viewLifecycleOwner) { s ->
            if (s == null) return@observe

            tvTitle.text = s.title
            tvSubtitle.text = "${s.trainingType.uppercase()} ${s.level} · ${s.sessionsCount} занять"
            tvPrice.text = "${s.price} грн"
            tvTotal.text = "Сума: ${s.price} грн"
        }

        vm.compatibleGroupsText.observe(viewLifecycleOwner) { text ->
            tvCompatibleGroups.text = text
        }

        vm.compatibleGroups.observe(viewLifecycleOwner) { groups ->
            btnBuy.isEnabled = (vm.loading.value != true) && groups.isNotEmpty()
        }

        vm.loading.observe(viewLifecycleOwner) { isLoading ->
            btnBuy.isEnabled = !isLoading
        }

        vm.error.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        vm.purchaseSuccess.observe(viewLifecycleOwner) { ok ->
            if (ok == true) {
                Toast.makeText(requireContext(), "Абонемент оформлено ✅", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
        vm.purchaseSuccess.value = false
    }

}