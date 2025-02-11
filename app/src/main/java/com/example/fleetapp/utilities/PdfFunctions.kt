package com.example.fleetapp.utilities

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.fleetapp.R
import com.example.fleetapp.dataclasses.FormData
import com.example.fleetapp.dataclasses.PartnerMetaData
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
        private fun getPublicDirectory(context: Context): File {
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "${context.getString(R.string.fleet_report)}s"
            )
            if (!directory.exists()) {
                directory.mkdirs()
            }
            return directory
        }

        fun createPdf(
            context: Context,
            formDataMap: Map<String, List<FormData>>,
            partnerMetadata: Map<String, PartnerMetaData>,
            nameOfCompany: String,
            startDate: String,
            endDate: String,
            isIndividual: Boolean = false
        ): Boolean {
            try {
                val directory = getPublicDirectory(context)
                val currentTime = LocalTime.now().toString().replace(":", "-").substringBefore('.')
                val sanitizedStartDate = startDate.replace("/", "-")
                val sanitizedEndDate = endDate.replace("/", "-")
                val title = "${nameOfCompany}--${sanitizedStartDate}--${sanitizedEndDate}--${currentTime}"

                val pdfFile = File(directory, "${title}.pdf")
                val pdfWriter = PdfWriter(pdfFile)
                val pdfDocument = PdfDocument(pdfWriter)
                val document = Document(pdfDocument)

                document.add(
                    Paragraph("$nameOfCompany ${if (isIndividual) "" else context.getString(R.string.fleet_report)}")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(18f)
                        .setBold().setMarginBottom(8f)
                )
                document.add(
                    Paragraph("From $startDate to $endDate")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(14f).setItalic()
                        .setMarginBottom(20f)
                )

                var grandTotalAmount = 0f
                var grandTotalIncentives = 0f
                var grandTotalCommission = 0f

                val grayColor = DeviceRgb(200, 200, 200)
                val whiteColor = DeviceRgb(255, 255, 255)
                val lightGrayColor = DeviceRgb(240, 240, 240)

                formDataMap.forEach { (partnerName, formDataList) ->
                    document.add(
                        Paragraph("${context.getString(R.string.partner_name)}: $partnerName")
                            .setFontSize(16f)
                            .setBold()
                            .setMarginBottom(10f)
                    )

                    val table = Table(floatArrayOf(3f, 3f, 2f, 2f))
                        .useAllAvailableWidth()

                    val labels = listOf(
                        context.getString(R.string.vehicle_number),
                        context.getString(R.string.driver_name),
                        context.getString(R.string.amount),
                        context.getString(R.string.incentive)
                    )

                    labels.forEach {
                        table.addHeaderCell(
                            Cell()
                                .add(Paragraph(it).setBold())
                                .setBackgroundColor(grayColor)
                                .setTextAlignment(TextAlignment.CENTER)
                        )
                    }

                    var totalAmount = 0f
                    var totalIncentives = 0f

                    var isGray = false
                    formDataList.forEach { formData ->
                        val bgColor = if (isGray) lightGrayColor else whiteColor
                        isGray = !isGray

                        table.addCell(
                            Cell()
                                .add(Paragraph(formData.vehicleNumber))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setBackgroundColor(bgColor)
                        )

                        table.addCell(
                            Cell()
                                .add(Paragraph(formData.driverName))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setBackgroundColor(bgColor)
                        )

                        table.addCell(
                            Cell()
                                .add(Paragraph("₹${formData.amount}"))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setBackgroundColor(bgColor)
                        )

                        table.addCell(
                            Cell()
                                .add(Paragraph("₹${formData.incentive}"))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setBackgroundColor(bgColor)
                        )

                        totalAmount += formData.amount
                        totalIncentives += formData.incentive
                    }

                    table.addCell(
                        Cell(1, 2)
                            .add(Paragraph(context.getString(R.string.total)))
                            .setBackgroundColor(grayColor)
                            .setTextAlignment(TextAlignment.RIGHT)
                    )

                    listOf("₹$totalAmount", "₹$totalIncentives").forEach {
                        table.addCell(
                            Cell()
                                .add(Paragraph(it))
                                .setBackgroundColor(grayColor)
                                .setTextAlignment(TextAlignment.CENTER)
                        )
                    }

                    table.addCell(
                        Cell(1, 2)
                            .add(Paragraph(context.getString(R.string.total_incentive)))
                            .setBackgroundColor(whiteColor)
                            .setTextAlignment(TextAlignment.RIGHT)
                    )

                    listOf("+₹${totalIncentives}", "").forEach {
                        table.addCell(
                            Cell()
                                .add(Paragraph(it))
                                .setBackgroundColor(whiteColor)
                                .setTextAlignment(TextAlignment.CENTER)
                        )
                    }

                    val totalCommission = partnerMetadata[partnerName]?.commission ?: 0f
                    grandTotalAmount += totalAmount
                    grandTotalIncentives += totalIncentives
                    grandTotalCommission += totalCommission

                    table.addCell(
                        Cell(1, 2)
                            .add(Paragraph(context.getString(R.string.total_commission)))
                            .setBackgroundColor(whiteColor)
                            .setTextAlignment(TextAlignment.RIGHT)
                    )

                    listOf("-₹$totalCommission", "").forEach {
                        table.addCell(
                            Cell().add(Paragraph(it))
                                .setBackgroundColor(whiteColor)
                                .setTextAlignment(TextAlignment.CENTER)
                        )
                    }

                    val payableAmount = totalAmount + totalIncentives - totalCommission

                    table.addCell(
                        Cell(1, 2)
                            .add(Paragraph(context.getString(R.string.payable)))
                            .setBackgroundColor(grayColor)
                            .setTextAlignment(TextAlignment.RIGHT)
                    )

                    listOf("₹$payableAmount", "").forEach {
                        table.addCell(
                            Cell()
                                .add(Paragraph(it))
                                .setBackgroundColor(grayColor)
                                .setTextAlignment(TextAlignment.CENTER)
                        )
                    }

                    if (partnerMetadata[partnerName]?.remarks != "") {
                        table.addCell(
                            Cell()
                                .add(Paragraph(context.getString(R.string.remarks)))
                                .setBackgroundColor(lightGrayColor)
                                .setTextAlignment(TextAlignment.CENTER)
                        )
                        table.addCell(
                            Cell(1, 3)
                                .add(Paragraph(partnerMetadata[partnerName]?.remarks ?: ""))
                                .setBackgroundColor(lightGrayColor)
                                .setTextAlignment(TextAlignment.CENTER)
                        )
                    }

                    document.add(table)
                    document.add(
                        Paragraph("")
                            .setMarginBottom(15f)
                    )
                }

                document.add(
                    Paragraph("")
                        .setMarginBottom(30f)
                )

                if (!isIndividual) {
                    val summaryTable = Table(floatArrayOf(3f, 3f))
                        .useAllAvailableWidth()

                    summaryTable.addHeaderCell(
                        Cell(1, 2)
                            .add(Paragraph(context.getString(R.string.fleet_report_summary)).setBold())
                            .setBackgroundColor(grayColor)
                            .setTextAlignment(TextAlignment.CENTER)
                    )

                    listOf(
                        context.getString(R.string.total_amount), "₹$grandTotalAmount",
                        context.getString(R.string.total_incentive), "₹$grandTotalIncentives",
                        context.getString(R.string.total_commission), "₹$grandTotalCommission",
                        context.getString(R.string.total_payable_amount), "₹${grandTotalAmount + grandTotalIncentives - grandTotalCommission}"
                    ).chunked(2).forEach {
                        summaryTable.addCell(
                            Cell()
                                .add(Paragraph(it[0]))
                                .setBackgroundColor(lightGrayColor)
                                .setTextAlignment(TextAlignment.CENTER)
                        )

                        summaryTable.addCell(
                            Cell()
                                .add(Paragraph(it[1]))
                                .setBackgroundColor(lightGrayColor)
                                .setTextAlignment(TextAlignment.CENTER)
                        )
                    }

                    document.add(summaryTable)
                }

                document.close()
                return true
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                return false
            }
        }

        fun loadPdfs(context: Context): List<File> {
            return try {
                val directory = getPublicDirectory(context)
                directory.listFiles()?.filter { it.extension == "pdf" } ?: emptyList()
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                emptyList()
            }
        }

        fun deletePdf(context: Context, file: File): Boolean {
            return try {
                if (file.delete()) {
                    true
                } else {
                    Toast.makeText(context, context.getString(R.string.failed_to_delete_pdf), Toast.LENGTH_SHORT).show()
                    false
                }
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.failed_to_delete_pdf), Toast.LENGTH_SHORT).show()
                false
            }
        }

        fun sharePdf(context: Context, file: File) {
            try {
                val authority = context.getString(R.string.authority)
                val uri = FileProvider.getUriForFile(context, authority, file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share PDF"))
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.failed_to_share_pdf), Toast.LENGTH_SHORT).show()
            }
        }

        fun openPdf(context: Context, file: File) {
            try {
                val authority = context.getString(R.string.authority)
                val uri = FileProvider.getUriForFile(context, authority, file)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.failed_to_open_pdf), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
