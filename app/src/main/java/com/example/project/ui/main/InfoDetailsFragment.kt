package com.example.project.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.R

class InfoDetailsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_info_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val ivBanner = view.findViewById<ImageView>(R.id.ivBanner)
        val tvContent = view.findViewById<TextView>(R.id.tvContent)

        val title = arguments?.getString("title") ?: ""
        val imageRes = arguments?.getInt("imageRes") ?: 0
        val textRes = arguments?.getInt("textRes") ?: 0

        tvContent.text = HtmlCompat.fromHtml(
            getString(R.string.info_rules),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        tvTitle.text = title

        if (imageRes != 0) {
            ivBanner.setImageResource(imageRes)
        }

        if (textRes != 0) {
            tvContent.setText(textRes)
        }

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}