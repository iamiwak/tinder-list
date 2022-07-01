package com.example.tinder

import android.app.Activity
import android.content.Context
import android.graphics.PointF
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import com.bumptech.glide.Glide
import kotlin.math.abs
import kotlin.math.sign

class Tinder(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs) {
    private val imageItems = mutableListOf<ImageView>()
    private val cardItems = mutableListOf<CardView>()

    private val swipeThreshold: Float
    private val imageAlpha: Float
    private val imageRotation: Float
    private val imagesCount: Int
    private val imageRadiusPercent: Float

    private val touchDown = PointF(0f, 0f)
    private val startCardViewPosition = PointF(0f, 0f)

    private val imagesList = mutableListOf<String>()

    init {
        fun getComponentSizes(): Pair<Int, Int> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return Pair(-1, -1)

            val sizes = (context as Activity).windowManager.currentWindowMetrics.bounds
            return Pair(sizes.right, sizes.bottom)
        }

        fun getImageSizes(lp: ViewGroup.LayoutParams): Pair<Int, Int> {
            val componentWidth: Int
            val componentHeight: Int

            with(getComponentSizes()) {
                componentWidth = this.first
                componentHeight = this.second
            }

            return Pair(
                when (lp.width) {
                    IMAGE_WRAP_CONTENT -> 200
                    IMAGE_MATCH_PARENT -> (componentWidth * IMAGE_SIZE_PERCENT).toInt()
                    0 -> 0
                    else -> (lp.width * IMAGE_SIZE_PERCENT).toInt()
                },
                when (lp.height) {
                    IMAGE_WRAP_CONTENT -> 200
                    IMAGE_MATCH_PARENT -> (componentHeight * IMAGE_SIZE_PERCENT).toInt()
                    0 -> 0
                    else -> (lp.height * IMAGE_SIZE_PERCENT).toInt()
                }
            )
        }

        this.layoutParams = LayoutParams(context, attrs)
        val imageSizes = getImageSizes(this.layoutParams)
        val view = inflate(context, R.layout.tinder_layout, this)

        fun <T : Comparable<T>> getValidValue(value: T, minValue: T, maxValue: T): T {
            return when {
                value is Float && value == -1f -> value
                value < minValue -> minValue
                value > maxValue -> maxValue
                else -> value
            }
        }

        val tinderAttrs = context.obtainStyledAttributes(attrs, R.styleable.Tinder, 0, 0)
        imageAlpha = getValidValue(tinderAttrs.getFloat(R.styleable.Tinder_minAlpha, -1f), 0f, 1f)
        imageRotation = getValidValue(tinderAttrs.getFloat(R.styleable.Tinder_rotationDegrees, 30f), 0f, 30f)

        debugLog("$imageAlpha - $imageRotation")

        swipeThreshold = getValidValue(
            tinderAttrs.getFloat(R.styleable.Tinder_swipeThreshold, (getComponentSizes().first - imageSizes.first) / 2f),
            0f,
            getComponentSizes().first / 2f
        )
        imagesCount = getValidValue(tinderAttrs.getInt(R.styleable.Tinder_elementsCount, 1), 1, 5)
        imageRadiusPercent = getValidValue(tinderAttrs.getFloat(R.styleable.Tinder_elementsRadius, .25f), 0f, 1f)

        fun createImageView(item: Int): ImageView {
            val sizes =
                if (item == 0) imageSizes
                else Pair(
                    (imageItems[item - 1].layoutParams.width * (1 - IMAGE_SIZE_LESS_PERCENT)).toInt(),
                    (imageItems[item - 1].layoutParams.height * (1 - IMAGE_SIZE_LESS_PERCENT)).toInt()
                )

            return ImageView(context).apply {
                id = View.generateViewId()
                layoutParams = LayoutParams(sizes.first, sizes.second)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        }

        fun createCardView(item: Int): CardView {
            return CardView(context).apply {
                id = View.generateViewId()
                addView(imageItems[item])
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                radius = imageItems[item].layoutParams.width * imageRadiusPercent
            }
        }

        fun createElement(item: Int) {
            imageItems.add(createImageView(item))
            cardItems.add(createCardView(item))
        }

        createElement(0)
        with(cardItems[0]) {
            this@Tinder.addView(this)
            ConstraintSet().apply {
                clone(view as ConstraintLayout)
                connect(id, TOP, view.id, TOP)
                connect(id, BOTTOM, view.id, BOTTOM)
                connect(id, START, view.id, START)
                connect(id, END, view.id, END)
                applyTo(view)
            }
        }

        for (idx in 1 until imagesCount) {
            createElement(idx)

            this.addView(cardItems[idx])
            ConstraintSet().apply {
                clone(view as ConstraintLayout)
                connect(cardItems[idx].id, TOP, cardItems[idx - 1].id, TOP)
                connect(cardItems[idx].id, START, cardItems[idx - 1].id, START)
                connect(cardItems[idx].id, END, cardItems[idx - 1].id, END)
                setMargin(
                    cardItems[idx].id,
                    TOP,
                    (imageItems[idx - 1].layoutParams.height * IMAGE_SIZE_LESS_PERCENT + imageItems[idx - 1].layoutParams.height * IMAGE_MARGIN_PERCENT).toInt()
                )
                applyTo(view)
            }
        }

        for (idx in imagesCount - 1 downTo 0) {
            cardItems[idx].bringToFront()
        }

        fun removeMainImage() {
            if (imagesList.isEmpty()) return
            loadImage(imagesList.drop(1))
        }

        cardItems[0].setOnTouchListener { curView, event ->
            val viewDiff = curView.x - startCardViewPosition.x
            val absoluteDiff = abs(viewDiff)
            val absoluteDiffPercent = absoluteDiff / swipeThreshold

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startCardViewPosition.x = curView.x
                    with(touchDown) {
                        x = event.x
                        y = event.y
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    // Положение начала = смещение между текущими координатами и точкой взятия
                    curView.x += event.x - touchDown.x

                    if (imageAlpha != -1f)
                        curView.alpha = if (absoluteDiffPercent < 1f) 1 - (1 - imageAlpha) * absoluteDiffPercent else imageAlpha
//
                    if (imageRotation != -1f) {
                        val diffPercent = viewDiff / swipeThreshold
                        curView.rotation = if (absoluteDiffPercent < 1f) diffPercent * imageRotation else imageRotation * diffPercent.sign
                    }
                }
                MotionEvent.ACTION_UP -> {
                    curView.x = startCardViewPosition.x

                    if (absoluteDiffPercent >= 1f)
                        removeMainImage()

                    if (imageAlpha != -1f) curView.alpha = 1f
                    if (imageRotation != -1f) curView.rotation = 0f
                }
            }
            true
        }

        tinderAttrs.recycle()
    }

    /**
     * Загружает фотографии по ссылкам из [images] в компонент.
     * @param images список ссылок на фотографии
     */
    fun loadImage(images: List<String>) {
        if (images.isEmpty()) return hideImageCard(0)

        with(imagesList) {
            removeAll(this)
            addAll(images)
        }

        for (idx in 0 until imagesCount) {
            if (idx < images.size) {
                cardItems[idx].visibility = View.VISIBLE
                Glide.with(context).load(images[idx]).into(imageItems[idx])
            } else hideImageCard(idx)
        }
    }

    private fun hideImageCard(item: Int) {
        cardItems[item].visibility = View.INVISIBLE
    }

    companion object {
        private const val IMAGE_SIZE_PERCENT = .5f
        private const val IMAGE_SIZE_LESS_PERCENT = .1f
        private const val IMAGE_MARGIN_PERCENT = .05f

        private const val IMAGE_WRAP_CONTENT = -2
        private const val IMAGE_MATCH_PARENT = -1

        private val debugLog: (String) -> Unit = { Log.d("tinder", it) }
    }
}

