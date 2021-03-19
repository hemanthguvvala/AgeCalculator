package com.hkgroups.agecalculator

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import androidx.cardview.widget.CardView
import com.airbnb.lottie.LottieAnimationView
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

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

       val dataPickDialog =  DatePickerDialog (this,DatePickerDialog.OnDateSetListener {
            view,Year,Month,dayOfMonth ->
            val userSelectedDate = "$dayOfMonth/${Month+1}/$Year"
            val userSelectedDateText:TextView = findViewById(R.id.userSelectedDate)
            userSelectedDateText.text = userSelectedDate
            val hiddenCardView:CardView = findViewById(R.id.hiddenCard)
            hiddenCardView.visibility = View.VISIBLE
            val dateFormat  =  SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH)
           val formatDate = dateFormat.parse(userSelectedDate)
           val minutes = 60000
           val days = 86400000
           val timeFormatDate = formatDate!!.time
           val actualDateTime = dateFormat.parse(dateFormat.format(System.currentTimeMillis()))!!.time
           val minResultTime =   (actualDateTime/minutes) -(timeFormatDate/minutes)
           val dayResultTime =   (actualDateTime/days) -(timeFormatDate/days)
           val yearResultTime =    Calendar.getInstance().get(Calendar.YEAR) - Year
           val minBtn :CardView = findViewById(R.id.minuteCalBtn)
           val dayBtn :CardView = findViewById(R.id.daysCalBtn)
           val yearBtn :CardView = findViewById(R.id.yearCalBtn)
           val resultText:TextView = findViewById(R.id.resultText)
           minBtn.setOnClickListener {
               resultText.visibility = View.VISIBLE
               resultText.text = minResultTime.toString()
               ageAnimation(yearResultTime)
           }
           dayBtn.setOnClickListener {
               resultText.visibility = View.VISIBLE
               resultText.text = dayResultTime.toString()
               ageAnimation(yearResultTime)
           }
           yearBtn.setOnClickListener {
               resultText.visibility = View.VISIBLE
               resultText.text = yearResultTime.toString()
               ageAnimation(yearResultTime)
           }
         //  val dayResultTime = (timeFormatDate*
        },year,month,date)

       dataPickDialog.datePicker.maxDate = Date().time - 86400000
        dataPickDialog.show()

    }

    private fun ageAnimation(yearResultTime: Int) {
       when(yearResultTime){
           in 1..10 -> {
               val image:LottieAnimationView = findViewById(R.id.animationImage)
               image.setAnimation(R.raw.old_man_rocking)
               image.visibility = View.VISIBLE
               image.playAnimation()
           }
       }
    }

}