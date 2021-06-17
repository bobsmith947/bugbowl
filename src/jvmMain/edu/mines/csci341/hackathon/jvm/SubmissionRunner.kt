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
			// send an interrupt and stop current execution in case it has not finished
			if (task.cancel(true)) {
				Simulator.getInstance().stopExecution()
			}
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
			val reason: Simulator.Reason = program.simulate()
			if (Thread.interrupted()) {
				// when the task times out, Simulator.stopExecution() forces program.simulate() to return
				// we can then receive an interrupt to prevent further execution
				return
			}
			// TODO handle reasons
			when (reason) {
				Simulator.Reason.BREAKPOINT -> Unit
				Simulator.Reason.EXCEPTION -> Unit
				Simulator.Reason.MAX_STEPS -> Unit
				Simulator.Reason.NORMAL_TERMINATION -> Unit
				Simulator.Reason.CLIFF_TERMINATION -> Unit
				Simulator.Reason.PAUSE -> Unit
				Simulator.Reason.STOP -> Unit
			}
			outputs[index] = program.getSTDOUT().trim('\u0000')
		}
		sub.results = inputs zip outputs
	}
}