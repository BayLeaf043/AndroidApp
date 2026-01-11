package com.example.project.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.data.model.ScheduleUi
import com.example.project.data.remote.SessionsRemoteDataSource
import com.example.project.data.repository.ScheduleGeneratorRepository
import com.example.project.viewmodel.schedule.CalendarViewModel
import com.example.project.viewmodel.schedule.ScheduleViewModel
import com.example.project.viewmodel.schedule.ScheduleViewModelFactory
import java.util.Calendar

class ScheduleFragment: Fragment() {

    private val calendarViewModel: CalendarViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels {
        ScheduleViewModelFactory()
    }
    private lateinit var btnBack: ImageButton
    private lateinit var tvMonthTitle: TextView
    private lateinit var rvDays: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var rvTrainings: RecyclerView
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton

    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var daysAdapter: DaysAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        tvMonthTitle = view.findViewById(R.id.tvMonthTitle)
        rvDays = view.findViewById(R.id.rvDays)
        rvTrainings = view.findViewById(R.id.rvTrainings)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        btnPrevMonth = view.findViewById<ImageButton>(R.id.btnPrevMonth)
        btnNextMonth = view.findViewById<ImageButton>(R.id.btnNextMonth)

        btnPrevMonth.setOnClickListener { calendarViewModel.prevMonth() }
        btnNextMonth.setOnClickListener { calendarViewModel.nextMonth() }

        btnBack.setOnClickListener { findNavController().navigateUp() }

        daysAdapter = DaysAdapter(emptyList()) { pos ->
            calendarViewModel.selectDay(pos)
        }

        rvDays.apply {
            adapter = daysAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        scheduleAdapter = ScheduleAdapter { training ->
            onTrainingClick(training)
        }
        rvTrainings.apply {
            adapter = scheduleAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        observeVm()
        // автозаповнення sessions (якщо ще немає розкладу)
        scheduleViewModel.ensureSeeded(
            daysAhead = 365,
            reloadDate = calendarViewModel.selectedDate.value ?: Calendar.getInstance()
        )
    }

    private fun observeVm() {

        calendarViewModel.days.observe(viewLifecycleOwner) { list ->
            daysAdapter.submitList(list)
        }

        calendarViewModel.selectedIndex.observe(viewLifecycleOwner) { index ->
            daysAdapter.selectedPosition = index
            rvDays.post { rvDays.smoothScrollToPosition(index) }
        }

        calendarViewModel.selectedDate.observe(viewLifecycleOwner) { cal ->
            scheduleViewModel.loadForDate(cal)
        }

        calendarViewModel.monthCalendar.observe(viewLifecycleOwner) { cal ->
            tvMonthTitle.text = "${monthName(cal)} ${cal.get(Calendar.YEAR)}"
        }

        scheduleViewModel.trainings.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                rvTrainings.visibility = View.GONE
            } else {
                tvEmpty.visibility = View.GONE
                rvTrainings.visibility = View.VISIBLE
                scheduleAdapter.submitList(list)
            }
        }
    }

    private fun monthName(cal: Calendar): String = when (cal.get(Calendar.MONTH)) {
        Calendar.JANUARY -> "Січень"
        Calendar.FEBRUARY -> "Лютий"
        Calendar.MARCH -> "Березень"
        Calendar.APRIL -> "Квітень"
        Calendar.MAY -> "Травень"
        Calendar.JUNE -> "Червень"
        Calendar.JULY -> "Липень"
        Calendar.AUGUST -> "Серпень"
        Calendar.SEPTEMBER -> "Вересень"
        Calendar.OCTOBER -> "Жовтень"
        Calendar.NOVEMBER -> "Листопад"
        else -> "Грудень"
    }

    private fun onTrainingClick(item: ScheduleUi) {
        TrainingDetailsBottomSheet.newInstance(item).show(parentFragmentManager, "TrainingDetails")
    }

}