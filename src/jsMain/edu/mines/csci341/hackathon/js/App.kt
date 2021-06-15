package edu.mines.csci341.hackathon.js

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.clear
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.url.URLSearchParams
import org.w3c.xhr.FormData
import org.w3c.xhr.XMLHttpRequest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.html.*
import kotlinx.html.dom.*

typealias Result = List<Pair<String, String>>

fun main() {
	document.getElementById("creategroup")?.addEventListener("click", {
		val group: String? = window.prompt("Enter a group name (leave blank for default):")
		if (group != null) {
			val xhr = XMLHttpRequest()
			xhr.onreadystatechange = fun (ev: Event) {
				if (xhr.readyState == XMLHttpRequest.DONE) {
					when (xhr.status.toInt()) {
						200 -> window.location.reload()
						403 -> window.alert("Group name is already taken.")
						else -> return
					}
				}
			}
			xhr.open("POST", window.location.pathname)
			val params = URLSearchParams(window.location.search)
			params.append("action", "creategroup")
			params.append("group", group)
			xhr.send(params)
		}
	})
	
	document.getElementById("leavegroup")?.addEventListener("click", {
		val xhr = XMLHttpRequest()
		xhr.onreadystatechange = fun (ev: Event) {
			if (xhr.readyState == XMLHttpRequest.DONE && xhr.status.toInt() == 200) {
				window.location.reload()
			}
		}
		xhr.open("POST", window.location.pathname)
		val params = URLSearchParams(window.location.search)
		params.append("action", "leavegroup")
		xhr.send(params)
	})
	
	document.getElementById("joingroup")?.addEventListener("click", {
		val xhr = XMLHttpRequest()
		xhr.onreadystatechange = fun (ev: Event) {
			if (xhr.readyState == XMLHttpRequest.DONE && xhr.status.toInt() == 200) {
				val groupNames = Json.decodeFromString<List<String>>(xhr.responseText)
				val optGroup = document.querySelector("#joingroup optgroup") as HTMLOptGroupElement
				optGroup.clear()
				groupNames.forEach { name ->
					optGroup.append.option { +name }
				}
			}
		}
		xhr.open("POST", window.location.pathname)
		val params = URLSearchParams(window.location.search)
		params.append("action", "joingroup")
		xhr.send(params)
	})
	
	document.getElementById("joingroup")?.addEventListener("input", { ev: Event ->
		val xhr = XMLHttpRequest()
		xhr.onreadystatechange = fun (ev2: Event) {
			if (xhr.readyState == XMLHttpRequest.DONE && xhr.status.toInt() == 200) {
				window.location.reload()
			}
		}
		xhr.open("POST", window.location.pathname)
		val params = URLSearchParams(window.location.search)
		params.append("action", "joingroup")
		val selectedGroup: String = (ev.target as HTMLSelectElement).value
		if (selectedGroup.isNotBlank()) {
			params.append("group", selectedGroup)
			xhr.send(params)
		}
	})
	
	document.getElementById("checksub")?.addEventListener("click", {
		val xhr = XMLHttpRequest()
		xhr.onreadystatechange = fun (ev: Event) {
			if (xhr.readyState == XMLHttpRequest.DONE && xhr.status.toInt() == 200) {
				val (results, expected) = Json.decodeFromString<Pair<Result, Result>>(xhr.responseText)
				val input = document.getElementById("input") as HTMLTableRowElement
				input.clear()
				input.append.th { +"Input" }
				val output = document.getElementById("output") as HTMLTableRowElement
				output.clear()
				output.append.th { +"Output" }
				
				for (i in results.indices) {
					input.append.td { +results[i].first }
					output.append.td {
						if (results[i].second == expected[i].second) {
							classes += "table-success"
						} else {
							// TODO show tooltip with expected result
							classes += "table-danger"
						}
						+results[i].second
					}
				}
			}
		}
		xhr.open("POST", window.location.search)
		val contents = document.getElementById("contents") as HTMLTextAreaElement
		xhr.send(contents.value)
	})
	
	document.getElementById("addtest")?.addEventListener("click", {
		(document.getElementById("input") as HTMLTableRowElement).append.td {
			textInput()
		}
		(document.getElementById("output") as HTMLTableRowElement).append.td {
			textInput()
		}
	})
	
	document.getElementById("deletecomp")?.addEventListener("click", {
		val xhr = XMLHttpRequest()
		xhr.onreadystatechange = fun (ev: Event) {
			if (xhr.readyState == XMLHttpRequest.DONE && xhr.status.toInt() == 204) {
				window.location.replace(window.location.pathname)
			}
		}
		xhr.open("DELETE", window.location.search)
		xhr.send()
	})
	
	document.getElementById("editcomp")?.addEventListener("submit", { ev: Event ->
		ev.preventDefault()
		val xhr = XMLHttpRequest()
		xhr.onreadystatechange = fun (ev2: Event) {
			if (xhr.readyState == XMLHttpRequest.DONE && xhr.status.toInt() == 200) {
				window.location.replace(window.location.pathname)
			}
		}
		xhr.open("POST", window.location.search)
		val formData = FormData(ev.target as HTMLFormElement)
		val inputs = document.querySelectorAll("#input input")
		val outputs = document.querySelectorAll("#output input")
		val results: Result = List(inputs.length) { i ->
			val input = inputs[i] as HTMLInputElement
			val output = outputs[i] as HTMLInputElement
			if (input.value.isNotBlank() && output.value.isNotBlank()) {
				input.value to output.value
			} else null
		}.filterNotNull()
		formData.append("expectedResults", Json.encodeToString(results))
		xhr.send(formData)
	})
}