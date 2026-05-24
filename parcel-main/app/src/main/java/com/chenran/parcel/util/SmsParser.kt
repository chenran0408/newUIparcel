package com.chenran.parcel.util

import android.util.Log
import java.util.regex.Matcher
import java.util.regex.Pattern

class SmsParser {
    val lockerPattern: Pattern =
        Pattern.compile("""(?i)(([0-9①-⑳]+)号(?:智能柜|格口柜|兔喜快递柜|快递柜|丰巢柜|蜂巢柜|熊猫柜|柜)(?:[A-Za-z0-9\-]+(?:格口|智能柜|柜))?)""")
    private val lockerCodePattern: Pattern =
        Pattern.compile("""(?i)([0-9①-⑳]+)号(?:智能柜|格口柜|兔喜快递柜|快递柜|丰巢柜|蜂巢柜|熊猫柜|柜)(?:[A-Za-z0-9\-]+(?:格口|智能柜|柜))?[\s\-]*([A-Za-z0-9\-]+)""")
    private val addressPattern: Pattern =
        Pattern.compile("""(?i)(地址|收货地址|送货地址|位于|放至|已到达|到达|已到|送达|已放入|已存放至|已存放|放入|到|暂存|至|包裹在|派送至|送至|前往|存放在)[\s\S]*?([\u4e00-\u9fa5\w\s\-]+?(?:排\d+号|排|门牌|驿站|快递点|门面|便利店|超市|门口|号|店|柜|,|，|。|$))""")
    private val stationPattern: Pattern =
        Pattern.compile("""(?:^|[\s\|｜,，])((?:[\u4e00-\u9fa5][\u4e00-\u9fa5\w\s\-]*?)(?:驿站|快递超市|快递点|代收点|服务站|自提点|提货点|菜鸟驿站|便利店|超市|丰巢|蜂巢))""")
    private val codePattern: Pattern = Pattern.compile(
        """(?i)(请用|取件码为|取件码是|提货号为|取货码为|提货码为|取件码"|提货号"|取货码"|提货码"|凭"|取件码"|提货号"|取货码"|提货码"|凭"|取件码（|提货号（|取货码（|提货码（|凭（|取件码『|提货号『|取货码『|提货码『|凭『|取件码【|提货号【|取货码【|提货码【|凭【|取件码\(|提货号\(|取货码\(|提货码\(|凭\(|取件码\[|提货号\[|取货码\[|提货码\[|凭\[|取件码|件码|提货号|取货码|提货码|凭|签收码|操作码|提货编码|收货编码|签收编码|取件編號|提貨號碼|運單碼|快遞碼|快件碼|包裹碼|貨品碼)\s*([A-Za-z0-9\s\-]{2,}(?:[，,、][A-Za-z0-9\s\-]{2,})*)"""
    )

    private val verificationCodePattern: Pattern = Pattern.compile(
        """(?i)(验证码|校验码|动态码|安全码|登录码|注册码|短信码|确认码|auth\s*code|verification\s*code|OTP)"""
    )

    private val pickupKeywordPattern: Pattern = Pattern.compile(
        """(?i)(取件码|提货号|取货码|提货码|签收码|操作码|提货编码|收货编码|签收编码|取件編號|提貨號碼|運單碼|快遞碼|快件碼|包裹碼|貨品碼|凭[0-9A-Za-z]|快递柜|丰巢|蜂巢|件码)"""
    )

    private val customAddressPatterns = mutableListOf<String>()
    private val customCodePatterns = mutableListOf<Pattern>()
    private val customLockerPatterns = mutableListOf<Pattern>()
    private val ignoreKeywords = mutableListOf<String>()

    data class ParseResult(val address: String, val code: String, val lockerNumber: String, val success: Boolean, val isPickupSms: Boolean)

    fun parseSms(sms: String): ParseResult {
        var foundAddress = ""
        var foundCode = ""
        var foundLocker = ""

        val isPickupSms = pickupKeywordPattern.matcher(sms).find()

        if (verificationCodePattern.matcher(sms).find()) {
            if (!isPickupSms) {
                return ParseResult("", "", "", false, false)
            }
        }

        for (ignoreKeyword in ignoreKeywords) {
            if (ignoreKeyword.isNotBlank() && sms.contains(ignoreKeyword, ignoreCase = true)) {
                return ParseResult("", "", "", false, false)
            }
        }

        for (pattern in customAddressPatterns) {
            if (sms.contains(pattern, ignoreCase = true)) {
                foundAddress = pattern
                break
            }
        }
        for (pattern in customCodePatterns) {
            val matcher = pattern.matcher(sms)
            if (matcher.find()) {
                foundCode = matcher.group(1)?.toString() ?: ""
                break
            }
        }
        for (pattern in customLockerPatterns) {
            val matcher = pattern.matcher(sms)
            if (matcher.find()) {
                foundLocker = matcher.group(1)?.toString() ?: ""
                break
            }
        }

        if (foundAddress.isEmpty()) {
            val addressMatcher: Matcher = addressPattern.matcher(sms)
            var longestAddress = ""
            while (addressMatcher.find()) {
                val currentAddress = addressMatcher.group(2)?.toString() ?: ""
                if (currentAddress.length > longestAddress.length) {
                    longestAddress = currentAddress
                }
            }
            foundAddress = longestAddress
        }

        if (foundAddress.isEmpty()) {
            val stationMatcher: Matcher = stationPattern.matcher(sms)
            var longestStation = ""
            while (stationMatcher.find()) {
                val currentStation = stationMatcher.group(1)?.toString() ?: ""
                if (currentStation.length > longestStation.length) {
                    longestStation = currentStation
                }
            }
            foundAddress = longestStation
        }

        if (foundLocker.isEmpty()) {
            val lockerMatcher: Matcher = lockerPattern.matcher(sms)
            var lastMatch = ""
            while (lockerMatcher.find()) {
                lastMatch = lockerMatcher.group(1) ?: ""
            }
            if (lastMatch.isNotEmpty()) {
                foundLocker = lastMatch
            }
        }

        val lockerCodeMatcher = lockerCodePattern.matcher(sms)
        if (lockerCodeMatcher.find()) {
            if (foundCode.isEmpty()) {
                foundCode = lockerCodeMatcher.group(2) ?: ""
            }
        }

        if (foundCode.isEmpty()) {
            val codeMatcher: Matcher = codePattern.matcher(sms)
            while (codeMatcher.find()) {
                val match = codeMatcher.group(2)
                if (match != null) {
                    val codes = match.split(Regex("[，,、]"))
                    foundCode = codes.joinToString(", ") { it.trim() }
                    break
                }
            }
        }

        foundAddress = foundAddress.replace(Regex("[,，。]"), "")
        foundAddress = foundAddress.replace("取件", "")
        foundAddress = Regex("""[0-9①-⑳]+号(?:智能柜|格口柜|兔喜快递柜|快递柜|丰巢柜|蜂巢柜|熊猫柜|柜(?:[A-Za-z0-9\-]+(?:格口|智能柜|柜))?)""").replace(foundAddress, "").trim()
        val success = foundAddress.isNotEmpty() && foundCode.isNotEmpty()
        if (sms.contains("自定义取件短信")) {
            Log.d("SmsParser", "自定义短信解析: address='$foundAddress', code='$foundCode', locker='$foundLocker', success=$success, isPickupSms=$isPickupSms")
        }
        return ParseResult(
            foundAddress,
            foundCode,
            foundLocker,
            success,
            isPickupSms
        )
    }

    fun addCustomAddressPattern(pattern: String) {
        customAddressPatterns.add(pattern)
    }

    fun addCustomCodePattern(pattern: String) {
        customCodePatterns.add(Pattern.compile(pattern))
    }

    fun addCustomLockerPattern(pattern: String) {
        customLockerPatterns.add(Pattern.compile(pattern))
    }

    fun clearAllCustomPatterns() {
        customAddressPatterns.clear()
        customCodePatterns.clear()
        customLockerPatterns.clear()
        ignoreKeywords.clear()
    }

    fun addIgnoreKeyword(keyword: String) {
        if (keyword.isNotBlank() && !ignoreKeywords.contains(keyword)) {
            ignoreKeywords.add(keyword)
        }
    }

    fun removeIgnoreKeyword(keyword: String) {
        ignoreKeywords.remove(keyword)
    }

    fun getIgnoreKeywords(): List<String> = ignoreKeywords.toList()

    fun clearIgnoreKeywords() {
        ignoreKeywords.clear()
    }
}
