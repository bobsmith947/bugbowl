package edu.mines.csci341.hackathon.jvm

import java.util.concurrent.*
import edu.mines.csci341.hackathon.Submission
import rars.api.*
import rars.simulator.Simulator

object SubmissionRunner : Runnable {
	private const val TIMEOUT_SECONDS = 10L
	private const val TIMEOUT_STEPS = 10_000_000
	private val executor: ExecutorService = Executors.newSingleThreadExecutor()
	private val program = Program(Options().apply {
		maxSteps = TIMEOUT_STEPS
	})
	private val submissionQueue = SynchronousQueue<Pair<Submission, List<String>>>(true)
	
	fun runSubmission(sub: Submission, inputs: List<String>): String {
		val task: Future<*> = executor.submit(this)
		submissionQueue.put(sub to inputs)
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
		return message
	}
	
	override fun run() {
		val (sub, inputs) = submissionQueue.take()
		val outputs = MutableList<String>(inputs.size) { "null" }
		// TODO handle warnings/errors
		program.assembleString(sub.contents)
		inputs.forEachIndexed { index, input ->
			program.setup(null, input)
			program.simulate()
			outputs[index] = program.getSTDOUT().trim('\u0000')
		}
		sub.results = inputs zip outputs
	}
}