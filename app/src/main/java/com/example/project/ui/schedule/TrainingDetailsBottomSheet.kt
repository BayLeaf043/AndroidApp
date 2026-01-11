package com.example.project.ui.schedule

import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import android.widget.ProgressBar
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import com.example.project.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.widget.Toast
import com.example.project.data.model.ScheduleUi
import java.util.Calendar
import com.example.project.viewmodel.schedule.TrainingDetailsViewModel
import androidx.fragment.app.viewModels
import com.example.project.viewmodel.schedule.TrainingDetailsViewModelFactory
import com.example.project.data.remote.FirebaseProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.project.data.remote.FirestorePaths
import com.google.firebase.firestore.FirebaseFirestore

class TrainingDetailsBottomSheet: BottomSheetDialogFragment() {

    private val vm: TrainingDetailsViewModel by viewModels { TrainingDetailsViewModelFactory() }
    private val auth get() = FirebaseProvider.auth
    private val db: FirebaseFirestore = FirebaseProvider.db


    private lateinit var btnClose: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var tvTrainer: TextView
    private lateinit var tvDateTime: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvMeta: TextView
    private lateinit var tvCapacity: TextView
    private lateinit var tvHasMembership: TextView

    private lateinit var cardHasMembership: MaterialCardView
    private lateinit var btnJoinMembership: com.google.android.material.button.MaterialButton
    private lateinit var cardSingle: MaterialCardView
    private lateinit var tvSinglePrice: TextView
    private lateinit var btnJoinSingle: com.google.android.material.button.MaterialButton
    private lateinit var progress: ProgressBar

    private lateinit var sessionId: String
    private lateinit var groupId: String
    private var startAt: Long = 0L
    private var endAt: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_training_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)

        btnClose.setOnClickListener { dismiss() }

        val args = requireArguments()
        sessionId = args.getString(ARG_SESSION_ID).orEmpty()
        groupId = args.getString(ARG_GROUP_ID).orEmpty()
        val title = args.getString(ARG_TITLE).orEmpty()
        val trainer = args.getString(ARG_TRAINER).orEmpty()
        startAt = args.getLong(ARG_START_AT, 0L)
        endAt = args.getLong(ARG_END_AT, 0L)

        if (sessionId.isBlank() || groupId.isBlank() || startAt == 0L) {
            Toast.makeText(requireContext(), "Некоректні дані тренування", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }

        val dateMillis = normalizeDay(startAt)
        val timeText = formatTime(startAt)
        val durationMinutes = calcDurationMinutes(startAt, endAt)

        tvTitle.text = title
        tvTrainer.text = "Тренер: $trainer"
        tvDuration.text = "Тривалість: $durationMinutes хв"
        tvDateTime.text = "Дата/час: ${formatDate(dateMillis)} • $timeText"
        tvMeta.text = "Вік: - , Рівень: - , Тип: -"

        db.collection(FirestorePaths.SESSIONS).document(sessionId).get()
            .addOnSuccessListener { sDoc ->
                val cap = (sDoc.getLong("capacity") ?: 0L).toInt()
                val booked = (sDoc.getLong("bookedActive") ?: 0L).toInt()
                if (cap > 0) {
                    tvCapacity.text = "Місця: ${cap - booked}/$cap"
                } else {
                    tvCapacity.text = "Місця: без обмежень"
                }
            }
            .addOnFailureListener {
                tvCapacity.text = "Місця: -/-"
            }


        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            cardHasMembership.visibility = View.GONE
            cardSingle.visibility = View.GONE
            return
        }

        observe(uid)

        vm.load(uid, sessionId, groupId)

        btnJoinSingle.setOnClickListener { vm.joinSingle(uid, groupId, sessionId) }
        btnJoinMembership.setOnClickListener { vm.joinMembership(uid, groupId, sessionId) }
    }

    private fun bindViews(view: View) {
        btnClose = view.findViewById(R.id.btnClose)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvTrainer = view.findViewById(R.id.tvTrainer)
        tvDateTime = view.findViewById(R.id.tvDateTime)
        tvDuration = view.findViewById(R.id.tvDuration)
        tvMeta = view.findViewById(R.id.tvMeta)
        tvCapacity = view.findViewById(R.id.tvCapacity)
        tvHasMembership = view.findViewById(R.id.tvHasMembership)

        cardHasMembership = view.findViewById(R.id.cardHasMembership)
        btnJoinMembership = view.findViewById(R.id.btnJoinMembership)

        cardSingle = view.findViewById(R.id.cardSingle)
        tvSinglePrice = view.findViewById(R.id.tvSinglePrice)
        btnJoinSingle = view.findViewById(R.id.btnJoinSingle)

        progress = view.findViewById(R.id.progress)
    }

    private fun observe(uid: String) {
        vm.state.observe(viewLifecycleOwner) { st ->
            progress.visibility = if (st.loading || st.actionInProgress) View.VISIBLE else View.GONE

            tvMeta.text = st.metaText

            st.error?.takeIf { it.isNotBlank() }?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }

            // already booked
            if (st.alreadyBooked) {
                cardSingle.visibility = View.GONE
                cardHasMembership.visibility = View.VISIBLE
                tvHasMembership.text = "☑️ Ви вже записані на це тренування"
                btnJoinMembership.visibility = View.GONE
                return@observe
            }

            // membership available
            if (!st.membershipPurchaseId.isNullOrBlank()) {
                cardSingle.visibility = View.GONE
                cardHasMembership.visibility = View.VISIBLE
                tvHasMembership.text = st.membershipMessage ?: "✅ Можна використати абонемент"
                btnJoinMembership.visibility = View.VISIBLE
                btnJoinMembership.isEnabled = !st.actionInProgress
                return@observe
            }

            // else single
            cardHasMembership.visibility = View.GONE
            cardSingle.visibility = View.VISIBLE
            tvSinglePrice.text = st.singlePriceText ?: "Разове тренування: ціна недоступна"
            btnJoinSingle.isEnabled = !st.singleServiceId.isNullOrBlank() && !st.actionInProgress
        }

        vm.singleSuccess.observe(viewLifecycleOwner) { ev ->
            ev?.getContentIfNotHandled()?.let {
                Toast.makeText(requireContext(), "Запис створено ✅", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }

        vm.membershipSuccess.observe(viewLifecycleOwner) { ev ->
            ev?.getContentIfNotHandled()?.let {
                Toast.makeText(requireContext(), "Запис створено ✅", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }

    private fun formatDate(millis: Long): String {
        if (millis == 0L) return "-"
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("uk", "UA"))
        return sdf.format(Date(millis))
    }

    private fun formatTime(millis: Long): String {
        if (millis == 0L) return "--:--"
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        return String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
    }

    private fun calcDurationMinutes(startAt: Long, endAt: Long): Int {
        if (startAt == 0L || endAt == 0L) return 0
        if (endAt <= startAt) return 60
        return ((endAt - startAt) / 60000L).toInt().coerceAtLeast(1)
    }

    companion object {
        private const val ARG_GROUP_ID = "groupId"
        private const val ARG_TITLE = "title"
        private const val ARG_TRAINER = "trainerName"
        private const val ARG_SESSION_ID = "sessionId"
        private const val ARG_START_AT = "startAt"
        private const val ARG_END_AT = "endAt"

        fun newInstance(item: ScheduleUi): TrainingDetailsBottomSheet {
            return TrainingDetailsBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_SESSION_ID, item.sessionId)
                    putString(ARG_GROUP_ID, item.groupId)
                    putString(ARG_TITLE, item.title)
                    putString(ARG_TRAINER, item.trainerName)
                    putLong(ARG_START_AT, item.startAt)
                    putLong(ARG_END_AT, item.endAt)
                }
            }
        }

        private fun normalizeDay(millis: Long): Long {
            val c = Calendar.getInstance().apply { timeInMillis = millis }
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            return c.timeInMillis
        }
    }

}