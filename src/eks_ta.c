
#include "tee_internal_api.h"
#include "tee_logging.h"
#include "tee_ta_properties.h"

#define INITIATE_DH_CMD    0x00000001
#define RESPOND_DH_CMD     0x00000002
#define COMPLETE_DH_CMD    0x00000003
#define g_size 4
#define p_size 256

uint8_t g[ g_size ] = { 0x00, 0x00, 0x00, 0x03 };
uint8_t p[ p_size ] = {	0xFF,0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xC9, 0x0F, 0xDA, 0xA2, 0x21, 0x68, 0xC2, 0x34, 0xC4, 0xC6, 0x62, 0x8B, 0x80, 0xDC, 0x1C, 0xD1,
     						0x29, 0x02, 0x4E, 0x08, 0x8A, 0x67, 0xCC, 0x74, 0x02, 0x0B, 0xBE, 0xA6, 0x3B, 0x13, 0x9B, 0x22, 0x51, 0x4A, 0x08, 0x79, 0x8E, 0x34, 0x04, 0xDD,
     				    	0xEF, 0x95, 0x19, 0xB3, 0xCD, 0x3A, 0x43, 0x1B, 0x30, 0x2B, 0x0A, 0x6D, 0xF2, 0x5F, 0x14, 0x37, 0x4F, 0xE1, 0x35, 0x6D, 0x6D, 0x51, 0xC2,0x45,
      				    	0xE4, 0x85, 0xB5, 0x76, 0x62, 0x5E, 0x7E, 0xC6, 0xF4, 0x4C, 0x42, 0xE9, 0xA6, 0x37, 0xED, 0x6B, 0x0B, 0xFF, 0x5C, 0xB6, 0xF4, 0x06, 0xB7, 0xED,
      						0xEE, 0x38, 0x6B, 0xFB, 0x5A, 0x89, 0x9F, 0xA5, 0xAE, 0x9F, 0x24, 0x11, 0x7C, 0x4B, 0x1F, 0xE6, 0x49, 0x28, 0x66, 0x51, 0xEC, 0xE4, 0x5B, 0x3D,
      						0xC2, 0x00, 0x7C, 0xB8, 0xA1, 0x63, 0xBF, 0x05, 0x98, 0xDA, 0x48, 0x36, 0x1C, 0x55, 0xD3, 0x9A, 0x69, 0x16, 0x3F, 0xA8, 0xFD, 0x24, 0xCF, 0x5F,
     				    	0x83, 0x65, 0x5D, 0x23, 0xDC, 0xA3, 0xAD, 0x96, 0x1C, 0x62, 0xF3, 0x56, 0x20, 0x85, 0x52, 0xBB, 0x9E, 0xD5, 0x29, 0x07, 0x70, 0x96, 0x96, 0x6D,
      				    	0x67, 0x0C, 0x35, 0x4E, 0x4A, 0xBC, 0x98, 0x04, 0xF1, 0x74, 0x6C, 0x08, 0xCA, 0x18, 0x21, 0x7C, 0x32, 0x90, 0x5E, 0x46, 0x2E, 0x36, 0xCE, 0x3B,
      						0xE3, 0x9E, 0x77, 0x2C, 0x18, 0x0E, 0x86, 0x03, 0x9B, 0x27, 0x83, 0xA2, 0xEC, 0x07, 0xA2, 0x8F, 0xB5, 0xC5, 0x5D, 0xF0, 0x6F, 0x4C, 0x52, 0xC9,
      						0xDE, 0x2B, 0xCB, 0xF6, 0x95, 0x58, 0x17, 0x18, 0x39, 0x95, 0x49, 0x7C, 0xEA, 0x95, 0x6A, 0xE5, 0x15, 0xD2, 0x26, 0x18, 0x98, 0xFA, 0x05, 0x10,
      				    	0x15, 0x72, 0x8E, 0x5A, 0x8A, 0xAC, 0xAA, 0x68, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF }; /*From 'RFC 3526', 2048-bit MODP Group*/


SET_TA_PROPERTIES(
	{ 0x316cd877, 0xebae, 0x59d2, {0xa7,0x66,0x6c,0xce,0x17,0xe1,0xd4,0x71} }, /* UUID */
		512, /* dataSize */
		1028, /* stackSize */
		0, /* singletonInstance */
		1, /* multiSession */
0) /* instanceKeepAlive */

/*Length of the sysmmetric secret key(in bits) to be generated*/
uint32_t key_length;

/*Key Id*/
uint32_t key_id = 3;

/*Entry point for the TA*/
TEE_Result TA_EXPORT TA_CreateEntryPoint(void){
	
	OT_LOG(LOG_DEBUG, "Calling entry point for EKS_TA");
	return TEE_SUCCESS;
}

static TEE_Result validate_param_type( uint32_t paramTypes, uint32_t command_id )
{

	/* Check parameter type if command is INITIATE_DH_CMD*/
	if( INITIATE_DH_CMD == command_id ){

		if ( TEE_PARAM_TYPE_MEMREF_OUTPUT != TEE_PARAM_TYPE_GET(paramTypes, 0) ){

			OT_LOG(LOG_ERR, "For command INITIATE_DH_CMD first parameter type is incorrect");
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_MEMREF_OUTPUT != TEE_PARAM_TYPE_GET(paramTypes, 1) ){

			OT_LOG(LOG_ERR, "For command INITIATE_DH_CMD second parameter type is incorrect");
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_MEMREF_OUTPUT != TEE_PARAM_TYPE_GET(paramTypes, 2) ){

			OT_LOG(LOG_ERR, "For command INITIATE_DH_CMD third parameter type is incorrect");
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_VALUE_INOUT != TEE_PARAM_TYPE_GET(paramTypes, 3) ){

			OT_LOG(LOG_ERR, "For command INITIATE_DH_CMD forth parameter type is incorrect");
			return TEE_ERROR_BAD_PARAMETERS;
		}
	}
	
	/* Check parameter type if command is RESPOND_DH_CMD*/
	if( RESPOND_DH_CMD == command_id ){

		if ( TEE_PARAM_TYPE_MEMREF_OUTPUT != TEE_PARAM_TYPE_GET(paramTypes, 0) ){

			OT_LOG(LOG_ERR, "For command INITIATE_DH_CMD first parameter type is incorrect");
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_MEMREF_OUTPUT != TEE_PARAM_TYPE_GET(paramTypes, 1) ){

			OT_LOG(LOG_ERR, "For command INITIATE_DH_CMD second parameter type is incorrect");
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_MEMREF_OUTPUT != TEE_PARAM_TYPE_GET(paramTypes, 2) ){

			OT_LOG(LOG_ERR, "For command INITIATE_DH_CMD third parameter type is incorrect");
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_VALUE_INOUT != TEE_PARAM_TYPE_GET(paramTypes, 3) ){

			OT_LOG(LOG_ERR, "For command INITIATE_DH_CMD forth parameter type is incorrect");
			return TEE_ERROR_BAD_PARAMETERS;
		}
	}

	/* Check parameter type if command is COMPLETE_DH_CMD*/
	if( COMPLETE_DH_CMD == command_id ){

		if ( TEE_PARAM_TYPE_MEMREF_OUTPUT != TEE_PARAM_TYPE_GET(paramTypes, 0) ){

			OT_LOG(LOG_ERR, "For command INITIATE_DH_CMD first parameter type is incorrect");
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_MEMREF_OUTPUT != TEE_PARAM_TYPE_GET(paramTypes, 1) ){

			OT_LOG(LOG_ERR, "For command INITIATE_DH_CMD second parameter type is incorrect");
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_MEMREF_OUTPUT != TEE_PARAM_TYPE_GET(paramTypes, 2) ){

			OT_LOG(LOG_ERR, "For command INITIATE_DH_CMD third parameter type is incorrect");
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_VALUE_INOUT != TEE_PARAM_TYPE_GET(paramTypes, 3) ){

			OT_LOG(LOG_ERR, "For command INITIATE_DH_CMD forth parameter type is incorrect");
			return TEE_ERROR_BAD_PARAMETERS;
		}
	}

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

	TEE_Result ret;
	TEE_ObjectHandle trs_key_obj_hdl;
	TEE_Attribute attrs[2];
	uint8_t public_value[256] = {0};
	uint32_t pv_len = 256;

	if( INITIATE_DH_CMD == commandID ){

		OT_LOG(LOG_INFO, "INITIATE_DH_CMD command invoked for EKS_TA");
		
		//key_id = 1;

		/*Validate parameter type*/
		ret = validate_param_type( paramTypes, INITIATE_DH_CMD );
		if( TEE_SUCCESS != ret )
			goto error;

		/*copy g to parameter 2*/
		TEE_MemMove( params[1].memref.buffer, (void *) g, g_size );

		/*copy g to parameter 23*/
		TEE_MemMove( params[2].memref.buffer, (void *) p, p_size );

		/*copy key_id to parameter 4 a*/
		params[3].value.a = key_id;

		/*retrieve key_len from parameter 4 b*/
		key_length = params[3].value.b;

		OT_LOG(LOG_INFO, "Test: INITIATE_DH_CMD: Key_len = %u   g = 0x%x%x%x%x    p = 0x%x%x%x%x%x%x%x%x  key_id %u  key_len %u\n", key_length, g[0], g[1], g[2], g[3], p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], params[3].value.a, params[3].value.b ); 

		
		/*create transient object for DH*/
		ret = TEE_AllocateTransientObject( TEE_TYPE_DH_KEYPAIR, key_length, &trs_key_obj_hdl );
		if( TEE_SUCCESS != ret )
			goto error;

		/*generate a DH public and private keys and populate the transient object*/
		TEE_InitRefAttribute(&attrs[0], TEE_ATTR_DH_PRIME, &p, g_size);
		TEE_InitRefAttribute(&attrs[1], TEE_ATTR_DH_BASE, &g, g_size);
		//TEE_InitValueAttribute(&attrs[2], TEE_ATTR_DH_X_BITS, xBits, 0);
		TEE_GenerateKey( trs_key_obj_hdl, key_length, attrs, sizeof( attrs )/sizeof( TEE_Attribute ) );

		/*Get public value*/
		ret = TEE_GetObjectBufferAttribute( trs_key_obj_hdl, TEE_ATTR_DH_PUBLIC_VALUE, public_value, &pv_len );
		if( TEE_SUCCESS != ret )
			goto error;
		
	
		OT_LOG(LOG_INFO, "Test: INITIATE_DH_CMD: pv = 0x%x%x%x%x%x%x%x%x%x%x%x%x    pv_len = %u \n", public_value[0], public_value[1], public_value[2], public_value[3], public_value[4], public_value[5], public_value[6], public_value[7], public_value[8], public_value[9], public_value[10], public_value[11], pv_len ); 
				
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

	error:
		return ret;
}
