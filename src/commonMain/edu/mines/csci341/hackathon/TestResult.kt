package edu.mines.csci341.hackathon

import kotlinx.serialization.Serializable

@Serializable
data class TestResult(
	val id: Int,
	val competition: Int,
	val input: String,
	val output: String,
)