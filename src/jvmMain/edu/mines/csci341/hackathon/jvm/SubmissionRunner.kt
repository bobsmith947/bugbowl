package edu.mines.csci341.hackathon

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

import java.lang.Process;
import java.time.Duration;
import java.time.LocalDateTime;

class SubmissionRunner: Runnable {
	enum class Status {
		READY,
		RUNNING,
		DONE,
		END
	}
	
	var proc: Process?=null;		//Set to startup on object creation
	var sub: SubmissionWatcher.SubmissionTestPair?=null;
	var running: Boolean=false;
	var status: Status=Status.READY;
	var idleTime: LocalDateTime=LocalDateTime.now();
	
	public fun getIdleTimeInSeconds(): Long {
		if (this.status==Status.READY){
			val now: LocalDateTime=LocalDateTime.now();
			val diff=Duration.between(now,this.idleTime);
			return diff.getSeconds();
		}
		return 0;
	}
	
	@Synchronized
	public fun setSubmission(sub: SubmissionWatcher.SubmissionTestPair){
		if (this.status==Status.READY){
			this.sub=sub;
			this.status=Status.RUNNING;
		}
	}
	
	public override fun run(){
		while(this.status!=Status.END){
			if (this.status==Status.RUNNING){
				//call func to run submission, set status to DONE when completed
			}
			if (this.status==Status.DONE){
				//call func to set submission results, set status to READY when completed
				this.idleTime=LocalDateTime.now();
			}
		}
		//kill process
	}
}
