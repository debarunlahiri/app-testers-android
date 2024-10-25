package com.summitcodeworks.apptesters.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

object BlurBuilder {
    private const val BITMAP_SCALE = 0.4f
    private const val BLUR_RADIUS = 7.5f

    fun blur(context: Context, image: Bitmap): Bitmap {
        // Scale down the image to save processing time
        val inputBitmap = Bitmap.createScaledBitmap(image,
            (image.width * BITMAP_SCALE).toInt(),
            (image.height * BITMAP_SCALE).toInt(), false)

        // Create output bitmap
        val outputBitmap = Bitmap.createBitmap(inputBitmap)

        // Create RenderScript
        val rs = RenderScript.create(context)

        // Create allocations for input and output
        val input = Allocation.createFromBitmap(rs, inputBitmap)
        val output = Allocation.createFromBitmap(rs, outputBitmap)

        // Create the ScriptIntrinsicBlur and set its properties
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        script.setRadius(BLUR_RADIUS)
        script.setInput(input)

        // Perform the blur operation
        script.forEach(output)  // Use forEach to perform the blur
        output.copyTo(outputBitmap)

        // Destroy the RenderScript
        rs.destroy()

        return outputBitmap
    }
}



