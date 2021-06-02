package edu.mines.csci341.hackathon.jvm

import kotlinx.datetime.periodUntil
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.html.*
import edu.mines.csci341.hackathon.*

object Templates {
	const val APP_NAME = "OrgaBOWL"
	
	fun FlowOrInteractiveOrPhrasingContent
			.formLabel(block: LABEL.() -> Unit) = label("form-label", block)
	fun FlowOrInteractiveOrPhrasingContent
			.formCheckLabel(block: LABEL.() -> Unit) = label("form-check-label", block)
	fun OL.listGroupItem(block: LI.() -> Unit) = li("list-group-item", block)
	
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
						a("?id=${comp.id}") {
							if (edit) {
								+"Click to edit"
							} else {
								if (comp.isActive) {
									+"Click to join" 
								} else {
									+"Click to view"
								}
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
	
	fun BODY.makeCompSubmit(compId: Int, groupNum: Int): Unit = makeCompSubmit(
		Competition.comps[compId - 1],
		Competition.comps[compId - 1].submissions[groupNum]?.lastOrNull()
	)
	fun BODY.makeCompSubmit(comp: Competition, sub: Submission?): Unit = div {
		h1 { +comp.title }
		p { +comp.description }
		if (comp.isActive) {
			textArea("8", "80", TextAreaWrap.hard) {
				id = "contents"
				+(sub?.contents ?: comp.contents)
			}
			button(type = ButtonType.button, classes = "btn btn-primary d-block") { +"Check Submission" }
		} else {
			h2 { +"Ranking" }
			ol("list-group list-group-numbered") {
				comp.submissions.mapValues { (_, subs) ->
					subs.filter { comp.checkSubmission(it) }
						.minOfOrNull { it.timestamp }
				}.filterValues { it != null }.toList()
					.sortedBy { (_, timestamp) -> timestamp }
					.forEach { (groupNum, timestamp) ->
						listGroupItem {
							+"Group $groupNum: "
							+timestamp!!.periodUntil(
								Clock.System.now(),
								TimeZone.currentSystemDefault()
							).toString()
						}
					}
			}
		}
	}
}
