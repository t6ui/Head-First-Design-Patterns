#ifdef __cplusplus
extern "C"
{
#endif
#include <sys/stat.h>

#define BUF_LEN_64 64
#define BUF_LEN_128 128
#define BUF_LEN_256 256
#define BUF_LEN_512 512
#define BUF_LEN_1K 1024
#define MAX_FILE_NAME_LEN 512
#define FILE_MODE_777 (S_IRWXU | S_IRWXG | S_IRWXO)
#define FILE_MODE_755 (S_IRWXU | S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH)

#define MAX_LOG_SIZE 600*1024*1024     //to set the max log size,default is 600M
#define MIN_FREE_DATA 1024*1024*1024     //to set the max log size,default is 600M

//define exception watch dir
#define DIR_DROPBOX "/data/system/dropbox/"
#define DIR_LOGFILE "/data/log/monitor_logs/"
#define DIR_TMPFILE "/data/log/monitor_tmp/"
#define MONITOR_UNZIP_TMP_FILE "/data/log/monitor_tmp/monitor_unzip_tmp_file.txt"
#define PANIC_LOG_COMPRESSED_FLAG_FILE "/data/log/crash_log/compressed_flag"
#define PANIC_LOG_FOLDER "/data/log/crash_log/"
#define DISK_DATA "/data"
#define LOG_MAX_NUM 5
#define ALL_LOG_MAX 20


enum {
    EVENT_ID_ANR = 101,
    EVENT_ID_CRASH = 102,
    EVENT_ID_TMB = 103,
};

enum FAULT_TYPE {
    ANR = 0,
    APPCRASH,
    TOMBSTONE,
    SSCRASH,
    SSWATCHDOG,
    FAULTMAX,
};

typedef struct LOG_FILE {
    int type;
    char *type_str;
    char *pfile;
    char *match;        //search match string
    char actual_name[BUF_LEN_128];
} log_files;

typedef struct LOG_EXCEPTION {
    int type;
    char *type_str;
    log_files *plogs;
} log_exceptions;

typedef struct log_info {
    char file_path[BUF_LEN_128];
    char stack_info1[BUF_LEN_128];
    char stack_info2[BUF_LEN_128];
    char proc_name[BUF_LEN_64];
    char app_version[BUF_LEN_64];
} log_info_s;

int logserver_init();
int start_log_monitor();
void stop_log_monitor();

#ifdef __cplusplus
}
#endif
