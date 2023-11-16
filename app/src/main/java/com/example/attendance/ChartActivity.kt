package com.example.attendance

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.db.williamchart.view.BarChartView
import com.db.williamchart.view.HorizontalBarChartView
import com.example.attendance.fragment.ReportFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChartActivity : AppCompatActivity() {
    private lateinit var barChart: BarChartView
    private lateinit var horizontabarchart : HorizontalBarChartView
    lateinit var ivBack: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)
        supportActionBar?.hide()

        horizontabarchart = findViewById(R.id.verticalbarchart)
        ivBack = findViewById(R.id.iv_back)
        ivBack.setOnClickListener {
            val moveIntent = Intent(this@ChartActivity, ReportFragment::class.java)
            finish()
        }
        // Mengambil referensi ke tabel kehadiran
        val database = FirebaseDatabase.getInstance()
        val kehadiranRef = database.getReference("1c27wQexbj78D4NFKKGyyJosZGeHWAgbdlJe2MChQ4zY/Sheet1")


        kehadiranRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nameCounts = mutableMapOf<String, Int>()
                // Loop melalui semua kehadiran
                for (kehadiranSnapshot in snapshot.children) {
                    val name = kehadiranSnapshot.child("Name").getValue(String::class.java)
                    // Tambahkan jumlah pembelian untuk name tertentu
                    if (name != null) {
                        if (nameCounts.containsKey(name)) {
                            nameCounts[name] = nameCounts[name]!! + 1
                        } else {
                            nameCounts[name] = 1
                        }
                    }
                }


                val entries = mutableListOf<Pair<String, Float>>()
                for ((name, count) in nameCounts) {
                    entries.add(name to count.toFloat())
                }
                //barChart.animate(entries)
                horizontabarchart.animate(entries)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Penanganan kesalahan jika ada
                val message = databaseError.message
            }
        })
    }
}