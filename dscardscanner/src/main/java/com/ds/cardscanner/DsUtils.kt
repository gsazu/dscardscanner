package com.ds.cardscanner

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.text.isDigitsOnly
import java.io.File


class DsUtils {


    companion object {
        private var expOne = ""
        private var countForExp = 0
        var cardName = ""
        var cardExpiry = ""
        var cardNumber = ""
        var cardCvv = ""

        fun getCardNumber(value: String): String {
            var rCardNumber = ""
            try {
                val trimmedString = value.replace(" ", "")
                if (trimmedString.length == 16 && trimmedString.isDigitsOnly()) {
                    Log.d("Hello", "getCardNumber: $trimmedString")
                    cardNumber = trimmedString
                    rCardNumber = trimmedString
                }
            } catch (e: Exception) {
            }
            return if (rCardNumber.isNotEmpty()) "Card Number: $rCardNumber" else ""
        }

        fun getCardNumberFromList(list: List<String>): String {
            var rCardNumber = ""
            try {
                for (i in list) {
                    val trimmedString = i.replace(" ", "")
                    if (trimmedString.length == 16 && trimmedString.isDigitsOnly()) {
                        Log.d("Hello", "getCardNumber: $trimmedString")
                        cardNumber = trimmedString
                        rCardNumber = trimmedString
                    }
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    if (rCardNumber.isEmpty()) {
                        for (i in list) {
                            val trimmedStringg = i.replace(" ", "")
                            val trimmedString = removeAlphabetsAndSpl(trimmedStringg)
                            if (trimmedString.length == 4 || trimmedString.length == 5) {
                                if (trimmedString.length == 4) {
                                    if(cardNumber.length < 16) {
                                        cardNumber += trimmedString
                                        rCardNumber += trimmedString
                                    }
                                } else {
                                    if(cardNumber.length < 16) {
                                        cardNumber += trimmedString.substring(1)
                                        rCardNumber += trimmedString.substring(1)
                                    }
                                }

                                Log.d("Hello", "getCardNumber: $trimmedString")
                            }
                        }
                    }
                }, 1500)
            } catch (e: Exception) {
            }

            return rCardNumber
        }

        fun getExpiryDate(value: String): String {
            var rExpDate = ""
            try {
                val trimmedString = value.replace(" ", "")
                val withoutAlphabetsString = removeAlphabets(trimmedString)
                Log.d("WALPHA", "$withoutAlphabetsString")
                if (withoutAlphabetsString.isNotEmpty()) {
                    if (withoutAlphabetsString.contains("/")) {
                        if (withoutAlphabetsString.length < 11) {
                            val count = countSlashes(withoutAlphabetsString)
                            if (count == 1) {
                                if (withoutAlphabetsString.length == 5) {
                                    if (countForExp == 0) {
                                        expOne = withoutAlphabetsString
                                        Log.d("Exp", "getExpiryDateOne: $expOne")
//                                    cardExpiry = expOne
                                        countForExp += 1
                                    } else {
                                        val expTwo = withoutAlphabetsString
                                        Log.d("Exp", "getExpiryDateTwo: $expTwo")
                                        if (expOne.substring(3).toInt() > expTwo.substring(3)
                                                .toInt()
                                        ) {
                                            Log.d("Hello", "getExpiryDate: $expOne")
                                            rExpDate = expOne
                                            cardExpiry = expOne
                                        } else {
                                            Log.d("Hello", "getExpiryDate: $expTwo")
                                            rExpDate = expTwo
                                            cardExpiry = expTwo
                                        }
                                        countForExp = 0
                                        expOne = ""
                                    }
                                }
                            } else if (count == 2) {
                                if (withoutAlphabetsString.length == 10) {
                                    expOne = withoutAlphabetsString.substring(0, 5)
                                    val expTwo = withoutAlphabetsString.substring(5)
                                    Log.d("Exp", "getExpiryDateOne: $expOne")
                                    Log.d("Exp", "getExpiryDateTwo: $expTwo")
                                    if (expOne.substring(3).toInt() > expTwo.substring(3).toInt()) {
                                        Log.d("Hello", "getExpiryDate: $expOne")
                                        rExpDate = expOne
                                        cardExpiry = expOne
                                    } else {
                                        Log.d("Hello", "getExpiryDate: $expTwo")
                                        rExpDate = expTwo
                                        cardExpiry = expTwo
                                    }
                                    expOne = ""
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
            }
            return if (rExpDate.isNotEmpty()) "Expiry Date: $rExpDate" else ""
        }

        fun getCvv(value: String): String {
            var rCvv = ""
            try {
                val trimmedString = value.trim()
                if (trimmedString.length == 3 && trimmedString.isDigitsOnly()) {
                    Log.d("Hello", "getCvv: $trimmedString")
                    rCvv = trimmedString
                    cardCvv = trimmedString
                } else if (trimmedString.length == 8) {
                    val cvvList = trimmedString.split(" ")
                    if (cvvList.size == 2) {
                        for (cv in cvvList) {
                            if (cv.length == 3 && cv.isDigitsOnly()) {
                                rCvv = cv
                                cardCvv = cv
                                Log.d("Hello", "getCvv: $cv")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
            }

            return if (rCvv.isNotEmpty()) "CVV: $rCvv" else ""

        }

        fun getCardName(value: String): String {
            var rCardName = ""
            try {
                val formedString = onlyAlphabets(value).trim()
                Log.d("formedString", "getCardName: $formedString")
                if (formedString.isNotEmpty() && formedString.length > 2) {
                    if (!isBluff(formedString)) {
                        if (cardName.isEmpty()) {
                            cardName = formedString
                            rCardName = formedString
                            cardName = formedString
                            Log.d("Hello", "getCardName: $cardName")
                        }
                    }
                }
            } catch (e: Exception) {
            }
            return if (rCardName.isNotEmpty()) "CardName: $rCardName" else ""
        }

        fun removeAlphabets(input: String): String {
            // Use regex to remove all alphabets (both lowercase and uppercase)
            return input.replace(Regex("[^0-9/]"), "")
        }

        fun onlyAlphabets(input: String): String {
            // Use regex to remove all alphabets (both lowercase and uppercase)
            return input.replace(Regex("[^a-zA-Z]"), "")
        }

        fun removeAlphabetsAndSpl(input: String): String {
            // Use regex to remove all alphabets (both lowercase and uppercase)
            return input.replace(Regex("[^0-9]"), "")
        }

        fun countSlashes(input: String): Int {
            return input.count { it == '/' }
        }

        fun isBluff(value: String): Boolean {
            var rValue = false
            val lowerCaseValue = value.lowercase()
            val bluffCharList = listOf(
                "signature",
                "debit", "credit", "platinum", "classic",
                "visa", "rupay", "mastercard", "global",
                "amex", "gold", "bank", "legend", "valid",
                "from", "thru", "exp", "date", "security",
                "code", "syndicate", "india", "shopping",
                "month", "year", "simply", "card", "end", "electronic",
                "only", "use"
            )
            for (i in bluffCharList) {
                if (lowerCaseValue.contains(i)) {
                    rValue = true
                    break
                }
            }

            return rValue
        }

        fun clearCache(context: Context) {
            try {
                val dir = context.cacheDir
                if (dir != null && dir.isDirectory) {
                    deleteDir(dir)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun deleteDir(dir: File?): Boolean {
            if (dir != null && dir.isDirectory) {
                val children = dir.list()
                for (child in children) {
                    val success = deleteDir(File(dir, child))
                    if (!success) {
                        return false
                    }
                }
                return dir.delete()
            } else if (dir != null && dir.isFile) {
                return dir.delete()
            }
            return false
        }
    }

    fun clearCache(context: Context) {
        try {
            val dir = context.cacheDir
            if (dir != null && dir.isDirectory) {
                deleteDir(dir)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (child in children) {
                val success = deleteDir(File(dir, child))
                if (!success) {
                    return false
                }
            }
            return dir.delete()
        } else if (dir != null && dir.isFile) {
            return dir.delete()
        }
        return false
    }

}