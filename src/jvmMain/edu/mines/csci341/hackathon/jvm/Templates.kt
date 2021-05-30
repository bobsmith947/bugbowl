package edu.mines.csci341.hackathon.jvm

import kotlinx.html.*
import edu.mines.csci341.hackathon.Competition

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
	
	fun BODY.makeCompTable(comps: List<Competition> = Competition.comps) = table {
		thead {
			tr {
				th { +"Competition Title" }
				th { +"Competition Description" }
				th { +"Competition Semester" }
			}
		}
		tbody {
			comps.forEach { comp ->
				tr {
					td { +comp.title }
					td { +comp.description }
					td { +comp.semester }
				}
			}
		}
	}
}