package com.example.attendance.late

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.attendance.fragment.HomeFragment
import com.example.attendance.R
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class LateActivity : AppCompatActivity() {
    private lateinit var tableLayout: TableLayout
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var startDate: Date
    private lateinit var endDate: Date
    private lateinit var ivBackLate: ImageView
    private lateinit var tvDetail: TextView
    private lateinit var lastLateDates: MutableMap<String, Date>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_late)

        tableLayout = findViewById(R.id.tableLayout)
        database = FirebaseDatabase.getInstance()
        reference = database.reference.child("1c27wQexbj78D4NFKKGyyJosZGeHWAgbdlJe2MChQ4zY/Sheet1")

        ivBackLate = findViewById(R.id.iv_backLate)
        ivBackLate.setOnClickListener {
            val moveIntent = Intent(this@LateActivity, HomeFragment::class.java)
            finish()
        }
        tvDetail = findViewById(R.id.tv_detail)
        tvDetail.setOnClickListener {
            val moveIntent = Intent(this@LateActivity, LateDetail::class.java)
            startActivity(moveIntent)
        }

        startDate = parseDateString("2023-01-01T00:00:00.000Z")
        endDate = Date()
        lastLateDates = mutableMapOf()

        fetchDataFromFirebase()
    }

    private fun fetchDataFromFirebase() {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Clear the table before adding new data
                tableLayout.removeAllViews()

                val namaMap = HashMap<String, Int>()

                for (childSnapshot in dataSnapshot.children) {
                    val nama = childSnapshot.child("Name").getValue(String::class.java)
                    val statusTerlambat = childSnapshot.child("Status").getValue(String::class.java)
                    val dateTimeString = childSnapshot.child("DateTime").getValue(String::class.java)

                    if (nama != null && statusTerlambat != null && dateTimeString != null && statusTerlambat == "Terlambat") {
                        // Convert date string to Date
                        val entryDate = parseDateString(dateTimeString)

                        // Check if the entry date is within the selected date range
                        if (entryDate in startDate..endDate) {
                            // Jika nama sudah ada dalam map dan belum terlambat pada tanggal yang sama, tambahkan 1 ke jumlahnya
                            if (namaMap.containsKey(nama) && !isSameDay(entryDate, lastLateDates[nama])) {
                                namaMap[nama] = namaMap[nama]!! + 1
                                lastLateDates[nama] = entryDate
                            } else if (!namaMap.containsKey(nama)) {
                                // Jika nama belum ada dalam map, tambahkan nama dan set jumlahnya menjadi 1
                                namaMap[nama] = 1
                                lastLateDates[nama] = entryDate
                            }
                        }
                    }
                }

                // Tampilkan hasil
                for ((nama, jumlahTerlambat) in namaMap) {
                    val row = TableRow(this@LateActivity)

                    // Display Name
                    val nameTextView = TextView(this@LateActivity)
                    nameTextView.text = "$nama"
                    nameTextView.layoutParams =
                        TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)

                    // Mengganti font
                    val customFont = ResourcesCompat.getFont(this@LateActivity, R.font.ofmedium)
                    nameTextView.typeface = customFont

                    // Mengganti warna teks
                    nameTextView.setTextColor(ContextCompat.getColor(this@LateActivity,
                        R.color.main
                    ))

                    row.addView(nameTextView)

                    // Display the count of "Terlambat" status
                    val statusTextView = TextView(this@LateActivity)
                    statusTextView.text = "$jumlahTerlambat"
                    statusTextView.layoutParams =
                        TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)

                    // Mengganti font
                    statusTextView.typeface = customFont

                    // Mengganti warna teks
                    statusTextView.setTextColor(ContextCompat.getColor(this@LateActivity,
                        R.color.main
                    ))

                    row.addView(statusTextView)

                    // Menambahkan TableRow ke dalam TableLayout (sesuai dengan struktur UI Anda)
                    tableLayout.addView(row)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("LateActivity", "Failed to read value: ${databaseError.toException()}")
            }
        })
    }

    private fun parseDateString(dateTimeString: String): Date {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC") // Set timezone sesuai kebutuhan, misalnya "UTC"
        return sdf.parse(dateTimeString) ?: Date()
    }

    private fun isSameDay(date1: Date?, date2: Date?): Boolean {
        if (date1 == null || date2 == null) {
            return false
        }

        val cal1 = Calendar.getInstance()
        cal1.time = date1

        val cal2 = Calendar.getInstance()
        cal2.time = date2

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    fun onPickStartDateClicked(view: View) {
        showDatePickerDialog(true)
    }

    fun onPickEndDateClicked(view: View) {
        showDatePickerDialog(false)
    }

    private fun showDatePickerDialog(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                // Set selected date
                calendar.set(year, monthOfYear, dayOfMonth)
                val selectedDate = calendar.time

                if (isStartDate) {
                    startDate = selectedDate
                } else {
                    endDate = selectedDate
                }

                // Fetch data with the selected date range
                fetchDataFromFirebase()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }
}