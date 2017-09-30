#ifdef __cplusplus
extern "C"
{
#endif

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <string.h>
#include <unistd.h>
#include <sys/time.h>
#include <sys/types.h>
#include <sys/stat.h>

#include <utils/Log.h>
#include "logserver.h"
#include "utils.h"
#include "socket.h"

static void clean_at_exit(void)
{
    stop_log_monitor();
    stop_log_control();
}

int logserver_init()
{
    umask(000);
    atexit(clean_at_exit);

    start_log_monitor();
    start_log_control ();
    start_apanic_monitor();

    return 0;
}

#ifdef __cplusplus
}
#endif
