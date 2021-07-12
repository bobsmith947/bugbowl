package edu.mines.csci341.hackathon.jvm

import java.net.URLEncoder.encode
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.annotation.MultipartConfig
import javax.servlet.annotation.WebServlet
import javax.servlet.http.*
import kotlinx.html.stream.appendHTML
import kotlinx.html.*
import edu.mines.csci341.hackathon.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

@MultipartConfig
@WebServlet("/admin")
class AdminServlet : HttpServlet() {
	
	@Throws(ServletException::class, IOException::class)
	override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
		val compId: Int? = req.getParameter("id")?.toInt()
		val groupName: String? = req.getParameter("group")
		val action: String? = req.getParameter("action")
		res.setContentType("text/html;charset=UTF-8")
		res.writer.use { out ->
			out.println("<!DOCTYPE html>")
			out.appendHTML().html {
				with(Templates) {
					makeHead("Admin")
					body {
						makeNav("admin", true)
						if (compId == null) {
							a("?id=0", classes = "btn btn-primary mt-3") { +"Add a competition" }
							makeCompTable(edit = true)
						} else if (groupName == null) {
							makeCompEdit(compId)
						} else {
							val comp = Database.comps[compId]!!
							val sub = comp.correctSubmissions[groupName]!!
							h1 { +"$groupName Submission" }
							if (action == "clear") {
								sub.reportedBy = null
							} else if (sub.reportedBy != null) {
								p("text-danger") {
									+"This submission was reported by ${sub.reportedBy}."
									a("?id=${comp.id}&group=${encode(groupName, "UTF-8")}&action=clear") {
										+"Clear this report."
									}
								}
							}
							pre { +sub.contents }
							h2("d-inline-block") { +"$groupName Members" }
							button(type = ButtonType.button, classes = "btn btn-danger mb-2") {
								id = "removegroup"
								+"Remove group from competition"
							}
							ul("list-group") {
								comp.groups[groupName]!!.forEach {
									listGroupItem { +it.name }
								}
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
		val parts: Map<String, JsonElement> = req.parts.associate { part: Part ->
			// we have to normalize line endings because RARS freaks out with CRLF
			val content = part.content.trimIndent()
			part.name to try {
				Json.parseToJsonElement(content)
			} catch (e: SerializationException) {
				JsonPrimitive(content)
			}
		}
		val json = JsonObject(parts + ("id" to JsonPrimitive(compId)))
		val comp = Json { isLenient = true }.decodeFromJsonElement<Competition>(json)
		if (comp.solutionContents?.isNotBlank() == true) {
			val solution = Submission(0, comp.solutionContents)
			SubmissionRunner.runSubmission(solution, comp.inputs)
			comp.expectedResults = solution.results
		}
		res.setContentType("application/json;charset=UTF-8")
		res.writer.use { out ->
			with(Database) {
				val newComp = if (compId == 0) addCompetition(comp) else updateCompetition(comp)
				comps[newComp.id] = newComp
				out.println(defaultJson.encodeToString(newComp))
			}
		}
	}
	
	@Throws(ServletException::class, IOException::class)
	override fun doDelete(req: HttpServletRequest, res: HttpServletResponse) {
		val compId: Int = req.getParameter("id")!!.toInt()
		val groupName: String? = req.getParameter("group")
		with(Database) {
			if (groupName == null) {
				removeCompetition(compId)
			} else {
				updateCompetition(
					comps[compId]!!.apply {
						groups.remove(groupName)
						submissions.remove(groupName)
					}
				)
			}
		}
		res.setStatus(HttpServletResponse.SC_NO_CONTENT)
	}

	companion object {
		private val serialVersionUID = 1L
		
		val Part.content: String
			get() = inputStream.bufferedReader().use { it.readText() }
	}
}
