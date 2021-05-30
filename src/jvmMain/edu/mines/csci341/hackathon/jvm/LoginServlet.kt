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
		session.setAttribute("userId", 1)
		session.setAttribute("userName", "testuser")
		// comment out to test as a regular user
		session.setAttribute("admin", true)
		if (session.getAttribute("admin") != null) {
			res.sendRedirect("admin")
		} else {
			res.sendRedirect("competition")
		}
	}
	
	companion object {
		private val serialVersionUID = 1L
	}
}