#include <stdio.h> 
#include <unistd.h> 
#include <assert.h> 
#include "dmm.h"

typedef struct metadata {
	
	       /* size_t is the return type of the sizeof operator. Since the size of
 	* an object depends on the architecture and its implementation, size_t 
	* is used to represent the maximum size of any object in the particular
 	* implementation. 
	* size contains the size of the data object or the amount of free
 	* bytes 
	*/

	 //I maintained the same metadata structure and used the original three variables
	size_t size;
	struct metadata* next;
	struct metadata* prev; 
} metadata_t;

/* freelist maintains all the blocks which are not in use; freelist is kept
 * always sorted to improve the efficiency of coalescing 
 */

static metadata_t* freelist = NULL;
static metadata_t* original = NULL;


void* dmalloc(size_t numbytes) {
	if(freelist == NULL) { 			//Initialize through sbrk call first time
		if(!dmalloc_init())
			return NULL;
		original = freelist;

	}

	assert(numbytes > 0);
	size_t aligned_num = ALIGN(numbytes);

	
	if (freelist->next == NULL) {
	  //Deal with the case when freelist is the last free node
	  //Check first if freelist is big enough for required allocation
	  
	  if(freelist->size >= aligned_num) {
		 metadata_t* temp = freelist;
		 if (freelist->size<= aligned_num + METADATA_T_ALIGNED){
		   freelist->next = NULL;
		   freelist->prev = NULL;
		   freelist = NULL;
		 } else {
			 metadata_t* record = (void*)temp + aligned_num + METADATA_T_ALIGNED;
			 record->size = temp->size - aligned_num - METADATA_T_ALIGNED;
			 record->next = NULL;
			 record->prev = NULL;
			 temp->size =  aligned_num;
			 temp->next = NULL;
			 temp->prev = NULL;
			 freelist = record;
                 }

		 return ((void*)temp+METADATA_T_ALIGNED);
	    } else {
	    return NULL;
            }
	} else {
	  metadata_t* temp = freelist;
	  
	  //if freelist is not big enough then we need to traverse down the list
	    if (temp->size<aligned_num){	
	      if (temp->next != NULL){
		temp = temp->next;
		} else {
		return NULL;
	      }
	      while (temp->next !=NULL) {
		if(temp->size >= aligned_num) {
		  if(temp->size > (aligned_num+METADATA_T_ALIGNED)) {
		              metadata_t* temp2 = (void*)temp + aligned_num + METADATA_T_ALIGNED;
			      temp2->next = temp->next;
			      temp->next->prev = temp2;
			      temp-> prev->next = temp2; 
			      temp2->prev = temp->prev;
			      temp2->size = temp->size - (aligned_num+METADATA_T_ALIGNED);
			      temp->size = aligned_num;
			       temp->prev = NULL;
			       temp->next= NULL;
		  } else {
		    (temp->prev)->next = temp->next;
		    (temp->next)->prev = temp->prev;
		     temp->next = NULL;
		     temp->prev = NULL;
		  }
		   return ((void*)temp+METADATA_T_ALIGNED);
		} else {
		  temp = temp->next;
		}
	      } 
	      if(temp->size >= aligned_num)
		{
		  if (temp->size > (aligned_num + METADATA_T_ALIGNED))
		    {
		      metadata_t* temp2 = (void*)temp + aligned_num + METADATA_T_ALIGNED;
		      temp->prev->next = temp2;
		      temp2->next = NULL;
		      temp2->prev = temp->prev;
		      temp2->size = temp->size - (aligned_num+METADATA_T_ALIGNED);
		      temp->size = aligned_num;
		       temp->next = NULL;
		       temp->prev = NULL;
		    } else {
		      temp->prev->next = NULL;
		      temp->prev = NULL;
		      temp->next = NULL;
		      freelist = NULL;
		  }
		  return ((void*)temp + METADATA_T_ALIGNED);
		}
	      return NULL;
	    } else {
	    if(temp->size > (aligned_num+METADATA_T_ALIGNED)){
	      	metadata_t* temp2 = (void *)temp  + aligned_num+METADATA_T_ALIGNED;
		temp2->next = freelist->next;
		freelist->next->prev = temp2;
		temp2->size = temp->size-aligned_num-METADATA_T_ALIGNED;
		temp2->prev = NULL;			
		freelist->size = aligned_num;
		freelist->next = NULL;
		freelist = temp2;
	     } else {
		freelist = freelist->next;
		freelist->prev->next = NULL;
	        freelist->prev = NULL;
	    }
	     return ((void*)temp+METADATA_T_ALIGNED);
	  }
	  return NULL;
	}
}

void dfree(void* ptr) {
  metadata_t* pointer = ptr-METADATA_T_ALIGNED;
  metadata_t* garbage_ptr = (void*)pointer+ pointer->size+METADATA_T_ALIGNED;
  if(pointer == NULL)
  	return;
  if(pointer == freelist)
        return;
  if(pointer->prev != NULL)
  	return;
  	
  //This is the case when there is no free node in the list	
  if(freelist == NULL) {
  	freelist = pointer;
	freelist->next = NULL;
	freelist->size = (void*)garbage_ptr-(void*)pointer-METADATA_T_ALIGNED;
	freelist->prev= NULL;
	return;
  }

  if (garbage_ptr < freelist) {
      pointer->next = freelist;
      freelist->prev = pointer;
      pointer->size =(void*)garbage_ptr-(void*)pointer-METADATA_T_ALIGNED;
      freelist = pointer;
      pointer->prev = NULL;
      return;
  } 

  //this is the case where the payload is sitting right next to the freelist on the left
  if (garbage_ptr == freelist) {
	if (freelist->next != NULL) {
		pointer->next = freelist->next;
		freelist->next->prev = pointer;
		pointer->prev = NULL;
        	pointer->size = freelist->size + (void*)freelist -(void*)pointer;
		freelist = pointer;
	} else {
	  	pointer->next = NULL;
	  	pointer->size = freelist->size + (void*)freelist -(void*)pointer;
	  	freelist = pointer;
	}

	return;
  }



  if (((void*)freelist+(freelist->size)+METADATA_T_ALIGNED) == pointer) {

      if(freelist->next == NULL) {
	  freelist->size = freelist->size + ((void*)garbage_ptr-(void*)pointer);
	  return;
      }
      //now there are other free nodes after freelist, thus check if freelist->next is sitting right next to ptr
      if (garbage_ptr == freelist->next)  {
      	
	  freelist->size = freelist->size+freelist->next->size+METADATA_T_ALIGNED+((void*)garbage_ptr-(void*)pointer);
	  
	  if(freelist->next->next != NULL) {
	      metadata_t* copy = freelist->next;
	      freelist->next = freelist->next->next;
	      freelist->next->prev = freelist;
	      copy->next= NULL;
	      copy->prev = NULL;
	   } else {
	     freelist->next->next = NULL;
       	     freelist->next->prev = NULL;
	     freelist->next = NULL;

	  }
	} else {
		freelist->size = freelist->size + (void*)garbage_ptr-(void*)pointer;
		return;
	}
    }
 
   if(freelist->next == NULL) {
       freelist->next= pointer;
       pointer->prev = freelist;
       pointer->next = NULL;
       pointer->size = (void*)garbage_ptr-(void*)pointer-METADATA_T_ALIGNED;
       return;
    }
    
  metadata_t* search_ptr = freelist->next;
  
  while(search_ptr < pointer && (search_ptr->next != NULL) ) {
      search_ptr = search_ptr->next;
  }
 //now the search_ptr sits next to ptr on the right
  if (search_ptr > pointer) {
      if((garbage_ptr == search_ptr) && ((void*)search_ptr->prev+search_ptr->prev->size+METADATA_T_ALIGNED == pointer))	{	
	  if(search_ptr->next == NULL){
	      	search_ptr->prev->next = NULL;
	      	search_ptr->prev->size =  search_ptr->prev->size+ (void*)garbage_ptr-(void*)pointer+METADATA_T_ALIGNED+search_ptr->size;
	      	search_ptr->prev = NULL;
	      	search_ptr->next = NULL;
	      	return;
	   } else {
	    	search_ptr->prev->next = search_ptr->next;
	    	search_ptr->next->prev = search_ptr->prev;
	    	search_ptr->prev->size =  search_ptr->prev->size+ (void*)garbage_ptr-(void*)pointer+METADATA_T_ALIGNED+search_ptr->size;
	    	search_ptr->prev = NULL;
	    	search_ptr->next = NULL;
	    	return;
	   }
	} else if ((garbage_ptr != search_ptr) && ((void*)search_ptr->prev+search_ptr->prev->size+METADATA_T_ALIGNED == pointer)) {
		search_ptr->prev->size = search_ptr->prev->size + (void*)garbage_ptr-(void*)pointer;
		return;
        } else if ((garbage_ptr == search_ptr) && ((void*)search_ptr->prev+search_ptr->prev->size+METADATA_T_ALIGNED != pointer)) {
		if (search_ptr->next == NULL) {
	    		search_ptr->prev->next = pointer;
	    		pointer->prev = search_ptr->prev;
	    		pointer->next = NULL;
	    		pointer->size = search_ptr->size+(void*)garbage_ptr-(void*)pointer;
	    		search_ptr->prev = NULL;
	    		search_ptr->next = NULL;
	    		return;
	 } else {
	    search_ptr->prev->next = pointer;
	    pointer->prev = search_ptr->prev;
	    pointer->next = search_ptr->next;
	    search_ptr->next->prev = pointer;
	    pointer->size = search_ptr->size+(void*)garbage_ptr-(void*)pointer;
	    search_ptr->prev = NULL;
	    search_ptr->next = NULL;
	    return;
	}
      } else {
	  search_ptr->prev->next= pointer;
	  pointer->prev = search_ptr->prev;
	  pointer->next = search_ptr;
	  search_ptr->prev = pointer;
	  pointer->size = (void*)garbage_ptr-(void*)pointer-METADATA_T_ALIGNED;
	  return;
	}	
      return;
    }
  //now ptr must be on the right side of search_ptr
  if ((void*)search_ptr+search_ptr->size != pointer) {
      search_ptr->next = pointer;
      pointer->prev = search_ptr;
      pointer->next = NULL;
      pointer->size = (void*)garbage_ptr-(void*)pointer-METADATA_T_ALIGNED;
  } else {
      search_ptr->size = (void*)garbage_ptr-(void*)search_ptr-METADATA_T_ALIGNED;
  }
  return;
}

bool dmalloc_init() {
	size_t max_bytes = ALIGN(MAX_HEAP_SIZE);
	freelist = (metadata_t*) sbrk(max_bytes); // returns heap_region, which is initialized to freelist

	if (freelist == (void *)-1)
		return false;

	freelist->next = NULL;
	freelist->prev = NULL;
	freelist->size = max_bytes-METADATA_T_ALIGNED;
	return true;
}

void print_freelist() {
	metadata_t *freelist_head = freelist;
	while(freelist_head != NULL) {
		DEBUG("\tFreelist Size:%zd, Head:%p, Prev:%p, Next:%p\t",freelist_head->size,freelist_head,freelist_head->prev,freelist_head->next);
		freelist_head = freelist_head->next;
	}
	DEBUG("\n");
}
