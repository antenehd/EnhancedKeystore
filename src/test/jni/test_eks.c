
#include <stdint.h>
#include <stdlib.h>
#include <stdio.h>
#include "tee_client_api.h"
#include "tee_logging.h"
#include <string.h>

#define INITIATE_DH_CMD    0x00000001
#define RESPOND_DH_CMD     0x00000002
#define COMPLETE_DH_CMD    0x00000003

#define PRIn(str, ...) printf(str "\n", ##__VA_ARGS__);
#define PRI(str, ...) printf(str, ##__VA_ARGS__);

static const TEEC_UUID uuid = {0x316cd877, 0xebae, 0x59d2, {0xa7,0x66,0x6c,0xce,0x17,0xe1,0xd4,0x71}};

#define  key_len  2048 
#define  gx_size  ( key_len / 8 )
#define  gy_size  ( key_len / 8 )
#define  g_size   4
#define  p_size   ( key_len / 8 )

uint8_t gx_buffer[ gx_size ] = {0};
uint8_t gy_buffer[gy_size] = {0};
uint8_t g_buffer[g_size] = {0};
uint8_t p_buffer[p_size] = {0};
uint32_t key_len_id[8] = {0};
uint32_t key_len_id_size = 8;

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

	PRI("Registering shared memory: ");
	reg_shm->buffer = buffer;
	reg_shm->size = buffer_size;
	reg_shm->flags = flags;
	ret = TEEC_RegisterSharedMemory(context, reg_shm);
	if (ret != TEE_SUCCESS) {
		PRIn("TEEC_RegisterSharedMemory failed: 0x%x", ret);
		return 1;
	}

	PRIn("registered");
	return 0;
}

static int test_eks()
{

	int i, j;

	TEEC_Context context;
	TEEC_Session session, session_2;
	TEEC_Operation operation = {0};
	TEEC_Operation operation_2 = {0};
	TEEC_Result ret;
	//TEEC_SharedMemory reg_inout_mem = {0}, alloc_inout_mem = {0};
	TEEC_SharedMemory gx = {0};
	TEEC_SharedMemory gy = {0};
	TEEC_SharedMemory g_out = {0};
	TEEC_SharedMemory p_out = {0};	
	TEEC_SharedMemory g_in = {0};
	TEEC_SharedMemory p_in = {0};
	TEEC_SharedMemory k_len_id_inout = {0};
	TEEC_SharedMemory key_id = {0};
	TEEC_Value init_val = {0};
	uint32_t key_length = key_len;
	
	TEEC_Parameter params[4] = {0};
	//TEEC_Parameter params_2[4] = {0};
	uint32_t return_origin;
	uint32_t connection_method = TEEC_LOGIN_PUBLIC;

	OT_LOG(LOG_DEBUG, "Calling entry point for EKS_ca");

	/* Initialize context */
	PRI("Initializing context: ");
	ret = TEEC_InitializeContext(NULL, &context);
	if (ret != TEEC_SUCCESS) {
		PRI("TEEC_InitializeContext failed: 0x%x", ret);
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

	/*prepare memory for initiate DH*/
	reg_shared_memory(&context, &gx, gx_buffer, gx_size, TEEC_MEM_OUTPUT );
	reg_shared_memory(&context, &g_out, g_buffer, g_size, TEEC_MEM_OUTPUT );
	reg_shared_memory(&context, &p_out, p_buffer, p_size, TEEC_MEM_OUTPUT );
	init_val.b = key_len;	
	
	/*set operation parameter for initiate DH*/	
	params[0].memref.parent = &gx;
	params[0].memref.size = gx_size;
	params[1].memref.parent = &g_out;
	params[1].memref.size = g_size;
	params[2].memref.parent = &p_out;
	params[2].memref.size = p_size;
	params[3].value.b = key_len;
	fill_operation_params( &operation, 0, TEEC_PARAM_TYPES( TEEC_MEMREF_WHOLE,TEEC_MEMREF_WHOLE, TEEC_MEMREF_WHOLE, TEEC_VALUE_INOUT), params);
	
	/*send init dh command*/
	ret = TEEC_InvokeCommand(&session, INITIATE_DH_CMD, &operation, &return_origin);
	if (ret != TEEC_SUCCESS) {
		PRIn("TEEC_InvokeCommand for INITIATE_DH_CMD failed: 0x%x\n", ret);
		return -1;
	}

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

	
	/************************ APPLICATION B *****************************************************************************************/

	/*Prepare memory for public shared key of application b*/


	/* Open session */
	PRIn("************************** APPLICATION B ******************************************************************************** \n");
	PRI("Openning session for RESPOND_DH_CMD: ");
	ret = TEEC_OpenSession( &context, &session_2, &uuid, connection_method, NULL, &operation_2, &return_origin);

	if( ret != TEE_SUCCESS ){

		PRIn("TEEC_OpenSession FOR APPLICATION B failed: 0x%x", ret);
		return -1;
	}
	PRIn("opened");

	/*prepare memory for initiate DH*/
	reg_shared_memory(&context, &gx, gx_buffer, gx_size, TEEC_MEM_OUTPUT |TEEC_MEM_INPUT  );
	reg_shared_memory(&context, &g_out, g_buffer, g_size, TEEC_MEM_INPUT );
	reg_shared_memory(&context, &p_out, p_buffer, p_size, TEEC_MEM_INPUT );
	reg_shared_memory(&context, &k_len_id_inout, key_len_id, key_len_id_size, TEEC_MEM_OUTPUT |TEEC_MEM_INPUT );
	
	/*set operation parameter for respond DH*/	
	params[0].memref.parent = &gx;
	params[0].memref.size = gx_size;
	params[1].memref.parent = &g_out;
	params[1].memref.size = g_size;
	params[2].memref.parent = &p_out;
	params[2].memref.size = p_size;
	params[3].memref.parent = &k_len_id_inout;
	params[3].memref.size =  key_len_id_size;
	memcpy( key_len_id, &key_length, 4 );
	fill_operation_params( &operation, 0, TEEC_PARAM_TYPES( TEEC_MEMREF_WHOLE,TEEC_MEMREF_WHOLE, TEEC_MEMREF_WHOLE, TEEC_MEMREF_WHOLE), params );	

	/*send respond dh command*/
	ret = TEEC_InvokeCommand(&session_2, RESPOND_DH_CMD, &operation, &return_origin);
	if (ret != TEEC_SUCCESS) {
		PRIn("TEEC_InvokeCommand for RESPOND_DH_CMD failed: 0x%x\n", ret);
		return -1;
	}

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

	PRIn("************************** END OF APPLICATION B ******************************************************************************** ");

	/*prepare memory for initiate DH*/
	reg_shared_memory(&context, &gx, gx_buffer, gx_size, TEEC_MEM_INPUT  );
	reg_shared_memory(&context, &key_id, key_len_id, key_len_id_size, TEEC_MEM_OUTPUT );

	/*set operation parameter for complete dh*/	
	params[0].memref.parent = &gx;
	params[0].memref.size = gx_size;
	memset( key_len_id, 0, key_len_id_size );
	params[1].memref.parent = &key_id;
	params[1].memref.size =  key_len_id_size;
	fill_operation_params( &operation, 0, TEEC_PARAM_TYPES( TEEC_MEMREF_WHOLE, TEEC_MEMREF_WHOLE, TEEC_NONE, TEEC_NONE ), params );
	
	/*send complete dh command*/
	ret = TEEC_InvokeCommand( &session, COMPLETE_DH_CMD, &operation, &return_origin );
	if (ret != TEEC_SUCCESS) {
		PRIn("TEEC_InvokeCommand for COMPLETE_DH_CMD failed: 0x%x\n", ret);
		return -1;
	}

	/*Check returned key id*/
	PRIn( "TEST: KEY_ID = %02x%02x%02x%02x%02x%02x%02x%02x\n", key_len_id[0], key_len_id[1], key_len_id[2], key_len_id[3], key_len_id[4], key_len_id[5], key_len_id[6], key_len_id[7] );	
	/*check for public value*/

	return 0;
}

int main(){

	PRIn("START: eks_ca test app\n");

	test_eks();

	exit(0);
}
