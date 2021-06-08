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
	
	val users: MutableList<User> = mutableListOf()
	val comps: MutableList<Competition> = mutableListOf()
	
	init {
		val ctx = InitialContext()
		db = ctx.lookup("java:/comp/env/jdbc/hackathonDB") as DataSource
		
		var stmt: Statement? = null
		try {
			_conn = db.getConnection()
			stmt = conn.createStatement()
			
			var rs: ResultSet = stmt.executeQuery("SELECT data FROM hackathon_users ORDER BY id")
			while (rs.next()) {
				users.add(Json.decodeFromString<User>(rs.getString(1)))
			}
			
			rs = stmt.executeQuery("SELECT data FROM hackathon_competitions ORDER BY id")
			while (rs.next()) {
				comps.add(Json.decodeFromString<Competition>(rs.getString(1)))
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
			val rs: ResultSet = ps.executeQuery()
			if (rs.next()) {
				Json.decodeFromString<User>(rs.getString(1))
			} else {
				null
			}
		} catch (e: SQLException) {
			System.err.println(e.message)
			null
		} finally {
			ps?.close()
		}
		return user
	}
}