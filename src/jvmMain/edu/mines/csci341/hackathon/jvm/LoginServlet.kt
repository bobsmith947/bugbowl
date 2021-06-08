package edu.mines.csci341.hackathon.jvm

import javax.servlet.http.*
import javax.servlet.annotation.WebServlet

@WebServlet("/login")
class LoginServlet : HttpServlet() {
	
	override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
		// invalidate previous session, if it exists
		req.getSession(false)?.invalidate()
		val session: HttpSession = req.getSession()
		// TODO use multipass login
		val userName = "admin"
		val user = Database.getUser(userName)
		if (user == null) {
			res.sendError(HttpServletResponse.SC_FORBIDDEN)
		} else {
			session.setAttribute("user", user)
			if (user.isAdmin) {
				res.sendRedirect("admin")
			} else {
				res.sendRedirect("competition")
			}
		}
	}
	
	companion object {
		private val serialVersionUID = 1L
	}
}