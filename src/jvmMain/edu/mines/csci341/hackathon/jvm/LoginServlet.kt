package edu.mines.csci341.hackathon.jvm

import javax.servlet.http.*
import javax.servlet.annotation.WebServlet

@WebServlet("/login")
class LoginServlet : HttpServlet() {
	
	override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
		// invalidate previous session, if it exists
		req.getSession(false)?.invalidate()
		val session: HttpSession = req.getSession()
		// we are given the user's email but we only care about the part before the @
		val user = req.remoteUser?.let { Database.getUser(it.substringBefore('@')) }
		if (user == null) {
			res.sendError(HttpServletResponse.SC_FORBIDDEN)
		} else {
			session.setAttribute("user", user)
			// we need to manually add the session cookie for some reason
			// this might have something to do with the request coming from the AJP connector
			res.addCookie(Cookie("JSESSIONID", session.id))
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