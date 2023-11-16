package com.example.attendance.late

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.attendance.R
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.min

data class LateData(val nama: String, val tanggal: String, val waktu: String, val status: String)

class LateDetail : AppCompatActivity() {
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
    private val lastLateDataPerDayMap = HashMap<String, MutableSet<LocalDate>>()
    private val lastLateDataMap = HashMap<String, Date>()
    private val filteredLateDataList = mutableListOf<LateData>()
    private var currentPage = 0
    private val pageSize = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_late_detail)

        tableLayout = findViewById(R.id.tableLayout)
        startDateButton = findViewById(R.id.btnPickStartDate)
        endDateButton = findViewById(R.id.btnPickEndDate)
        nextButton = findViewById(R.id.btn_next)
        previousButton = findViewById(R.id.btn_previous)
        nextButton.isEnabled = false
        previousButton.isEnabled = false
        databaseReference = FirebaseDatabase.getInstance().reference.child("1c27wQexbj78D4NFKKGyyJosZGeHWAgbdlJe2MChQ4zY/Sheet1")

        ivBack = findViewById(R.id.iv_backLate2)
        ivBack.setOnClickListener {
            val moveIntent = Intent(this@LateDetail, LateActivity::class.java)
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
            if ((currentPage + 1) * pageSize < filteredLateDataList.size) {
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
                lastLateDataMap.clear()
                lastLateDataPerDayMap.clear()
                filteredLateDataList.clear()

                for (childSnapshot in snapshot.children) {
                    val nama = childSnapshot.child("Name").value.toString()
                    val dateTimeString = childSnapshot.child("DateTime").value.toString()
                    val status = childSnapshot.child("Status").value.toString()

                    if (status == "Terlambat" && isDateInRange(dateTimeString) && !isAlreadyAddedToday(nama, dateTimeString)) {
                        val formattedDate = formatDate(dateTimeString)
                        val formattedTime = formatTime(dateTimeString)
                        val localDate = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                        addRowToFilteredData(LateData(nama, formattedDate, formattedTime, status))
                        addToLastLateDataPerDayMap(nama, localDate)
                        lastLateDataMap[nama] = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                    }
                }

                updateTable()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun isAlreadyAddedToday(nama: String, dateTimeString: String): Boolean {
        val datesSet = lastLateDataPerDayMap[nama]
        val dateWithoutTime = LocalDate.parse(dateTimeString.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        return datesSet != null && dateWithoutTime in datesSet
    }

    private fun addToLastLateDataPerDayMap(nama: String, date: LocalDate) {
        val datesSet = lastLateDataPerDayMap.getOrPut(nama) { mutableSetOf() }
        datesSet.add(date)
    }

    private fun isDateInRange(dateTimeString: String): Boolean {
        val date = LocalDate.parse(dateTimeString.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        return (startDate == null || date.isEqual(startDate) || date.isAfter(startDate) || date.isEqual(startDate!!.plusDays(1))) &&
                (endDate == null || date.isEqual(endDate) || date.isBefore(endDate) || date.isEqual(endDate!!.plusDays(1)))
    }

    private fun formatDate(dateTimeString: String): String {
        val inputDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val outputDateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        try {
            val date = LocalDate.parse(dateTimeString, inputDateFormat)
            return outputDateFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    private fun formatTime(dateTimeString: String): String {
        val inputDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val outputTimeFormat = DateTimeFormatter.ofPattern("HH:mm")

        try {
            val dateTime = LocalDateTime.parse(dateTimeString, inputDateFormat)
            val zonedDateTime = dateTime.atZone(ZoneId.of("UTC"))
            val localDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Jakarta")).toLocalDateTime()

            return outputTimeFormat.format(localDateTime.toLocalTime())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }


    private fun addRowToTable(data: LateData) {
        val tableRow = TableRow(this)

        val params = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tableRow.layoutParams = params

        val textViewNama = TextView(this)
        val textViewTanggal = TextView(this)
        val textViewWaktu = TextView(this)
        val textViewStatus = TextView(this)

        val customTypeface = ResourcesCompat.getFont(this, R.font.ofmedium)
        textViewNama.typeface = customTypeface
        textViewTanggal.typeface = customTypeface
        textViewWaktu.typeface = customTypeface
        textViewStatus.typeface = customTypeface

        textViewNama.setTextColor(ContextCompat.getColor(this, R.color.main))
        textViewTanggal.setTextColor(ContextCompat.getColor(this, R.color.main))
        textViewWaktu.setTextColor(ContextCompat.getColor(this, R.color.main))
        textViewStatus.setTextColor(ContextCompat.getColor(this, R.color.main))

        textViewNama.text = data.nama
        textViewTanggal.text = data.tanggal
        textViewWaktu.text = data.waktu
        textViewStatus.text = data.status

        tableRow.addView(textViewNama)
        tableRow.addView(textViewTanggal)
        tableRow.addView(textViewWaktu)
        tableRow.addView(textViewStatus)

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

    private fun addRowToFilteredData(data: LateData) {
        filteredLateDataList.add(data)
    }

    private fun updateTable() {
        tableLayout.removeAllViews()

        val filteredDataInRange = filteredLateDataList.filter { data ->
            val date = LocalDate.parse(data.tanggal, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            (startDate == null || date.isAfter(startDate) || date.isEqual(startDate)) &&
                    (endDate == null || date.isBefore(endDate) || date.isEqual(endDate))
        }

        val startIndex = currentPage * pageSize
        val endIndex = minOf((currentPage + 1) * pageSize, filteredDataInRange.size)

        for (i in startIndex until endIndex) {
            addRowToTable(filteredDataInRange[i])
        }

        // Enable/disable Next and Previous buttons based on data availability
        nextButton.isEnabled = endIndex < filteredDataInRange.size
        previousButton.isEnabled = currentPage > 0
    }
}
