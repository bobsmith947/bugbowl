package edu.mines.csci341.hackathon

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.Serializable

@Serializable
data class Submission(
	val id: Int,
	val user: Int,
	val contents: String,
	@Serializable(with = InstantIso8601Serializer::class)
	val timestamp: Instant = Clock.System.now(),
) {
	val results: MutableList<TestResult> = mutableListOf()
}