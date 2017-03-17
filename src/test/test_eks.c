
#include <stdint.h>
#include <stdlib.h>
#include <stdio.h>
#include "tee_client_api.h"
#include "tee_logging.h"
#include <string.h>

#define INITIATE_DH_CMD    0x00000001
#define RESPOND_DH_CMD     0x00000002
#define COMPLETE_DH_CMD    0x00000003
#define ENCRYPT_AES_CMD    0x00000004
#define DECRYPT_AES_CMD	   0x00000005

#define PRIn(str, ...) printf(str "\n", ##__VA_ARGS__);
#define PRI(str, ...) printf(str, ##__VA_ARGS__);

#define  BUFF_SIZE 256
#define  g_size   4

#define  KEY_LEN_256	256
#define  KEY_LEN_128	128

#define  PLAIN_TXT_SIZE 130
#define AES_BLOCK_SIZE  16

uint8_t gx_buffer[ BUFF_SIZE ] = {0};
uint8_t g_buffer[ BUFF_SIZE ] = {0};
uint8_t p_buffer[ BUFF_SIZE ] = {0};
uint8_t key_len_id[8] = {0};
uint32_t key_len_id_size = 8;

static const TEEC_UUID uuid = {0x316cd877, 0xebae, 0x59d2, {0xa7,0x66,0x6c,0xce,0x17,0xe1,0xd4,0x71}};

/*only for values and registered shared memory*/
static void fill_operation_params(TEEC_Operation *operation, uint32_t start, uint32_t parm_type, TEEC_Parameter params[4])
{
	int i;
	operation->paramTypes =	parm_type;

	for( i=0; i<4; i++)	
		operation->params[i] = params[i];
}

static int reg_shared_memory(TEEC_Context *context, TEEC_SharedMemory *reg_shm, void *buffer, uint32_t buffer_size, uint32_t flags)
{
	TEEC_Result ret;

	reg_shm->buffer = buffer;
	reg_shm->size = buffer_size;
	reg_shm->flags = flags;
	ret = TEEC_RegisterSharedMemory(context, reg_shm);
	if (ret != TEE_SUCCESS) {
		PRIn("TEEC_RegisterSharedMemory failed: 0x%x", ret);
		return 1;
	}

	return 0;
}

int send_init( uint32_t key_size, TEEC_Session *session, TEEC_Context *context, uint32_t *return_origin ){

	TEEC_Operation operation = {0};
	TEEC_Result ret;
	//TEEC_SharedMemory reg_inout_mem = {0}, alloc_inout_mem = {0};
	TEEC_SharedMemory gx = {0};
	TEEC_SharedMemory g_out = {0};
	TEEC_SharedMemory p_out = {0};	
	TEEC_SharedMemory k_len_id_inout = {0};
	TEEC_SharedMemory key_id = {0};
	TEEC_Value init_val = {0};
	TEEC_Parameter params[4] = {0};

	uint32_t gx_size = BUFF_SIZE;
	uint32_t p_size = BUFF_SIZE;

	
	/*prepare memory for initiate DH*/
	reg_shared_memory( context, &gx, gx_buffer, gx_size, TEEC_MEM_OUTPUT );
	reg_shared_memory( context, &g_out, g_buffer, g_size, TEEC_MEM_OUTPUT );
	reg_shared_memory( context, &p_out, p_buffer, p_size, TEEC_MEM_OUTPUT );
	init_val.b = key_size;	
	
	/*set operation parameter for initiate DH*/	
	params[0].memref.parent = &gx;
	params[0].memref.size = gx_size;
	params[1].memref.parent = &g_out;
	params[1].memref.size = g_size;
	params[2].memref.parent = &p_out;
	params[2].memref.size = p_size;
	params[3].value.b = key_size;
	fill_operation_params( &operation, 0, TEEC_PARAM_TYPES( TEEC_MEMREF_WHOLE,TEEC_MEMREF_WHOLE, TEEC_MEMREF_WHOLE, TEEC_VALUE_INOUT), params );
	
	/*send init dh command*/
	PRIn("Trying to send INITIATE_DH_CMD");
	ret = TEEC_InvokeCommand( session, INITIATE_DH_CMD, &operation, return_origin );
	if (ret != TEEC_SUCCESS) {

		PRIn("TEEC_InvokeCommand for INITIATE_DH_CMD failed: 0x%x\n", ret);
		return -1;
	}
	PRIn("INITIATE_DH_CMD send");
	return 1;
}

int send_respond( uint32_t key_size, TEEC_Session *session, TEEC_Context *context, uint32_t *return_origin ){

	TEEC_Operation operation = {0};
	TEEC_Result ret;
	//TEEC_SharedMemory reg_inout_mem = {0}, alloc_inout_mem = {0};
	TEEC_SharedMemory gx = {0};
	TEEC_SharedMemory g_out = {0};
	TEEC_SharedMemory p_out = {0};	
	TEEC_SharedMemory k_len_id_inout = {0};
	TEEC_SharedMemory key_id = {0};
	TEEC_Parameter params[4] = {0};

	uint32_t gx_size = BUFF_SIZE;
	uint32_t p_size = BUFF_SIZE;

	/*prepare memory for initiate DH*/
	reg_shared_memory( context, &gx, gx_buffer, gx_size, TEEC_MEM_OUTPUT |TEEC_MEM_INPUT  );
	reg_shared_memory( context, &g_out, g_buffer, g_size, TEEC_MEM_INPUT );
	reg_shared_memory( context, &p_out, p_buffer, p_size, TEEC_MEM_INPUT );
	reg_shared_memory( context, &k_len_id_inout, key_len_id, key_len_id_size, TEEC_MEM_OUTPUT |TEEC_MEM_INPUT );
	
	/*set operation parameter for respond DH*/	
	params[0].memref.parent = &gx;
	params[0].memref.size = gx_size;
	params[1].memref.parent = &g_out;
	params[1].memref.size = g_size;
	params[2].memref.parent = &p_out;
	params[2].memref.size = p_size;
	params[3].memref.parent = &k_len_id_inout;
	params[3].memref.size =  key_len_id_size;
	memcpy( key_len_id, &key_size, 4 );
	fill_operation_params( &operation, 0, TEEC_PARAM_TYPES( TEEC_MEMREF_WHOLE,TEEC_MEMREF_WHOLE, TEEC_MEMREF_WHOLE, TEEC_MEMREF_WHOLE), params );	

	/*send respond dh command*/
	PRIn("Trying to send RESPOND_DH_CMD");
	ret = TEEC_InvokeCommand( session, RESPOND_DH_CMD, &operation, return_origin);
	if (ret != TEEC_SUCCESS) {
		PRIn("TEEC_InvokeCommand for RESPOND_DH_CMD failed: 0x%x\n", ret);
		return -1;
	}
	PRIn("RESPOND_DH_CMD sent");
	return 1;
}

int send_complet( uint32_t key_size, TEEC_Session *session, TEEC_Context *context, uint32_t *return_origin ){

	TEEC_Operation operation = {0};
	TEEC_Result ret;
	//TEEC_SharedMemory reg_inout_mem = {0}, alloc_inout_mem = {0};
	TEEC_SharedMemory gx = {0};
	TEEC_SharedMemory k_len_id_inout = {0};
	TEEC_SharedMemory key_id = {0};
	TEEC_Parameter params[4] = {0};

	uint32_t gx_size = BUFF_SIZE;

	/*prepare memory for initiate DH*/
	reg_shared_memory( context, &gx, gx_buffer, gx_size, TEEC_MEM_INPUT  );
	reg_shared_memory( context, &key_id, key_len_id, key_len_id_size, TEEC_MEM_OUTPUT );

	/*set operation parameter for complete dh*/	
	params[0].memref.parent = &gx;
	params[0].memref.size = gx_size;
	memset( key_len_id, 0, key_len_id_size );
	params[1].memref.parent = &key_id;
	params[1].memref.size =  key_len_id_size;
	fill_operation_params( &operation, 0, TEEC_PARAM_TYPES( TEEC_MEMREF_WHOLE, TEEC_MEMREF_WHOLE, TEEC_NONE, TEEC_NONE ), params );
	
	/*send complete dh command*/
	PRIn("Trying to send COMPLETE_DH_CMD");
	ret = TEEC_InvokeCommand( session, COMPLETE_DH_CMD, &operation, return_origin );
	if (ret != TEEC_SUCCESS) {
		PRIn("TEEC_InvokeCommand for COMPLETE_DH_CMD failed: 0x%x\n", ret);
		return -1;
	}
	PRIn("COMPLETE_DH_CMD sent");
	return 1;
}

void check_init_ret( uint32_t key_size ){

	int i, j;

	uint32_t gx_size = BUFF_SIZE;
	uint32_t p_size = BUFF_SIZE;

	
	/*check generator value*/
	PRIn( "TEST: GENERATOR_VALUE = %02x%02x%02x%02x\n", g_buffer[0], g_buffer[1], g_buffer[2], g_buffer[3] );	
	/*check for prime number*/
	PRI( "TEST: PRIME_NUMBER = {\n");
	j = 0;
	for( i=0; i < p_size; i++){

		
		PRI( " %02x,", p_buffer[ i ] );
		
		if( 20 == j ){
			
	 		PRI( "\n" );
			j = 0;
		}

		j++;
	}	
	PRIn( "}\n");	
	/*check for public value*/
	PRI( "TEST: PUBLIC_NUMBER = {\n");
	j = 0;
	for( i=0; i < gx_size; i++ ){

		
		PRI( " %02x,", gx_buffer[ i ] );
		
		if( 20 == j ){
			
	 		PRI( "\n" );
			j = 0;
		}
		j++;
	}	
	PRIn( "}" );
}

void check_ret_resp( uint32_t key_size ){

	int i, j;

	uint32_t gx_size = BUFF_SIZE;
	
	/*Check returned key id*/
	PRIn( "TEST: KEY_ID = %02x%02x%02x%02x%02x%02x%02x%02x\n", key_len_id[0], key_len_id[1], key_len_id[2], key_len_id[3], key_len_id[4], key_len_id[5], key_len_id[6], key_len_id[7] );	
	/*check for public value*/
	PRI( "TEST: PUBLIC_NUMBER gy = {\n");
	j = 0;
	for( i=0; i < gx_size; i++ ){
		
		PRI( " %02x,", gx_buffer[ i ] );
		
		if( 20 == j ){
			
	 		PRI( "\n" );
			j = 0;
		}
		j++;
	}	
	PRIn( "}" )
}

int encrypt_data( uint8_t key_size[8], uint8_t *plain_txt, uint8_t *encrypted, uint8_t *initial_vector, uint32_t txt_size, TEEC_Session *session, TEEC_Context *context, uint32_t *return_origin ){

	TEEC_Operation operation = {0};
	TEEC_Result ret;
	//TEEC_SharedMemory reg_inout_mem = {0}, alloc_inout_mem = {0};
	TEEC_SharedMemory pln_txt = {0};
	TEEC_SharedMemory encry_data = {0};
	TEEC_SharedMemory iv = {0};	
	TEEC_SharedMemory key_id = {0};
	TEEC_Parameter params[4] = {0};

	if( ( NULL == plain_txt) || ( NULL == encrypted ) || ( NULL == session ) || ( NULL == context ) || ( NULL == return_origin ) )
		return -1;

	/*prepare memory for initiate DH*/
	reg_shared_memory( context, &pln_txt, plain_txt, txt_size, TEEC_MEM_INPUT  );
	reg_shared_memory( context, &encry_data, encrypted, txt_size, TEEC_MEM_OUTPUT );
	reg_shared_memory( context, &iv, initial_vector, AES_BLOCK_SIZE, TEEC_MEM_OUTPUT );
	reg_shared_memory( context, &key_id, key_size, 8, TEEC_MEM_INPUT );
	
	/*set operation parameter for respond DH*/	
	params[0].memref.parent = &pln_txt;
	params[0].memref.size = txt_size;
	params[1].memref.parent = &encry_data;
	params[1].memref.size = txt_size;
	params[2].memref.parent = &iv;
	params[2].memref.size = AES_BLOCK_SIZE;
	params[3].memref.parent = &key_id;
	params[3].memref.size =  8;

	fill_operation_params( &operation, 0, TEEC_PARAM_TYPES( TEEC_MEMREF_WHOLE,TEEC_MEMREF_WHOLE, TEEC_MEMREF_WHOLE, TEEC_MEMREF_WHOLE), params );	

	/*send encrypt command*/
	PRIn("Trying to encrypted plain text");
	ret = TEEC_InvokeCommand( session, ENCRYPT_AES_CMD, &operation, return_origin);
	if (ret != TEEC_SUCCESS) {

		PRIn("TEEC_InvokeCommand for ENCRYPT_AES_CMD failed: 0x%x\n", ret);
		return -1;
	}
	PRIn("Plain text encrypted");
	return 1;
}

int decrypt_data( uint8_t key_size[8], uint8_t *encrypted_data, uint8_t *decrypted, uint8_t *initial_vector, uint32_t encry_size, TEEC_Session *session, TEEC_Context *context, uint32_t *return_origin ){

	TEEC_Operation operation = {0};
	TEEC_Result ret;
	//TEEC_SharedMemory reg_inout_mem = {0}, alloc_inout_mem = {0};
	TEEC_SharedMemory encry_data = {0};
	TEEC_SharedMemory decry_data = {0};
	TEEC_SharedMemory iv = {0};	
	TEEC_SharedMemory key_id = {0};
	TEEC_Parameter params[4] = {0};

	if( ( NULL == encrypted_data ) || ( NULL == decrypted ) || ( NULL == initial_vector ) || ( NULL == session ) || ( NULL == context ) || ( NULL == return_origin ) )
		return -1;

	/*prepare memory for initiate DH*/
	reg_shared_memory( context, &encry_data, encrypted_data, encry_size, TEEC_MEM_INPUT  );
	reg_shared_memory( context, &decry_data, decrypted, encry_size, TEEC_MEM_OUTPUT );
	reg_shared_memory( context, &iv, initial_vector, AES_BLOCK_SIZE, TEEC_MEM_INPUT );
	reg_shared_memory( context, &key_id, key_size, 8, TEEC_MEM_INPUT );
	
	/*set operation parameter for respond DH*/	
	params[0].memref.parent = &encry_data;
	params[0].memref.size = encry_size;
	params[1].memref.parent = &decry_data;
	params[1].memref.size = encry_size;
	params[2].memref.parent = &iv;
	params[2].memref.size = AES_BLOCK_SIZE;
	params[3].memref.parent = &key_id;
	params[3].memref.size =  8;

	fill_operation_params( &operation, 0, TEEC_PARAM_TYPES( TEEC_MEMREF_WHOLE,TEEC_MEMREF_WHOLE, TEEC_MEMREF_WHOLE, TEEC_MEMREF_WHOLE), params );	

	/*send decrypt command*/
	PRIn("Trying to decrypted encrypted data");
	ret = TEEC_InvokeCommand( session, DECRYPT_AES_CMD, &operation, return_origin);
	if (ret != TEEC_SUCCESS) {

		PRIn("TEEC_InvokeCommand for DECRYPT_AES_CMD failed: 0x%x\n", ret);
		return -1;
	}
	PRIn("Encrypted data decrypted");
	return 1;
}


int test_encry_decr( uint8_t *plain_txt, uint8_t *decrypted, uint32_t size){

	int i, j;

	PRIn("\nTesting Encryption Decryption operation");	
	if( ( NULL == plain_txt ) || ( NULL == decrypted ) )
		return -1;

	for( i = 0; i < size; i++ ){

		if( plain_txt[ i ] != decrypted[ i ] ){

			PRIn("Encryption Decryption test failed.");	
			return -1;
		}
	}

	PRIn("Encryption Decryption test passed.");
}

static int test_eks()
{

	int i;

	uint32_t key_size= KEY_LEN_256; 

	TEEC_Context context;
	TEEC_Session session, session_2;
	TEEC_Result ret;
	TEEC_Operation operation = {0};
	TEEC_Operation operation_2 = {0};

	uint8_t plain_txt[ PLAIN_TXT_SIZE ] = { 'a', 'b', 'c', 'd', 'e', '1', '2', '3', '4', '5', 'a', 'b', 'c', 'd', 'e', '1', '2', '3', '4', '5', 'a', 'b', 'c', 'd', 'e', '1', '2', '3', '4', '5', 'a', 'b', 'c', 'd', 'e', '1', '2', '3', '4', '5', 'a', 'b', 'c', 'd', 'e', '1', '2', '3', '4', '5', 'a', 'b', 'c', 'd', 'e', '1', '2', '3', '4', '5', 'a', 'b', 'c', 'd', 'e', '1', '2', '3', '4', '5', 'a', 'b', 'c', 'd', 'e', '1', '2', '3', '4', '5', 'a', 'b', 'c', 'd', 'e', '1', '2', '3', '4', '5', 'a', 'b', 'c', 'd', 'e', '1', '2', '3', '4', '5', 'a', 'b', 'c', 'd', 'e', '1', '2', '3', '4', '5', 'a', 'b', 'c', 'd', 'e', '1', '2', '3', '4', '5', 'a', 'b', 'c', 'd', 'e', '1', '2', '3', '4', '5' };

	uint8_t encrypted[ PLAIN_TXT_SIZE ] = { 0 }; 
	uint8_t decrypted[ PLAIN_TXT_SIZE ] = { 0 }; 
	uint8_t initial_vector[ 8 ] = { 0 };
	uint32_t return_origin;
	uint32_t connection_method = TEEC_LOGIN_PUBLIC;

	OT_LOG(LOG_DEBUG, "Calling entry point for EKS_ca");

	/* Initialize context */
	PRI("Initializing context: ");
	ret = TEEC_InitializeContext( NULL, &context);
	if (ret != TEEC_SUCCESS) {
		PRIn("TEEC_InitializeContext failed: 0x%x", ret);
		return -1;
	} else {
		PRIn("initialized");
	}
	
	/* Open session */
	PRI("Openning session: ");
	ret = TEEC_OpenSession( &context, &session, &uuid, connection_method, NULL, &operation, &return_origin);

	if( ret != TEE_SUCCESS ){

		PRIn("TEEC_OpenSession failed: 0x%x", ret);
		return -1;
	}
	PRIn("opened");

	/*Send init command*/
	if ( -1 == send_init( key_size, &session, &context, &return_origin ) ){
		
		PRIn( "Failed to send init command " );
		return -1;
	}

	/*check init return*/
	check_init_ret( key_size );
	
	/************************ APPLICATION B *****************************************************************************************/

	/* Open session */
	PRIn("\n************************** APPLICATION B ******************************************************************************** \n");
	PRI("Openning session for RESPOND_DH_CMD: ");
	ret = TEEC_OpenSession( &context, &session_2, &uuid, connection_method, NULL, &operation_2, &return_origin);
	
	if( ret != TEE_SUCCESS ){
	
		PRIn("TEEC_OpenSession FOR APPLICATION B failed: 0x%x", ret);
		return -1;
	}
	PRIn("opened");

	/*Send respond command*/
	if( -1 == send_respond( key_size, &session_2, &context, &return_origin ) ){

		PRIn( " Failed to send respond command " );
		return -1;
	}

	/*check return of respond command*/
	check_ret_resp( key_size );

	TEEC_CloseSession ( &session_2 );

	PRIn("\n************************** END OF APPLICATION B ******************************************************************************** \n");

	/************************ END OF APPLICATION B *****************************************************************************************/

	/*Send respond command*/
	if( -1 == send_complet( key_size, &session, &context, &return_origin ) ){

		PRIn( " Failed to send complet command " );
		return -1;
	}
	/*Check returned key id*/
	PRIn( "TEST: KEY_ID = %02x%02x%02x%02x%02x%02x%02x%02x\n", key_len_id[0], key_len_id[1], key_len_id[2], key_len_id[3], key_len_id[4], key_len_id[5], key_len_id[6], key_len_id[7] );	
	/*check for public value*/

	TEEC_CloseSession ( &session );

	/*check encryption*/
	/* Open session */
	PRI("\nOpenning session for ENCRYPT_AES_CMD: ");
	ret = TEEC_OpenSession( &context, &session, &uuid, connection_method, NULL, NULL, &return_origin);
	
	if( ret != TEE_SUCCESS ){
	
		PRIn("TEEC_OpenSession FOR ENCRYPTION failed: 0x%x", ret);
		return -1;
	}
	PRIn("opened");
	/*Send encrypt command*/
	if( -1 == encrypt_data( key_len_id, plain_txt, encrypted, initial_vector, PLAIN_TXT_SIZE, &session, &context, &return_origin) ){

		PRIn( " Encryption failed." );
		return -1;
	}	

	TEEC_CloseSession ( &session );

	/*check decryption*/
	/* Open session */
	PRI("\nOpenning session for DECRYPT_AES_CMD: ");
	ret = TEEC_OpenSession( &context, &session, &uuid, connection_method, NULL, NULL, &return_origin);
	
	if( ret != TEE_SUCCESS ){
	
		PRIn("TEEC_OpenSession for decryption failed: 0x%x", ret);
		return -1;
	}
	PRIn("opened");
	/*Send decrypt command*/
	if( -1 == decrypt_data( key_len_id, encrypted, decrypted, initial_vector, PLAIN_TXT_SIZE, &session, &context, &return_origin ) ){

		PRIn( "Decryption failed." );
		return -1;
	}	

	TEEC_CloseSession ( &session );
	
	if( -1 == test_encry_decr( plain_txt, decrypted, PLAIN_TXT_SIZE) )
		PRIn("Test for encryption and decryption failed.");

	return 0;
}

int main(){

	PRIn("START: eks_ca test app\n");

	test_eks();

	exit(0);
}
