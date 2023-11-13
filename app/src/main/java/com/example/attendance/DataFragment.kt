package com.example.attendance

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.database.*

class DataFragment : Fragment() {
    private lateinit var resultTextView: TextView
    private lateinit var resultTextView2: TextView
    private lateinit var DescTextView: TextView
    lateinit var button: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var namesRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_data, container, false)
        resultTextView = rootView.findViewById(R.id.resultTextView)
        resultTextView2 = rootView.findViewById(R.id.resultTextView2)
        DescTextView = rootView.findViewById(R.id.tv_desc1)
        database = FirebaseDatabase.getInstance()
        namesRef = database.getReference("1c27wQexbj78D4NFKKGyyJosZGeHWAgbdlJe2MChQ4zY/Sheet1")
        button = rootView.findViewById(R.id.button)
        button.setOnClickListener {
            val context = requireContext()
            val moveIntent = Intent(context, ChartActivity::class.java)
            context.startActivity(moveIntent)
        }


        fetchDataFromFirebase()
        fetchMostRecentName()

        return rootView
    }
    private fun fetchDataFromFirebase() {
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
                        nameCounts.filterValues { it == maxDuplicateCount }.keys
                    val resultText =
                        " $mostDuplicateNames"
                    resultTextView.text = resultText
                    val descText =
                        "Dengan total kehadiran paling banyak"
                    DescTextView.text = descText
                } else {
                    resultTextView.text = "Tidak ada nama dengan duplikat."
                }
            }

            override fun onCancelled(error: DatabaseError) {
                resultTextView.text = "Error: ${error.message}"
            }
        })
    }
    private fun fetchMostRecentName() {
        namesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val namesOnMostRecentDate = mutableSetOf<String>()
                var mostRecentDate = ""

                for (snapshot in dataSnapshot.children) {
                    val nameMap = snapshot.value as HashMap<*, *>
                    val name = nameMap["Name"] as String?
                    val date = nameMap["DateTime"] as String?

                    if (name != null && date != null) {
                        if (date > mostRecentDate) {
                            mostRecentDate = date
                            namesOnMostRecentDate.clear()
                        }
                        if (date == mostRecentDate) {
                            namesOnMostRecentDate.add(name)
                        }
                    }
                }

                val totalNamesOnMostRecentDate = namesOnMostRecentDate.size

                if (totalNamesOnMostRecentDate > 0) {
                    val resultText = "Jumlah yang hadir hari ini \n$totalNamesOnMostRecentDate"
                    resultTextView2.text = resultText
                } else {
                    resultTextView2.text = "Tidak ada nama yang hadir pada tanggal terbaru."
                }
            }

            override fun onCancelled(error: DatabaseError) {
                resultTextView.text = "Error: ${error.message}"
            }
        })
    }
}