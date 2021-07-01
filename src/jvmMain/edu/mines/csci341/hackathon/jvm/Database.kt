package edu.mines.csci341.hackathon.jvm

import edu.mines.csci341.hackathon.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.naming.InitialContext
import javax.sql.DataSource
import java.sql.*
import java.util.concurrent.ConcurrentHashMap

object Database {
	private val db: DataSource
	private var _conn: Connection? = null
	private val conn get() = _conn!!
	private val defaultJson = Json { encodeDefaults = true }
	
	val users: MutableMap<Int, User> = ConcurrentHashMap()
	val comps: MutableMap<Int, Competition> = ConcurrentHashMap()
	
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
				comp.isActive = comp.checkActive()
				comps[comp.id] = comp
			}
		} catch (e: SQLException) {
			System.err.println(e.message)
		} finally {
			stmt?.close()
		}
	}
	
	fun getUser(userName: String): User? {
		var ps: PreparedStatement? = null
		val user: User? = try {
			ps = conn.prepareStatement("SELECT data FROM hackathon_users WHERE data ->> 'name' = ?")
			ps.setString(1, userName)
			var rs: ResultSet = ps.executeQuery()
			if (rs.next()) {
				Json.decodeFromString<User>(rs.getString(1))
			} else {
				// add the user to the database when logging in for the first time
				val newUser = User(0, userName)
				ps.close()
				ps = conn.prepareStatement(
					"INSERT INTO hackathon_users (data) VALUES (?::jsonb)",
					Statement.RETURN_GENERATED_KEYS
				)
				ps.setString(1, defaultJson.encodeToString(newUser))
				ps.executeUpdate()
				// update with the correct ID
				rs = ps.generatedKeys
				if (rs.next()) {
					val id = rs.getInt(1)
					ps.close()
					ps = conn.prepareStatement("""
												UPDATE hackathon_users
												SET data = jsonb_set(data, '{id}', id::text::jsonb)
												WHERE id = ?""".trimIndent())
					ps.setInt(1, id)
					ps.executeUpdate()
					newUser.copy(id)
				} else throw SQLException("Could not get generated ID.")
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
			ps.setString(1, defaultJson.encodeToString(comp))
			ps.executeUpdate()
			// update with the correct ID
			val rs: ResultSet = ps.generatedKeys
			if (rs.next()) {
				val id = rs.getInt(1)
				ps.close()
				ps = conn.prepareStatement("""
											UPDATE hackathon_competitions
											SET data = jsonb_set(data, '{id}', id::text::jsonb)
											WHERE id = ?""".trimIndent())
				ps.setInt(1, id)
				ps.executeUpdate()
				comp.copy(id)
			} else throw SQLException("Could not get generated ID.")
		} catch (e: SQLException) {
			System.err.println(e.message)
			null
		} finally {
			ps?.close()
		}
		return newComp ?: comp
	}
	
	fun updateCompetition(comp: Competition): Competition {
		var ps: PreparedStatement? = null
		val oldComp = comps[comp.id]!!
		val newComp = comp.copy(created = oldComp.created).apply {
			expectedResults = comp.expectedResults
			oldComp.groups.mapValuesTo(groups) { it.value.toMutableList() }
			oldComp.submissions.mapValuesTo(submissions) {
				it.value.filter(::checkSubmission).toMutableList()
			}
			isActive = comp.isActive
		}
		try {
			ps = conn.prepareStatement("UPDATE hackathon_competitions SET data = ?::jsonb WHERE id = ?")
			ps.setString(1, defaultJson.encodeToString(newComp))
			ps.setInt(2, comp.id)
			ps.executeUpdate()
		} catch (e: SQLException) {
			System.err.println(e.message)
		} finally {
			ps?.close()
		}
		return newComp
	}
	
	fun removeCompetition(compId: Int) {
		var ps: PreparedStatement? = null
		try {
			ps = conn.prepareStatement("DELETE FROM hackathon_competitions WHERE id = ?")
			ps.setInt(1, compId)
			ps.executeUpdate()
		} catch (e: SQLException) {
			System.err.println(e.message)
		} finally {
			ps?.close()
		}
		comps.remove(compId)
	}
}