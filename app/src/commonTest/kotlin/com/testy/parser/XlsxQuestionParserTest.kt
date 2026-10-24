package com.testy.parser

import com.testy.model.CorrectAnswer
import com.testy.model.QuestionScope
import kotlin.test.Test
import kotlin.test.assertEquals

class XlsxQuestionParserTest {

    @Test
    fun testXmlParser() {
        val sharedStringsXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                <si><t>101</t></si>
                <si><t>Czy w tej sytuacji masz obowiązek zatrzymać pojazd?</t></si>
                <si><t>T</t></si>
                <si><t>AK_D05.wmv</t></si>
                <si><t>PODSTAWOWY</t></si>
                <si><t>3</t></si>
                <si><t>B,A</t></si>
            </sst>
        """.trimIndent()

        val sheet1Xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                <sheetData>
                    <row r="1">
                        <c r="A1"><v>Header</v></c>
                    </row>
                    <row r="2">
                        <c r="A2" t="s"><v>0</v></c>
                        <c r="B2" t="s"><v>0</v></c>
                        <c r="C2" t="s"><v>1</v></c>
                        <c r="G2" t="s"><v>2</v></c>
                        <c r="H2" t="s"><v>3</v></c>
                        <c r="I2" t="s"><v>4</v></c>
                        <c r="J2" t="s"><v>5</v></c>
                        <c r="K2" t="s"><v>6</v></c>
                    </row>
                </sheetData>
            </worksheet>
        """.trimIndent()

        val parsed = XlsxQuestionParser.parseXlsxFromXmlStrings(sharedStringsXml, sheet1Xml)
        assertEquals(1, parsed.size)

        val q = parsed[0]
        assertEquals("101", q.id)
        assertEquals("Czy w tej sytuacji masz obowiązek zatrzymać pojazd?", q.question)
        assertEquals(CorrectAnswer.TAK, q.correctAnswer)
        assertEquals("AK_D05.wmv", q.mediaFileName)
        assertEquals(QuestionScope.PODSTAWOWY, q.scope)
        assertEquals(3, q.points)
        assertEquals(listOf("B", "A"), q.categories)
    }
}
