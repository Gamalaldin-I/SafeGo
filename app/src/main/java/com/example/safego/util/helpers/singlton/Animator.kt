package com.example.safego.util.helpers.singlton

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AccelerateDecelerateInterpolator

object Animator {
    fun animateTxt(view:View,onEnd : ()-> Unit){
        view.animate().scaleY(0.8f).scaleX(0.8f).setDuration(200).withEndAction{
            view.animate().scaleX(1f).scaleY(1f).setDuration(100).withEndAction(onEnd).start()
        }.start()
    }
    fun animateSearchbar(searchView:View,backArrow:View){
        searchView.animate().translationX(130f).setDuration(100).withEndAction {
            searchView.animate().scaleX(0.9f).setDuration(100).withEndAction {
                searchView.animate().scaleY(.9f).setDuration(70).translationX(40f).withEndAction {
                    backArrow.animate().scaleY(0.9f).translationX(15f).setDuration(70).start()
                }.start()
            }.start()
        }.start()
    }
    fun deAnimateSearchBar(searchView:View,backArrow:View)
    {
        searchView.animate().scaleX(1f).setDuration(0).translationX(0f).start()
        searchView.animate().scaleY(1f).setDuration(0).start()
        backArrow.animate().scaleY(1f).setDuration(0).start()

    }


    // ======================== ANIMATIONS ========================
     fun animateFadeIn(view: View, delay: Long = 0, duration: Long = 500) {
        view.apply {
            visibility = VISIBLE
            alpha = 0f
            translationY = 90f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(duration)
                .start()
        }
    }

     fun animateFadeOut(view: View, duration: Long = 300) {
        view.animate()
            .alpha(0f)
            .translationY(30f)
            .setDuration(duration)
            .withEndAction {
                view.visibility = GONE
            }
            .start()
    }
     fun animateButtonClick(view: View, action: () -> Unit) {
        view.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(50)
                    .withEndAction {
                        action() // Perform action after animation finishes
                    }
            }
    }
    fun animateDetailsCard(cardView: View) {
        cardView.animate().translationY(100f).alpha(0f).setDuration(0).start()
        cardView.visibility = VISIBLE
        cardView.animate()
            .translationY(0f)
            .alpha(0.85f)
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }
    fun hideDetailsCard(cardView: View) {
        cardView.animate()
            .translationY(100f)
            .alpha(0f)
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction { cardView.visibility = GONE }
            .start()
    }




}