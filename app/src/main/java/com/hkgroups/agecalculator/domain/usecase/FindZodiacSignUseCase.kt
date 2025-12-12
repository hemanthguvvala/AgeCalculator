package com.hkgroups.agecalculator.domain.usecase

import com.hkgroups.agecalculator.data.model.ZodiacSign
import java.time.LocalDate
import javax.inject.Inject

class FindZodiacSignUseCase @Inject constructor() {

    operator fun invoke(birthDate: LocalDate, allSigns: List<ZodiacSign>): ZodiacSign? {
        val month = birthDate.monthValue
        val day = birthDate.dayOfMonth

        return when {
            (month == 3 && day >= 21) || (month == 4 && day <= 19) -> allSigns.find { it.name == "Aries" }
            (month == 4 && day >= 20) || (month == 5 && day <= 20) -> allSigns.find { it.name == "Taurus" }
            (month == 5 && day >= 21) || (month == 6 && day <= 20) -> allSigns.find { it.name == "Gemini" }
            (month == 6 && day >= 21) || (month == 7 && day <= 22) -> allSigns.find { it.name == "Cancer" }
            (month == 7 && day >= 23) || (month == 8 && day <= 22) -> allSigns.find { it.name == "Leo" }
            (month == 8 && day >= 23) || (month == 9 && day <= 22) -> allSigns.find { it.name == "Virgo" }
            (month == 9 && day >= 23) || (month == 10 && day <= 22) -> allSigns.find { it.name == "Libra" }
            (month == 10 && day >= 23) || (month == 11 && day <= 21) -> allSigns.find { it.name == "Scorpio" }
            (month == 11 && day >= 22) || (month == 12 && day <= 21) -> allSigns.find { it.name == "Sagittarius" }
            (month == 12 && day >= 22) || (month == 1 && day <= 19) -> allSigns.find { it.name == "Capricorn" }
            (month == 1 && day >= 20) || (month == 2 && day <= 18) -> allSigns.find { it.name == "Aquarius" }
            (month == 2 && day >= 19) || (month == 3 && day <= 20) -> allSigns.find { it.name == "Pisces" }
            else -> null
        }
    }
}