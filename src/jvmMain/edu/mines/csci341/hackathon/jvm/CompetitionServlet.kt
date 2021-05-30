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
import edu.mines.csci341.hackathon.jvm.Templates.makeCompSubmit

@WebServlet("/competition")
class CompetitionServlet : HttpServlet() {
	
	@Throws(ServletException::class, IOException::class)
	override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
		val compId: String? = req.getParameter("id")
		val userId = req.getSession(false).getAttribute("userId") as Int
		res.setContentType("text/html")
		res.getWriter().use { out ->
			out.println("<!DOCTYPE html>")
			out.appendHTML().html {
				makeHead("Competition")
				body {
					makeNav()
					if (compId == null) {
						makeCompTable()
					} else {
						makeCompSubmit(compId.toInt(), userId)
					}
				}
			}
		}
	}

	companion object {
		private val serialVersionUID = 1L
	}
}