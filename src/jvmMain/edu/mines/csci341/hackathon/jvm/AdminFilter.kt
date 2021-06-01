package edu.mines.csci341.hackathon.jvm

import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.FilterChain
import javax.servlet.annotation.WebFilter
import javax.servlet.http.*

@WebFilter("/admin")
class AdminFilter : HttpFilter() {
	
	@Throws(ServletException::class, IOException::class)
	override fun doFilter(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
		val session: HttpSession? = req.getSession(false)
		if (session != null) {
			val userId = session.getAttribute("userId") as Int?
			val userName = session.getAttribute("userName") as String?
			val isAdmin = session.getAttribute("admin") as Boolean?
			if (userId != null && userName != null && isAdmin == true) {
				// user is logged in as admin
				chain.doFilter(req, res)
			} else {
				// user is missing credentials
				res.sendError(HttpServletResponse.SC_FORBIDDEN)
			}
		} else {
			// user is not logged in
			res.sendRedirect("index.html")
		}
	}
}