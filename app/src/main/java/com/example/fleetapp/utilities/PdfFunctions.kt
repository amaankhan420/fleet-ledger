package com.example.fleetapp.utilities

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.example.fleetapp.R
import com.example.fleetapp.dataclasses.FormData
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import java.io.File
import java.time.LocalTime


class PdfFunctions {
    companion object {
        fun getPublicDirectory(context: Context): File {
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "${context.getString(R.string.fleet_report)}s"
            )
            if (!directory.exists()) {
                directory.mkdirs()
            }
            return directory
        }

        fun createPdf(
            context: Context,
            formDataMap: Map<String, List<FormData>>,
            nameOfCompany: String,
            startDate: String,
            endDate: String
        ): Boolean {
            try {
                val directory = getPublicDirectory(context)
                val currentTime = LocalTime.now().toString().replace(":", "-").substringBefore('.')
                val sanitizedStartDate = startDate.replace("/", "-")
                val sanitizedEndDate = endDate.replace("/", "-")
                val title =
                    "${nameOfCompany}--${sanitizedStartDate}-${sanitizedEndDate}--${currentTime}"

                val pdfFile = File(directory, "${title}.pdf")
                val pdfWriter = PdfWriter(pdfFile)
                val pdfDocument = PdfDocument(pdfWriter)
                val document = Document(pdfDocument)

                document.add(
                    Paragraph("$nameOfCompany ${context.getString(R.string.fleet_report)}")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(18f)
                        .setBold()
                        .setMarginBottom(8f)
                )
                document.add(
                    Paragraph("From $startDate to $endDate")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(14f)
                        .setItalic()
                        .setMarginBottom(20f)
                )

                var grandTotalAmount = 0f
                var grandTotalIncentives = 0f
                var grandTotalCommission = 0f

                val grayColor = DeviceRgb(200, 200, 200)

                formDataMap.forEach { (partnerName, formDataList) ->
                    document.add(
                        Paragraph("${context.getString(R.string.partner_name)}: $partnerName")
                            .setFontSize(16f)
                            .setBold()
                            .setMarginBottom(10f)
                    )

                    val table = Table(floatArrayOf(3f, 3f, 2f)).useAllAvailableWidth()

                    val labels = context.resources.getStringArray(R.array.form_labels).toList()
                    labels.forEach {
                        val cell = Cell().add(Paragraph(it).setBold()).setBackgroundColor(grayColor)
                        table.addHeaderCell(cell)
                    }

                    var totalAmount = 0f
                    var totalIncentives = 0f
                    var totalCommission = 0f

                    formDataList.forEach { formData ->
                        table.addCell(formData.vehicleNumber)
                        table.addCell(formData.driverName)
                        table.addCell("₹${formData.amount}")
                        totalAmount += formData.amount
                    }

                    listOf(context.getString(R.string.total), "", "₹$totalAmount").forEach {
                        table.addCell(Cell().add(Paragraph(it)).setBackgroundColor(grayColor))
                    }

                    formDataList.filter { it.incentive > 0f }.forEach { incentiveEntry ->
                        table.addCell(incentiveEntry.vehicleNumber)
                        table.addCell(context.getString(R.string.incentive))
                        table.addCell("₹${incentiveEntry.incentive}")
                        totalIncentives += incentiveEntry.incentive
                    }

                    val partnerCommission = formDataList.find { it.commission > 0f }
                    totalCommission += partnerCommission?.commission ?: 0f

                    listOf(context.getString(R.string.commission), "", "-₹$totalCommission").forEach {
                        table.addCell(Cell().add(Paragraph(it)))
                    }

                    val payableAmount = totalAmount + totalIncentives - totalCommission
                    grandTotalAmount += totalAmount
                    grandTotalIncentives += totalIncentives
                    grandTotalCommission += totalCommission

                    listOf(context.getString(R.string.payable), "", "₹$payableAmount").forEach {
                        table.addCell(Cell().add(Paragraph(it)).setBackgroundColor(grayColor))
                    }

                    document.add(table)
                    document.add(Paragraph(""))
                }

                val summaryTable = Table(floatArrayOf(3f, 3f)).useAllAvailableWidth()
                summaryTable.addHeaderCell(
                    Cell(
                        1,
                        2
                    ).add(Paragraph(context.getString(R.string.fleet_report_summary)).setBold())
                        .setBackgroundColor(DeviceRgb(150, 150, 150))
                )
                listOf(
                    context.getString(R.string.total_amount),
                    "₹$grandTotalAmount",
                    context.getString(R.string.total_incentive),
                    "₹$grandTotalIncentives",
                    context.getString(R.string.total_commission),
                    "₹$grandTotalCommission",
                    context.getString(R.string.total_payable_amount),
                    "₹${grandTotalAmount + grandTotalIncentives - grandTotalCommission}"
                ).chunked(2).forEach {
                    summaryTable.addCell(Cell().add(Paragraph(it[0])).setBackgroundColor(grayColor))
                    summaryTable.addCell(Cell().add(Paragraph(it[1])).setBackgroundColor(grayColor))
                }

                document.add(summaryTable)
                document.close()
                Toast.makeText(context, context.getString(R.string.pdf_generated_successfully), Toast.LENGTH_SHORT).show()
                return true
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                return false
            }
        }
    }
}
