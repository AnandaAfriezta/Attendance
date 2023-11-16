package com.example.attendance.employee

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.attendance.fragment.HomeFragment
import com.example.attendance.R
import com.google.firebase.database.*

class EmployeeActivity : AppCompatActivity() {
    private lateinit var ivBackEmployee: ImageView
    private lateinit var tableLayout: TableLayout
    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee)

        tableLayout = findViewById(R.id.tableLayout)
        databaseReference = FirebaseDatabase.getInstance().reference.child("1c27wQexbj78D4NFKKGyyJosZGeHWAgbdlJe2MChQ4zY/Sheet1")
        ivBackEmployee = findViewById(R.id.iv_backPegawai)
        ivBackEmployee.setOnClickListener {
            val moveIntent = Intent(this@EmployeeActivity, HomeFragment::class.java)
            finish()
        }

        fetchData()
    }

    private fun fetchData() {
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)
        tableLayout?.removeAllViews()

        val databaseReference = FirebaseDatabase.getInstance().reference
        val query = databaseReference.child("1c27wQexbj78D4NFKKGyyJosZGeHWAgbdlJe2MChQ4zY/Sheet1") // Replace with your specific path in Firebase

        val uniqueIdsSet = HashSet<String>() // Use a set to store unique IDs

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                uniqueIdsSet.clear() // Clear the set on data change

                for (dataSnapshot in snapshot.children) {
                    val id = dataSnapshot.child("ID").value.toString() // Get the ID

                    // Add the ID to the set
                    uniqueIdsSet.add(id)
                }
                val customTypeface = ResourcesCompat.getFont(this@EmployeeActivity, R.font.ofmedium)
                val customColor = ContextCompat.getColor(this@EmployeeActivity, R.color.main)
                val sortedIds = uniqueIdsSet.mapNotNull { it.toIntOrNull() }.sorted()

                // Display unique IDs and their corresponding names in TableLayout
                for (sortedId in sortedIds) {
                    val row = TableRow(this@EmployeeActivity)

                    // Find the first occurrence of the ID
                    val firstDataSnapshot = snapshot.children.firstOrNull { it.child("ID").value.toString().toIntOrNull() == sortedId }

                    if (firstDataSnapshot != null) {
                        val name = firstDataSnapshot.child("Name").value.toString()

                        // Display ID
                        val idTextView = TextView(this@EmployeeActivity)
                        idTextView.text = "$sortedId"
                        idTextView.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
                        customTypeface?.let {
                            idTextView.typeface = it
                        }
                        idTextView.setTextColor(customColor)
                        row.addView(idTextView)

                        // Display Name
                        val nameTextView = TextView(this@EmployeeActivity)
                        nameTextView.text = "$name"
                        nameTextView.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
                        customTypeface?.let {
                            nameTextView.typeface = it
                        }
                        nameTextView.setTextColor(customColor)
                        row.addView(nameTextView)

                        // Add the TableRow to the TableLayout
                        tableLayout?.addView(row)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}