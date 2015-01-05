#ifndef __DSH_H__         
#define __DSH_H__

#include <stdio.h>
#include <sys/types.h>  
#include <unistd.h>     
#include <signal.h>     
#include <stdlib.h>     
#include <errno.h>      
#include <sys/wait.h>   
#include <string.h>     
#include <sys/stat.h>   
#include <fcntl.h>      

#define MAX_LEN_FILENAME 80
#define MAX_LEN_CMDLINE	120
#define MAX_ARGS 20 
#define INPUT_FD  1000
#define OUTPUT_FD 1001
#define MAX_HISTORY 20 
#define MAX_ARGS 20 
#define PRINT_INFO 1 

typedef enum { false, true } bool;

typedef struct process {
        struct process *next;       
	    int argc;		          
        char **argv;               
        pid_t pid;                 
        bool completed;             
        bool stopped;               
        int status;                
        char *ifile;                
        char *ofile;                
} process_t;

typedef struct job {
        struct job *next;           
        char *commandinfo;          
        process_t *first_process;   
        pid_t pgid;                 
        bool notified;              
        int mystdin, mystdout, mystderr;  
        bool bg;                   
} job_t;

job_t *detach_job(job_t *first_job);

bool job_is_stopped(job_t *j);

bool job_is_completed(job_t *j);

job_t *find_last_job();

void delete_job(job_t *j, job_t *first_job);

bool init_job(job_t *j);

bool init_process(process_t *p);

void print_job();

void init_dsh();

void seize_tty(pid_t callingprocess_pgid);

int endswith(const char* haystack, const char* needle);

job_t* readcmdline(char *msg);

#ifdef NDEBUG
        #define DEBUG(M, ...)
#else
        #define DEBUG(M, ...) fprintf(stderr, "[DEBUG] %s:%d: " M "\n", __FILE__, __LINE__, ##__VA_ARGS__)
#endif

#endif /* __DSH_H__*/
