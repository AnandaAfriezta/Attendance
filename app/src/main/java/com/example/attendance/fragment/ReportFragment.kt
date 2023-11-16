package com.example.attendance.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.attendance.ChartActivity
import com.example.attendance.Quadruple
import com.example.attendance.R
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class ReportFragment : Fragment() {
    private lateinit var tableLayout: TableLayout
    private lateinit var databaseReference: DatabaseReference
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button
    private lateinit var ivBack: ImageView
    private lateinit var tvChart: TextView
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null
    private val uniqueEntries = HashSet<Pair<String, String>>()
    private val dataMap = HashMap<Pair<String, String>, Date>()
    private val dataList = mutableListOf<Quadruple<String, String, String, String>>()
    private var currentPage = 0
    private val pageSize = 20

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_report, container, false)

        tableLayout = rootView.findViewById(R.id.tableLayout)
        startDateButton = rootView.findViewById(R.id.btnPickStartDate)
        endDateButton = rootView.findViewById(R.id.btnPickEndDate)
        nextButton = rootView.findViewById(R.id.btn_next)
        previousButton = rootView.findViewById(R.id.btn_previous)
        nextButton.isEnabled = false
        previousButton.isEnabled = false
        databaseReference = FirebaseDatabase.getInstance().reference.child("1c27wQexbj78D4NFKKGyyJosZGeHWAgbdlJe2MChQ4zY/Sheet1")

        tvChart = rootView.findViewById(R.id.tv_detail)
        tvChart.setOnClickListener {
            val moveIntent = Intent(requireActivity(), ChartActivity::class.java)
            startActivity(moveIntent)
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
        return rootView
    }

    private fun fetchDataFromFirebase() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataMap.clear()
                dataList.clear()

                for (childSnapshot in snapshot.children) {
                    val nama = childSnapshot.child("Name").value.toString()
                    val dateString = childSnapshot.child("DateTime").value.toString()
                    val status = childSnapshot.child("Status").value.toString()

                    if (isDateInRange(dateString)) {
                        val formattedDate = formatDate(dateString)
                        val formattedTime = formatTime(dateString)
                        val localDate = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"))

                        val existingEntry = dataList.find { it.first == nama && it.second == formattedDate && it.fourth == status }

                        if (existingEntry == null) {
                            addRowToFilteredData(nama, formattedDate, formattedTime, status)
                            dataMap[Pair(nama, formattedDate)] = Date.from(localDate.atStartOfDay(
                                ZoneId.systemDefault()).toInstant())
                        }
                    }
                }

                updateTable()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
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

    private fun formatTime(dateString: String): String {
        val inputDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val outputTimeFormat = DateTimeFormatter.ofPattern("HH:mm")

        try {
            val dateTime = LocalDateTime.parse(dateString, inputDateFormat)
            val zonedDateTime = dateTime.atZone(ZoneId.of("UTC"))
            val localDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Jakarta")).toLocalDateTime()
            return outputTimeFormat.format(localDateTime.toLocalTime())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    private fun addRowToTable(nama: String, tanggal: String, jamMenit: String, status: String) {
        val tableRow = TableRow(requireContext())

        val params = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tableRow.layoutParams = params

        val textViewNama = TextView(requireContext())
        val textViewTanggal = TextView(requireContext())
        val textViewJamMenit = TextView(requireContext())
        val textViewStatus = TextView(requireContext())

        val customTypeface = ResourcesCompat.getFont(requireContext(), R.font.ofmedium)
        textViewNama.typeface = customTypeface
        textViewTanggal.typeface = customTypeface
        textViewJamMenit.typeface = customTypeface
        textViewStatus.typeface = customTypeface

        textViewNama.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        textViewTanggal.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        textViewJamMenit.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))
        textViewStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.main))

        textViewNama.text = nama
        textViewTanggal.text = tanggal
        textViewJamMenit.text = jamMenit
        textViewStatus.text = status

        tableRow.addView(textViewNama)
        tableRow.addView(textViewTanggal)
        tableRow.addView(textViewJamMenit)
        tableRow.addView(textViewStatus)

        tableLayout.addView(tableRow)
    }


    private fun showDatePicker(onDateSelected: (LocalDate) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                onDateSelected(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun addRowToFilteredData(nama: String, tanggal: String, jamMenit: String, status: String) {
        dataList.add(Quadruple(nama, tanggal, jamMenit, status))
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
                filteredDataInRange[i].second,
                filteredDataInRange[i].third,
                filteredDataInRange[i].fourth
            )
        }

        nextButton.isEnabled = endIndex < filteredDataInRange.size
        previousButton.isEnabled = currentPage > 0
    }
}