package edu.mines.csci341.hackathon.jvm

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
		res.setContentType("text/html;charset=UTF-8")
		res.writer.use { out ->
			out.println("<!DOCTYPE html>")
			out.appendHTML().html {
				with(Templates) {
					makeHead("Admin")
					body {
						makeNav("admin", true)
						if (compId == null) {
							a("?id=0", classes = "btn btn-primary mt-3") { +"Add a Competition" }
							makeCompTable(edit = true)
						} else {
							makeCompEdit(compId)
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
				comps[compId] = newComp
				out.println(Json.encodeToString(newComp))
			}
		}
	}
	
	@Throws(ServletException::class, IOException::class)
	override fun doDelete(req: HttpServletRequest, res: HttpServletResponse) {
		val compId: Int = req.getParameter("id")!!.toInt()
		Database.removeCompetition(compId)
		res.setStatus(HttpServletResponse.SC_NO_CONTENT)
	}

	companion object {
		private val serialVersionUID = 1L
		
		val Part.content: String
			get() = inputStream.bufferedReader().use { it.readText() }
	}
}