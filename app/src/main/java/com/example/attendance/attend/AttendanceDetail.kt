package com.example.attendance.attend

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.attendance.R
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class AttendanceDetail : AppCompatActivity() {
    private lateinit var tableLayout: TableLayout
    private lateinit var databaseReference: DatabaseReference
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button
    private lateinit var ivBack: ImageView
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null
    private val dataMap = HashMap<String, Date>()
    private val dataList = mutableListOf<Triple<String, String, String>>()
    private var currentPage = 0
    private val pageSize = 10

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_detail)

        tableLayout = findViewById(R.id.tableLayout)
        startDateButton = findViewById(R.id.btnPickStartDate)
        endDateButton = findViewById(R.id.btnPickEndDate)
        nextButton = findViewById(R.id.btn_next)
        previousButton = findViewById(R.id.btn_previous)
        nextButton.isEnabled = false
        previousButton.isEnabled = false
        databaseReference = FirebaseDatabase.getInstance().reference.child("1c27wQexbj78D4NFKKGyyJosZGeHWAgbdlJe2MChQ4zY/Sheet1")

        ivBack = findViewById(R.id.iv_backAttend2)
        ivBack.setOnClickListener {
            val moveIntent = Intent(this@AttendanceDetail, AttendanceActivity::class.java)
            finish()
        }

        startDateButton.setOnClickListener {
            showDatePicker { selectedDate ->
                startDate = selectedDate
                updateTable()
            }
        }

        endDateButton.setOnClickListener {
            showDatePicker { selectedDate ->
                endDate = selectedDate
                updateTable()
            }
        }

        nextButton.setOnClickListener {
            if ((currentPage + 1) * pageSize < dataList.size) {
                currentPage++
                updateTable()
            }
        }

        previousButton.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                updateTable()
            }
        }

        fetchDataFromFirebase()
    }

    private fun fetchDataFromFirebase() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataMap.clear()
                dataList.clear()

                for (childSnapshot in snapshot.children) {
                    val nama = childSnapshot.child("Name").value.toString()
                    val dateString = childSnapshot.child("DateTime").value.toString()

                    if (isDateInRange(dateString) && !isAlreadyAddedToday(nama, dateString)) {
                        val formattedDate = formatDate(dateString)
                        val localDate = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                        addRowToFilteredData(nama, formattedDate)
                        dataMap[nama] = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                    }
                }

                updateTable()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun isAlreadyAddedToday(nama: String, dateString: String): Boolean {
        val dateWithoutTime = LocalDate.parse(dateString.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        return dataMap.containsKey(nama) && dateWithoutTime == dataMap[nama]?.toInstant()
            ?.atZone(ZoneId.systemDefault())?.toLocalDate()
    }

    private fun isDateInRange(dateString: String): Boolean {
        val date = LocalDate.parse(dateString.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        return (startDate == null || date.isEqual(startDate) || date.isAfter(startDate) || date.isEqual(startDate!!.plusDays(1))) &&
                (endDate == null || date.isEqual(endDate) || date.isBefore(endDate) || date.isEqual(endDate!!.plusDays(1)))
    }

    private fun formatDate(dateString: String): String {
        val inputDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val outputDateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        try {
            val date = LocalDate.parse(dateString, inputDateFormat)
            return outputDateFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    private fun addRowToTable(nama: String, tanggal: String) {
        val tableRow = TableRow(this)

        val params = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tableRow.layoutParams = params

        val textViewNama = TextView(this)
        val textViewTanggal = TextView(this)

        val customTypeface = ResourcesCompat.getFont(this, R.font.ofmedium)
        textViewNama.typeface = customTypeface
        textViewTanggal.typeface = customTypeface

        textViewNama.setTextColor(ContextCompat.getColor(this, R.color.main))
        textViewTanggal.setTextColor(ContextCompat.getColor(this, R.color.main))

        textViewNama.text = nama
        textViewTanggal.text = tanggal

        tableRow.addView(textViewNama)
        tableRow.addView(textViewTanggal)

        tableLayout.addView(tableRow)
    }

    private fun showDatePicker(onDateSelected: (LocalDate) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                onDateSelected(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun addRowToFilteredData(nama: String, tanggal: String) {
        dataList.add(Triple(nama, tanggal, ""))
    }

    private fun updateTable() {
        tableLayout.removeAllViews()

        val filteredDataInRange = dataList.filter { data ->
            val date = LocalDate.parse(data.second, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            (startDate == null || date.isAfter(startDate) || date.isEqual(startDate)) &&
                    (endDate == null || date.isBefore(endDate) || date.isEqual(endDate))
        }

        val startIndex = currentPage * pageSize
        val endIndex = minOf((currentPage + 1) * pageSize, filteredDataInRange.size)

        for (i in startIndex until endIndex) {
            addRowToTable(
                filteredDataInRange[i].first,
                filteredDataInRange[i].second
            )
        }

        // Enable/disable Next and Previous buttons based on data availability
        nextButton.isEnabled = endIndex < filteredDataInRange.size
        previousButton.isEnabled = currentPage > 0
    }
}