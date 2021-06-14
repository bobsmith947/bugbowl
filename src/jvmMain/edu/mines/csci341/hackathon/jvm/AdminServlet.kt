package edu.mines.csci341.hackathon.jvm

import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.annotation.MultipartConfig
import javax.servlet.annotation.WebServlet
import javax.servlet.http.*
import kotlinx.html.stream.appendHTML
import kotlinx.html.*
import edu.mines.csci341.hackathon.jvm.Templates.makeHead
import edu.mines.csci341.hackathon.jvm.Templates.makeNav
import edu.mines.csci341.hackathon.jvm.Templates.makeCompTable
import edu.mines.csci341.hackathon.jvm.Templates.makeCompEdit
import edu.mines.csci341.hackathon.Competition
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@MultipartConfig
@WebServlet("/admin")
class AdminServlet : HttpServlet() {
	
	@Throws(ServletException::class, IOException::class)
	override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
		val compId: String? = req.getParameter("id")
		res.setContentType("text/html;charset=UTF-8")
		res.writer.use { out ->
			out.println("<!DOCTYPE html>")
			out.appendHTML().html {
				makeHead("Admin")
				body {
					makeNav()
					if (compId == null) {
						a("?id=0", classes = "btn btn-primary mt-3") { +"Add a Competition" }
						makeCompTable(edit = true)
					} else {
						makeCompEdit(compId.toInt())
					}
				}
			}
		}
	}
	
	@Throws(ServletException::class, IOException::class)
	override fun doPost(req: HttpServletRequest, res: HttpServletResponse) {
		val compId: String = req.getParameter("id")!!
		val parts: Map<String, String> = req.parts.associate { part: Part ->
			part.name to part.content
		}
		val json = Json.encodeToString(parts + ("id" to compId))
		val comp = Json { isLenient = true }.decodeFromString<Competition>(json)
		res.setContentType("application/json;charset=UTF-8")
		res.writer.use { out ->
			if (compId == "0") {
				val newComp = Database.addCompetition(comp)
				out.println(Json.encodeToString(newComp))
			} else {
				Database.updateCompetition(comp)
				out.println(Json.encodeToString(comp))
			}
		}
	}
	
	@Throws(ServletException::class, IOException::class)
	override fun doDelete(req: HttpServletRequest, res: HttpServletResponse) {
		val compId: String? = req.getParameter("id")
		if (compId != null) {
			Database.removeCompetition(compId.toInt())
		}
		res.setStatus(HttpServletResponse.SC_NO_CONTENT)
	}

	companion object {
		private val serialVersionUID = 1L
		
		val Part.content: String
			get() = inputStream.bufferedReader().use { it.readText() }
	}
}