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
		// the HTTP thread will be blocked here until the submission thread is available
		submissionQueue.put(sub to inputs)
		val message: String = try {
			task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
			"Submission ran normally, results are below."
		} catch (e: TimeoutException) {
			"""
			Submission timed out after $TIMEOUT_SECONDS seconds.
			This most likely means there is an infinite loop in the code.
			""".trimIndent()
		} catch (e: ExecutionException) {
			"""
			Submission failed to run.
			This is either due to an error in assembling the code,
			or an uncaught interrupt during the simulation.
			""".trimIndent()
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
			outputs[index] = when (reason) {
				Simulator.Reason.BREAKPOINT -> "Breakpoint reached"
				Simulator.Reason.EXCEPTION -> "Exception thrown"
				Simulator.Reason.MAX_STEPS -> "Max steps exceeded"
				Simulator.Reason.NORMAL_TERMINATION, Simulator.Reason.CLIFF_TERMINATION ->
					program.getSTDOUT().trim('\u0000')
				Simulator.Reason.PAUSE -> "Simulation paused"
				Simulator.Reason.STOP -> "Simulation stopped"
			}
		}
		sub.results = inputs zip outputs
	}
}