
#include "tee_internal_api.h"
#include "tee_logging.h"
#include "tee_ta_properties.h"

#define INITIATE_DH_CMD 	   0x00000001
#define RESPOND_DH_CMD  	   0x00000002
#define COMPLETE_DH_CMD 	   0x00000003
#define ENCRYPT_AES_CMD		   0x00000004
#define DECRYPT_AES_CMD		   0x00000005
#define GEN_SIZE		   4
#define PRIME_SIZE	           256
#define SUPPORTED_KEY_LEN	   256
#define MAX_KEY_LEN_SUPP	   256
#define AES_BLOCK_SIZE		   16
#define BUFFER_PLAIN_TEXT_SIZE	   AES_BLOCK_SIZE * 16
#define BUFFER_ENCRYPTED_SIZE	   AES_BLOCK_SIZE * 16
#define BUFFER_DECRYPTED_SIZE	   AES_BLOCK_SIZE * 16
#define KEY_ID_SIZE		   8
#define RAND_VALUE_LEN		   8

uint8_t gn[ GEN_SIZE ] = { 0x00, 0x00, 0x00, 0x02 };

uint8_t prime[ PRIME_SIZE] = {	0xD3,0x37,0xA9,0x85,0x96,0xF8,0x1E,0x8B,0x88,0xC3,0x1B,0x07,
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
		0 /* instanceKeepAlive */)

/*transient object to store DH parameters temporarly*/
TEE_ObjectHandle trs_pub_key_obj_hdl;

/*Length of the sysmmetric secret key(in bits) to be generated*/
uint32_t key_length;

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

			OT_LOG(LOG_ERR, "For command INITIATE_DH_CMD fourth parameter type is incorrect");
			return TEE_ERROR_BAD_PARAMETERS;
		}
	}
	
	/* Check parameter type if command is RESPOND_DH_CMD*/
	else if( RESPOND_DH_CMD == command_id ){

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
	else if( COMPLETE_DH_CMD == command_id ){

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

			OT_LOG( LOG_ERR, "For command COMPLETE_DH_CMD fourth parameter type is incorrect" );
			return TEE_ERROR_BAD_PARAMETERS;
		}
	}
	/* Check parameter type if command is ENCRYPT_AES_CMD*/
	else if( ENCRYPT_AES_CMD == command_id ){

		if ( TEE_PARAM_TYPE_MEMREF_INPUT != TEE_PARAM_TYPE_GET( paramTypes, 0 ) ){

			OT_LOG( LOG_ERR, "For command ENCRYPT_AES_CMD first parameter type is incorrect" );
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_MEMREF_OUTPUT != TEE_PARAM_TYPE_GET( paramTypes, 1 ) ){

			OT_LOG( LOG_ERR, "For command ENCRYPT_AES_CMD second parameter type is incorrect" );
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_MEMREF_OUTPUT != TEE_PARAM_TYPE_GET(paramTypes, 2 ) ){

			OT_LOG( LOG_ERR, "For command ENCRYPT_AES_CMD third parameter type is incorrect" );
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_MEMREF_INPUT != TEE_PARAM_TYPE_GET(paramTypes, 3 ) ){

			OT_LOG( LOG_ERR, "For command ENCRYPT_AES_CMD fourth parameter type is incorrect" );
			return TEE_ERROR_BAD_PARAMETERS;
		}
	}
	/* Check parameter type if command is ENCRYPT_AES_CMD*/
	else if( DECRYPT_AES_CMD == command_id ){

		if ( TEE_PARAM_TYPE_MEMREF_INPUT != TEE_PARAM_TYPE_GET( paramTypes, 0 ) ){

			OT_LOG( LOG_ERR, "For command DECRYPT_AES_CMD first parameter type is incorrect" );
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_MEMREF_OUTPUT != TEE_PARAM_TYPE_GET( paramTypes, 1 ) ){

			OT_LOG( LOG_ERR, "For command DECRYPT_AES_CMD second parameter type is incorrect" );
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_MEMREF_INPUT != TEE_PARAM_TYPE_GET(paramTypes, 2 ) ){

			OT_LOG( LOG_ERR, "For command DECRYPT_AES_CMD third parameter type is incorrect" );
			return TEE_ERROR_BAD_PARAMETERS;
		}

		if ( TEE_PARAM_TYPE_MEMREF_INPUT != TEE_PARAM_TYPE_GET(paramTypes, 3 ) ){

			OT_LOG( LOG_ERR, "For command DECRYPT_AES_CMD fourth parameter type is incorrect" );
			return TEE_ERROR_BAD_PARAMETERS;
		}
	}

	return TEE_SUCCESS;
}

/*generate a DH public and private keys and populate the transient object*/
TEE_Result generate_public_key( uint32_t key_length, uint8_t *prime, uint32_t prime_size, uint8_t *g ){

	TEE_Attribute attrs[2];

	if( ( NULL == prime ) || ( NULL == g ) ){

		OT_LOG( LOG_INFO, "IN FUNCTION generate_public_key: NULL parameter passed\n");
		return TEE_ERROR_BAD_PARAMETERS;
	}

	TEE_InitRefAttribute(&attrs[0], TEE_ATTR_DH_PRIME, prime, prime_size );
	TEE_InitRefAttribute(&attrs[1], TEE_ATTR_DH_BASE, g, GEN_SIZE);
	if( TEE_SUCCESS != TEE_GenerateKey( trs_pub_key_obj_hdl, key_length, attrs, sizeof( attrs )/sizeof( TEE_Attribute ) ) ){

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

	uint8_t shared_key[ 32 ] = { 0 };
	uint32_t shared_key_len = 32;

	/*create key_id (object id for the persistent object)*/
	generate_key_id( key_id);

	/*extract shared key from transient object*/
	ret = TEE_GetObjectBufferAttribute( attrbs_obj, TEE_ATTR_SECRET_VALUE, shared_key, &shared_key_len );
	if( TEE_SUCCESS != ret )
		return ret;

	/*Create persistent object to store the shared key*/
	ret = TEE_CreatePersistentObject( TEE_STORAGE_PRIVATE, key_id, 8, 0, attrbs_obj, NULL, 0,  &prs_obj_hndl );

	if( TEE_ERROR_ACCESS_CONFLICT == ret ){
			
		check = 0;
		do{

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

	TEE_CloseObject( prs_obj_hndl );

	return TEE_SUCCESS;

	error:
		return ret;
}

/*Generate shared secret*/
TEE_Result generate_shared_secret( TEE_ObjectHandle *trs_secret_key_obj_hdl, uint32_t key_length, uint8_t * public_value_received, uint32_t public_received_len ){

	TEE_Result ret;
	TEE_OperationHandle sk_generation;
	uint8_t shared_key[ PRIME_SIZE ] = { 0 };
	uint32_t shared_key_len = PRIME_SIZE;
	TEE_Attribute attr_secret_val;
	TEE_Attribute attr_public_val;

	if( ( NULL == trs_secret_key_obj_hdl ) || ( NULL == public_value_received ) )
		return TEE_ERROR_BAD_PARAMETERS;

	/*Create secret key generation operation*/
	ret = TEE_AllocateOperation( &sk_generation, TEE_ALG_DH_DERIVE_SHARED_SECRET, TEE_MODE_DERIVE, key_length );
	if( TEE_SUCCESS != ret )
		goto error;	

	/*allocate a transient object to hold the shared secret key*/
	ret = TEE_AllocateTransientObject( TEE_TYPE_GENERIC_SECRET, key_length, trs_secret_key_obj_hdl );
	if( TEE_SUCCESS != ret )
		goto error;

	/*populate transient object with TEE_ATTR_SECRET_VALUE attribute*/
	TEE_InitRefAttribute( &attr_secret_val, TEE_ATTR_SECRET_VALUE, shared_key, shared_key_len );
	ret = TEE_PopulateTransientObject( *trs_secret_key_obj_hdl, &attr_secret_val, 1 );
	if( TEE_SUCCESS != ret )
		goto error;

	/*Set private value in to operation*/
	ret = TEE_SetOperationKey( sk_generation, trs_pub_key_obj_hdl );
	if( TEE_SUCCESS != ret )
		goto error;

	/*Set the received public value*/
	TEE_InitRefAttribute( &attr_public_val, TEE_ATTR_DH_PUBLIC_VALUE, public_value_received, public_received_len );

	/*Derive shared secret key*/
	TEE_DeriveKey( sk_generation, &attr_public_val, 1, *trs_secret_key_obj_hdl );

	return TEE_SUCCESS;

	error:
		return ret;
}

/*Extracts a shared secret key from transient object 'trs_source' and hash it to a length of 'key_length' and set the resulting hash in to uninitialized transient object 'trs_store'*/
TEE_Result hash_shared_key(  TEE_ObjectHandle *trs_source, uint32_t key_length, TEE_ObjectHandle *trs_store ){

	uint8_t shared_key_received[ PRIME_SIZE ] = { 0 };
	uint32_t shared_key_rec_len = PRIME_SIZE;
	uint8_t hash[ MAX_KEY_LEN_SUPP / 8 ] = { 0 };
	uint32_t hash_len = key_length / 8;	
	uint8_t shared_key[ PRIME_SIZE ] = { 0 };
	uint32_t shared_key_len = PRIME_SIZE;
	TEE_OperationHandle operation;
	TEE_Attribute attr_secret_val;
	TEE_Result ret;

	if( ( NULL == trs_source ) || ( NULL == trs_store ) )
		return TEE_ERROR_BAD_PARAMETERS;

	if( SUPPORTED_KEY_LEN != key_length )
		return TEE_ERROR_BAD_PARAMETERS;

	ret = TEE_AllocateOperation( &operation, TEE_ALG_SHA256, TEE_MODE_DIGEST, 0 );
	if( TEE_SUCCESS != ret )
		return ret;
	
	/*extract shared key from transient object*/
	ret = TEE_GetObjectBufferAttribute( *trs_source, TEE_ATTR_SECRET_VALUE, shared_key, &shared_key_len );
	if( TEE_SUCCESS != ret )
		return ret;

	ret = TEE_DigestDoFinal( operation, shared_key_received, shared_key_rec_len, hash, &hash_len );

	/*allocated a transient object to store the hash (shared secret key)*/
	/*Create transient object to hold the shared secret key*/
	ret = TEE_AllocateTransientObject( TEE_TYPE_GENERIC_SECRET, key_length, trs_store );
	if( TEE_SUCCESS != ret )
		return ret;


	/*populate transient object with TEE_ATTR_SECRET_VALUE attribute*/
	TEE_InitRefAttribute( &attr_secret_val, TEE_ATTR_SECRET_VALUE, hash, hash_len );
	ret = TEE_PopulateTransientObject( *trs_store, &attr_secret_val, 1 );
	if( TEE_SUCCESS != ret )
		return ret;
	
	TEE_FreeOperation( operation );

	return TEE_SUCCESS;
}

/*Used to process initialize command */
TEE_Result init_cmd( TEE_Param params[4] ){

	TEE_Result ret;
	uint8_t public_value[ PRIME_SIZE ] = {0};
	uint32_t pb_len = PRIME_SIZE;

	/*retrieve key_len from parameter 4 b*/
	key_length = params[3].value.b;

	if( SUPPORTED_KEY_LEN != key_length )					
		return TEE_ERROR_BAD_PARAMETERS;		

	/*copy generator to parameter 2*/
	TEE_MemMove( params[1].memref.buffer, (void *) gn, GEN_SIZE );

	/*copy prime value to parameter 3*/
	TEE_MemMove( params[2].memref.buffer, (void *) prime, PRIME_SIZE );	

	/*create transient object for DH*/
	ret = TEE_AllocateTransientObject( TEE_TYPE_DH_KEYPAIR, PRIME_SIZE * 8, &trs_pub_key_obj_hdl );
	if( TEE_SUCCESS != ret )
		return ret;

	/*Generate public key*/
	if( TEE_SUCCESS != ( ret = generate_public_key( PRIME_SIZE * 8, prime,  PRIME_SIZE, gn ) ) )
		return ret;

	/*Get public value*/
	ret = TEE_GetObjectBufferAttribute( trs_pub_key_obj_hdl, TEE_ATTR_DH_PUBLIC_VALUE, public_value, &pb_len );
	if( TEE_SUCCESS != ret )
		return ret;	

	/*Set public value in to parameter for Client application*/
	TEE_MemMove( params[0].memref.buffer,  public_value, pb_len );	

	return TEE_SUCCESS;
}

/*Used to process respond command */
TEE_Result  respond_cmd( TEE_Param params[4] ){

	TEE_Result ret;
	uint32_t key_length;
	uint8_t key_id[ 8 ] = { 0 };
	uint8_t prime_received[PRIME_SIZE] = { 0 };
	uint32_t prime_received_len;
	uint8_t generator_received[ GEN_SIZE ] = { 0 };
	uint8_t public_value_received[PRIME_SIZE] = {0};
	uint32_t public_received_len;
	uint8_t public_value[ PRIME_SIZE ] = {0};
	uint32_t pb_len = PRIME_SIZE;

	TEE_ObjectHandle trs_secret_key_obj_hdl;	/*Holds a 2048 bit intermediate shared secrete key*/
	TEE_ObjectHandle trs_secret_key_store_hdl;		/*Holds the final shared secrete key which is of the requested size*/

	/*retrieve key_len from parameter */
	TEE_MemMove( &key_length, params[3].memref.buffer, 4 );

	if( SUPPORTED_KEY_LEN != key_length )					
		return TEE_ERROR_BAD_PARAMETERS;

	/*retrieve generator value*/
	if( GEN_SIZE != params[1].memref.size )	
		return TEE_ERROR_BAD_PARAMETERS;
	TEE_MemMove( generator_received, params[1].memref.buffer, GEN_SIZE );

	/*retrieve public value*/
	public_received_len = params[0].memref.size;
	TEE_MemMove( public_value_received, params[0].memref.buffer, public_received_len );		

	/*retrieve prime value*/
	prime_received_len = params[2].memref.size;
	TEE_MemMove( prime_received, params[2].memref.buffer, prime_received_len );
	if(  PRIME_SIZE != prime_received_len ){

		OT_LOG(LOG_INFO, " Prime size not supported. Only 2048 bit prime supported.\n" );
		return TEE_ERROR_BAD_PARAMETERS;
	}

	/*create transient object for DH*/
	ret = TEE_AllocateTransientObject( TEE_TYPE_DH_KEYPAIR, PRIME_SIZE * 8, &trs_pub_key_obj_hdl );
	if( TEE_SUCCESS != ret )
		return ret;
					
	/*Generate public key*/
	if( TEE_SUCCESS != ( ret = generate_public_key( PRIME_SIZE * 8, prime_received, prime_received_len, generator_received ) ) )
		return ret;

	/*Get public value*/
	ret = TEE_GetObjectBufferAttribute( trs_pub_key_obj_hdl, TEE_ATTR_DH_PUBLIC_VALUE, public_value, &pb_len );
	if( TEE_SUCCESS != ret )
		return ret;	

	/*Set public value in to shared memory for Client application*/
	TEE_MemMove( params[0].memref.buffer,  public_value, pb_len );

	/*Generate a 2048 bit shared secret key*/
	ret = generate_shared_secret( &trs_secret_key_obj_hdl, PRIME_SIZE * 8, public_value_received, public_received_len );
	if( TEE_SUCCESS != ret )
		return ret;

	/*Hash the 2048 bit generated shared secret key to generate a shared secret key of the requested length*/
	ret = hash_shared_key( &trs_secret_key_obj_hdl, key_length, &trs_secret_key_store_hdl );

	/*Create a persistent object and store the shared secret key*/
	ret = creat_prs_obj( key_id, trs_secret_key_store_hdl );
	if( TEE_SUCCESS != ret )
		return ret;

	/*set key_id into shared memory*/
	TEE_MemMove( params[3].memref.buffer, key_id, 8 );

	TEE_FreeTransientObject( trs_pub_key_obj_hdl );

	return TEE_SUCCESS;
}

/*Used to process complete command */
TEE_Result  complete_cmd( TEE_Param params[4] ){

	TEE_Result ret;
	uint8_t key_id[ 8 ] = { 0 };
	uint8_t public_value_received[PRIME_SIZE] = {0};
	uint32_t public_received_len;

	TEE_ObjectHandle trs_secret_key_obj_hdl;	/*Holds a 2048 bit intermediate shared secrete key*/
	TEE_ObjectHandle trs_secret_key_store_hdl;	/*Holds the final shared secrete key which is of the requested size*/

	/*Retrieve public value*/
	public_received_len = params[0].memref.size;
	TEE_MemMove( public_value_received, params[0].memref.buffer, public_received_len );

	/*Generate 2048 bit shared secret key*/
	ret = generate_shared_secret( &trs_secret_key_obj_hdl, PRIME_SIZE * 8, public_value_received, public_received_len );
	if( TEE_SUCCESS != ret )
		return ret;

	/*Hash the 2048 bit generated shared secret key to generate a shared secret key of the requested length*/
	ret = hash_shared_key( &trs_secret_key_obj_hdl, key_length, &trs_secret_key_store_hdl );

	/*Create a persistent object and store the shared secret key*/
	ret = creat_prs_obj( key_id, trs_secret_key_store_hdl );
	if( TEE_SUCCESS != ret )
		return ret;

OT_LOG(LOG_INFO, "TEST: key_id %02x%02x%02x%02x%02x%02x%02x%02x", key_id[0], key_id[1], key_id[2], key_id[3], key_id[4], key_id[5], key_id[6], key_id[7] );
	/*set key_id into shared memory*/
	TEE_MemMove( params[1].memref.buffer, key_id, KEY_ID_SIZE );

	return TEE_SUCCESS;
}

/*Used to process the encryption of plain text sent from CA*/
TEE_Result  encrypt( TEE_Param params[4] ){

	TEE_ObjectHandle key_obj_hdl;
	TEE_OperationHandle operation;
	TEE_Result ret;
		
	uint8_t key_id[ KEY_ID_SIZE ] = { 0 };
	uint8_t buffer_plain_text[ BUFFER_PLAIN_TEXT_SIZE ] = { 0 };	
	uint8_t buffer_encrypted[ BUFFER_ENCRYPTED_SIZE ] = { 0 };  /* holds up to 'AES_BLOCK_SIZE * 16' bytes of encrypted data, after this much data is decrypted this buffere is coppied to the
								     the output parameter and the buffer is reset in preparation to hold the next encrypted values*/
	uint8_t initial_counter_value[ AES_BLOCK_SIZE ] = { 0 };
	uint8_t random_value[ RAND_VALUE_LEN ] = { 0 };
	uint32_t plain_text_size;
	uint32_t encrypted_size;
	uint32_t copy_size;
	uint32_t index = 0;

	/*Retrieve key id*/
	TEE_MemMove( key_id, params[3].memref.buffer, KEY_ID_SIZE );

	/*open persistent object containing the syymetric key*/
	ret = TEE_OpenPersistentObject( TEE_STORAGE_PRIVATE, key_id, KEY_ID_SIZE, TEE_DATA_FLAG_ACCESS_READ, &key_obj_hdl );
	if( TEE_SUCCESS != ret )
		return ret;

	ret = TEE_AllocateOperation( &operation, TEE_ALG_AES_CTR, TEE_MODE_ENCRYPT, MAX_KEY_LEN_SUPP );
	if( TEE_SUCCESS != ret )
		return ret;

	/*set operation key*/
	ret = TEE_SetOperationKey( operation, key_obj_hdl );
	if( TEE_SUCCESS != ret )
		return ret;

	TEE_CloseObject( key_obj_hdl );	

	/*extract plain text size*/
	plain_text_size = params[0].memref.size;

	/* generate rendom value for initial counter value */
	TEE_GenerateRandom( random_value, RAND_VALUE_LEN );

	/*prepare initial counter value*/
	TEE_MemMove( initial_counter_value, random_value, RAND_VALUE_LEN); 

	/*Initialize the cipher*/
	TEE_CipherInit( operation, initial_counter_value, AES_BLOCK_SIZE );

	do{

		copy_size = plain_text_size - index;
		if( BUFFER_PLAIN_TEXT_SIZE < copy_size ){

			/*copy plain text*/
			TEE_MemMove( buffer_plain_text, params[0].memref.buffer + index, BUFFER_PLAIN_TEXT_SIZE );

			/*enrypt plain text*/
			ret = TEE_CipherDoFinal( operation, buffer_plain_text, BUFFER_PLAIN_TEXT_SIZE, buffer_encrypted, &encrypted_size );
			if( TEE_SUCCESS != ret )
				return ret;
		}
		else{

			/*copy plain text*/
			TEE_MemMove( buffer_plain_text, params[0].memref.buffer + index, copy_size );

			/*encrypt plain text*/
			ret = TEE_CipherDoFinal( operation, buffer_plain_text, copy_size, buffer_encrypted, &encrypted_size );
			if( TEE_SUCCESS != ret )
				return ret;			
		}
		
		/*copy encrypted data to output parameter buffer*/
		TEE_MemMove( params[1].memref.buffer, buffer_encrypted, encrypted_size );

		index += BUFFER_PLAIN_TEXT_SIZE;

	} while ( index < plain_text_size );
	
	/*copy initial counter value to output parameter buffer*/
	TEE_MemMove( params[2].memref.buffer, initial_counter_value, AES_BLOCK_SIZE );

	return TEE_SUCCESS;			
}

/*Used to process the decryption of encrypted data sent from CA*/
TEE_Result  decrypt( TEE_Param params[4] ){

	TEE_ObjectHandle key_obj_hdl;
	TEE_OperationHandle operation;
	TEE_Result ret;
		
	uint8_t key_id[ KEY_ID_SIZE ] = { 0 };
	uint8_t buffer_decrypted[ BUFFER_DECRYPTED_SIZE ] = { 0 };	
	uint8_t buffer_encrypted[ BUFFER_ENCRYPTED_SIZE ] = { 0 };  /* holds up to 'AES_BLOCK_SIZE * 16' bytes of encrypted data, after this much data is decrypted this buffere is coppied to the
								     the output parameter and the buffer is reset in preparation to hold the next encrypted values*/
	uint8_t initial_counter_value[ AES_BLOCK_SIZE ] = { 0 };
	uint32_t decrypted_size;
	uint32_t encrypted_size;
	uint32_t copy_size;
	uint32_t index = 0;

	/*Retrieve key id*/
	TEE_MemMove( key_id, params[3].memref.buffer, KEY_ID_SIZE );

	/*open persistent object containing the syymetric key*/
	ret = TEE_OpenPersistentObject( TEE_STORAGE_PRIVATE, key_id, KEY_ID_SIZE, TEE_DATA_FLAG_ACCESS_READ, &key_obj_hdl );
	if( TEE_SUCCESS != ret )
		return ret;

	ret = TEE_AllocateOperation( &operation, TEE_ALG_AES_CTR, TEE_MODE_DECRYPT, MAX_KEY_LEN_SUPP );
	if( TEE_SUCCESS != ret )
		return ret;

	/*set operation key*/
	ret = TEE_SetOperationKey( operation, key_obj_hdl );
	if( TEE_SUCCESS != ret )
		return ret;

	TEE_CloseObject( key_obj_hdl );	

	/*extract encrypted data size*/
	encrypted_size = params[0].memref.size;

	/*extract initial counter value*/
	TEE_MemMove( initial_counter_value, params[2].memref.buffer, AES_BLOCK_SIZE ); 

	/*Initialize the cipher*/
	TEE_CipherInit( operation, initial_counter_value, AES_BLOCK_SIZE );

	do{

		copy_size = encrypted_size - index;
		if( BUFFER_ENCRYPTED_SIZE < copy_size ){

			/*copy encrypted data*/
			TEE_MemMove( buffer_encrypted, params[0].memref.buffer + index, BUFFER_ENCRYPTED_SIZE );

			/*decrypt plain text*/
			ret = TEE_CipherDoFinal( operation, buffer_encrypted, BUFFER_PLAIN_TEXT_SIZE, buffer_decrypted, &decrypted_size );
			if( TEE_SUCCESS != ret )
				return ret;
		}
		else{

			/*copy encrypted data*/
			TEE_MemMove( buffer_encrypted, params[0].memref.buffer + index, copy_size );

			/*decrypt encrypted data*/
			ret = TEE_CipherDoFinal( operation, buffer_encrypted, copy_size, buffer_decrypted, &decrypted_size );
			if( TEE_SUCCESS != ret )
				return ret;			
		}
		
		/*copy encrypted data to output parameter buffer*/
		TEE_MemMove( params[1].memref.buffer, buffer_decrypted, decrypted_size );

		index += BUFFER_ENCRYPTED_SIZE;

	} while ( index < encrypted_size );

	return TEE_SUCCESS;			
}

/*Entry point when the trusted application instance is created*/
TEE_Result TA_EXPORT TA_CreateEntryPoint(void){
	
	OT_LOG(LOG_DEBUG, "Calling entry point for EKS_TA");
	return TEE_SUCCESS;
}

/*Entry point when trusted application instance is destroyed*/
void TA_EXPORT TA_DestroyEntryPoint(void)
{

	OT_LOG(LOG_DEBUG, "Calling Destroy entry point for EKS_TA");
}

/*Entry point when open session request is received*/
TEE_Result TA_EXPORT TA_OpenSessionEntryPoint(uint32_t paramTypes, TEE_Param params[4], void **sessionContext){

	OT_LOG(LOG_INFO, "Calling Open session entry point for EKS_TA");

   	return TEE_SUCCESS;
}

/*Entry point when close session request is received*/
void TA_EXPORT TA_CloseSessionEntryPoint(void *sessionContext){

	OT_LOG(LOG_INFO, "Calling Close session entry point for EKS_TA");
}

/*Entry point when command is received*/
TEE_Result TA_EXPORT TA_InvokeCommandEntryPoint(void *sessionContext, uint32_t commandID, uint32_t paramTypes, TEE_Param params[4]){

	TEE_Result ret;

	if( INITIATE_DH_CMD == commandID ){

		OT_LOG(LOG_INFO, "INITIATE_DH_CMD command invoked for EKS_TA");
		
		/*Validate parameter type*/
		ret = validate_param_type( paramTypes, INITIATE_DH_CMD );
		if( TEE_SUCCESS != ret )
			goto error;
	
		/*process the initiate DH command received*/
		ret = init_cmd( params );	
		if( TEE_SUCCESS != ret )
			goto error;		
	} 
	else if( RESPOND_DH_CMD == commandID ){

		OT_LOG(LOG_INFO, "RESPOND_DH_CMD command invoked for EKS_TA"); 

		/*Validate parameter type*/
		ret = validate_param_type( paramTypes,  RESPOND_DH_CMD );
		if( TEE_SUCCESS != ret )
			goto error;

		/*process the respond DH command received*/
		ret = respond_cmd( params );
		if( TEE_SUCCESS != ret )
			goto error;	
	}
	else if(  COMPLETE_DH_CMD == commandID ){

		OT_LOG(LOG_INFO, "COMPLETE_DH_CMD command invoked for EKS_TA\n");	
		
		/*Validate parameter type*/
		ret = validate_param_type( paramTypes,  COMPLETE_DH_CMD );
		if( TEE_SUCCESS != ret )
			goto error;		

		/*process the complete DH command received*/
		ret = complete_cmd( params );
		if( TEE_SUCCESS != ret )
			goto error;	

		TEE_FreeTransientObject( trs_pub_key_obj_hdl );

	}
	else if(  ENCRYPT_AES_CMD == commandID ){


		OT_LOG(LOG_INFO, "ENCRYPT_AES_CMD command invoked for EKS_TA\n");

		/*Validate parameter type*/
		ret = validate_param_type( paramTypes,  ENCRYPT_AES_CMD );
		if( TEE_SUCCESS != ret )
			goto error;	
		/*process the encryption command received*/
		ret = encrypt( params );
		if( TEE_SUCCESS != ret )
			goto error;
	}
	else if(  DECRYPT_AES_CMD == commandID ){
	
		OT_LOG(LOG_INFO, "DECRYPT_AES_CMD command invoked for EKS_TA\n");

		/*Validate parameter type*/
		ret = validate_param_type( paramTypes,  DECRYPT_AES_CMD );
		if( TEE_SUCCESS != ret )
			goto error;	
	
		/*process the decryption command received*/
		ret = decrypt( params );
		if( TEE_SUCCESS != ret )
			goto error;	
	}
	else{

		OT_LOG(LOG_INFO, "Invalid command invoked for EKS_TA");
		return TEE_ERROR_BAD_PARAMETERS;		
 	}
  
	return TEE_SUCCESS;

	error:
		return ret;
}
