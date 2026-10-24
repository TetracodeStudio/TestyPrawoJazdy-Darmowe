package com.testy.domain

import com.testy.model.CorrectAnswer
import com.testy.model.Question
import com.testy.model.QuestionScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExamGeneratorTest {

    private fun createSampleQuestion(id: String, isBasic: Boolean, points: Int): Question {
        return Question(
            id = id,
            question = "Pytanie testowe $id?",
            answerA = if (!isBasic) "Odp A" else null,
            answerB = if (!isBasic) "Odp B" else null,
            answerC = if (!isBasic) "Odp C" else null,
            correctAnswer = if (isBasic) CorrectAnswer.TAK else CorrectAnswer.A,
            scope = if (isBasic) QuestionScope.PODSTAWOWY else QuestionScope.SPECJALISTYCZNY,
            points = points,
            categories = listOf("B")
        )
    }

    private fun buildSampleQuestionPool(): List<Question> {
        val list = mutableListOf<Question>()
        var idCounter = 1

        // Basic questions: 15x3pt, 10x2pt, 8x1pt
        repeat(15) { list.add(createSampleQuestion((idCounter++).toString(), true, 3)) }
        repeat(10) { list.add(createSampleQuestion((idCounter++).toString(), true, 2)) }
        repeat(8) { list.add(createSampleQuestion((idCounter++).toString(), true, 1)) }

        // Specialist questions: 10x3pt, 8x2pt, 5x1pt
        repeat(10) { list.add(createSampleQuestion((idCounter++).toString(), false, 3)) }
        repeat(8) { list.add(createSampleQuestion((idCounter++).toString(), false, 2)) }
        repeat(5) { list.add(createSampleQuestion((idCounter++).toString(), false, 1)) }

        return list
    }

    @Test
    fun testGeneratedExamStructure() {
        val questions = buildSampleQuestionPool()
        val session = ExamGenerator.generateExam("B", questions)

        assertEquals(32, session.questions.size, "Exam should contain exactly 32 questions")
        assertEquals(74, session.maxPoints, "Max points should be 74")

        val basicCount = session.questions.count { it.isBasic }
        val specCount = session.questions.count { it.isSpecialist }

        assertEquals(20, basicCount, "Exam should have 20 basic questions")
        assertEquals(12, specCount, "Exam should have 12 specialist questions")
    }

    @Test
    fun testExamScoringAndPassingThreshold() {
        val questions = buildSampleQuestionPool()
        var session = ExamGenerator.generateExam("B", questions)

        // Answer all correctly
        session.questions.forEach { q ->
            session = session.answerCurrentQuestion(q.correctAnswer)
        }

        val result = session.getResult()
        assertEquals(74, result.totalScore)
        assertTrue(result.isPassed, "74 points should pass (minimum is 68)")
    }
}
