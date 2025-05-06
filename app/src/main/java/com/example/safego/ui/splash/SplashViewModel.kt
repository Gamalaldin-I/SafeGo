package com.example.safego.ui.splash

class SplashViewModel {
    private val driverSafetyMessages = listOf(
        "By Allah’s Will, We Help You Drive Safely.",
        "With Allah’s Mercy, Your Journey is Secured.",
        "By Allah’s Blessing, Your Safety is Our Priority.",
        "With Faith in Allah, Every Road Leads to Safety.",
        "By Allah’s Guidance, We Keep You on the Right Path.",
        "With Trust in Allah, You Travel with Peace.",
        "By the Will of Allah, We Protect Your Journey.",
        "With Allah’s Help, Every Destination is Reached Safely.",
        "By Allah’s Light, Every Path Becomes Clear.",
        "With Allah’s Guidance, Every Drive is a Safe One.",
        "By Allah’s Grace, Your Road is Full of Blessings.",
        "With Patience and Trust, Allah Guides Your Way.",
        "By the Mercy of Allah, Every Journey is Guarded.",
        "With the Help of Allah, You’ll Always Reach Home Safe.",
        "By Allah’s Wisdom, We Drive with Caution and Care.",
        "With the Protection of Allah, No Road is Dangerous.",
        "By Allah’s Love, Every Drive is Filled with Peace.",
        "With Allah’s Power, You Are Always in Safe Hands.",
        "By Trusting Allah, You Drive Without Fear.",
        "With Allah’s Care, Every Journey is a Blessing.",
        "By Allah’s Guidance, Every Turn is the Right One.",
        "With the Strength of Faith, No Road is Uncertain.",
        "By the Will of Allah, You Travel with Confidence.",
        "With the Grace of Allah, Every Road is a Safe Path.",
        "By Seeking Allah’s Protection, We Drive Without Worries.",
        "With the Help of Allah, Roads Become Paths to Success.",
        "By Allah’s Mercy, Every Journey Ends in Peace.",
        "With Trust in Allah, Even the Bumps Lead to Goodness.",
        "By Remembering Allah, We Drive with a Peaceful Heart.",
        "With the Name of Allah, Every Journey Begins Safely."
    )

    fun getRandomDriverSafetyMessage(): String {
        val randomIndex = driverSafetyMessages.indices.random()
        return driverSafetyMessages[randomIndex]
    }


}