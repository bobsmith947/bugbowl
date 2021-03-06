package edu.mines.csci341.hackathon

import kotlinx.serialization.Serializable

@Serializable
data class User(
	val id: Int,
	val name: String,
	val isAdmin: Boolean = false,
) : Comparable<User> {
	override fun compareTo(other: User) = name.compareTo(other.name)
	override fun toString() = name
}
