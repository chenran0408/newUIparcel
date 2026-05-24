package com.chenran.parcel.model

data class ParcelData(
    val address: String,
    val smsDataList: MutableList<SmsData>,
    var num: Int=0,
    val groupKey: String = address,
)
