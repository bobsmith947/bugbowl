package edu.mines.csci341.hackathon.jvm

import java.net.URLEncoder.encode
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.*
import kotlinx.html.stream.appendHTML
import kotlinx.html.*
import edu.mines.csci341.hackathon.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@WebServlet("/competition")
class CompetitionServlet : HttpServlet() {
	
	@Throws(ServletException::class, IOException::class)
	override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
		val compId: Int? = req.getParameter("id")?.toInt()
		val comp = compId?.let { Database.comps[compId] }
		val groupName: String? = req.getParameter("group")
		val action: String? = req.getParameter("action")
		val user = req.getSession().getAttribute("user") as User
		res.setContentType("text/html;charset=UTF-8")
		res.writer.use { out ->
			out.println("<!DOCTYPE html>")
			out.appendHTML().html {
				with(Templates) {
					makeHead("Competition")
					body {
						makeNav("competition", user)
						if (compId == null) {
							makeCompTable()
							makeLeaderboard()
						} else if (groupName == null) {
							makeCompSubmit(comp!!, comp.getGroupName(user))
						} else {
							val sub = comp!!.correctSubmissions[groupName]!!
							if (comp.isActive) {
								res.sendError(HttpServletResponse.SC_FORBIDDEN)
							} else {
								h1 { +"$groupName Submission" }
								if (action == "report") {
									sub.reportedBy = sub.reportedBy ?: user.name
								}
								if (sub.reportedBy != null) {
									p("text-danger") { +"This submission has been reported." }
								} else {
									a("?id=${comp.id}&group=${encode(groupName, "UTF-8")}&action=report",
										classes = "btn btn-danger") {
										+"Report this submission"
									}
								}
								pre { +sub.contents }
							}
						}
					}
				}
			}
		}
	}
	
	@Throws(ServletException::class, IOException::class)
	override fun doPost(req: HttpServletRequest, res: HttpServletResponse) {
		val compId: Int = req.getParameter("id")!!.toInt()
		val comp: Competition = Database.comps[compId]!!
		val action: String? = req.getParameter("action")
		val user = req.getSession().getAttribute("user") as User
		var groupName: String? = req.getParameter("group") ?: comp.getGroupName(user)
		if (!comp.isActive) {
			res.sendError(HttpServletResponse.SC_FORBIDDEN)
		} else when (action) {
			"joingroup" -> {
				res.setContentType("application/json;charset=UTF-8")
				res.writer.use { out ->
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
				if (groupName in comp.groups) {
					res.sendError(HttpServletResponse.SC_FORBIDDEN)
				} else {
					comp.groups[groupName!!] = mutableListOf(user)
					comp.submissions[groupName] = mutableListOf()
				}
			}
			"leavegroup" -> {
				comp.groups[groupName]!!.remove(user)
				if (comp.groups[groupName]!!.isEmpty()) {
					comp.groups.remove(groupName)
					comp.submissions.remove(groupName)
				}
			}
			"updategroup" -> {
				Database.updateCompetition(comp)
			}
			else -> {
				val contents = req.reader.use { it.readText() }
				val sub = Submission(0, contents)
				val msg = SubmissionRunner.runSubmission(sub, comp.inputs)
				comp.submissions[groupName]!!.add(sub)
				res.setContentType("application/json;charset=UTF-8")
				res.writer.use { out ->
					out.println(Json.encodeToString(Triple(sub.results, comp.expectedResults, msg)))
				}
			}
		}
	}

	companion object {
		private val serialVersionUID = 1L
	}
}