package edu.mines.csci341.hackathon.jvm

import javax.servlet.*
import javax.servlet.annotation.WebFilter
import javax.servlet.http.*

@WebFilter("/competition")
class CompetitionFilter : Filter {
	
	override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
		val session: HttpSession? = (req as HttpServletRequest).getSession(false)
		if (session != null) {
			val userId = session.getAttribute("userId") as Int?
			val userName = session.getAttribute("userName") as String?
			if (userId != null && userName != null) {
				// user is logged in
				chain.doFilter(req, res)
			} else {
				// user is missing credentials
				(res as HttpServletResponse).sendError(HttpServletResponse.SC_FORBIDDEN)
			}
		} else {
			// user is not logged in
			(res as HttpServletResponse).sendRedirect("index.html")
		}
	}
	
	override fun init(config: FilterConfig) {}
	override fun destroy() {}
}