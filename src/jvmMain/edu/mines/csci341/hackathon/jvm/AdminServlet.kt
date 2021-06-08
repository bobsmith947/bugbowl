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
		res.setContentType("text/plain;charset=UTF-8")
		res.getWriter().use { out ->
			req.getParameterMap().forEach { (k, v) ->
				out.print("$k: ")
				out.println(v[0])
			}
		}
	}

	companion object {
		private val serialVersionUID = 1L
	}
}