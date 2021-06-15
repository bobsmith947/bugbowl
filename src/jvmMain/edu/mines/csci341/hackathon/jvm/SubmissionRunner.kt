package edu.mines.csci341.hackathon.jvm

import java.util.concurrent.Executors.newCachedThreadPool
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import edu.mines.csci341.hackathon.Submission
import rars.api.Program

object SubmissionRunner {
	private const val TIMEOUT_SECONDS = 30L
	private val pool: ExecutorService = newCachedThreadPool()
	
	fun runSubmission(sub: Submission, inputs: List<String>) {
		pool.submit {
			val outputs = MutableList<String>(inputs.size) { "null" }
			val prog = Program()
			// TODO handle warnings/errors
			prog.assembleString(sub.contents)
			inputs.forEachIndexed { index, input ->
				prog.setup(null, input)
				prog.simulate()
				outputs[index] = prog.getSTDOUT().trim('\u0000')
			}
			sub.results = inputs zip outputs
		}.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
	}
}