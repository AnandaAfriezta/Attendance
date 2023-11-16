package com.example.attendance.attend

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.attendance.fragment.HomeFragment
import com.example.attendance.R
import com.google.firebase.database.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AttendanceActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var tableLayout: TableLayout
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private lateinit var ivBackAttendance: ImageView
    private lateinit var tvDetail: TextView

    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        // Inisialisasi Firebase Database
        databaseReference = FirebaseDatabase.getInstance().reference

        // Inisialisasi TableLayout dari layout XML
        tableLayout = findViewById(R.id.tableLayout)
        ivBackAttendance = findViewById(R.id.iv_backAttend)
        ivBackAttendance.setOnClickListener {
            val moveIntent = Intent(this@AttendanceActivity, HomeFragment::class.java)
            finish()
        }
        tvDetail = findViewById(R.id.tv_detail)
        tvDetail.setOnClickListener {
            val moveIntent = Intent(this@AttendanceActivity, AttendanceDetail::class.java)
            startActivity(moveIntent)
            finish()
        }

        // Inisialisasi Button untuk memilih tanggal start
        startDateButton = findViewById(R.id.btnPickStartDate)
        startDateButton.setOnClickListener {
            showDatePickerDialog(true)
        }

        // Inisialisasi Button untuk memilih tanggal end
        endDateButton = findViewById(R.id.btnPickEndDate)
        endDateButton.setOnClickListener {
            showDatePickerDialog(false)
        }

        // Contoh penggunaan
        getDataFromFirebase()
    }

    private fun getDataFromFirebase() {
        // Mendapatkan referensi ke node utama di database
        val mainNodeRef =
            databaseReference.child("1c27wQexbj78D4NFKKGyyJosZGeHWAgbdlJe2MChQ4zY/Sheet1") // Ganti dengan nama node utama yang sesuai

        // Mendapatkan data dari Firebase
        mainNodeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Hapus semua baris yang ada sebelum menambahkan yang baru
                    tableLayout.removeAllViews()

                    // Tambahkan baris header

                    // Inisialisasi HashMap untuk menyimpan kehadiran unik setiap nama pada setiap tanggal
                    val tanggalKehadiranMap = HashMap<String, HashSet<String>>()

                    // Iterasi setiap entri
                    for (entrySnapshot in snapshot.children) {
                        val tanggalString = entrySnapshot.child("DateTime").getValue(String::class.java)
                        val nama = entrySnapshot.child("Name").getValue(String::class.java)

                        if (tanggalString != null && nama != null) {
                            // Konversi tanggalString ke LocalDate
                            val formatter = DateTimeFormatter.ISO_DATE_TIME
                            val localDate = LocalDate.parse(tanggalString, formatter)

                            // Format kembali ke string hanya dengan tanggal
                            val tanggal = localDate.format(DateTimeFormatter.ISO_DATE)

                            // Menambahkan nama ke HashSet pada tanggal tertentu
                            tanggalKehadiranMap.computeIfAbsent(nama) { HashSet() }.add(tanggal)
                        }
                    }

                    // Tambahkan data kehadiran ke tabel
                    for ((nama, uniqueDatesSet) in tanggalKehadiranMap) {
                        // Hitung jumlah hadir hanya untuk tanggal dalam rentang yang dipilih
                        val jumlahHadir =
                            uniqueDatesSet.count { tanggal -> isDateInRange(LocalDate.parse(tanggal)) }

                        // Menampilkan nama dan jumlah hadir untuk setiap tanggal dalam rentang
                        addDataToTable(nama, jumlahHadir.toString())
                    }
                } else {
                    // Tidak ada data pada node utama
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun isDateInRange(date: LocalDate): Boolean {
        // Fungsi untuk memeriksa apakah tanggal berada di dalam range yang dipilih
        return (startDate == null || date.isEqual(startDate) || date.isAfter(startDate)) &&
                (endDate == null || date.isEqual(endDate) || date.isBefore(endDate))
    }

    private fun showDatePickerDialog(isStartDate: Boolean) {
        // Fungsi untuk menampilkan dialog pemilihan tanggal
        val currentDate = if (isStartDate) startDate else endDate
        val calendar =
            currentDate?.atStartOfDay(ZoneId.systemDefault()) ?: LocalDate.now().atStartOfDay(ZoneId.systemDefault())

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker?, year: Int, month: Int, day: Int ->
                val selectedDate = LocalDate.of(year, month + 1, day) // month dimulai dari 0
                if (isStartDate) {
                    startDate = selectedDate
                    startDateButton.text = selectedDate.format(DateTimeFormatter.ISO_DATE)
                } else {
                    endDate = selectedDate
                    endDateButton.text = selectedDate.format(DateTimeFormatter.ISO_DATE)
                }

                // Ambil data dari Firebase setelah tanggal dipilih
                getDataFromFirebase()
            },
            calendar.year,
            calendar.monthValue - 1, // month dimulai dari 0
            calendar.dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun addDataToTable(nama: String, jumlahHadir: String) {
        val dataRow = TableRow(this)

        val namaTextView = TextView(this)
        namaTextView.text = nama
        // Mengganti font
        val customFont = ResourcesCompat.getFont(this@AttendanceActivity, R.font.ofmedium)
        namaTextView.typeface = customFont

        // Mengganti warna teks
        namaTextView.setTextColor(ContextCompat.getColor(this@AttendanceActivity, R.color.main))
        dataRow.addView(namaTextView)

        val jumlahHadirTextView = TextView(this)
        jumlahHadirTextView.text = jumlahHadir

        jumlahHadirTextView.typeface = customFont

        // Mengganti warna teks
        jumlahHadirTextView.setTextColor(ContextCompat.getColor(this@AttendanceActivity,
            R.color.main
        ))
        dataRow.addView(jumlahHadirTextView)

        tableLayout.addView(dataRow)
    }
}