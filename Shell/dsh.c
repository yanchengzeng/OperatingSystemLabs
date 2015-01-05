
#include "dsh.h"
#include <assert.h>

void seize_tty(pid_t callingprocess_pgid); /* Grab control of the terminal for the calling process pgid.  */
void continue_job(job_t *j); /* resume a stopped job */
void spawn_job(job_t *j, bool fg); /* spawn a new job */
void wait_call(job_t* j,int num);


/* Sets the process group id for a given job and process */
int set_child_pgid(job_t *j, process_t *p) {
	if (j->pgid < 0) /* first child: use its pid for job pgid */
		j->pgid = p->pid;
	return(setpgid(p->pid,j->pgid));
}

/* Creates the context for a new child by setting the pid, pgid and tcsetpgrp */
void new_child(job_t *j, process_t *p, bool fg) {
	/* establish a new process group, and put the child in
	 * foreground if requested
	 */

	/* Put the process into the process group and give the process
	 * group the terminal, if appropriate.  This has to be done both by
	 * the dsh and in the individual child processes because of
	 * potential race conditions.
	 * */

	p->pid = getpid();

	/* also establish child process group in child to avoid race (if parent has not done it yet). */
	set_child_pgid(j, p);

	if(fg) // if fg is set
		seize_tty(j->pgid); // assign the terminal
	perror("seize_tty: ");

	/* Set the handling for job control signals back to the default. */
	signal(SIGTTOU, SIG_DFL);
	perror("Signal setting: ");

}

/* Spawning a process with job control. fg is true if the
 * newly-created process is to be placed in the foreground.
 * (This implicitly puts the calling process in the background,
 * so watch out for tty I/O after doing this.) pgid is -1 to
 * create a new job, in which case the returned pid is also the
 * pgid of the new job.  Else pgid specifies an existing job's
 * pgid: this feature is used to start the second or
 * subsequent processes in a pipeline.
 * */

void spawn_job(job_t *j, bool fg) {

	pid_t pid;
	process_t *p;
	int process_num = 0;

	//count the number of processes in the job
	for(p=j->first_process; p; p = p->next) {
		process_num++;

	}
	//set up the pipe
	int pipe_num = process_num-1;
	bool is_pipe = false;
	int pipefd[pipe_num][2];
	int z;

	for (z=0;z<pipe_num;z++) {
		pipefd[z][0] = 0;
		pipefd[z][1] = 0;

	}


	if(pipe_num>0) {
		is_pipe = true;


		//  pid_t cpid;


		/* if (argc != 2) {
          fprintf(stderr, "Usage: %s <string>\n", argv[0]);
              exit(EXIT_FAILURE);
          }*/

		//call pipe() pipe_num times
		int i;
		for (i=0; i<pipe_num; i++) {
			pipefd[i][0] = 1;
			pipefd[i][1] = 1;



			if (pipe(pipefd[i]) == -1) {
				perror("pipe");
				exit(EXIT_FAILURE);
			}
		}
	}

	// printf("Is it a pipe: %d.\n",(int)is_pipe);

	//number each child forked by parent
	int child_track = -1;


	//finishing creating pipes, now ready for fork

	for(p = j->first_process; p; p = p->next) {

		/* YOUR CODE HERE? */
		/* Builtin commands are already taken care earlier */

		//need to create pipe here

		//first increment the child tracking number
		child_track++;

		// printf("Child_track is %d.\n", child_track);

		//for waitpid() status return
		// int status;

		// printf("Before fork.\n");

		switch (pid = fork()) {

		case -1: /* fork failure */
			perror("fork");
			exit(EXIT_FAILURE);

		case 0: /* child process  */
			p->pid = getpid();
			new_child(j, p, fg);
			//   printf("the beginning of the child now tty belongs to %d\n", tcgetpgrp(STDIN_FILENO));


			//check if any arguments is equal to "<", if so input redirection

			if (p->ifile != NULL) {
				if(child_track == 0) {
					//  printf("We are about to open a file!\n");
					close(STDIN_FILENO);
					int fd = open(p->ifile,O_RDONLY);
					assert(fd != -1);
					//read(fd,buf,7);
					//close(fd);
				}

			}
			//check for output redirection
			if (p->ofile != NULL) {
				if (child_track == process_num - 1) {
					//  printf("We are about to create a file!\n");
					close(STDOUT_FILENO);
					int fd = creat(p->ofile, 0777); //open(p->ofile,O_WRONLY);
					// write(fd,buf,7);
					// close(fd);
				}

			}

			//		seize_tty(j->pgid);
			//	 printf("right before pipe stuff now tty belongs to %d\n", tcgetpgrp(STDIN_FILENO));
			/* YOUR CODE HERE?  Child-side code for new process. */

			//implement the pipeline

			if (is_pipe) {
				int i;
				//	printf("The pipe_num is: %d.\n",pipe_num);

				//first close all the other copies of the pipeline that this child doesn't use
				for(i=0; i<pipe_num; i++) {
					if(i != child_track && i != child_track-1) {

						close(pipefd[i][0]);
						close(pipefd[i][1]);
					}
				}
				// if the child is the first one in the pipeline
				// printf("between 111 and 222 now tty belongs to %d\n", tcgetpgrp(STDIN_FILENO));
				if (child_track == 0) {
					//	 seize_tty(j->pgid);
					//   printf("222 now tty belongs to %d\n", tcgetpgrp(STDIN_FILENO));
					close(pipefd[child_track][0]);
					close(STDOUT_FILENO);

					dup2(pipefd[child_track][1],STDOUT_FILENO);
					close(pipefd[child_track][1]);
					//     perror("First dup.");

				} else if (child_track == process_num-1) { // if the child is the last one in the pipeline
					//	  printf("333\n");

					close(pipefd[child_track-1][1]);
					close(STDIN_FILENO);

					dup2(pipefd[child_track-1][0],STDIN_FILENO);
					close(pipefd[child_track-1][0]);

					//   perror("Last dup.");


				} else { // or it is in the middle
					close(pipefd[child_track-1][1]);
					dup2(pipefd[child_track-1][0],STDIN_FILENO);
					close(pipefd[child_track-1][0]);


					close(pipefd[child_track][0]);
					dup2(pipefd[child_track][1],STDOUT_FILENO);
					close(pipefd[child_track][1]);

					//   perror("Middle dup.");

				}
				//	printf("The child bianhao is: %d.\n",child_track);
				//	printf("The pid is %ld.\n",(long)getpid());
				//	printf("pipe now tty belongs to %d, and the pid is %ld\n", tcgetpgrp(STDIN_FILENO),(long)getppid());
				//	 perror("Why there is a turnover.");
				execvp(p->argv[0],p->argv);


			} else {
				// printf("Right before execs.\n");
				// printf("single now tty belongs to %d\n", tcgetpgrp(STDIN_FILENO));
				execvp(p->argv[0],p->argv);
			}


			perror("New child should have done an exec");
			exit(EXIT_FAILURE);  /* NOT REACHED */
			break;    /* NOT REACHED */

		default: /* parent */
			/* establish child process group */
			p->pid = pid;
			set_child_pgid(j, p);


			/* YOUR CODE HERE?  Parent-side code for new process.  */
		}

		/* YOUR CODE HERE?  Parent-side code for new job.*/
		// printf("Is it because of the seize.\n");

		//seize_tty(getpid()); // assign the terminal back to dsh

	}

	int i;
	for (i=0; i<pipe_num; i++) {
		close(pipefd[i][0]);
		close(pipefd[i][1]);
	}
	// close(STDIN_FILENO);
	// close(STDOUT_FILENO);

	/* int status[process_num];
    int k;
    for (k = 0; k<process_num; k++)
    {
      pid_t result = waitpid(-1,&status[k],WUNTRACED);
        if(result != -1)
            printf("Child %d exits with status %d.\n",result, WEXITSTATUS(status[k]));
	    }*/
	wait_call(j,process_num);
	process_t *ppp;

	for(ppp = j->first_process; ppp; ppp = ppp->next)
		ppp->completed = true;

	seize_tty(getpid());

}

void wait_call(job_t* j, int num){

	while(!job_is_completed(j)){
		int status;
		pid_t result = waitpid(-1,&status,WUNTRACED);
		process_t* p;
		for (p = j->first_process;p;p=p->next){
			if (p->pid == result){

				if(WIFSTOPPED(status) && WEXITSTATUS(status) != 0){
					printf("The dawn before stops.");
					p->stopped = true;
				}else
					p->completed = true;

				printf("Child %d is stopped?: %d.\n",(int)WIFSTOPPED(status));
			}

		}

		printf("Child %d exits with status %d.\n",result, WEXITSTATUS(status));
	}
	return;
}



/* Sends SIGCONT signal to wake up the blocked job */
void continue_job(job_t *j){
	if(kill(-j->pgid, SIGCONT) < 0)
		perror("kill(SIGCONT)");
}


/*
 * builtin_cmd - If the user has typed a built-in command then execute
 * it immediately.
 */
bool builtin_cmd(job_t *last_job, int argc, char **argv){

	/* check whether the cmd is a built in command
	 */

	if (!strcmp(argv[0], "quit")) {
		/* Your code here */
		job_t* check;
		for (check = last_job; check; check = check->next){
			delete_job(check,last_job);
		}

		//   printf("Please work.\n");

		// free_job(last_job);
		exit(EXIT_SUCCESS);
	} else if (!strcmp("jobs", argv[0])) {
		/* Your code here */
		//    printf("Enter jobs1.\n");
		//	print_job(last_job);

		// job_t* check1 = find_last_job(last_job);
		//  delete_job(check1,last_job);
		job_t* check;
		/* if (job_is_completed(check))
	{
	  printf("Job %ld is %s, and the status is %d.\n", (long)check->pgid,check->commandinfo,check->first_process->status);
	  }*/
		// printf("The check mark is %d, and the pid is: %ld\n",job_is_completed(check),(long)check->pgid);
		for (check = last_job->next; check; check = check->next){
			//	  printf("Enter jobs2.\n");

			if(job_is_completed(check)){
				//  printf("Enter jobs.\n");

				printf("%ld (completed): %s\n", (long)check->pgid,check->commandinfo);
			}
		}

		return true;
	}else if (!strcmp("cd", argv[0])) {
		/* Your code here */
		// printf("Enter cd.\n");

		//  chdir(argv[1]);



		char path[200];
		strcpy(path,argv[1]);



		char cwd[200];
		if(argv[1][0] != '/'){   // true for the dir in cwd
			getcwd(cwd,sizeof(cwd));
			strcat(cwd,"/");
			strcat(cwd,path);
			chdir(cwd);
		} else { //true for dir w.r.t. /
			chdir(argv[1]);
		}
		//	printf("Yeah?.\n");
		job_t* current = find_last_job(last_job);
		current->pgid = getpid();

		process_t *ppp;
		for(ppp = current->first_process; ppp; ppp = ppp->next)
			ppp->completed = true;

		return true;
	}else if (!strcmp("bg", argv[0])) {
		/* Your code here */
	}else if (!strcmp("fg", argv[0])) {
		/* Your code here */
	}
	return false;       /* not a builtin command */
}

/* Build prompt messaage */
char* promptmsg(){
	/* Modify this to include pid */
	char s1[] = "dsh-";
	char s2[10];
	sprintf(s2,"%ld",(long)getpid());
	char* result = malloc(strlen(s1)+strlen(s2)+1);
	strcpy(result,s1);
	strcat(result,s2);
	char s3[3]="$ ";
	strcat(result,s3);
	return result;
}

int main(){

	init_dsh();
	DEBUG("Successfully initialized\n");

	job_t *first_job = (job_t *)malloc(sizeof(job_t));
	if(!first_job) {
		fprintf(stderr, "%s\n","malloc: no space");
		return -1;
	}

	job_t *current_job = (job_t *)malloc(sizeof(job_t));
	if(!current_job) {
		fprintf(stderr, "%s\n","malloc: no space");
		return -1;
	}
	current_job = first_job;

	while(1) {
		job_t *j = NULL;
		//	printf("Create a brand new job.\n");
		//   printf("now tty belongs to %d\n", tcgetpgrp(STDIN_FILENO));
		if(!(j = readcmdline(promptmsg()))) {
			if (feof(stdin)) { /* End of file (ctrl-d) */
				fflush(stdout);
				printf("\n");

				exit(EXIT_SUCCESS);
			}
			continue; /* NOOP; user entered return or spaces with return */
		}

		/* Only for debugging purposes to show parser output; turn off in the
		 * final code */
		//  if(PRINT_INFO) print_job(j);


		/* Your code goes here */
		/* You need to loop through jobs list since a command line can contain ;*/
		/* Check for built-in commands */
		/* If not built-in */
		/* If job j runs in foreground */
		/* SPAWN_JOB(J,TRUE) */
		/* ELSE */
		/* SPAWN_JOB(J,FALSE) */
		current_job ->next = j;


		while (current_job->next != NULL){
			current_job = current_job->next;

			process_t* current_process = j->first_process;

			if(!builtin_cmd(first_job,current_process->argc,current_process->argv)){

				spawn_job(current_job,true);
				//  printf("It does spawn a job.\n");
			}
			// current_job = current_job->next;
			//   printf("End of the job list.\n");
		}

	}
}
