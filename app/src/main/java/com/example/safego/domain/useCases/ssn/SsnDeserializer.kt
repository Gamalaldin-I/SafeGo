package com.example.safego.domain.useCases.ssn

import com.example.safego.dataSource.local.model.User

class SsnDeserializer(user : User){
    private val ssn = user.ssn
    private val governmentCodes = mapOf(
        "01" to "Cairo",
        "02" to "Alexandria",
        "03" to "Port Said",
        "04" to "Suez",
        "11" to "Damietta",
        "12" to "Dakahlia",
        "13" to "Sharqia",
        "14" to "Qalyubia",
        "15" to "Kafr El Sheikh",
        "16" to "Gharbia",
        "17" to "Menoufia",
        "18" to "Beheira",
        "19" to "Ismailia",
        "21" to "Giza",
        "22" to "Beni Suef",
        "23" to "Faiyum",
        "24" to "Minya",
        "25" to "Assiut",
        "26" to "Sohag",
        "27" to "Qena",
        "28" to "Aswan",
        "29" to "Luxor",
        "31" to "Red Sea",
        "32" to "New Valley",
        "33" to "Matrouh",
        "34" to "North Sinai",
        "35" to "South Sinai",
        "88" to "Outside Egypt"
    )


    private fun getGovernment(governmentCode: String):String{
        return governmentCodes[governmentCode] ?:"Unknown"
    }
    private fun getBirthDay(birthCode:String,century:Char):String{
        val years = when(century){
            '2' -> 1900
            '3' -> 2000
            else -> 0
        }
        val day = birthCode.substring(4,6)
        val month = birthCode.substring(2,4)
        val year = birthCode.substring(0,2)
        val yearInt = years + year.toInt()
        return "$day-$month-$yearInt"

    }
    private fun getGender(genderCode:String):String{
        return if(genderCode.toInt()%2==0) "Female" else "Male"
    }
    fun getInformation():Triple<String,String,String>{
        val governmentCode = ssn.substring(7,9)
        val birthCode = ssn.substring(1,7)
        val genderCode = ssn.substring(9,13)
        val century = ssn[0]
        return Triple(getGovernment(governmentCode),getBirthDay(birthCode,century),getGender(genderCode))
    }
}