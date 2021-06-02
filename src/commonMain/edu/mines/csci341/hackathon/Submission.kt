package edu.mines.csci341.hackathon

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Submission(
	val id: Int,
	val contents: String,
	val timestamp: Instant = Clock.System.now(),
) {
	var results: List<Pair<String, String>> = listOf()

	companion object {
		val subs = listOf(
			Submission(1, "Test Submission 1"),
			Submission(2, "Test Submission 2"),
			Submission(3, "Test Submission 3"),
		)
		init {
			subs[0].results = listOf("1" to "2")
			subs[1].results = listOf("2" to "1")
		}
	}
}
