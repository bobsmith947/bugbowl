package edu.mines.csci341.hackathon.jvm

import kotlinx.html.*
import edu.mines.csci341.hackathon.*

object Templates {
	const val APP_NAME = "OrgaBOWL"
	
	fun HTML.makeHead(title: String) = head {
		meta(charset = "UTF-8")
		meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
		title { +"$APP_NAME: $title"}
	}
	
	fun BODY.makeNav() = nav(classes = "navbar fixed-top navbar-dark bg-primary navbar-expand-md") {
		div(classes = "container") {
			a("./", classes = "navbar-brand") { +APP_NAME }
		}
	}
	
	fun BODY.makeCompTable(
		comps: List<Competition> = Competition.comps,
		edit: Boolean = false
	) = table {
		thead {
			tr {
				th { +"Title" }
				th { +"Description" }
				th { +"Semester" }
				if (edit) {
					th { +"Manage" }
				} else {
					th { +"Participate" }
				}
			}
		}
		tbody {
			comps.forEach { comp ->
				tr {
					td { +comp.title }
					td { +comp.description }
					td { +comp.semester }
					td {
						if (edit) {
							a("?id=${comp.id}") { +"Click to edit" }
						} else {
							if (comp.isActive) {
								a("?id=${comp.id}") { +"Click to join" }
							} else {
								+"Closed"
							}
						}
					}
				}
			}
		}
	}
	
	fun BODY.makeCompEdit(compId: Int): Unit = makeCompEdit(Competition.comps[compId - 1])
	fun BODY.makeCompEdit(comp: Competition): Unit = div {
		h1 { +"Edit Competition ${comp.id}" }
		form(method = FormMethod.post) {
			hiddenInput(name = "id") { value = comp.id.toString() }
			label() {
				htmlFor = "title"
				+"Title"
			}
			textInput(name = "title") {
				id = "title"
				value = comp.title
			}
			label() {
				htmlFor = "description"
				+"Description"
			}
			textArea("4", "50") {
				id = "description"
				name = "description"
				+comp.description
			}
			label() {
				htmlFor = "contents"
				+"Base code contents"
			}
			textArea("8", "80") {
				id = "contents"
				name = "contents"
				+comp.contents
			}
			label() {
				htmlFor = "active"
				+"Active"
			}
			checkBoxInput(name = "isActive") {
				id = "active"
				value = "true"
				checked = comp.isActive
			}
			button(type = ButtonType.submit) { +"Submit" }
		}
	}
	
	fun BODY.makeCompSubmit(compId: Int, userId: Int): Unit = makeCompSubmit(
		Competition.comps[compId - 1],
		User.users[userId - 1].submissions.filter { it.competition.id == compId }.lastOrNull()
	)
	fun BODY.makeCompSubmit(comp: Competition, sub: Submission?): Unit = div {
		h1 { +comp.title }
		p { +comp.description }
		textArea("8", "80") {
			id = "contents"
			+(sub?.contents ?: comp.contents)
		}
		button(type = ButtonType.button) { +"Check Submission" }
	}
}