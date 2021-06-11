package edu.mines.csci341.hackathon.jvm

import edu.mines.csci341.hackathon.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.naming.InitialContext
import javax.sql.DataSource
import java.sql.*

object Database {
	private val db: DataSource
	private var _conn: Connection? = null
	private val conn get() = _conn!!
	
	val users: MutableMap<Int, User> = mutableMapOf()
	val comps: MutableMap<Int, Competition> = mutableMapOf()
	
	init {
		val ctx = InitialContext()
		db = ctx.lookup("java:/comp/env/jdbc/hackathonDB") as DataSource
		
		var stmt: Statement? = null
		try {
			_conn = db.getConnection()
			stmt = conn.createStatement()
			
			var rs: ResultSet = stmt.executeQuery("SELECT data FROM hackathon_users")
			while (rs.next()) {
				val user = Json.decodeFromString<User>(rs.getString(1))
				users[user.id] = user
			}
			
			rs = stmt.executeQuery("SELECT data FROM hackathon_competitions")
			while (rs.next()) {
				val comp = Json.decodeFromString<Competition>(rs.getString(1))
				comps[comp.id] = comp
			}
		} catch (e: SQLException) {
			System.err.println(e.message)
		} finally {
			stmt?.close()
		}
	}
	
	// database index may allow for faster lookups
	// rather than searching through user ID map in-place
	// or allocating another map with user names as keys
	fun getUser(userName: String): User? {
		var ps: PreparedStatement? = null
		val user: User? = try {
			ps = conn.prepareStatement("SELECT data FROM hackathon_users WHERE data ->> 'name' = ?")
			ps.setString(1, userName)
			val rs: ResultSet = ps.executeQuery()
			if (rs.next()) {
				Json.decodeFromString<User>(rs.getString(1))
			} else {
				throw SQLException("Username not found in database.")
			}
		} catch (e: SQLException) {
			System.err.println(e.message)
			null
		} finally {
			ps?.close()
		}
		return user
	}
	
	fun addCompetition(comp: Competition): Competition {
		var ps: PreparedStatement? = null
		val newComp: Competition? = try {
			ps = conn.prepareStatement(
				"INSERT INTO hackathon_competitions (data) VALUES (?::jsonb)",
				Statement.RETURN_GENERATED_KEYS
			)
			ps.setString(1, Json.encodeToString(comp))
			ps.executeUpdate()
			val rs: ResultSet = ps.generatedKeys
			if (rs.next()) {
				// get the generated ID
				val id = rs.getInt(1)
				ps.close()
				// update the JSON to reflect the inserted ID
				ps = conn.prepareStatement("""
											UPDATE hackathon_competitions
											SET data = jsonb_set(data, '{id}', id::text::jsonb)
											WHERE id = ?""".trimIndent())
				ps.setInt(1, id)
				ps.executeUpdate()
				// make a copy of the competition with the correct ID
				comp.copy(id)
			} else {
				throw SQLException("Could not get the generated ID for this competition.")
			}
		} catch (e: SQLException) {
			System.err.println(e.message)
			null
		} finally {
			ps?.close()
		}
		if (newComp != null) {
			comps[newComp.id] = newComp
			return newComp
		}
		return comp
	}
	
	fun updateCompetition(comp: Competition) {
		var ps: PreparedStatement? = null
		try {
			ps = conn.prepareStatement("UPDATE hackathon_competitions SET data = ?::jsonb WHERE id = ?")
			ps.setString(1, Json.encodeToString(comp))
			ps.setInt(2, comp.id)
			ps.executeUpdate()
			comps[comp.id] = comp
		} catch (e: SQLException) {
			System.err.println(e.message)
		} finally {
			ps?.close()
		}
	}
	
	fun removeCompetition(comp: Competition) {
		var ps: PreparedStatement? = null
		try {
			ps = conn.prepareStatement("DELETE FROM hackathon_competitions WHERE id = ?")
			ps.setInt(1, comp.id)
			ps.executeUpdate()
			comps.remove(comp.id)
		} catch (e: SQLException) {
			System.err.println(e.message)
		} finally {
			ps?.close()
		}
	}
}