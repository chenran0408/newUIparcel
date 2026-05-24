package com.chenran.parcel.util

import android.content.Context
import com.chenran.parcel.model.ParcelData
import com.chenran.parcel.model.SmsData
import com.chenran.parcel.model.SmsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ProcessResult(
    val successful: List<SmsData>,
    val parcels: List<ParcelData>,
    val failed: List<SmsModel>
)

object SmsProcessor {

    private fun lockerSortKey(locker: String): Int {
        if (locker.isEmpty()) return Int.MAX_VALUE
        val firstChar = locker.first()
        if (firstChar in '\u2460'..'\u2473') return firstChar.code - 0x2460 + 1
        val numMatch = Regex("""^\d+""").find(locker)
        return numMatch?.value?.toIntOrNull() ?: Int.MAX_VALUE
    }

    suspend fun loadMessages(context: Context, daysFilter: Int): Pair<List<SmsModel>, List<SmsModel>> = withContext(Dispatchers.IO) {
        val systemSms = SmsUtil.readSmsByTimeFilter(context, daysFilter)
        val customSms = getCustomSmsByTimeFilter(context, daysFilter)
        Pair(systemSms, customSms)
    }

    suspend fun loadAndProcess(
        context: Context,
        daysFilter: Int,
        parser: SmsParser,
        completedIds: List<String>
    ): ProcessResult = withContext(Dispatchers.IO) {
        val systemSms = SmsUtil.readSmsByTimeFilter(context, daysFilter)
        val customSms = getCustomSmsByTimeFilter(context, daysFilter)
        val mergedList = systemSms + customSms
        val addressMappings = getAddressMappings(context)

        process(mergedList, parser, completedIds, addressMappings, getSortByLocker(context))
    }

    fun process(
        messages: List<SmsModel>,
        parser: SmsParser,
        completedIds: List<String>,
        addressMappings: Map<String, String> = emptyMap(),
        sortByLocker: Boolean = false
    ): ProcessResult {
        val successful = mutableListOf<SmsData>()
        val parcelsMap = mutableMapOf<String, ParcelData>()
        val failed = mutableListOf<SmsModel>()

        messages.forEach { sms ->
            val result = parser.parseSms(sms.body)

            if (result.success) {
                val combinedKey = "${sms.id}_${sms.timestamp}"
                val originalAddress = result.address
                val groupAddress = addressMappings[originalAddress] ?: originalAddress
                
                val smsData = SmsData(originalAddress, result.code, sms, combinedKey, false, result.lockerNumber)
                successful.add(smsData)

                val existingParcel = parcelsMap[groupAddress]
                val newItem = SmsData(originalAddress, result.code, sms, combinedKey, false, result.lockerNumber)

                if (existingParcel != null) {
                    val existsSameDaySameAddrCode = existingParcel.smsDataList.any { existing ->
                        existing.address == newItem.address &&
                                existing.code == newItem.code &&
                                isSameDay(existing.sms.timestamp, newItem.sms.timestamp)
                    }
                    if (!existsSameDaySameAddrCode) {
                        existingParcel.smsDataList.add(newItem)
                    }
                } else {
                    parcelsMap[groupAddress] = ParcelData(
                        groupAddress,
                        mutableListOf(newItem)
                    )
                }
            } else if (result.isPickupSms) {
                val combinedKey = "${sms.id}_${sms.timestamp}"
                val displayAddress = if (result.address.isNotEmpty()) result.address else "未识别"
                val displayCode = if (result.code.isNotEmpty()) result.code else ""
                val rawBody = sms.body
                val groupAddress = addressMappings[displayAddress] ?: displayAddress

                val smsData = SmsData(displayAddress, displayCode, sms, combinedKey, false, result.lockerNumber, rawBody)
                successful.add(smsData)

                val existingParcel = parcelsMap[groupAddress]
                val newItem = SmsData(displayAddress, displayCode, sms, combinedKey, false, result.lockerNumber, rawBody)

                if (existingParcel != null) {
                    val existsSameDaySameAddrCode = existingParcel.smsDataList.any { existing ->
                        existing.address == newItem.address &&
                                existing.code == newItem.code &&
                                isSameDay(existing.sms.timestamp, newItem.sms.timestamp)
                    }
                    if (!existsSameDaySameAddrCode) {
                        existingParcel.smsDataList.add(newItem)
                    }
                } else {
                    parcelsMap[groupAddress] = ParcelData(
                        groupAddress,
                        mutableListOf(newItem)
                    )
                }
            } else {
                failed.add(sms)
            }
        }

        // Sorting
        successful.sortByDescending { it.sms.timestamp }
        failed.sortByDescending { it.timestamp }

        val initialParcels = parcelsMap.values.toList()

        initialParcels.forEach { parcel ->
            if (sortByLocker) {
                parcel.smsDataList.sortWith(
                    compareBy(
                        { it.lockerNumber.isEmpty() },
                        { lockerSortKey(it.lockerNumber) },
                        { it.code }
                    )
                )
            } else {
                parcel.smsDataList.sortByDescending { it.sms.timestamp }
            }
        }

        // Calculate num and isCompleted
        val finalParcels = recalculateParcels(initialParcels, completedIds)

        return ProcessResult(successful, finalParcels, failed)
    }

    fun recalculateParcels(parcels: List<ParcelData>, completedIds: List<String>): List<ParcelData> {
        val completedIdsSet = HashSet(completedIds)

        return parcels.map { parcel ->
            // Deep copy of SmsDataList with updated status
            val newSmsDataList = parcel.smsDataList.map { smsData ->
                val combinedKey = "${smsData.sms.id}_${smsData.sms.timestamp}"
                val simpleKey = smsData.sms.id

                val isCompleted = completedIdsSet.contains(combinedKey) || completedIdsSet.contains(simpleKey)

                smsData.copy(isCompleted = isCompleted)
            }.toMutableList()

            val newNum = newSmsDataList.sumOf { smsData ->
                if (!smsData.isCompleted) smsData.code.split(", ").size else 0
            }

            parcel.copy(smsDataList = newSmsDataList, num = newNum)
        }.sortedWith(
            compareByDescending<ParcelData> { it.num > 0 }
                .thenBy { it.address }
        )
    }
}
