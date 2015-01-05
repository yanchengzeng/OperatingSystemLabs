#ifndef __CPS210_MM_H__ 	
#define __CPS210_MM_H__


#define MAX_HEAP_SIZE	(1024*1024) /* max size restricted to 1kB*/
#define WORD_SIZE	8
#define ALIGNMENT 	WORD_SIZE	/* typically, single word on 32-bit systems and double word on 64-bit systems */
#define ALIGN(size) (((size) + (ALIGNMENT-1)) & ~(ALIGNMENT-1))
#define SIZE_T_ALIGNED (ALIGN(sizeof(size_t)))
#define METADATA_T_ALIGNED (ALIGN(sizeof(metadata_t)))
#ifdef NDEBUG
	#define DEBUG(M, ...)
	#define PRINT_FREELIST print_freelist
#else
	#define DEBUG(M, ...) fprintf(stderr, "[DEBUG] %s:%d: " M "\n", __FILE__, __LINE__, ##__VA_ARGS__)
	#define PRINT_FREELIST
#endif

typedef enum{false, true} bool;

bool dmalloc_init();
void *dmalloc(size_t numbytes);
void dfree(void *allocptr);


void print_freelist(); 

#endif /* end of __CPS210_MM_H__ */
