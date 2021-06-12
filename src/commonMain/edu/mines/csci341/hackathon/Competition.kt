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
	val isActive: Boolean = false,
	val created: LocalDate = Clock.System.now()
		.toLocalDateTime(TimeZone.currentSystemDefault()).date,
) {
	var expectedResults: List<Pair<String, String>> = listOf()
	var groups: MutableMap<String, MutableList<User>> = mutableMapOf()
	var submissions: MutableMap<String, MutableList<Submission>> = mutableMapOf()
	
	val nextGroupNum: Int
		get() = groups.size + 1
	
	val participants: Set<User>
		get() = groups.flatMap { it.value }.toSet()
	
	val semester: String
		get() = when (created.monthNumber) {
			in 1..4 -> "Spring ${created.year}"
			in 5..7 -> "Summer ${created.year}"
			in 8..12 -> "Fall ${created.year}"
			else -> throw IllegalStateException()
		}
	
	fun checkSubmission(sub: Submission): Boolean {
		return sub.results == expectedResults
	}
	
	fun getGroupName(user: User): String? {
		groups.forEach { (name, users) ->
			if (users.contains(user)) {
				return@getGroupName name
			}
		}
		return null
	}
}
