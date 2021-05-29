package edu.mines.csci341.hackathon

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable

@Serializable
data class Competition(
	val id: Int,
	val title: String,
	val description: String,
	val contents: String,
	val created: LocalDate = Clock.System.now()
		.toLocalDateTime(TimeZone.currentSystemDefault()).date,
	val isActive: Boolean = true,
) {
	var expectedResults: List<Pair<String, String>> = listOf()
	var participants: List<User> = listOf()
	val semester: String
		get() = when (created.monthNumber) {
			1..4 -> "Spring ${created.year}"
			5..7 -> "Summer ${created.year}"
			8..12 -> "Fall ${created.year}"
			else -> throw IllegalStateException()
		}
}
