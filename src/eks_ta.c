

#include "tee_internal_api.h"
#include "tee_logging.h"
#include "tee_ta_properties.h"


#define INITIATE_DH_CMD    0x00000001
#define RESPOND_DH_CMD     0x00000002
#define COMPLETE_DH_CMD    0x00000003

SET_TA_PROPERTIES(
	{ 0x316cd877, 0xebae, 0x59d2, {0xa7,0x66,0x6c,0xce,0x17,0xe1,0xd4,0x71} }, /* UUID */
		512, /* dataSize */
		255, /* stackSize */
		0, /* singletonInstance */
		1, /* multiSession */
0) /* instanceKeepAlive */

/*Length of the sysmmetric secret key(in bits) to be generated*/
size_t key_length;

/*Entry point for the TA*/
TEE_Result TA_EXPORT TA_CreateEntryPoint(void){
	
	OT_LOG(LOG_DEBUG, "Calling entry point for EKS_TA");
	return TEE_SUCCESS;
}

/**/
void TA_EXPORT TA_DestroyEntryPoint(void)
{

	OT_LOG(LOG_DEBUG, "Calling Destroy entry point for EKS_TA");
}

/**/
TEE_Result TA_EXPORT TA_OpenSessionEntryPoint(uint32_t paramTypes, TEE_Param params[4], void **sessionContext){

	OT_LOG(LOG_INFO, "Calling Open session entry point for EKS_TA");

   	return TEE_SUCCESS;
}

/**/
void TA_EXPORT TA_CloseSessionEntryPoint(void *sessionContext){

	OT_LOG(LOG_INFO, "Calling Close session entry point for EKS_TA");
}

/**/
TEE_Result TA_EXPORT TA_InvokeCommandEntryPoint(void *sessionContext, uint32_t commandID, uint32_t paramTypes, TEE_Param params[4]){

	if( INITIATE_DH_CMD == commandID ){

			OT_LOG(LOG_INFO, "INITIATE_DH_CMD command invoked for EKS_TA");
 
	}
	else if( RESPOND_DH_CMD == commandID ){

			OT_LOG(LOG_INFO, "RESPOND_DH_CMD command invoked for EKS_TA");
 
	}
	else if(  COMPLETE_DH_CMD == commandID ){

			OT_LOG(LOG_INFO, "COMPLETE_DH_CMD command invoked for EKS_TA");		

	}
	else{

		OT_LOG(LOG_INFO, "Invalid command invoked for EKS_TA");
		return TEE_ERROR_BAD_PARAMETERS;		
 	}
  
	return TEE_SUCCESS;
}
