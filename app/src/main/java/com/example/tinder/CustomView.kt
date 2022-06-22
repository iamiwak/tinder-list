package com.example.tinder

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

class CustomView(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs) {
    init {
        inflate(context, R.layout.custom_view_layout, this)
    }
}