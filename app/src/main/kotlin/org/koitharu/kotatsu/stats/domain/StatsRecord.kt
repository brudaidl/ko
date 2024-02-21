package org.koitharu.kotatsu.stats.domain

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.google.android.material.R
import com.google.android.material.color.MaterialColors
import org.koitharu.kotatsu.core.util.ext.getThemeColor
import org.koitharu.kotatsu.details.data.ReadingTime
import org.koitharu.kotatsu.parsers.model.Manga
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue
import kotlin.math.min

data class StatsRecord(
	val manga: Manga,
	val duration: Long,
) {

	val time: ReadingTime

	init {
		val minutes = TimeUnit.MILLISECONDS.toMinutes(duration).toInt()
		time = ReadingTime(
			minutes = minutes % 60,
			hours = minutes / 60,
			isContinue = false,
		)
	}

	@ColorInt
	fun getColor(context: Context): Int {
		val hue = (manga.id.absoluteValue % 360).toFloat()
		val color = ColorUtils.HSLToColor(floatArrayOf(hue, 0.5f, 0.5f))
		val backgroundColor = context.getThemeColor(R.attr.colorSurfaceContainerHigh)
		return MaterialColors.harmonize(color, backgroundColor)
	}
}
