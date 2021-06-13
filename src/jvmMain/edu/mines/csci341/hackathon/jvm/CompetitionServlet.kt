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
import edu.mines.csci341.hackathon.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@WebServlet("/competition")
class CompetitionServlet : HttpServlet() {
	
	@Throws(ServletException::class, IOException::class)
	override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
		val compId: String? = req.getParameter("id")
		val user = req.getSession(false).getAttribute("user") as User
		res.setContentType("text/html;charset=UTF-8")
		res.getWriter().use { out ->
			out.println("<!DOCTYPE html>")
			out.appendHTML().html {
				makeHead("Competition")
				body {
					makeNav()
					if (compId == null) {
						makeCompTable()
					} else {
						makeCompSubmit(compId.toInt(), user)
					}
				}
			}
		}
	}
	
	@Throws(ServletException::class, IOException::class)
	override fun doPost(req: HttpServletRequest, res: HttpServletResponse) {
		val compId: String = req.getParameter("id")!!
		val action: String? = req.getParameter("action")
		var groupName: String? = req.getParameter("group")
		val user = req.getSession(false).getAttribute("user") as User
		val comp = Database.comps[compId.toInt()]!!
		when (action) {
			"joingroup" -> {
				res.setContentType("application/json;charset=UTF-8")
				res.getWriter().use { out ->
					if (groupName != null) {
						comp.groups[groupName]!!.add(user)
						out.println(Json.encodeToString(comp.groups[groupName]))
					} else {
						out.println(Json.encodeToString(comp.groups.keys))
					}
				}
			}
			"creategroup" -> {
				if (groupName?.isBlank() ?: true) {
					groupName = "Group ${comp.nextGroupNum}"
				}
				if (comp.groups.containsKey(groupName)) {
					res.sendError(HttpServletResponse.SC_FORBIDDEN)
				} else {
					comp.groups[groupName!!] = mutableListOf(user)
					comp.submissions[groupName] = mutableListOf()
				}
			}
			"leavegroup" -> {
				comp.groups[comp.getGroupName(user)]!!.remove(user)
			}
			else -> res.sendError(HttpServletResponse.SC_BAD_REQUEST)
		}
		Database.updateCompetition(comp)
	}

	companion object {
		private val serialVersionUID = 1L
	}
}