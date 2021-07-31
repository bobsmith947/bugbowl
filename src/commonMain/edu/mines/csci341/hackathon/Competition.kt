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
	val created: LocalDate = currentDateTime.date,
	val activated: Pair<LocalDateTime, LocalDateTime>? = null,
) {
	var expectedResults: List<Pair<String, String>> = listOf()
	var groups: MutableMap<String, MutableList<User>> = mutableMapOf()
	var submissions: MutableMap<String, MutableList<Submission>> = mutableMapOf()
	
	var isActive: Boolean = false
		get() = field || checkActive()
	
	val isCurrent: Boolean
		get() = semester == currentSemester
	
	val inputs: List<String>
		get() = expectedResults.unzip().first
	
	val nextGroupNum: Int
		get() = groups.size + 1
	
	val participants: Set<User>
		get() = groups.flatMap { it.value }.toSet()
	
	val correctSubmissions: Map<String, Submission?>
		get() = submissions.mapValues { (_, subs) ->
			subs.filter(::checkSubmission)
				.minByOrNull { it.timestamp }
		}
	
	val semester: String
		get() = dateToSemester(created)
	
	fun checkSubmission(sub: Submission): Boolean {
		return sub.results == expectedResults
	}
	
	fun checkActive(): Boolean {
		val default: LocalDateTime = Instant.DISTANT_FUTURE
				.toLocalDateTime(TimeZone.currentSystemDefault())
		val start: LocalDateTime = activated?.first ?: default
		val end: LocalDateTime = activated?.second ?: default
		return currentDateTime in start..end
	}
	
	fun getGroupName(user: User): String? {
		groups.forEach { (name, users) ->
			if (users.contains(user)) {
				return name
			}
		}
		return null
	}
	
	companion object {
		val currentDateTime: LocalDateTime = Clock.System.now()
				.toLocalDateTime(TimeZone.currentSystemDefault())
		
		val currentSemester: String
			get() = dateToSemester(currentDateTime.date)
		
		fun dateToSemester(date: LocalDate) = when (date.monthNumber) {
			in 1..4 -> "Spring ${date.year}"
			in 5..7 -> "Summer ${date.year}"
			in 8..12 -> "Fall ${date.year}"
			else -> throw IllegalStateException()
		}
	}
}
