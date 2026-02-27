package com.planwise.data.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.planwise.domain.model.DayStatus
import com.planwise.domain.model.ShiftDay
import com.planwise.domain.model.ShiftType
import com.planwise.domain.repo.ShiftRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class ShiftPdfExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shiftRepository: ShiftRepository,
) {

    suspend fun exportMonth(year: Int, month: Int): android.net.Uri = withContext(Dispatchers.IO) {
        val ym = YearMonth.of(year, month)
        val first = ym.atDay(1)
        val shifts = shiftRepository.getAllNow().associateBy { it.dateYyyymmdd }

        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true; textSize = 12f }
        val titlePaint = Paint().apply { isAntiAlias = true; textSize = 16f; isFakeBoldText = true }

        var y = 40f
        canvas.drawText("PlanWise – Shift Plan ${ym.year}-${ym.monthValue.toString().padStart(2,'0')}", 40f, y, titlePaint)
        y += 20f

        val startX = 40f
        val colW = 75f
        val rowH = 22f

        val headers = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
        headers.forEachIndexed { idx, h ->
            canvas.drawText(h, startX + idx * colW + 4f, y, paint)
        }
        y += 10f

        val firstDow = (first.dayOfWeek.value % 7)
        var cursor = first.minusDays((firstDow - 1).toLong())

        for (r in 0 until 6) {
            for (c in 0..6) {
                val cellX = startX + c * colW
                val cellY = y + r * rowH
                canvas.drawRect(cellX, cellY, cellX + colW, cellY + rowH, Paint().apply { style = Paint.Style.STROKE })

                val inMonth = cursor.month == ym.month && cursor.year == ym.year
                if (inMonth) {
                    val dateInt = cursor.year * 10000 + cursor.monthValue * 100 + cursor.dayOfMonth
                    val shift = shifts[dateInt]
                    val label = buildLabel(shift)
                    canvas.drawText("${cursor.dayOfMonth}", cellX + 4f, cellY + 14f, paint)
                    canvas.drawText(label, cellX + 20f, cellY + 14f, paint)
                }
                cursor = cursor.plusDays(1)
            }
        }

        doc.finishPage(page)

        val file = File(context.cacheDir, "planwise_shifts_${ym.year}_${ym.monthValue}.pdf")
        file.outputStream().use { doc.writeTo(it) }
        doc.close()

        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun buildLabel(shift: ShiftDay?): String {
        if (shift == null) return ""
        val st = when (shift.shiftType) {
            ShiftType.EARLY -> "E"
            ShiftType.LATE -> "L"
            ShiftType.NIGHT -> "N"
            ShiftType.NONE -> "-"
        }
        val ds = when (shift.dayStatus) {
            DayStatus.NORMAL -> ""
            DayStatus.FREE -> " (F)"
            DayStatus.VACATION -> " (U)"
            DayStatus.SICK -> " (K)"
        }
        return st + ds
    }
}
