package edu.mines.csci341.hackathon.jvm

import java.util.concurrent.Executors.newSingleThreadExecutor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.ExecutionException
import edu.mines.csci341.hackathon.Submission
import rars.api.Program
import rars.simulator.Simulator

object SubmissionRunner {
	private const val TIMEOUT_SECONDS = 10L
	private val executor: ExecutorService = newSingleThreadExecutor()
	private val program = Program()
	
	fun runSubmission(sub: Submission, inputs: List<String>): String {
		val outputs = MutableList<String>(inputs.size) { "null" }
		val task: Future<*> = synchronized(this) {
			executor.submit {
				// TODO handle warnings/errors
				program.assembleString(sub.contents)
				inputs.forEachIndexed { index, input ->
					program.setup(null, input)
					program.simulate()
					outputs[index] = program.getSTDOUT().trim('\u0000')
				}
			}
		}
		val message: String = try {
			task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
			"Submission ran normally."
		} catch (e: TimeoutException) {
			"Submission timed out."
		} catch (e: ExecutionException) {
			"Submission failed to run."
		} finally {
			Simulator.getInstance().stopExecution()
			task.cancel(true)
		}
		sub.results = inputs zip outputs
		return message
	}
}