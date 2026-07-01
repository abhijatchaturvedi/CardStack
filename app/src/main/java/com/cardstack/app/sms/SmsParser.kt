package com.cardstack.app.sms

import com.cardstack.app.data.db.TransactionCategory
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ParsedSmsTransaction(
    val amount: Double,
    val lastFourDigits: String,
    val merchant: String,
    val dateMillis: Long,
    val category: TransactionCategory
)

object SmsParser {

    // Rs.1,234.56 | Rs 1234 | INR 1,234 | ₹500
    private val AMOUNT_RE = Regex(
        """(?:Rs\.?|INR|₹)\s*([0-9,]+(?:\.[0-9]{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    // "ending 1234" | "ending XX1234" | "XX1234" | "xx1234"
    private val LAST4_RE = Regex(
        """(?:ending\s+(?:XX)?|XX|xx)([0-9]{4})""",
        RegexOption.IGNORE_CASE
    )

    // "at MERCHANT on" / "at MERCHANT." — stops before " on ", comma, period, or end
    private val MERCHANT_AT_RE = Regex(
        """\bat\s+([A-Za-z0-9][A-Za-z0-9 &'\-()/,.]{0,39}?)(?=\s+on\b|\s*[.,]|\s*$)""",
        RegexOption.IGNORE_CASE
    )

    // "Merchant: MERCHANT" — used by Axis, some Kotak formats
    private val MERCHANT_LABEL_RE = Regex(
        """[Mm]erchant[:\s]+([A-Za-z0-9][A-Za-z0-9 &'\-()/,.]{0,39})"""
    )

    private val DATE_RE = Regex(
        """\b(\d{4}-\d{2}-\d{2}|\d{2}[-/][A-Za-z]{3}[-/]\d{2,4}|\d{2}[-/]\d{2}[-/]\d{2,4})\b"""
    )
    private val DATE_FORMATS = listOf(
        "yyyy-MM-dd",
        "dd-MMM-yy", "dd-MMM-yyyy",
        "dd/MMM/yy", "dd/MMM/yyyy",
        "dd-MM-yy", "dd-MM-yyyy",
        "dd/MM/yy", "dd/MM/yyyy"
    ).map { DateTimeFormatter.ofPattern(it, Locale.ENGLISH) }

    fun parse(sms: String): ParsedSmsTransaction? {
        if (!isTransactionSms(sms)) return null

        val amount = AMOUNT_RE.find(sms)
            ?.groupValues?.get(1)
            ?.replace(",", "")
            ?.toDoubleOrNull()
            ?: return null

        val lastFour = LAST4_RE.find(sms)?.groupValues?.get(1) ?: return null

        val merchant = (MERCHANT_AT_RE.find(sms)?.groupValues?.get(1)
            ?: MERCHANT_LABEL_RE.find(sms)?.groupValues?.get(1))
            ?.trim()
            ?.trimEnd(',', '.', ' ')
            ?.take(40)
            ?: return null

        val dateMillis = DATE_RE.find(sms)?.groupValues?.get(1)
            ?.let(::parseDate)
            ?: System.currentTimeMillis()

        return ParsedSmsTransaction(
            amount = amount,
            lastFourDigits = lastFour,
            merchant = merchant,
            dateMillis = dateMillis,
            category = inferCategory(merchant)
        )
    }

    private fun isTransactionSms(sms: String): Boolean {
        val s = sms.lowercase()
        val hasCard = "credit card" in s || "creditcard" in s || "debit card" in s
        val hasTxn = "spent" in s || "debited" in s || "used at" in s || "purchase" in s || "txn" in s
        val hasCurrency = "rs" in s || "inr" in s || "₹" in s
        return hasCard && hasTxn && hasCurrency
    }

    private fun parseDate(raw: String): Long? {
        for (fmt in DATE_FORMATS) {
            runCatching {
                return LocalDate.parse(raw, fmt)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }
        }
        return null
    }

    private fun inferCategory(merchant: String): TransactionCategory {
        val m = merchant.lowercase()
        return when {
            foodWords.any { it in m }          -> TransactionCategory.FOOD
            travelWords.any { it in m }        -> TransactionCategory.TRAVEL
            fuelWords.any { it in m }          -> TransactionCategory.FUEL
            entertainmentWords.any { it in m } -> TransactionCategory.ENTERTAINMENT
            utilityWords.any { it in m }       -> TransactionCategory.UTILITIES
            shoppingWords.any { it in m }      -> TransactionCategory.SHOPPING
            else                               -> TransactionCategory.OTHER
        }
    }

    private val foodWords = listOf(
        "zomato", "swiggy", "restaurant", "cafe", "food", "pizza",
        "burger", "kitchen", "dhaba", "bistro", "bakery", "diner"
    )
    private val travelWords = listOf(
        "airlines", "flight", "hotel", "makemytrip", "ola", "uber",
        "cab", "irctc", "train", "railway", "mmt", "goibibo", "redbus"
    )
    private val fuelWords = listOf(
        "petrol", "fuel", "diesel", "iocl", "bpcl", "hpcl",
        "hp petro", "indian oil", "gas station", "cng"
    )
    private val entertainmentWords = listOf(
        "netflix", "spotify", "prime", "hotstar", "youtube",
        "pvr", "inox", "bookmyshow", "cinema", "movie"
    )
    private val utilityWords = listOf(
        "electricity", "jio", "airtel", "bsnl", "broadband",
        "water", "gas", "bill", "insurance", "recharge"
    )
    private val shoppingWords = listOf(
        "amazon", "flipkart", "myntra", "ajio", "nykaa",
        "mall", "mart", "shop", "store", "retail"
    )
}
