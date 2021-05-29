package edu.mines.csci341.hackathon

import kotlinx.serialization.Serializable

@Serializable
data class User(
	val id: Int,
	val name: String,
	val isAdmin: Boolean = false,
) {
	var submissions: List<Submission> = listOf()

	companion object {
		val users = listOf(
			User(1, "Bob Smith"),
			User(2, "John Smith"),
			User(3, "Susan Smith", true),
		)
		init {
			users[0].submissions = Submission.subs
			users[1].submissions = Submission.subs
		}
	}
}
