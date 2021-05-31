package edu.mines.csci341.hackathon.jvm

import kotlinx.html.*
import edu.mines.csci341.hackathon.*

object Templates {
	const val APP_NAME = "OrgaBOWL"
	
	fun FlowOrInteractiveOrPhrasingContent
			.formLabel(block: LABEL.() -> Unit) = label("form-label", block)
	fun FlowOrInteractiveOrPhrasingContent
			.formCheckLabel(block: LABEL.() -> Unit) = label("form-check-label", block)
	
	fun HTML.makeHead(title: String) = head {
		meta(charset = "UTF-8")
		meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
		title { +"$APP_NAME: $title"}
		link(
			"index.css",
			LinkRel.stylesheet,
			LinkType.textCss
		)
	}
	
	fun BODY.makeNav() = nav("navbar fixed-top navbar-dark bg-primary navbar-expand-md") {
		div("container") {
			a("./", classes = "navbar-brand") { +APP_NAME }
		}
	}
	
	fun BODY.makeCompTable(
		comps: List<Competition> = Competition.comps,
		edit: Boolean = false
	) = table("table caption-top") {
		caption("fs-1 fw-bold") { +"Competitions" }
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
					if (comp.isActive) {
						classes += "table-active"
					}
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
			div {
				formLabel {
					htmlFor = "title"
					+"Title"
				}
				textInput(name = "title", classes = "form-control") {
					id = "title"
					value = comp.title
				}
			}
			div {
				formLabel {
					htmlFor = "description"
					+"Description"
				}
				textArea("4", "40", TextAreaWrap.soft, "form-control") {
					id = "description"
					name = "description"
					+comp.description
				}
			}
			div {
				formLabel {
					htmlFor = "contents"
					+"Base code contents"
				}
				textArea("8", "80", TextAreaWrap.hard, "form-control") {
					id = "contents"
					name = "contents"
					+comp.contents
				}
			}
			div("form-check") {
				checkBoxInput(name = "isActive", classes = "form-check-input") {
					id = "active"
					value = "true"
					checked = comp.isActive
				}
				formCheckLabel {
					htmlFor = "active"
					+"Active"
				}
			}
			button(type = ButtonType.submit, classes = "btn btn-primary") { +"Submit" }
		}
	}
	
	fun BODY.makeCompSubmit(compId: Int, userId: Int): Unit = makeCompSubmit(
		Competition.comps[compId - 1],
		User.users[userId - 1].submissions.filter { it.competition.id == compId }.lastOrNull()
	)
	fun BODY.makeCompSubmit(comp: Competition, sub: Submission?): Unit = div {
		h1 { +comp.title }
		p { +comp.description }
		textArea("8", "80", TextAreaWrap.hard) {
			id = "contents"
			+(sub?.contents ?: comp.contents)
		}
		button(type = ButtonType.button, classes = "btn btn-primary d-block") { +"Check Submission" }
	}
}
