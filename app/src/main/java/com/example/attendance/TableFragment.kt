package com.example.attendance

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TableFragment : Fragment() {
    private lateinit var tableLayout: TableLayout
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button
    private lateinit var filterButton: Button
    private lateinit var resetFilter: Button
    private var selectedDate: Date? = null
    private val itemsPerPage = 10
    private var startIndex = 0
    private var totalData = 0
    private lateinit var databaseReference: DatabaseReference
    private val dataSnapshotList = ArrayList<DataSnapshot>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_table, container, false)
        tableLayout = rootView.findViewById(R.id.tableLayout)
        nextButton = rootView.findViewById(R.id.nextButton)
        previousButton = rootView.findViewById(R.id.previousButton)
        filterButton = rootView.findViewById(R.id.filterButton)
        resetFilter = rootView.findViewById(R.id.resetFilterButton)

        resetFilter.setOnClickListener {
            resetFilter()
        }
        filterButton.setOnClickListener {
            showDatePickerDialog()
        }
        databaseReference = FirebaseDatabase.getInstance().reference.child("1c27wQexbj78D4NFKKGyyJosZGeHWAgbdlJe2MChQ4zY/Sheet1")

        nextButton.setOnClickListener {
            startIndex += itemsPerPage
            updateTable()
        }

        previousButton.setOnClickListener {
            startIndex -= itemsPerPage
            updateTable()
        }

        fetchData()
        return rootView
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth, 0, 0, 0)
                selectedDate = selectedCalendar.time
                startIndex = 0
                dataSnapshotList.clear()
                fetchData()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun updateTable() {
        tableLayout.removeAllViews()

        for (i in startIndex until minOf(startIndex + itemsPerPage, totalData)) {
            val dataSnapshot = dataSnapshotList[i]
            val no = dataSnapshot.child("No").value.toString()
            val name = dataSnapshot.child("Name").value.toString()
            val dateString = dataSnapshot.child("DateTime").value.toString()
            val status = dataSnapshot.child("Status").value.toString()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val dateTime = dateFormat.parse(dateString)

            if (selectedDate == null || isSameDate(dateTime, selectedDate)) {
                val noTextView = TextView(requireContext())
                val row = TableRow(requireContext())
                val nameTextView = TextView(requireContext())
                val dateTimeTextView = TextView(requireContext())
                val statusTextView = TextView(requireContext())

                val customTypeface = ResourcesCompat.getFont(requireContext(), R.font.ofmedium)
                noTextView.typeface = customTypeface
                nameTextView.typeface = customTypeface
                dateTimeTextView.typeface = customTypeface
                statusTextView.typeface = customTypeface

                val textColor = ContextCompat.getColor(requireContext(), R.color.main)
                noTextView.setTextColor(textColor)
                nameTextView.setTextColor(textColor)
                dateTimeTextView.setTextColor(textColor)
                statusTextView.setTextColor(textColor)

                noTextView.text = no
                nameTextView.text = name
                dateTimeTextView.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(dateTime)
                statusTextView.text = status

                noTextView.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
                nameTextView.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
                dateTimeTextView.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
                statusTextView.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)

                row.addView(noTextView)
                row.addView(nameTextView)
                row.addView(dateTimeTextView)
                row.addView(statusTextView)

                tableLayout.addView(row)
            }
        }

        nextButton.isEnabled = startIndex + itemsPerPage < totalData
        previousButton.isEnabled = startIndex > 0
    }

    private fun isSameDate(date1: Date, date2: Date?): Boolean {
        if (date2 == null) {
            return true
        }
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    private fun resetFilter() {
        selectedDate = null
        startIndex = 0
        dataSnapshotList.clear()
        fetchData()
    }

    private fun fetchData() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val dateString = childSnapshot.child("DateTime").value.toString()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                    val dateTime = dateFormat.parse(dateString)
                    if (isSameDate(dateTime, selectedDate)) {
                        dataSnapshotList.add(childSnapshot)
                    }
                }
                totalData = dataSnapshotList.size
                updateTable()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchDataError", "Error fetching data from Firebase: ${error.message}")
                // Handle error
            }
        })
    }
}
