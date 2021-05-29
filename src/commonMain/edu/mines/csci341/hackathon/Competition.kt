package edu.mines.csci341.hackathon

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toLocalDate

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
			in 1..4 -> "Spring ${created.year}"
			in 5..7 -> "Summer ${created.year}"
			in 8..12 -> "Fall ${created.year}"
			else -> throw IllegalStateException()
		}

	companion object {
		val comps = listOf(
			Competition(1, "Test 1", "Test Description", "Test Contents", "2020-08-29".toLocalDate()),
			Competition(2, "Test 2", "Test Description", "Test Contents", "2021-01-29".toLocalDate()),
			Competition(3, "Test 3", "Test Description", "Test Contents", "2021-05-29".toLocalDate(), false),
		)
		init {
			comps[0].apply {
				expectedResults = listOf("1" to "2")
				participants = User.users
			}
			comps[1].apply {
				expectedResults = listOf("2" to "3")
				participants = User.users
			}
			comps[2].apply {
				expectedResults = listOf("3" to "4")
				participants = User.users
			}
		}
	}
}
