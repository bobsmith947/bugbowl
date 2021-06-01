package edu.mines.csci341.hackathon.jvm

import javax.naming.InitialContext
import javax.sql.DataSource

object Database {
	val db: DataSource
	
	init {
		val ctx = InitialContext()
		db = ctx.lookup("java:/comp/env/jdbc/hackathonDB") as DataSource
	}
}