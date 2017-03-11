
#include "tee_internal_api.h"
#include "tee_logging.h"
#include "tee_ta_properties.h"
#include <string.h>

#define INITIATE_DH_CMD    0x00000001
#define RESPOND_DH_CMD     0x00000002
#define COMPLETE_DH_CMD    0x00000003
#define G_SIZE 4
#define P_2048_SIZE 256

uint8_t g[] = { 0x00, 0x00, 0x00, 0x02 };

uint8_t p_2048[] = {	0xD3,0x37,0xA9,0x85,0x96,0xF8,0x1E,0x8B,0x88,0xC3,0x1B,0x07,
						0x07,0x7D,0xF1,0x4A,0x79,0x7C,0xCB,0xE3,0x58,0x55,0x2C,0x6C,
						0x85,0xD2,0x56,0xF9,0xA1,0x80,0x04,0xFB,0x56,0xD0,0x7D,0xE9,
						0xB1,0x63,0x88,0x66,0x43,0x2C,0x94,0x33,0x9F,0x6B,0xBD,0xA6,
						0x7C,0xA7,0x5B,0x70,0x97,0x36,0xC3,0x7F,0x51,0xB6,0x4E,0xD9,
						0xB2,0x2A,0xBC,0x22,0x64,0x50,0x91,0x42,0xEC,0x4C,0x66,0x7B,
						0x08,0xCC,0xA5,0x11,0x00,0x43,0xFB,0xA3,0xF6,0x67,0x43,0x3E,
						0xE7,0x92,0xFA,0x44,0xF3,0xC3,0xEC,0x0C,0x06,0xFB,0x9D,0x83,
						0xE6,0xD5,0xE2,0xE5,0x79,0x41,0xBF,0x75,0xD1,0xB4,0x5E,0x2A,
						0x75,0xE3,0x54,0x1E,0x7A,0x21,0xF8,0x8D,0xDE,0xC3,0xCF,0x55,
						0x80,0x75,0xD6,0xD5,0x4A,0x13,0x1A,0x32,0xE1,0x72,0x52,0x54,
						0x42,0xB3,0x56,0x69,0x54,0x85,0x40,0x43,0x33,0x79,0xF4,0x73,
						0xFC,0xC7,0x2B,0xBF,0xC5,0xE7,0x4D,0x02,0x8B,0x32,0x22,0x61,
						0xF1,0x67,0x2F,0x4E,0xDC,0xDA,0x60,0x39,0x58,0x74,0xCA,0xA7,
						0x35,0x39,0xB9,0xB7,0x52,0x34,0x47,0x40,0x99,0xBB,0x7C,0xCB,
						0x0A,0x2F,0xEF,0xD6,0x9C,0x47,0x68,0xB6,0x66,0x75,0x86,0x40,
						0xCE,0xB4,0x3A,0x73,0x62,0x1B,0x60,0x2A,0xB1,0x94,0x27,0x9E,
						0xB4,0xFC,0x42,0x2C,0x5B,0x60,0x9C,0xAF,0xA6,0x52,0x53,0x91,
						0x5F,0xE8,0x6C,0x70,0xC6,0x07,0xB1,0x0F,0xEB,0xA6,0xE5,0x61,
						0x1C,0x7E,0xB1,0x91,0x50,0x2B,0x70,0x06,0xAB,0xA6,0x7E,0x9C,
						0xB0,0x58,0x11,0xA2,0x42,0x70,0x32,0xA1,0x44,0x01,0xFD,0x9C,
						0x23,0xB7,0x2F,0x53 }; /*Generated using the following commad ' openssl dhparam -C 2048 '*/




SET_TA_PROPERTIES(
	{ 0x316cd877, 0xebae, 0x59d2, {0xa7,0x66,0x6c,0xce,0x17,0xe1,0xd4,0x71} }, /* UUID */
		512, /* dataSize */
		1028, /* stackSize */
		0, /* singletonInstance */
		1, /* multiSession */
0) /* instanceKeepAlive */

/*Length of the sysmmetric secret key(in bits) to be generated*/
uint32_t key_length;

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

		if ( TEE_PARAM_TYPE_MEMREF_INOUT != TEE_PARAM_TYPE_GET(paramTypes, 0) ){

			OT_LOG(LOG_ERR, "For command RESPOND_DH_CMD first parameter type is incorrect");
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_MEMREF_INPUT != TEE_PARAM_TYPE_GET(paramTypes, 1) ){

			OT_LOG(LOG_ERR, "For command RESPOND_DH_CMD second parameter type is incorrect");
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_MEMREF_INPUT != TEE_PARAM_TYPE_GET(paramTypes, 2) ){

			OT_LOG(LOG_ERR, "For command RESPOND_DH_CMD third parameter type is incorrect");
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_MEMREF_INOUT != TEE_PARAM_TYPE_GET(paramTypes, 3) ){

			OT_LOG(LOG_ERR, "For command RESPOND_DH_CMD fourth parameter type is incorrect");
			return TEE_ERROR_BAD_PARAMETERS;
		}
	}

	/* Check parameter type if command is COMPLETE_DH_CMD*/
	if( COMPLETE_DH_CMD == command_id ){

		if ( TEE_PARAM_TYPE_MEMREF_INPUT != TEE_PARAM_TYPE_GET( paramTypes, 0 ) ){

			OT_LOG( LOG_ERR, "For command COMPLETE_DH_CMD first parameter type is incorrect" );
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_MEMREF_OUTPUT != TEE_PARAM_TYPE_GET( paramTypes, 1 ) ){

			OT_LOG( LOG_ERR, "For command COMPLETE_DH_CMD second parameter type is incorrect" );
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_NONE != TEE_PARAM_TYPE_GET(paramTypes, 2 ) ){

			OT_LOG( LOG_ERR, "For command COMPLETE_DH_CMD third parameter type is incorrect" );
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_NONE != TEE_PARAM_TYPE_GET(paramTypes, 3 ) ){

			OT_LOG( LOG_ERR, "For command COMPLETE_DH_CMD forth parameter type is incorrect" );
			return TEE_ERROR_BAD_PARAMETERS;
		}
	}

	return TEE_SUCCESS;
}

/*generate a DH public and private keys and populate the transient object*/
TEE_Result generate_public_key( TEE_ObjectHandle *trs_key_obj_hdl, uint32_t key_length, uint8_t *prime, uint32_t prime_size, uint8_t *g, uint32_t g_size ){

	TEE_Attribute attrs[2];

	if( ( NULL == *trs_key_obj_hdl ) || ( NULL == prime ) || ( NULL == g ) ){

		OT_LOG( LOG_INFO, "IN FUNCTION generate_public_key: NULL parameter passed\n");
		return TEE_ERROR_BAD_PARAMETERS;
	}

	TEE_InitRefAttribute(&attrs[0], TEE_ATTR_DH_PRIME, prime, prime_size );
	TEE_InitRefAttribute(&attrs[1], TEE_ATTR_DH_BASE, g, G_SIZE);
	if( TEE_SUCCESS != TEE_GenerateKey( *trs_key_obj_hdl, key_length, attrs, sizeof( attrs )/sizeof( TEE_Attribute ) ) ){

		OT_LOG( LOG_INFO, "IN FUNCTION generate_public_key: Bad parameter given to TEE_GenerateKey\n");
		return TEE_ERROR_BAD_PARAMETERS;
	}

	return TEE_SUCCESS;
}

/*generate a DH secret key and populate the transient object*/
TEE_Result generate_secret_key( TEE_ObjectHandle *trs_key_obj_hdl, uint32_t key_length, uint8_t *prime, uint32_t prime_size, uint8_t *g, uint32_t g_size ){

	TEE_Attribute attrs[2];

	if( ( NULL == *trs_key_obj_hdl ) || ( NULL == prime ) || ( NULL == g ) ){

		OT_LOG( LOG_INFO, "IN FUNCTION generate_public_key: NULL parameter passed\n");
		return TEE_ERROR_BAD_PARAMETERS;
	}

	TEE_InitRefAttribute(&attrs[0], TEE_ATTR_DH_PRIME, prime, prime_size );
	TEE_InitRefAttribute(&attrs[1], TEE_ATTR_DH_BASE, g, G_SIZE);
	if( TEE_SUCCESS != TEE_GenerateKey( *trs_key_obj_hdl, key_length, attrs, sizeof( attrs )/sizeof( TEE_Attribute ) ) ){

		OT_LOG( LOG_INFO, "IN FUNCTION generate_public_key: Bad parameter given to TEE_GenerateKey\n");
		return TEE_ERROR_BAD_PARAMETERS;
	}

	return TEE_SUCCESS;
}

/*Generate key_id*/
void generate_key_id( uint8_t key_id[8]){

	TEE_Time time;

	TEE_GetSystemTime( &time );

	TEE_MemMove( key_id, &(time.seconds), 4 );
	TEE_MemMove( key_id + 4, &(time.millis), 4 );
}


/*Create persistent object and store the attribute found in transient object 'attrbs_obj'*/
TEE_Result creat_prs_obj( uint8_t key_id[8], TEE_ObjectHandle attrbs_obj ){

	TEE_Result ret;
	TEE_ObjectHandle prs_obj_hndl;
	size_t check;

	/*create key_id (object id for the persistent object)*/
	generate_key_id( key_id);

	/*TEST*/
	OT_LOG( LOG_INFO, "TEST: KEY_ID = %02x%02x%02x%02x%02x%02x%02x%02x\n", key_id[0], key_id[1], key_id[2], key_id[3], key_id[4], key_id[5], key_id[6], key_id[7] );

	/*Create persistent object to store the shared key*/
	ret = TEE_CreatePersistentObject( TEE_STORAGE_PRIVATE, key_id, 8, 0, attrbs_obj, NULL, 0,  &prs_obj_hndl );
	if( TEE_ERROR_ACCESS_CONFLICT == ret ){
			
		check = 0;
		do{
	OT_LOG( LOG_INFO, "TEST: check: %zu KEY_ID = %02x%02x%02x%02x%02x%02x%02x%02x\n", check, key_id[0], key_id[1], key_id[2], key_id[3], key_id[4], key_id[5], key_id[6], key_id[7] );
			if( 100 == check ) 
				goto error;

			TEE_Wait( 5 );

			generate_key_id( key_id);
	
			ret = TEE_CreatePersistentObject( TEE_STORAGE_PRIVATE, key_id, 8, 0, attrbs_obj, NULL, 0,  &prs_obj_hndl );

			check ++;				
				
		}while( TEE_ERROR_ACCESS_CONFLICT == ret );
	}
	else if( TEE_SUCCESS != ret )
		goto error;	

	return TEE_SUCCESS;

	error:
		return ret;
}

/*Generate shared secret*/
TEE_Result generate_shared_secret( TEE_ObjectHandle *trs_secret_key_obj_hdl, uint32_t key_length, uint8_t * public_value_received, uint32_t public_received_len, TEE_ObjectHandle trs_key_obj_hdl  ){

	TEE_Result ret;
	TEE_OperationHandle sk_generation;
	uint8_t shared_key[ 256 ] = { 0 };
	uint32_t shared_key_len = 256;
	TEE_Attribute attr_secret_val;
	TEE_Attribute attr_public_val;

	if( ( NULL == trs_secret_key_obj_hdl ) || ( NULL == public_value_received ) )
		return TEE_ERROR_BAD_PARAMETERS;

	/*Create secret key generation operation*/
	ret = TEE_AllocateOperation( &sk_generation, TEE_ALG_DH_DERIVE_SHARED_SECRET, TEE_MODE_DERIVE, key_length );
	if( TEE_SUCCESS != ret )
		goto error;	

	/*Create transient object to hold the shared secret key*/
	ret = TEE_AllocateTransientObject( TEE_TYPE_GENERIC_SECRET, key_length, trs_secret_key_obj_hdl );
	if( TEE_SUCCESS != ret )
		goto error;

	/*populate transient object with TEE_ATTR_SECRET_VALUE attribute*/
	TEE_InitRefAttribute( &attr_secret_val, TEE_ATTR_SECRET_VALUE, shared_key, shared_key_len );
	ret = TEE_PopulateTransientObject( *trs_secret_key_obj_hdl, &attr_secret_val, 1 );
	if( TEE_SUCCESS != ret )
		goto error;
		
	/*Set private value in to operation*/
	ret = TEE_SetOperationKey( sk_generation, trs_key_obj_hdl );
	if( TEE_SUCCESS != ret )
		goto error;

	/*Set the received public value*/
	TEE_InitRefAttribute( &attr_public_val, TEE_ATTR_DH_PUBLIC_VALUE, public_value_received, public_received_len );

	/*Derive shared secret key*/
	TEE_DeriveKey( sk_generation, &attr_public_val, 1, *trs_secret_key_obj_hdl );

	/******************************************************** Test  ***********************************************************************************/
	OT_LOG( LOG_INFO, "TEST: shared key= %02x %02x %02x %02x %02x %02x %02x %02x\n", shared_key[0],  shared_key[1], shared_key[2], shared_key[124], shared_key[125], shared_key[253], shared_key[254],  shared_key[255] );
	/*Get private value*/
	ret = TEE_GetObjectBufferAttribute( *trs_secret_key_obj_hdl, TEE_ATTR_SECRET_VALUE, shared_key, &shared_key_len );
	if( TEE_SUCCESS != ret )
		goto error;
	OT_LOG( LOG_INFO, "TEST: shared key= %02x %02x %02x %02x %02x %02x %02x %02x\n", shared_key[0],  shared_key[1], shared_key[2], shared_key[124], shared_key[125], shared_key[253], shared_key[254],  shared_key[255] );
	/************************************************************ End test ******************************************************************************/
	
	return TEE_SUCCESS;

	error:
		return ret;
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
	uint8_t public_value[256] = {0};
	uint32_t pv_len = 256;	
	uint8_t *prime;
	uint32_t prime_size;
	uint8_t key_id[8];

	/*transient object to store DH parameters temporarly*/
	TEE_ObjectHandle trs_key_obj_hdl;
	TEE_ObjectHandle trs_secret_key_obj_hdl;

	uint8_t prime_received[256] = { 0 };
	uint8_t public_value_received[256] = {0};
	uint8_t generator_received[4] = { 0 };
	uint32_t prime_received_len;
	uint32_t public_received_len;
	
	if( INITIATE_DH_CMD == commandID ){

		OT_LOG(LOG_INFO, "INITIATE_DH_CMD command invoked for EKS_TA");
		
		/*Validate parameter type*/
		ret = validate_param_type( paramTypes, INITIATE_DH_CMD );
		if( TEE_SUCCESS != ret )
			goto error;

		/*copy g to parameter 2*/
		TEE_MemMove( params[1].memref.buffer, (void *) g, G_SIZE );

		/*copy g to parameter 23*/
		TEE_MemMove( params[2].memref.buffer, (void *) p_2048, P_2048_SIZE );

		/*retrieve key_len from parameter 4 b*/
		key_length = params[3].value.b;

		/*choose a prime number based on key length provided*/
		if( 2048 == key_length ){

			prime = p_2048;
			prime_size = P_2048_SIZE;
		}
		else{

			OT_LOG(LOG_INFO, "Key length not supported");
			return TEE_ERROR_BAD_PARAMETERS;
		}
		
		/*create transient object for DH*/
		ret = TEE_AllocateTransientObject( TEE_TYPE_DH_KEYPAIR, key_length, &trs_key_obj_hdl );
		if( TEE_SUCCESS != ret )
			goto error;

		/*Generate public key*/
		if( TEE_SUCCESS != ( ret = generate_public_key( &trs_key_obj_hdl, key_length, prime, prime_size, g, G_SIZE ) ) )
			goto error;

		/*Get public value*/
		ret = TEE_GetObjectBufferAttribute( trs_key_obj_hdl, TEE_ATTR_DH_PUBLIC_VALUE, public_value, &pv_len );
		if( TEE_SUCCESS != ret )
			goto error;	

		/*Set public value in to parameter for Client application*/
		TEE_MemMove( params[0].memref.buffer,  public_value, pv_len );			
				
	} 
	else if( RESPOND_DH_CMD == commandID ){

		OT_LOG(LOG_INFO, "RESPOND_DH_CMD command invoked for EKS_TA"); 

		/*Validate parameter type*/
		ret = validate_param_type( paramTypes,  RESPOND_DH_CMD );
		if( TEE_SUCCESS != ret )
			goto error;

		/*retrieve key_len from parameter */
		TEE_MemMove( &key_length, params[3].memref.buffer, 4 );

		/*retrieve generator value*/
		if( G_SIZE != params[1].memref.size )	return TEE_ERROR_BAD_PARAMETERS;
		TEE_MemMove( generator_received, params[1].memref.buffer, G_SIZE );

		/*retrieve public value*/
		public_received_len = params[0].memref.size;
		TEE_MemMove( public_value_received, params[0].memref.buffer, public_received_len );		

		/*retrieve prime value*/
		prime_received_len = params[2].memref.size;
		TEE_MemMove( prime_received, params[2].memref.buffer, prime_received_len );

		/*Test: generator value*/
		OT_LOG(LOG_INFO, "TEST: RECEIVED genrator_VALUE = %02x %02x %02x %02x\n", generator_received[0], generator_received[1], generator_received[2], generator_received[3] );
		/*Test: show received public value*/
		OT_LOG(LOG_INFO, "TEST: RECEIVED PUBLIC_VALUE = %02x %02x %02x %02x %02x %02x %02x %02x\n", public_value_received[0], public_value_received[1],public_value_received[2],public_value_received[124],public_value_received[125],public_value_received[253],public_value_received[254],public_value_received[255]);
		/*Test: show received prime value*/
		OT_LOG( LOG_INFO, "TEST: RECEIVED PRIME_VALUE = %02x %02x %02x %02x %02x %02x %02x %02x\n", prime_received[0], prime_received[1],prime_received[2],prime_received[124],prime_received[125], prime_received[253], prime_received[254], prime_received[255] );

		/*create transient object for DH*/
		ret = TEE_AllocateTransientObject( TEE_TYPE_DH_KEYPAIR, key_length, &trs_key_obj_hdl );
		if( TEE_SUCCESS != ret )
			goto error;

		/*Generate public key*/
		if( TEE_SUCCESS != ( ret = generate_public_key( &trs_key_obj_hdl, key_length, prime_received, prime_received_len, generator_received, G_SIZE ) ) )
			goto error;

		/*Get public value*/
		ret = TEE_GetObjectBufferAttribute( trs_key_obj_hdl, TEE_ATTR_DH_PUBLIC_VALUE, public_value, &pv_len );
		if( TEE_SUCCESS != ret )
			goto error;	

		/*Set public value in to shared memory for Client application*/
		TEE_MemMove( params[0].memref.buffer,  public_value, pv_len );

		/*Generate shared secret key*/
		ret = generate_shared_secret( &trs_secret_key_obj_hdl, key_length, public_value_received, public_received_len, trs_key_obj_hdl );
		if( TEE_SUCCESS != ret )
			goto error;

		/*Create a persistent object and store the shared secret key*/
		ret = creat_prs_obj( key_id, trs_secret_key_obj_hdl );
		if( TEE_SUCCESS != ret )
			goto error;

		/*set key_id into shared memory*/
		TEE_MemMove( params[3].memref.buffer, key_id, 8 );

	}
	else if(  COMPLETE_DH_CMD == commandID ){

		OT_LOG(LOG_INFO, "COMPLETE_DH_CMD command invoked for EKS_TA");	
		
		/*Validate parameter type*/
		ret = validate_param_type( paramTypes,  COMPLETE_DH_CMD );
		if( TEE_SUCCESS != ret )
			goto error;			

		/*Retrieve public value*/
		public_received_len = params[0].memref.size;
		TEE_MemMove( public_value_received, params[0].memref.buffer, public_received_len );

		/*Test: show received public value*/
		OT_LOG(LOG_INFO, "TEST: RECEIVED PUBLIC_VALUE = %02x %02x %02x %02x %02x %02x %02x %02x\n", public_value_received[0], public_value_received[1],public_value_received[2],public_value_received[124],public_value_received[125],public_value_received[253],public_value_received[254],public_value_received[255]);

		/*Generate shared secret key*/
		ret = generate_shared_secret( &trs_secret_key_obj_hdl, key_length, public_value_received, public_received_len, trs_key_obj_hdl );
		if( TEE_SUCCESS != ret )
			goto error;

		/*Create a persistent object and store the shared secret key*/
		ret = creat_prs_obj( key_id, trs_secret_key_obj_hdl );
		if( TEE_SUCCESS != ret )
			goto error;

		/*set key_id into shared memory*/
		TEE_MemMove( params[1].memref.buffer, key_id, 8 );

	}
	else{

		OT_LOG(LOG_INFO, "Invalid command invoked for EKS_TA");
		return TEE_ERROR_BAD_PARAMETERS;		
 	}
  
	return TEE_SUCCESS;

	error:
		return ret;
}
