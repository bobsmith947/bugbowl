package edu.mines.csci341.hackathon.jvm

import kotlinx.html.*
import edu.mines.csci341.hackathon.*
import java.net.URLEncoder.encode

object Templates {
	const val APP_NAME = "BugBOWL"
	
	fun FlowOrInteractiveOrPhrasingContent
			.formLabel(block: LABEL.() -> Unit) = label("form-label", block)
	fun FlowOrInteractiveOrPhrasingContent
			.formCheckLabel(block: LABEL.() -> Unit) = label("form-check-label", block)
	fun DIV.listGroupAction(href: String, block: A.() -> Unit) =
		a(href, "_self", "list-group-item list-group-item-action", block)
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
		currentPage: String,
		currentUser: User
	) = nav("navbar navbar-expand navbar-dark bg-primary fixed-top") {
		div("container") {
			a("./", classes = "navbar-brand") { +APP_NAME }
			div("navbar-collapse") {
				ul("navbar-nav") {
					li("nav-item") {
						a("./competition", classes = "nav-link") {
							if (currentPage == "competition") {
								classes += "active"
							}
							+"Competitions"
						}
					}
					if (currentUser.isAdmin) {
						li("nav-item") {
							a("./admin", classes = "nav-link") {
								if (currentPage == "admin") {
									classes += "active"
								}
								+"Admin"
							}
						}
					}
				}
				span("navbar-text") { +currentUser.name }
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
	
	fun BODY.makeCompEdit(comp: Competition?) = div {
		if (comp != null) {
			h1("d-inline-block") { +"Edit Competition ${comp.id}" }
			button(type = ButtonType.button, classes = "btn btn-danger mb-2") {
				id = "removecomp"
				+"Remove Competition"
			}
			makeRanking(comp)
			p {
				+"All participants: ${comp.participants.toSortedSet()}"
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
				textArea("5", "100", TextAreaWrap.soft, "form-control") {
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
				textArea("10", "100", TextAreaWrap.soft, "form-control") {
					id = "contents"
					name = "contents"
					spellCheck = false
					+(comp?.contents ?: "")
				}
			}
			div {
				formLabel {
					htmlFor = "solution"
					+"Solution code contents"
				}
				textArea("10", "100", TextAreaWrap.soft, "form-control") {
					id = "solution"
					name = "solutionContents"
					spellCheck = false
					+(comp?.solutionContents ?: "")
				}
				p("form-text") { +"If a solution is specified, outputs will be automatically generated." }
			}
			div("form-check") {
				checkBoxInput(name = "isActive", classes = "form-check-input") {
					id = "active"
					value = "true"
					checked = comp?.isActive ?: false
				}
				formCheckLabel {
					htmlFor = "active"
					+"Active (override)"
				}
			}
			div("input-group") {
				span("input-group-text") { +"Activate at" }
				dateTimeLocalInput(classes = "form-control") {
					id = "start"
					value = comp?.activated?.first?.toString() ?: ""
				}
				span("input-group-text") { +"until" }
				dateTimeLocalInput(classes = "form-control") {
					id = "end"
					value = comp?.activated?.second?.toString() ?: ""
				}
			}
			table("table table-sm table-borderless caption-top") {
				caption { +"Expected Results" }
				tr {
					id = "input"
					th { +"Input" }
					comp?.expectedResults?.forEach { (input, _) ->
						td {
							textArea(content = input)
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
	
	fun BODY.makeCompSubmit(comp: Competition, group: String?) = div {
		h1 { +comp.title }
		p { +comp.description }
		if (comp.isActive) {
			textArea("20", "100", TextAreaWrap.soft, "form-control") {
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
						id = "status"
						+"Submission Results"
					}
					tr { id = "input" }
					tr { id = "output" }
				}
				h2("d-inline-block") { +"$group Members" }
				button(type = ButtonType.button, classes = "btn btn-danger mb-2") {
					id = "leavegroup"
					+"Leave this group"
				}
				ul("list-group") {
					comp.groups[group]!!.forEach { user ->
						listGroupItem { +user.name }
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
						value = ""
						+"Join a group (click to update list)"
					}
					optGroup("Available Groups") {
						comp.groups.keys.forEach { option { +it } }
					}
				}
			}
		} else makeRanking(comp)
	}
	
	fun DIV.makeRanking(comp: Competition) = div {
		h2 { +"Ranking" }
		div("list-group") {
			comp.correctSubmissions.toList()
				.filter { it.second != null }
				.sortedBy { it.second!!.timestamp }
				.forEachIndexed { index, (group, sub) ->
					listGroupAction("?id=${comp.id}&group=${encode(group, "UTF-8")}") {
						if (sub!!.reportedBy != null) {
							classes += "list-group-item-danger"
						}
						+"#${index + 1} $group: ${sub.timestamp}"
					}
				}
		}
	}
}
