

#include <stdint.h>
#include <stdlib.h>
#include <stdio.h>
#include "tee_client_api.h"
#include "tee_logging.h"

#define PRIn(str, ...) printf(str "\n", ##__VA_ARGS__);
#define PRI(str, ...) printf(str, ##__VA_ARGS__);

static const TEEC_UUID uuid = {0x316cd877, 0xebae, 0x59d2, {0xa7,0x66,0x6c,0xce,0x17,0xe1,0xd4,0x71}};

static int test_eks()
{
	TEEC_Context context;
	TEEC_Session session;
	TEEC_Operation operation = {0};
	TEEC_Result ret;
	TEEC_SharedMemory reg_inout_mem = {0}, alloc_inout_mem = {0};
	uint32_t return_origin;
	uint32_t connection_method = TEEC_LOGIN_PUBLIC;
	uint32_t fn_ret = 1; /* Default is error return */

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

	return 0;
}

int main(){

	PRIn("START: eks_ca test app\n");

	test_eks();

	exit(0);
}
