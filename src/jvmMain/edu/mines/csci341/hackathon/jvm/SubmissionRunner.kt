package edu.mines.csci341.hackathon.jvm

import java.util.concurrent.*
import java.util.regex.*
import kotlin.streams.asSequence
import edu.mines.csci341.hackathon.Submission
import rars.api.*
import rars.simulator.Simulator
import rars.riscv.SyscallLoader
import rars.riscv.AbstractSyscall
import rars.riscv.syscalls.NullString
import rars.riscv.hardware.RegisterFile
import rars.riscv.hardware.FloatingPointRegisterFile
import rars.util.SystemIO

object SubmissionRunner : Runnable {
	private const val TIMEOUT_SECONDS = 10L
	private const val TIMEOUT_STEPS = 10_000_000
	private val executor: ExecutorService = Executors.newSingleThreadExecutor()
	private val program = Program(Options().apply {
		maxSteps = TIMEOUT_STEPS
	})
	private val submissionQueue = SynchronousQueue<Pair<Submission, List<String>>>(true)
	
	init {
		val syscalls: MutableList<AbstractSyscall> = SyscallLoader.getSyscallList()
		// disable syscalls that we don't want students to use/there's no need to be used
		// these include getcwd, open/close, lseek, as well as syscalls that wait for user input
		val disabledSyscalls = setOf(17, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 62, 1024)
		syscalls.removeIf { disabledSyscalls.contains(it.getNumber()) }
		// enable custom printf syscall
		syscalls.add(SyscallPrintF().apply {
			setNumber(1025)
		})
	}
	
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
			val cause = e.cause
			when (cause) {
				is rars.AssemblyException -> buildString {
					appendLine("Assembly error:")
					cause.errors().getErrorMessages().forEach {
						appendLine("${it.getMessage()}, ")
					}
					dropLast(3)
				}
				is rars.SimulationException -> "Simulation error: ${cause.error().getMessage()}"
				else -> "Unknown error: ${cause?.message}"
			}
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

class SyscallPrintF() : AbstractSyscall(
	"PrintF",
	"Prints a formatted string to standard output. Indexed arguments are not supported.",
	"""
	a0 = format string
	a1-a6 = positional arguments
	""".trimIndent(),
	"N/A"
) {
	private val specifier: Pattern = Pattern.compile("%[-#+ 0,(]*\\d*(\\.\\d+)?(\\w)")
	
	@ExperimentalUnsignedTypes
	override fun simulate(stmnt: rars.ProgramStatement) {
		var fmt: String = NullString.get(stmnt)
		val args: Array<Any?> = arrayOfNulls(6)
		val matches: Matcher = specifier.matcher(fmt)
		matches.results()
			.limit(6).asSequence()
			.forEachIndexed { index, match ->
				val intReg = "a${index + 1}"
				val floatReg = "fa${index + 1}"
				args[index] = when (match.group(2)) {
					"s", "S" -> NullString.get(stmnt, intReg)
					"u" -> RegisterFile.getValue(intReg).toUInt()
					"c", "C", "d", "o", "x", "X" -> RegisterFile.getValue(intReg)
					"e", "E", "f", "g", "G", "a", "A" -> FloatingPointRegisterFile.getFloatFromRegister(floatReg)
					else -> null
				}
			}
		// the Java Formatter has no 'u' conversion, so we can use the 's' conversion instead
		fmt = matches.replaceAll { it.group().replace('u', 's') }
		val out = fmt.format(*args)
		SystemIO.writeToFile(1, out.toByteArray(), out.length)
	}
}