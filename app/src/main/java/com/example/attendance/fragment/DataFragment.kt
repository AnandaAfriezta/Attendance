package com.example.attendance.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.attendance.R
import com.example.attendance.absent.AbsentActivity
import com.example.attendance.attend.AttendanceActivity
import com.example.attendance.late.LateActivity
import com.google.firebase.database.*

class DataFragment : Fragment() {
    private lateinit var resultTextViewDiligent: TextView
    private lateinit var descTextViewDiligent: TextView
    lateinit var buttonDiligent: Button
    private lateinit var resultTextViewLate: TextView
    private lateinit var descTextViewLate: TextView
    lateinit var buttonLate: Button
    private lateinit var resultTextViewAbsent: TextView
    private lateinit var descTextViewAbsent: TextView
    lateinit var buttonAbsent: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var namesRef: DatabaseReference


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_data, container, false)
        resultTextViewDiligent = rootView.findViewById(R.id.resultTextViewDiligent)
        descTextViewDiligent = rootView.findViewById(R.id.tv_descDiligent)
        resultTextViewLate = rootView.findViewById(R.id.resultTextViewLate)
        descTextViewLate = rootView.findViewById(R.id.tv_descLate)
        resultTextViewAbsent = rootView.findViewById(R.id.resultTextViewAbsent)
        descTextViewAbsent = rootView.findViewById(R.id.tv_descAbsent)
        database = FirebaseDatabase.getInstance()
        namesRef = database.getReference("1c27wQexbj78D4NFKKGyyJosZGeHWAgbdlJe2MChQ4zY/Sheet1")

        buttonDiligent = rootView.findViewById(R.id.buttonDiligent)
        buttonDiligent.setOnClickListener {
            val context = requireContext()
            val moveIntent = Intent(context, AttendanceActivity::class.java)
            context.startActivity(moveIntent)
        }
        buttonLate = rootView.findViewById(R.id.buttonLate)
        buttonLate.setOnClickListener {
            val context = requireContext()
            val moveIntent = Intent(context, LateActivity::class.java)
            context.startActivity(moveIntent)
        }
        buttonAbsent = rootView.findViewById(R.id.buttonAbsent)
        buttonAbsent.setOnClickListener {
            val context = requireContext()
            val moveIntent = Intent(context, AbsentActivity::class.java)
            context.startActivity(moveIntent)
        }

        fetchDataTelatFromFirebase()
        fetchDataRajinFromFirebase()
        return rootView
    }
    private fun fetchDataRajinFromFirebase() {
        namesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val names = mutableListOf<String>()

                for (snapshot in dataSnapshot.children) {
                    val nameMap = snapshot.value as HashMap<*, *>
                    val name =
                        nameMap["Name"] as String?
                    name?.let {
                        names.add(it)
                    }
                }

                val nameCounts = names.groupingBy { it }.eachCount()
                val maxDuplicateCount = nameCounts.values.maxOrNull()

                if (maxDuplicateCount != null && maxDuplicateCount > 1) {
                    val mostDuplicateNames =
                        nameCounts.filterValues { it == maxDuplicateCount }.keys.toList()

                    val resultText =
                        mostDuplicateNames.joinToString(", ")  // Menggunakan koma sebagai pemisah
                    resultTextViewDiligent.text = resultText
                    val descText =
                        "Dengan total kehadiran paling banyak"
                    descTextViewDiligent.text = descText

                } else {
                    resultTextViewDiligent.text = "Tidak ada nama dengan duplikat."
                }
            }

            override fun onCancelled(error: DatabaseError) {
                resultTextViewDiligent.text = "Error: ${error.message}"
            }
        })
    }
    private fun fetchDataTelatFromFirebase() {
        namesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val names = mutableListOf<String>()
                val statusTelatCountMap = mutableMapOf<String, Int>()

                for (snapshot in dataSnapshot.children) {
                    val nameMap = snapshot.value as HashMap<*, *>
                    val name = nameMap["Name"] as String?
                    val statusTelat = nameMap["Status"] as String?

                    name?.let {
                        names.add(it)
                        // Menambahkan jumlah status terlambat untuk setiap nama
                        if (statusTelat != null && statusTelat == "Terlambat") {
                            statusTelatCountMap[it] = (statusTelatCountMap[it] ?: 0) + 1
                        }
                    }
                }

                val maxStatusTelatCount = statusTelatCountMap.values.maxOrNull()

                if (maxStatusTelatCount != null && maxStatusTelatCount > 1) {
                    val mostStatusTelatNames =
                        statusTelatCountMap.filterValues { it == maxStatusTelatCount }.keys.toList()

                    val resultText =
                        mostStatusTelatNames.joinToString(", ")  // Menggunakan koma sebagai pemisah
                    resultTextViewLate.text = resultText
                    val descText =
                        "Dengan keterlambatan paling banyak"
                    descTextViewLate.text = descText

                } else {
                    resultTextViewLate.text = "Tidak ada nama dengan status terlambat paling banyak."
                }
            }

            override fun onCancelled(error: DatabaseError) {
                resultTextViewLate.text = "Error: ${error.message}"
            }
        })
    }

}

