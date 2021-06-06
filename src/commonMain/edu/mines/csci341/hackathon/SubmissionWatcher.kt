package edu.mines.csci341.hackathon

import kotlin.jvm.internal.Intrinsics.Kotlin
import kotlin.collections.Collection

object SubmissionWatcher: Runnable {
	data class SubmissionTestPair(val sub: Submission, val test: List<String>);
	
	var runners: MutableList<SubmissionRunner> =mutableListOf();
	var submissionQueue: MutableList<SubmissionTestPair> =mutableListOf<SubmissionTestPair>();
	val MAX_RUNNERS: Int=10;
	val MAX_IDLE_TIME: Long=3600*3;
	val submissionProcessingCompleted: Boolean
		get()= this.submissionQueue.size==0;
	
	init {
		this.run();
	}
	
	@Synchronized
	public fun addSubmissionToQueue(sub: Submission, inputs: List<Pair<String,String>>){
		var testStrs: MutableList<String> =mutableListOf<String>();
		for (iter in inputs){
			testStrs.add(iter.first);		//Assuming that inputs is defined as <Input Strs, OutputStrs>
		}
		this.submissionQueue.add(SubmissionTestPair(sub,testStrs));
	}
		
	public override fun run(){
		while (true){
			this.addAnotherProcess();
			this.addQueueItemToProcess();
			this.endIdleProcess();
		}
	}
	
	@Synchronized
	private fun addAnotherProcess(){
		if (this.submissionQueue.size>0){
			if (this.runners.size<SubmissionWatcher.MAX_RUNNERS){
				this.runners.add(SubmissionRunner());
			}
		}
	}
	
	@Synchronized
	private fun addQueueItemToProcess(){
		for (iter in this.runners){
			if (iter.status==SubmissionRunner.Status.READY && this.submissionQueue.size>0){
				iter.setSubmission(this.submissionQueue.get(this.submissionQueue.size-1));
				this.submissionQueue.removeAt(this.submissionQueue.size-1);
			}
		}
	}
	
	@Synchronized
	private fun endIdleProcess(){
		this.runners.removeAll{
			it.getIdleTimeInSeconds()>SubmissionWatcher.MAX_IDLE_TIME
		};
	}
}