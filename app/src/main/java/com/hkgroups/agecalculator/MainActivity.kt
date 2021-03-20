package com.hkgroups.agecalculator

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var mAdView: AdView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this) {}

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        val dateSelectorBtn: CardView = findViewById(R.id.dateSelector)
        dateSelectorBtn.setOnClickListener {
            datePickerView(it)
        }

    }

    private fun datePickerView(view: View?) {
        val calendar = Calendar.getInstance()
        val date = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        val dataPickDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, Year, Month, dayOfMonth ->
                val userSelectedDate = "$dayOfMonth/${Month + 1}/$Year"
                val userSelectedDateText: TextView = findViewById(R.id.userSelectedDate)
                userSelectedDateText.text = userSelectedDate
                val hiddenCardView: CardView = findViewById(R.id.hiddenCard)
                hiddenCardView.visibility = View.VISIBLE
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val formatDate = dateFormat.parse(userSelectedDate)
                val minutes = 60000
                val days = 86400000
                val timeFormatDate = formatDate!!.time
                val actualDateTime =
                    dateFormat.parse(dateFormat.format(System.currentTimeMillis()))!!.time
                val minResultTime = (actualDateTime / minutes) - (timeFormatDate / minutes)
                val dayResultTime = (actualDateTime / days) - (timeFormatDate / days)
                val yearResultTime = Calendar.getInstance().get(Calendar.YEAR) - Year
                val minBtn: CardView = findViewById(R.id.minuteCalBtn)
                val dayBtn: CardView = findViewById(R.id.daysCalBtn)
                val yearBtn: CardView = findViewById(R.id.yearCalBtn)

                minBtn.setOnClickListener {
                    resultOutput(
                        minResultTime,
                        "min, OMG you selected Minutes i think you are Kid "
                    )
                    ageAnimation(yearResultTime)
                }
                dayBtn.setOnClickListener {
                    resultOutput(dayResultTime, "days, Real Deal Man you are tough guy ")
                    ageAnimation(yearResultTime)
                }
                yearBtn.setOnClickListener {
                    resultOutput(yearResultTime.toLong(), " Legends only guess this, aren't you?")
                    ageAnimation(yearResultTime)
                }
                //  val dayResultTime = (timeFormatDate*
            },
            year,
            month,
            date
        )

        dataPickDialog.datePicker.maxDate = Date().time - 86400000
        dataPickDialog.show()

    }

    private fun resultOutput(result: Long, text: String) {
        val resultText: TextView = findViewById(R.id.resultText)
        resultText.visibility = View.VISIBLE
        resultText.text = "${result.toString()} $text"
    }

    private fun ageAnimation(yearResultTime: Int) {
        when (yearResultTime) {
            in 0..13 -> playAnimation(R.raw.children)
            in 14..18 -> playAnimation(R.raw.young_adult)
            in 19..24 -> playAnimation(R.raw.couples)
            in 25..29 -> playAnimation(R.raw.ageupto_28)
            in 30..40 -> playAnimation(R.raw.default_img)
            in 41..55 -> playAnimation(R.raw.old_man_rocking)
            in 56..80 -> playAnimation(R.raw.chilling_old_man)
            else -> playAnimation(R.raw.default_img)
        }
    }

    private fun playAnimation(animation: Int) {
        val image: LottieAnimationView = findViewById(R.id.animationImage)
        image.setAnimation(animation)
        image.visibility = View.VISIBLE
        image.playAnimation()
    }
   lateinit var  dialog :Dialog
    override fun onBackPressed() {
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_layout)
        dialog.window!!.setLayout(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        val hellYeah = dialog.findViewById<Button>(R.id.hellYeah)
        val nope = dialog.findViewById<Button>(R.id.nope)
        val animation = dialog.findViewById<LottieAnimationView>(R.id.animationImage)
        animation.setAnimation(R.raw.above_age_35)
        val rateBtn = dialog.findViewById<CardView>(R.id.rateBtn)
        animation.visibility = View.VISIBLE
        animation.playAnimation()
        dialog.show()

        if (dialog.isShowing){

            hellYeah.setOnClickListener {
                dialog.dismiss()
               super.onBackPressed()
            }
            nope.setOnClickListener {
                dialog.dismiss()
            }
            rateBtn.setOnClickListener {
                val uri = Uri.parse("https://play.google.com/store/apps/details?id=com.hkgroups.agecalculator")
                val intent = Intent(Intent.ACTION_VIEW,uri)
                dialog.dismiss()
                startActivity(intent)
            }
        }
//        super.onBackPressed()
    }
}
