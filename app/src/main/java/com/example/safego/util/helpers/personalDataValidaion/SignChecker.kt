package com.example.safego.util.helpers.personalDataValidaion

object SignChecker {

    fun isValidEmail(s:String):Boolean{
        if(s.endsWith(".com")&&s.contains('@')&&!(s.contains(' '))){
            val indexOfCom=s.indexOf(".com")
            val indexOfA=s.indexOf('@')
            return (indexOfCom-indexOfA>=2&&(indexOfA!=0))
        }
        else return false
    }


    fun passwordMoreThan8(password: String):Boolean{
        return password.length>=8
    }

    fun howStrongPassword(password:String):String{
        if(password.length<8) return "Less than 8 "
        else{
            var smallLetters=0
            var capitalLetters=0
            var signs=0
            var numeric=0
            val pass =password.toCharArray()
            for(i in pass){
                when (i.code) {
                    in 65..90 -> capitalLetters=1
                    in 97..122 -> smallLetters=1
                    in 48..57 -> numeric=1
                    else -> signs=1
                }
            }
            val sum=capitalLetters+smallLetters+signs+numeric
            return when(sum){
                1->"Week"
                2->"Weak"
                3->"Medium"
                4->"Strong"
                else->" "
            }

        }
    }

    fun isValidName(name: String): Boolean {
        return name.isNotBlank() && name.any { it.isLetter() } && name.all { it.isLetter() || it == ' ' } && name.length >= 3
    }


    fun validPhoneNumber(phone:String):Boolean{
        //starts with the code
        val prefix=phone.startsWith("010")||phone.startsWith("011")||phone.startsWith("012")||phone.startsWith("015")
        var strangeLetter=0
        //loop to check if there is strangeLetter
        for(i in phone.toCharArray()){
            if (i.code !in 48..57){
                strangeLetter=1
            }
        }
        //prefix & 11 digits & all numeric
        return (prefix&&phone.length==11&&strangeLetter==0)
    }

    fun validSSN(ssn:String):Boolean{
        val valid =(ssn.length==14)
        return valid
    }

}