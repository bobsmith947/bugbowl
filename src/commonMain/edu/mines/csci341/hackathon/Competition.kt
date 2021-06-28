package edu.mines.csci341.hackathon

import kotlinx.datetime.*
import kotlinx.serialization.Serializable

@Serializable
data class Competition(
	val id: Int,
	val title: String,
	val description: String,
	val contents: String,
	val solutionContents: String? = null,
	val created: LocalDate = Clock.System.now()
		.toLocalDateTime(TimeZone.currentSystemDefault()).date,
	val activated: Pair<LocalDateTime, LocalDateTime>? = null,
) {
	var expectedResults: List<Pair<String, String>> = listOf()
	var groups: MutableMap<String, MutableList<User>> = mutableMapOf()
	var submissions: MutableMap<String, MutableList<Submission>> = mutableMapOf()
	
	var isActive: Boolean = false
		get() = field || checkActive()
	
	val inputs: List<String>
		get() = expectedResults.unzip().first
	
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
	
	fun checkActive(): Boolean {
		val current: LocalDateTime = Clock.System.now()
			.toLocalDateTime(TimeZone.currentSystemDefault())
		val default: LocalDateTime = Instant.DISTANT_FUTURE
			.toLocalDateTime(TimeZone.currentSystemDefault())
		val start: LocalDateTime = activated?.first ?: default
		val end: LocalDateTime = activated?.second ?: default
		return current in start..end
	}
	
	fun getGroupName(user: User): String? {
		groups.forEach { (name, users) ->
			if (users.contains(user)) {
				return name
			}
		}
		return null
	}
}
