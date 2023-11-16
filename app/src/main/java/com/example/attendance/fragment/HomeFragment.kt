package com.example.attendance.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.attendance.R
import com.example.attendance.absent.AbsentActivity
import com.example.attendance.attend.AttendanceActivity
import com.example.attendance.employee.EmployeeActivity
import com.example.attendance.late.LateActivity
import com.google.firebase.database.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.HashMap

class HomeFragment : Fragment() {
    private lateinit var tvPegawai: TextView
    private lateinit var database: FirebaseDatabase
    private lateinit var namesRef: DatabaseReference
    private lateinit var cvPegawai: CardView
    private lateinit var cvLate: CardView
    private lateinit var cvAttend: CardView
    private lateinit var cvAbsent: CardView


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        tvPegawai = rootView.findViewById(R.id.tv_pegawai)
        database = FirebaseDatabase.getInstance()
        namesRef = database.getReference("1c27wQexbj78D4NFKKGyyJosZGeHWAgbdlJe2MChQ4zY/Sheet1")

        cvPegawai  = rootView.findViewById(R.id.cv_pegawai)
        cvPegawai.setOnClickListener {
            val moveIntent = Intent(requireActivity(), EmployeeActivity::class.java)
            startActivity(moveIntent)
        }
        cvLate  = rootView.findViewById(R.id.cv_late)
        cvLate.setOnClickListener {
            val moveIntent = Intent(requireActivity(), LateActivity::class.java)
            startActivity(moveIntent)
        }
        cvAttend  = rootView.findViewById(R.id.cv_attend)
        cvAttend.setOnClickListener {
            val moveIntent = Intent(requireActivity(), AttendanceActivity::class.java)
            startActivity(moveIntent)
        }
        cvAbsent  = rootView.findViewById(R.id.cv_absent)
        cvAbsent.setOnClickListener {
            val moveIntent = Intent(requireActivity(), AbsentActivity::class.java)
            startActivity(moveIntent)
        }

        fetchUniqueNames()

        return rootView
    }
    private fun fetchUniqueNames() {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val query = databaseReference.child("1c27wQexbj78D4NFKKGyyJosZGeHWAgbdlJe2MChQ4zY/Sheet1") // Sesuaikan dengan path di Firebase

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val uniqueNames = mutableSetOf<String>()

                for (snapshot in dataSnapshot.children) {
                    val nameMap = snapshot.value as HashMap<*, *>
                    val name = nameMap["Name"] as String?

                    if (name != null) {
                        uniqueNames.add(name)
                    }
                }

                val totalUniqueNames = uniqueNames.size

                if (totalUniqueNames > 0) {
                    val jumlah = "$totalUniqueNames"
                    tvPegawai.text = jumlah
                } else {
                    tvPegawai.text = "Tidak ada nama yang berbeda."
                }
            }

            override fun onCancelled(error: DatabaseError) {
                tvPegawai.text = "Error: ${error.message}"
            }
        })
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mendapatkan tanggal saat ini
        val currentDate = LocalDate.now()

        // Format tanggal menggunakan DateTimeFormatter
        val dateFormatter = DateTimeFormatter.ofPattern("dd")
        val formattedDate = currentDate.format(dateFormatter)

        // Mendapatkan bulan dan tahun saat ini
        val month = currentDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        val year = currentDate.year

        // Mengakses elemen-elemen TextView dari XML
        val textViewDate: TextView = view.findViewById(R.id.tv_day)
        val textViewMonth: TextView = view.findViewById(R.id.tv_month)
        val textViewYear: TextView = view.findViewById(R.id.tv_year)

        // Menampilkan hasil di dalam TextView
        textViewDate.text = "$formattedDate"
        textViewMonth.text = "$month"
        textViewYear.text = "$year"
    }
}