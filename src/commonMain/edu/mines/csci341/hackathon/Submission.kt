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
}
