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
	
	var reportedBy: String? = null
		set(value) {
			field = "$value at ${Clock.System.now()}"
		}
}
