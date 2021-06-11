package edu.mines.csci341.hackathon.jvm

import java.io.IOException
import javax.servlet.ServletException
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

@WebServlet("/admin")
class AdminServlet : HttpServlet() {
	
	@Throws(ServletException::class, IOException::class)
	override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
		val compId: String? = req.getParameter("id")
		res.setContentType("text/html;charset=UTF-8")
		res.getWriter().use { out ->
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
		res.setContentType("application/json;charset=UTF-8")
		res.getWriter().use { out ->
			val params = req.getParameterMap()
			val json = Json.encodeToString(params.mapValues { (_, v) -> v[0] })
			out.println(json)
			if (params["id"]?.get(0) == "0") {
				val comp = Json { isLenient = true }.decodeFromString<Competition>(json)
				Database.addCompetition(comp)
			}
		}
	}

	companion object {
		private val serialVersionUID = 1L
	}
}