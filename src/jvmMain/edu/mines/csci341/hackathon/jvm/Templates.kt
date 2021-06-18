package edu.mines.csci341.hackathon.jvm

import kotlinx.datetime.periodUntil
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.html.*
import edu.mines.csci341.hackathon.*

object Templates {
	const val APP_NAME = "BugBOWL"
	
	fun FlowOrInteractiveOrPhrasingContent
			.formLabel(block: LABEL.() -> Unit) = label("form-label", block)
	fun FlowOrInteractiveOrPhrasingContent
			.formCheckLabel(block: LABEL.() -> Unit) = label("form-check-label", block)
	fun OL.listGroupItem(block: LI.() -> Unit) = li("list-group-item", block)
	fun UL.listGroupItem(block: LI.() -> Unit) = li("list-group-item", block)
	
	fun HTML.makeHead(title: String) = head {
		meta(charset = "UTF-8")
		meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
		title { +"$APP_NAME: $title"}
		link(
			"index.css",
			LinkRel.stylesheet,
			LinkType.textCss
		)
		script(ScriptType.textJavaScript, "index.js") { defer = true }
	}
	
	fun BODY.makeNav(
		current: String,
		admin: Boolean
	) = nav("navbar navbar-expand navbar-dark bg-primary fixed-top") {
		div("container") {
			a("./", classes = "navbar-brand") { +APP_NAME }
			div("navbar-collapse") {
				ul("navbar-nav") {
					li("nav-item") {
						a("./competition", classes = "nav-link") {
							if (current == "competition") {
								classes += "active"
							}
							+"Competitions"
						}
					}
					if (admin) {
						li("nav-item") {
							a("./admin", classes = "nav-link") {
								if (current == "admin") {
									classes += "active"
								}
								+"Admin"
							}
						}
					}
				}
			}
		}
	}
	
	fun BODY.makeCompTable(
		comps: List<Competition> = Database.comps.values.toList().sortedByDescending { it.created },
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
	
	fun BODY.makeCompEdit(compId: Int): Unit = makeCompEdit(Database.comps[compId])
	fun BODY.makeCompEdit(comp: Competition?): Unit = div {
		if (comp != null) {
			h1("d-inline-block") { +"Edit Competition ${comp.id}" }
			button(type = ButtonType.button, classes = "btn btn-danger mb-2") {
				id = "deletecomp"
				+"Delete"
			}
		} else {
			h1 { +"New Competition" }
		}
		form(method = FormMethod.post) {
			id = "editcomp"
			onSubmit = "return false"
			div {
				formLabel {
					htmlFor = "title"
					+"Title"
				}
				textInput(name = "title", classes = "form-control") {
					id = "title"
					value = comp?.title ?: ""
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
					+(comp?.description ?: "")
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
					spellCheck = false
					+(comp?.contents ?: "")
				}
			}
			div("form-check") {
				checkBoxInput(name = "isActive", classes = "form-check-input") {
					id = "active"
					value = "true"
					checked = comp?.isActive ?: false
				}
				formCheckLabel {
					htmlFor = "active"
					+"Active"
				}
			}
			table("table table-sm table-borderless caption-top") {
				caption { +"Expected Results" }
				tr {
					id = "input"
					th { +"Input" }
					comp?.expectedResults?.forEach { (input, _) ->
						td {
							textInput { value = input }
						}
					}
				}
				tr {
					id = "output"
					th { +"Output" }
					comp?.expectedResults?.forEach { (_, output) ->
						td {
							textInput { value = output }
						}
					}
				}
			}
			button(type = ButtonType.button, classes = "btn btn-secondary me-3") {
				id = "addtest"
				+"Add expected result"
			}
			button(type = ButtonType.submit, classes = "btn btn-primary") { +"Submit" }
		}
	}
	
	fun BODY.makeCompSubmit(compId: Int, user: User): Unit {
		val comp = Database.comps[compId]!!
		val group = comp.getGroupName(user)
		return makeCompSubmit(comp, group)
	}
	fun BODY.makeCompSubmit(comp: Competition, group: String?): Unit = div {
		h1 { +comp.title }
		p { +comp.description }
		if (comp.isActive) {
			textArea("8", "80", TextAreaWrap.hard, "form-control") {
				id = "contents"
				spellCheck = false
				+(comp.submissions[group]?.lastOrNull()?.contents ?: comp.contents)
			}
			if (group != null) {
				button(type = ButtonType.button, classes = "btn btn-primary d-block my-3") {
					id = "checksub"
					+"Check Submission"
				}
				table("table table-sm table-borderless caption-top") {
					caption {
						id = "message"
						+"Submission Results"
					}
					tr { id = "input" }
					tr { id = "output" }
				}
				h2("d-inline-block") { +"$group Members" }
				button(type = ButtonType.button, classes = "btn btn-danger mb-2") {
					id = "leavegroup"
					+"Leave $group"
				}
				ul("list-group") {
					comp.groups[group]!!.forEach {
						listGroupItem { +it.name }
					}
				}
			} else {
				h3("text-warning") { +"You need to be in a group in order to submit!" }
				button(type = ButtonType.button, classes = "btn btn-primary") {
					id = "creategroup"
					+"Create a group"
				}
				select("form-select mt-3") {
					id = "joingroup"
					size = "10"
					option {
						selected = true
						value = ""
						+"Join a group (click to refresh)"
					}
					optGroup("Available Groups")
				}
			}
		} else {
			h2 { +"Ranking" }
			ol("list-group list-group-numbered") {
				comp.submissions.mapValues { (_, subs) ->
					subs.filter { comp.checkSubmission(it) }
						.minOfOrNull { it.timestamp }
				}.filterValues { it != null }.toList()
					.sortedBy { (_, timestamp) -> timestamp }
					.forEach { (groupName, timestamp) ->
						listGroupItem {
							+"$groupName: "
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
