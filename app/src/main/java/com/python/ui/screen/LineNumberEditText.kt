package com.python.ui.screen

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.InputType
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText


class LineNumberEditText(context: Context, attrs: AttributeSet? = null) : AppCompatEditText(context, attrs) {

    init {
        setHorizontallyScrolling(true)
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        isSingleLine = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}


class LineNumberEditText1 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    private val lineNumberPaint = Paint().apply {
        color = Color.GRAY
        textSize = textSize * 0.8f
        isAntiAlias = true
    }

    private val lineNumberPadding = 80f

    init {
        // 添加左边距给行号
        setPadding(lineNumberPadding.toInt(), paddingTop, paddingRight, paddingBottom)
        setHorizontallyScrolling(true)
        isVerticalScrollBarEnabled = true
    }

    override fun onDraw(canvas: Canvas) {
        val layout = layout ?: return
        val lineCount = lineCount
        for (i in 0 until lineCount) {
            val baseline = getLineBounds(i, null)
            canvas.drawText(
                (i + 1).toString(),
                10f,
                baseline.toFloat(),
                lineNumberPaint
            )
        }
        super.onDraw(canvas)
    }
}
