package com.hkgroups.agecalculator

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast

import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dateSelectorBtn :CardView = findViewById(R.id.dateSelector)
        dateSelectorBtn.setOnClickListener {
            datePickerView(it)
        }

    }

    private fun datePickerView(view: View?) {
        val calendar = Calendar.getInstance()
        val date = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        DatePickerDialog (this,DatePickerDialog.OnDateSetListener {
            view,Year,Month,dayOfMonth ->

            val userSelectedDate:TextView = findViewById(R.id.userSelectedDate)
            userSelectedDate.text = "$dayOfMonth / ${Month+1} / $Year"
            val hiddenCardView:CardView = findViewById(R.id.hiddenCard)
            hiddenCardView.visibility = View.VISIBLE

        },year,month,date).show()




    }

//    private fun datePicker(view: View?) {
//        val myCalender = Calendar.getInstance()
//        val date = myCalender.get(Calendar.DAY_OF_MONTH)
//        val month = myCalender.get(Calendar.MONTH)
//        val year = myCalender.get(Calendar.YEAR)
//      var dpd =  DatePickerDialog(
//            this,
//            DatePickerDialog.OnDateSetListener { view, selectedYear, selectedMonth, dayOfMonth ->
//                val selectedDate = "$dayOfMonth/${selectedMonth+1}/$selectedYear"
//                val displayDateOfUser: TextView = findViewById(R.id.selected_date)
//                displayDateOfUser.text = selectedDate
//
//                val formatDate = SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH);
//                val theDate = formatDate.parse(selectedDate)
//                val dateCalcute = theDate.time/60000
//                val currentDate = formatDate.parse(formatDate.format(System.currentTimeMillis())).time/60000
//                val actualminut = currentDate - dateCalcute
//                val resultMin: TextView = findViewById(R.id.age_in_minutes)
//                resultMin.text = actualminut.toString()
//
//            },
//            year,
//            month,
//            date
//        )
//        dpd.datePicker.maxDate = (Date().time -86400000)
//        dpd.show()
//        Toast.makeText(this, "$date", Toast.LENGTH_SHORT).show()
//
//
//    }

}