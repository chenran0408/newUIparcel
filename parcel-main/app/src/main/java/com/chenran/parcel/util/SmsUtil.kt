package com.chenran.parcel.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import com.chenran.parcel.model.SmsModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

class SmsUtil {
    companion object {
        private const val TAG = "SmsUtil"

        fun readAllSms(context: Context): List<SmsModel> {
            return readSmsByTimeFilter(context, 0)
        }

        fun readSmsByTimeFilter(context: Context, daysFilter: Int): List<SmsModel> {
            val smsList = mutableListOf<SmsModel>()
            val contentResolver: ContentResolver = context.contentResolver

            var selection: String? = null
            var selectionArgs: Array<String>? = null

            if (daysFilter > 0) {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.add(Calendar.DAY_OF_YEAR, -(daysFilter - 1))
                val startTime = calendar.timeInMillis
                selection = "date >= ?"
                selectionArgs = arrayOf(startTime.toString())
            }

            val projection = arrayOf("_id", "body", "date", "address")
            val sortOrder = "date DESC"

            // Try multiple URIs for maximum compatibility with HarmonyOS / different ROMs
            val urisToTry = listOf(
                Telephony.Sms.Inbox.CONTENT_URI to "Telephony.Sms.Inbox",
                Uri.parse("content://sms/inbox") to "sms/inbox",
                Telephony.Sms.CONTENT_URI to "Telephony.Sms (all)",
                Uri.parse("content://sms") to "sms (all)",
            )

            for ((uri, label) in urisToTry) {
                if (smsList.isNotEmpty()) break
                try {
                    val cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
                    if (cursor != null) {
                        val count = cursor.count
                        Log.d(TAG, "尝试 $label: cursor count=$count")
                        addLog(context, "尝试 $label: count=$count")
                        if (count > 0 && cursor.moveToFirst()) {
                            do {
                                val id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"))
                                val messageBody = cursor.getString(cursor.getColumnIndexOrThrow("body")) ?: ""
                                val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("date"))
                                smsList.add(SmsModel(id.toString(), messageBody, timestamp))
                            } while (cursor.moveToNext())
                            Log.d(TAG, "$label: 成功读取 ${smsList.size} 条短信")
                            addLog(context, "$label: 成功读取 ${smsList.size} 条短信")
                        }
                        cursor.close()
                    } else {
                        Log.w(TAG, "$label: cursor is null")
                        addLog(context, "$label: cursor=null")
                    }
                } catch (e: SecurityException) {
                    Log.w(TAG, "$label: 权限不足 - ${e.message}")
                    addLog(context, "$label: 权限不足 - ${e.message}")
                } catch (e: Exception) {
                    Log.e(TAG, "$label: 读取失败 - ${e.message}")
                    addLog(context, "$label: 读取失败 - ${e.message}")
                }
            }

            if (smsList.isEmpty()) {
                addLog(context, "所有短信URI均无法读取。请尝试使用通知监听功能。")
            }

            return smsList
        }

        fun inboxContainsBodyRecent(context: Context, body: String, windowMs: Long = 60 * 60 * 1000L): Boolean {
            return try {
                val resolver = context.contentResolver
                val now = System.currentTimeMillis()
                val selection = "date >= ? AND body = ?"
                val args = arrayOf((now - windowMs).toString(), body)
                val urisToTry = listOf(
                    Telephony.Sms.Inbox.CONTENT_URI,
                    Uri.parse("content://sms/inbox"),
                )
                for (uri in urisToTry) {
                    try {
                        resolver.query(uri, arrayOf("_id"), selection, args, null)?.use { c ->
                            if (c.moveToFirst()) return true
                        }
                    } catch (_: Exception) { }
                }
                false
            } catch (e: Exception) {
                addLog(context, "检查最近短信失败: ${e.message}")
                false
            }
        }
    }
}

fun dateToString(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        .format(Date(timestamp))
}

fun isCustomSms(sms: SmsModel): Boolean {
    return sms.body.startsWith("【自定义取件短信】")
}

fun isSameDay(ts1: Long, ts2: Long): Boolean {
    val c1 = Calendar.getInstance()
    c1.timeInMillis = ts1
    val c2 = Calendar.getInstance()
    c2.timeInMillis = ts2
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

fun formatPickupCode(code: String): String {
    return code.split(",").map { singleCode ->
        val trimmed = singleCode.trim()
        val digitsOnly = trimmed.filter { it.isDigit() }
        if (digitsOnly.length >= 8 && digitsOnly.length == trimmed.length) {
            digitsOnly.chunked(4).joinToString(" ")
        } else {
            trimmed
        }
    }.joinToString(", ")
}
