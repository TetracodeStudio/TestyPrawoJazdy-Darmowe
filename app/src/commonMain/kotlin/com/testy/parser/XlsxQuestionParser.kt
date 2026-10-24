package com.testy.parser

import com.testy.model.CorrectAnswer
import com.testy.model.Question
import com.testy.model.QuestionScope
import kotlin.text.RegexOption

object XlsxQuestionParser {

    /**
     * Parses questions from sharedStrings XML and sheet1 XML strings.
     */
    fun parseXlsxFromXmlStrings(
        sharedStringsXml: String,
        sheet1Xml: String
    ): List<Question> {
        val sharedStrings = parseSharedStrings(sharedStringsXml)
        return parseSheet1(sheet1Xml, sharedStrings)
    }

    private fun parseSharedStrings(xmlContent: String): List<String> {
        val strings = mutableListOf<String>()
        val siRegex = Regex("""<si\b[^>]*>(.*?)</si>""", RegexOption.DOT_MATCHES_ALL)
        val tRegex = Regex("""<t\b[^>]*>(.*?)</t>""", RegexOption.DOT_MATCHES_ALL)

        for (match in siRegex.findAll(xmlContent)) {
            val siContent = match.groupValues[1]
            val textBuilder = StringBuilder()
            for (tMatch in tRegex.findAll(siContent)) {
                textBuilder.append(tMatch.groupValues[1])
            }
            val decoded = decodeXmlEntities(textBuilder.toString())
            strings.add(decoded)
        }
        return strings
    }

    private fun parseSheet1(xmlContent: String, sharedStrings: List<String>): List<Question> {
        val questions = mutableListOf<Question>()
        val rowRegex = Regex("""<row\b[^>]*>(.*?)</row>""", RegexOption.DOT_MATCHES_ALL)
        val cellRegex = Regex("""<c\b([^>]*)>(.*?)</c>""", RegexOption.DOT_MATCHES_ALL)
        val cellValueRegex = Regex("""<v>(.*?)</v>""")

        var isHeader = true

        for (rowMatch in rowRegex.findAll(xmlContent)) {
            if (isHeader) {
                isHeader = false
                continue
            }

            val rowContent = rowMatch.groupValues[1]
            val cellValues = mutableMapOf<String, String>()

            for (cellMatch in cellRegex.findAll(rowContent)) {
                val cellAttrs = cellMatch.groupValues[1]
                val cellInner = cellMatch.groupValues[2]

                val refMatch = Regex("""r="([A-Z]+)\d+"""").find(cellAttrs) ?: continue
                val colRef = refMatch.groupValues[1]

                val isSharedString = cellAttrs.contains("""t="s"""")

                val valueMatch = cellValueRegex.find(cellInner)
                val rawVal = valueMatch?.groupValues?.get(1)?.trim() ?: ""

                val cellValue = if (isSharedString && rawVal.isNotEmpty()) {
                    val index = rawVal.toIntOrNull()
                    if (index != null && index in sharedStrings.indices) {
                        sharedStrings[index]
                    } else ""
                } else {
                    rawVal
                }

                cellValues[colRef] = cellValue
            }

            val questionNumber = cellValues["B"] ?: cellValues["A"] ?: continue
            if (questionNumber.isBlank()) continue

            val questionText = cellValues["C"] ?: continue
            if (questionText.isBlank()) continue

            val answerA = cellValues["D"]?.takeIf { it.isNotBlank() }
            val answerB = cellValues["E"]?.takeIf { it.isNotBlank() }
            val answerC = cellValues["F"]?.takeIf { it.isNotBlank() }
            val rawCorrectAnswer = cellValues["G"] ?: "TAK"
            val mediaFileName = cellValues["H"]?.takeIf { it.isNotBlank() }
            val rawScope = cellValues["I"] ?: "PODSTAWOWY"
            val rawPoints = cellValues["J"] ?: "3"
            val rawCategories = cellValues["K"] ?: ""

            val points = rawPoints.toIntOrNull() ?: 3
            val categoriesList = rawCategories.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            val questionObj = Question(
                id = questionNumber,
                question = questionText,
                answerA = answerA,
                answerB = answerB,
                answerC = answerC,
                correctAnswer = CorrectAnswer.parse(rawCorrectAnswer),
                mediaFileName = mediaFileName,
                scope = QuestionScope.parse(rawScope),
                points = points,
                categories = categoriesList,
                pjmMedia = cellValues["L"]?.takeIf { it.isNotBlank() },
                englishQuestion = cellValues["P"]?.takeIf { it.isNotBlank() },
                germanQuestion = cellValues["T"]?.takeIf { it.isNotBlank() },
                ukrainianQuestion = cellValues["X"]?.takeIf { it.isNotBlank() }
            )

            questions.add(questionObj)
        }

        return questions
    }

    private fun decodeXmlEntities(input: String): String {
        return input
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
    }
}
