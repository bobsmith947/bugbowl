package edu.mines.csci341.hackathon

import kotlinx.datetime.LocalDate
import kotlinx.datetime.serializers.LocalDateIso8601Serializer
import kotlinx.serialization.Serializable

@Serializable
data class Competition(
	val id: Int,
	val title: String,
	val description: String,
	val semester: String,
	val contents: String,
	@Serializable(with = LocalDateIso8601Serializer::class)
	val created: LocalDate,
) {
	val expectedResults: MutableList<TestResult> = mutableListOf()
}