package com.testy.domain

import com.testy.model.ExamSession
import com.testy.model.Question

object ExamGenerator {

    /**
     * Generates an official state driving exam session for a given category.
     * Total questions: 32 (20 Basic + 12 Specialist)
     * Total points: 74 max
     * Passing threshold: >= 68 points
     */
    fun generateExam(category: String, allQuestions: List<Question>): ExamSession {
        val categoryQuestions = allQuestions.filter { it.matchesCategory(category) }
        val pool = if (categoryQuestions.size >= 32) categoryQuestions else allQuestions

        val basicPool = pool.filter { it.isBasic }
        val specialistPool = pool.filter { it.isSpecialist }

        // Part 1: Basic questions (20 total: 10x3pt, 6x2pt, 4x1pt)
        val basic3pt = selectRandom(basicPool.filter { it.points == 3 }, 10)
        val basic2pt = selectRandom(basicPool.filter { it.points == 2 }, 6)
        val basic1pt = selectRandom(basicPool.filter { it.points == 1 }, 4)

        var selectedBasic = (basic3pt + basic2pt + basic1pt).distinctBy { it.id }
        if (selectedBasic.size < 20) {
            val remainingBasic = (basicPool - selectedBasic.toSet()).shuffled()
            selectedBasic = (selectedBasic + remainingBasic).take(20)
        }

        // Part 2: Specialist questions (12 total: 6x3pt, 4x2pt, 2x1pt)
        val spec3pt = selectRandom(specialistPool.filter { it.points == 3 }, 6)
        val spec2pt = selectRandom(specialistPool.filter { it.points == 2 }, 4)
        val spec1pt = selectRandom(specialistPool.filter { it.points == 1 }, 2)

        var selectedSpec = (spec3pt + spec2pt + spec1pt).distinctBy { it.id }
        if (selectedSpec.size < 12) {
            val remainingSpec = (specialistPool - selectedSpec.toSet()).shuffled()
            selectedSpec = (selectedSpec + remainingSpec).take(12)
        }

        val examQuestions = selectedBasic.shuffled() + selectedSpec.shuffled()

        return ExamSession(
            category = category.uppercase(),
            questions = examQuestions,
            startTimeMillis = 0L
        )
    }

    private fun selectRandom(list: List<Question>, count: Int): List<Question> {
        if (list.size <= count) return list.shuffled()
        return list.shuffled().take(count)
    }
}
