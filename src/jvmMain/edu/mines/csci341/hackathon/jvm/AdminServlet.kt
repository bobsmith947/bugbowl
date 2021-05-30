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

@WebServlet("/admin")
class AdminServlet : HttpServlet() {
	
	@Throws(ServletException::class, IOException::class)
	override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
		res.setContentType("text/html")
		res.getWriter().use { out ->
			out.println("<!DOCTYPE html>")
			out.appendHTML().html {
				makeHead("Admin")
				body {
					makeNav()
					makeCompTable()
				}
			}
		}
	}

	companion object {
		private val serialVersionUID = 1L
	}
}