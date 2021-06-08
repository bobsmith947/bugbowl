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
	var title: String,
	var description: String,
	var contents: String,
	var isActive: Boolean = false,
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

	companion object {
		val comps = listOf(
			Competition(1, "Test 1", "Test Description 1", "Test Contents 1", false, "2020-08-29".toLocalDate()),
			Competition(2, "Test 2", "Test Description 2", "Test Contents 2", false, "2021-01-29".toLocalDate()),
			Competition(3, "Test 3", "Test Description 3", "Test Contents 3", true, "2021-05-29".toLocalDate()),
		)
		init {
			comps[0].apply {
				expectedResults = listOf("1" to "2")
				groups = mutableMapOf("1" to User.users.toMutableList())
				submissions = mutableMapOf("1" to Submission.subs.toMutableList())
			}
			comps[1].apply {
				expectedResults = listOf("2" to "3")
				groups = mutableMapOf("1" to User.users.toMutableList())
				submissions = mutableMapOf("1" to Submission.subs.toMutableList())
			}
			comps[2].apply {
				expectedResults = listOf("3" to "4")
				groups = mutableMapOf("1" to User.users.toMutableList())
				submissions = mutableMapOf("1" to Submission.subs.toMutableList())
			}
		}
	}
}
