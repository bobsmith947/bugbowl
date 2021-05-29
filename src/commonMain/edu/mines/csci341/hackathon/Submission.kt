package edu.mines.csci341.hackathon

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Submission(
	val id: Int,
	val competition: Competition,
	val contents: String,
	val timestamp: Instant = Clock.System.now(),
) {
	var results: List<Pair<String, String>> = listOf()
	val isCorrect: Boolean
		get() = results == competition.expectedResults

	companion object {
		val subs = listOf(
			Submission(1, Competition.comps[0], "Test Contents"),
			Submission(2, Competition.comps[1], "Test Contents"),
			Submission(3, Competition.comps[2], "Test Contents"),
		)
		init {
			subs[0].results = listOf("1" to "2")
			subs[1].results = listOf("2" to "1")
		}
	}
}
