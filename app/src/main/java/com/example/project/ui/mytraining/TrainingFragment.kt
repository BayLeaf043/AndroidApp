package com.example.project.ui.mytraining

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.R
import com.example.project.viewmodel.mytraining.MyTrainingsViewModel
import androidx.fragment.app.viewModels
import com.example.project.viewmodel.mytraining.MyTrainingsViewModelFactory
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
class TrainingFragment: Fragment() {

    private val vm: MyTrainingsViewModel by viewModels { MyTrainingsViewModelFactory() }
    private lateinit var adapter: MyTrainingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_trainings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnPrevMonth = view.findViewById<ImageButton>(R.id.btnPrevMonth)
        val btnNextMonth = view.findViewById<ImageButton>(R.id.btnNextMonth)
        val tvMonthTitle = view.findViewById<TextView>(R.id.tvMonthTitle)

        val rv = view.findViewById<RecyclerView>(R.id.rvMyTrainings)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)
        val progress = view.findViewById<ProgressBar>(R.id.progressBar)

        btnBack.setOnClickListener { findNavController().navigateUp() }

        adapter = MyTrainingsAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Користувач не авторизований", Toast.LENGTH_SHORT).show()
            return
        }

        vm.init()

        btnPrevMonth.setOnClickListener {
            vm.prevMonth()
            vm.load(uid)
        }
        btnNextMonth.setOnClickListener {
            vm.nextMonth()
            vm.load(uid)
        }

        vm.monthTitle.observe(viewLifecycleOwner) { tvMonthTitle.text = it }

        vm.loading.observe(viewLifecycleOwner) { loading ->
            progress.visibility = if (loading) View.VISIBLE else View.GONE
        }

        vm.error.observe(viewLifecycleOwner) { err ->
            if (!err.isNullOrBlank()) Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
        }

        vm.items.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            rv.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
        }

        vm.load(uid)
    }
    }

