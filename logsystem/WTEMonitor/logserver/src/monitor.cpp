#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/inotify.h>
#include <sys/time.h>
#include <errno.h>
#include <utils/Log.h>
#include <pthread.h>
#include <pwd.h>
#include <inttypes.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include "logserver.h"
#include "socket.h"
#include "utils.h"
#include "wt_compress.h"

pthread_t g_tid_logfile_monitor = -1;

static int fd_notify = -1;

int g_inotify_fd;
int add_count = 0;
char *add_input_list[ALL_LOG_MAX] = {0};
int del_count = 0;
char *del_input_list[ALL_LOG_MAX] = {0};

char *applogcat_files[LOG_MAX_NUM] = {"/data/log/wt_logs/logcat-log",
    "/data/log/wt_logs/logcat-log.01"};

char *eventlog_files[LOG_MAX_NUM] = {"/data/log/wt_logs/logcat-event-log"};

char *kmsglog_files[LOG_MAX_NUM] = {"/data/log/wt_logs/logcat-kmsg-log",
    "/data/log/wt_logs/logcat-kmsg-log.01"};

struct inotify_dir_attr {
    char *path;
    char *owner;
    int mode;
};

struct inotify_dir_attr path_array[] = {
    {DIR_LOGFILE, "system", FILE_MODE_755},
    {DIR_TMPFILE, "system", FILE_MODE_755},
    {NULL, NULL, 0}
};

struct wd_name {
    int wd;
    char *name;
    unsigned int event;
};

//all watch directorys
struct wd_name wd_array[] = {
    {-1, DIR_DROPBOX, IN_MOVED_TO},
    {-1, NULL, 0},
};

struct LOG_FILE wt_anr_logfile[] =
{
    {ANR, "anr", DIR_DROPBOX, "app_anr", ""},
    {ANR, NULL, NULL, ""}
};

struct LOG_FILE wt_crash_logfile[] =
{
    {APPCRASH, "appcrash", DIR_DROPBOX, "app_crash", ""},
    {APPCRASH, NULL, NULL, ""}
};

struct LOG_FILE wt_tombstone_logfile[] =
{
    {TOMBSTONE, "appcrash", DIR_DROPBOX, "SYSTEM_TOMBSTONE", ""}, //移动要求tombstone也归类为appcrash类型
    {TOMBSTONE, NULL, NULL, ""}
};

struct LOG_FILE wt_sscrash_logfile[] =
{
    {SSCRASH, "vmreboot", DIR_DROPBOX, "system_server_crash", ""},
    {SSCRASH, NULL, NULL,""}
};

struct LOG_FILE wt_sswatchdog_logfile[] =
{
    {SSWATCHDOG, "watchdog", DIR_DROPBOX, "system_server_watchdog", ""},
    {SSWATCHDOG, NULL, NULL, NULL, ""}
};

//exception types
struct LOG_EXCEPTION wt_log_exception[FAULTMAX + 1] =
{
    {ANR, "anr", wt_anr_logfile},
    {APPCRASH, "appcrash", wt_crash_logfile},
    {TOMBSTONE, "tombstone", wt_tombstone_logfile},
    {SSCRASH, "vmreboot", wt_sscrash_logfile},
    {SSWATCHDOG, "watchdog", wt_sswatchdog_logfile}
};

log_files *wt_cur_logfiles = NULL;
log_files **wt_ppcur_logfiles = NULL;

/**
 * Function to get base log
 * applogcat, kernel log, event log
 * @param input base logs need be packed
 *
 * @return base logs
 */
static void get_wt_logs() {
    int i =0;
    int len = 0;

    for (i = 0; i < LOG_MAX_NUM; i++) {
        if ((NULL != applogcat_files[i]) && (0 == access(applogcat_files[i], F_OK))) {
            asprintf(&add_input_list[add_count++], "%s", applogcat_files[i]);
        }

        if ((NULL != kmsglog_files[i]) && (0 == access(kmsglog_files[i], F_OK))) {
            asprintf(&add_input_list[add_count++], "%s", kmsglog_files[i]);
        }

        if ((NULL != eventlog_files[i]) && (0 == access(eventlog_files[i], F_OK))) {
            asprintf(&add_input_list[add_count++], "%s", eventlog_files[i]);
        }
    }
}

/**
 * Function to get app_crash logs
 * just for app_crash
 * @param input all logs need be packed
 *
 * @return all logs
 */
static void get_crash_file() {
    get_wt_logs();

    char expPath[BUF_LEN_256]={0};
    strcat(expPath, DIR_DROPBOX);
    strcat(expPath, wt_cur_logfiles->actual_name);

    if (0 == access(expPath, F_OK)) {
        asprintf(&add_input_list[add_count++], "%s", expPath);
    } else {
        wtlog_info("the path:%s is not exist.", expPath);
    }
}

/**
 * Function to get app_anr logs
 * just for app_anr
 * @param
 *
 * @return all logs need be packed
 */
static void get_anr_file() {
    get_wt_logs();

    char expPath[BUF_LEN_256]={0};
    strcat(expPath, DIR_DROPBOX);
    strcat(expPath, wt_cur_logfiles->actual_name);

    if (0 == access(expPath, F_OK)) {
        asprintf(&add_input_list[add_count++], "%s", expPath);
    } else {
        wtlog_info("the path:%s is not exist.", expPath);
    }

}

/**
 * Function to get tombstone logs
 * just for tombstone
 * @param input all logs need be packed
 *
 * @return all logs
 */
static void get_tombstone_file() {
    get_wt_logs();

    char expPath[BUF_LEN_256]={0};
    strcat(expPath, DIR_DROPBOX);
    strcat(expPath, wt_cur_logfiles->actual_name);

    if (0 == access(expPath, F_OK)) {
        asprintf(&add_input_list[add_count++], "%s", expPath);
    } else {
        wtlog_info("the path:%s is not exist.", expPath);
    }

}

/**
 * Function to get system_server_crash logs
 * just for system_server_crash
 * @param input all logs need be packed
 *
 * @return all logs
 */
static void get_sscrash_file() {
    get_wt_logs();

    char expPath[BUF_LEN_256]={0};
    strcat(expPath, DIR_DROPBOX);
    strcat(expPath, wt_cur_logfiles->actual_name);

    if (0 == access(expPath, F_OK)) {
        asprintf(&add_input_list[add_count++], "%s", expPath);
    } else {
        wtlog_info("the path:%s is not exist.", expPath);
    }
}

/**
 * Function to get watchdog logs
 * just for watchdog
 * @param input all logs need be packed
 *
 * @return all logs
 */
static void get_sswatchdog_file() {
    get_wt_logs();

    char expPath[BUF_LEN_256]={0};
    strcat(expPath, DIR_DROPBOX);
    strcat(expPath, wt_cur_logfiles->actual_name);

    if (0 == access(expPath, F_OK)) {
        asprintf(&add_input_list[add_count++], "%s", expPath);
    } else {
        wtlog_info("the path:%s is not exist.", expPath);
    }

}

static char *ignore_library[] =
{
    "libc.so",
    "libclang_rt.asan-arm-android.so",
    "libclang_rt.asan-aarch64-android.so",
    "libc_fdleak_debug.so",
    "unknown",
};

static int is_ignore_library_in_tombtone_file(char *str) {
    if (NULL == str) {
        return 0;
    }
    int library_num = sizeof(ignore_library) / sizeof(ignore_library[0]);
    for (int i = 0; i < library_num; i++) {
        if (strstr(str, ignore_library[i])) {
            return 1;
        }
    }
    return 0;
}

static void get_crash_info_from_logfile(log_info_s *log_info, const char *log_file) {
    char line[BUF_LEN_512] = {0};
    char *start = NULL;
    char *end = NULL;
    FILE *fp = NULL;

    fp = fopen(log_file, "r");
    if (!fp) {
        wtlog_err("%s, open file %s failed", __FUNCTION__, log_file);
        return;
    }

    while (fgets(line, BUF_LEN_512, fp)) {
        /*Process: com.android.development
          Flags: 0x3888be44
          Package: com.android.development v1 (1.0)
          Foreground: Yes
          Build: CMCC/wt86617/wt86617:7.1.1/NMF26F/tangzi08161623:userdebug/dev-keys
        */
        if (!log_info->proc_name[0]) {
            start = strstr(line, "Process: ");
            if (start) {
                strncpy(log_info->proc_name, start+strlen("Process: "), sizeof(log_info->proc_name) - 1);
                rm_tail_space(log_info->proc_name);
                continue;
            }
        }
        if (!log_info->app_version[0]) {
            char tmp_buf[BUF_LEN_128] = {0};

            snprintf(tmp_buf, sizeof(tmp_buf), "Package: %s", log_info->proc_name);
            start = strstr(line, tmp_buf);
            if (start) {
                start = strchr(start+strlen(tmp_buf), '(');
                if (start) {
                    end = strchr(start, ')');
                    if (end)
                        *end = '\0';

                    strncpy(log_info->app_version, start+1, sizeof(log_info->app_version) - 1); // skip '('
                    rm_tail_space(log_info->app_version);
                }
                continue;
            }
        }
        /*get stack info
        com.android.development.BadBehaviorActivity$BadBehaviorException: Whatcha gonna do, whatcha gonna do
            at com.android.development.BadBehaviorActivity$2.onClick(BadBehaviorActivity.java:175)
            at android.view.View.performClick(View.java:5637)*/
        if (!log_info->stack_info1[0]) {
            start = strstr(line, "at ");
            if (start) {
                // get stack_info1
                strncpy(log_info->stack_info1, start, sizeof(log_info->stack_info1) - 1);
                rm_tail_space(log_info->stack_info1);

                // get stack_info2
                if (NULL == fgets(line, BUF_LEN_512 - 1, fp)) {
                    wtlog_err("%s, fgets next line of stack_info1 failed", __FUNCTION__);
                    break;
                }

                start = strstr(line, "at ");
                if (!start) {
                    rm_tail_space(line);
                    wtlog_err("%s, cannot find (at ) in line:%s", __FUNCTION__, line);
                    break;
                }

                strncpy(log_info->stack_info2, start, sizeof(log_info->stack_info2) - 1);
                rm_tail_space(log_info->stack_info2);

                break;
            }
        }
    }
    fclose(fp);
    return;
}

static void get_anr_info_from_logfile(log_info_s *log_info, const char *log_file) {
    char line[BUF_LEN_512] = {0};
    unsigned char flag = 0;
    char *start = NULL;
    char *end = NULL;
    FILE *fp = NULL;

    fp = fopen(log_file, "r");
    if (!fp) {
        wtlog_err("%s, open file %s failed", __FUNCTION__, log_file);
        return;
    }

    while (fgets(line, BUF_LEN_512, fp)) {
        if (!log_info->proc_name[0]) {
            start = strstr(line, "Process: ");
            if (start) {
                strncpy(log_info->proc_name, start+strlen("Process: "), sizeof(log_info->proc_name) - 1);
                rm_tail_space(log_info->proc_name);
                continue;
            }
        }

        if (!log_info->app_version[0]) {
            char tmp_buf[BUF_LEN_128] = {0};

            snprintf(tmp_buf, sizeof(tmp_buf), "Package: %s", log_info->proc_name);
            start = strstr(line, tmp_buf);
            if (start) {
                start = strchr(start+strlen(tmp_buf), '(');
                if (start) {
                    end = strchr(start, ')');
                    if (end)
                        *end = '\0';

                    strncpy(log_info->app_version, start+1, sizeof(log_info->app_version) - 1);
                    rm_tail_space(log_info->app_version);
                }
                continue;
            }
        }

        /* get stack info*/
        char tmp_buf[BUF_LEN_128] = {0};
        if (log_info->proc_name[0]) {
            snprintf(tmp_buf, BUF_LEN_128, "Cmd line: %s", log_info->proc_name);

            if ((flag & 0x1) && strstr(line, "----- end"))
                break;
            if ((flag & 0x2) && (flag & 0x1)) {
                if ((start = strstr(line, " at ")) && !strstr(line, "at android.os.BinderProxy.transact")) {
                    rm_tail_space(line);
                    strncpy(log_info->stack_info1, start, sizeof(log_info->stack_info1)-1);
                    break;
                }
                continue;
            } else {
                if (flag & 0x1) {
                    if (start = strstr(line, "\"main\"")) {
                        flag |= 0x2;
                    }
                    continue;
                }

                if (start = strstr(line, tmp_buf)) {
                    flag |= 0x1;
                }
                continue;
            }
        }
    }
    fclose(fp);
    return;
}

static void get_tombstone_info_from_logfile(log_info_s *log_info, const char *log_file) {
    char line[BUF_LEN_512] = {0};
    char *start = NULL;
    char *end = NULL;
    FILE *fp = NULL;

    fp = fopen(log_file, "r");
    if (!fp) {
        wtlog_err("%s, open file %s failed", __FUNCTION__, log_file);
        return;
    }

    while (fgets(line, BUF_LEN_512 - 1, fp)) {
        if (!log_info->proc_name[0]) {
            start = strstr(line, ">>> ");
            end = strstr(line, " <<<");
            if (start && end) {
                *end = '\0';
                strncpy(log_info->proc_name, start+strlen(">>> "), sizeof(log_info->proc_name) - 1);
                rm_tail_space(log_info->proc_name);
                continue;
            }
        }

        /* get stack info
           backtrace:
           #00 pc 000000000006501c  /system/lib64/libc.so (offset 0x6000)
           #01 pc 0000000001f933bc  /system/framework/arm64/boot-framework.oat (offset 0x16c6000) (android.os.Process.sendSignal+136)
        */
        if (!log_info->stack_info1[0]) {
            start = strstr(line, "#00 pc");
            if (start) {
                while (is_ignore_library_in_tombtone_file(line)) {
                    if (!fgets(line, BUF_LEN_512 - 1, fp)) {
                        goto READ_TOMEBSTONE_LOGFILE_OVER;
                    }
                }

                if (start = strchr(line, '#')) {
                    rm_tail_space(start);
                    strncpy(log_info->stack_info1, start, sizeof(log_info->stack_info1) - 1);
                    goto READ_TOMEBSTONE_LOGFILE_OVER;
                } else {
                    wtlog_err("%s, cannot get tombstone stack\n", __FUNCTION__);
                    goto READ_TOMEBSTONE_LOGFILE_OVER;
                }
            }
            continue;
        }
    }

READ_TOMEBSTONE_LOGFILE_OVER:
    fclose(fp);
    return;
}

static void get_log_info(log_info_s *log_info) {
    if (!log_info){
        wtlog_err("%s, malloc log info failed!", __FUNCTION__);
        return;
    }
    log_files *plogs = wt_cur_logfiles;

    while (plogs && plogs->pfile) {
        if (!strncmp(plogs->match, "app_crash", strlen(plogs->match))
                || !strncmp(plogs->match, "app_anr", strlen(plogs->match))
                || !strncmp(plogs->match, "SYSTEM_TOMBSTONE", strlen(plogs->match))
                || !strncmp(plogs->match, "system_server_crash", strlen(plogs->match))
                || !strncmp(plogs->match, "system_server_watchdog", strlen(plogs->match))) {
            if (strstr(plogs->actual_name, ".txt.gz")) {
                char cmd_buf[BUF_LEN_512] = {0};
                snprintf(cmd_buf, BUF_LEN_512, "%s %s%s > %s", "/system/bin/gzip -d -c", plogs->pfile, plogs->actual_name, MONITOR_UNZIP_TMP_FILE);
                SYSTEM(cmd_buf);

               struct passwd *p = getpwnam("system");
                if (p)
                    chown(MONITOR_UNZIP_TMP_FILE, p->pw_uid, p->pw_gid);
                chmod(MONITOR_UNZIP_TMP_FILE, 0600);

                if (!strncmp(plogs->match, "app_crash", strlen(plogs->match))
                        || !strncmp(plogs->match, "system_server_crash", strlen(plogs->match))) {
                    get_crash_info_from_logfile(log_info, MONITOR_UNZIP_TMP_FILE);
                } else if (!strncmp(plogs->match, "app_anr", strlen(plogs->match))
                        || !strncmp(plogs->match, "system_server_watchdog", strlen(plogs->match))) {
                    get_anr_info_from_logfile(log_info, MONITOR_UNZIP_TMP_FILE);
                } else if (!strncmp(plogs->match, "SYSTEM_TOMBSTONE", strlen(plogs->match))) {
                    get_tombstone_info_from_logfile(log_info, MONITOR_UNZIP_TMP_FILE);
                }

                snprintf(cmd_buf, BUF_LEN_512, "rm -f %s > /dev/null 2>&1", MONITOR_UNZIP_TMP_FILE);
                SYSTEM(cmd_buf);
            } else {
                char filename[BUF_LEN_512] = {0};
                snprintf(filename, BUF_LEN_512, "%s%s", plogs->pfile, plogs->actual_name);

                if (!strncmp(plogs->match, "app_crash", strlen(plogs->match))
                        || !strncmp(plogs->match, "system_server_crash", strlen(plogs->match))) {
                    get_crash_info_from_logfile(log_info, filename);
                } else if (!strncmp(plogs->match, "app_anr", strlen(plogs->match))
                        || !strncmp(plogs->match, "system_server_watchdog", strlen(plogs->match))) {
                    get_anr_info_from_logfile(log_info, filename);
                } else if (!strncmp(plogs->match, "SYSTEM_TOMBSTONE", strlen(plogs->match))) {
                    get_tombstone_info_from_logfile(log_info, filename);
                }
            }
            break;
        }
        plogs++;
    }

    char* position = strchr(plogs->actual_name, '@');
    char date[BUF_LEN_128];
    if (position != NULL) {
        position++;
        strncpy(date, position, BUF_LEN_128);
        position = strchr(date, '.');
        if (position != NULL)
            *position = '\0';
    } else {
        strncpy(date, "unknown", BUF_LEN_128);
    }
    strcat(log_info->file_path, DIR_LOGFILE);
    strcat(log_info->file_path, "ErrorType_");
    strcat(log_info->file_path, plogs->type_str);
    strcat(log_info->file_path, "_");
    strcat(log_info->file_path, date);
    strcat(log_info->file_path, ".tar.gz");

    if (!log_info->stack_info1[0]) {
            strncpy(log_info->stack_info1, "unknown", sizeof(log_info->stack_info1) - 1);
    }
    if (!log_info->stack_info2[0]) {
            strncpy(log_info->stack_info2, "unknown", sizeof(log_info->stack_info2) - 1);
    }
    if (!log_info->proc_name[0]) {
        strncpy(log_info->proc_name, "unknown", sizeof(log_info->proc_name) - 1);
    }
    if (!log_info->app_version[0]) {
        strncpy(log_info->app_version, "unknown", sizeof(log_info->app_version) - 1);
    }
    return;
}

/**
 * Function to capture logs about the event.
 * e.g. anr, crash, tombstone, vmreboot
 * @param void
 *
 * @return void
 */
static void capture_log_files()
{
    switch (wt_cur_logfiles->type) {
        case ANR:
            get_anr_file();
            break;
        case APPCRASH:
            get_crash_file();
            break;
        case TOMBSTONE:
            get_tombstone_file();
            break;
        case SSCRASH:
            get_sscrash_file();
            break;
        case SSWATCHDOG:
            get_sswatchdog_file();
            break;
         default:
            break;
    }
}

static void dump_logcat(char *dump_path)
{
    char cmd_buf[BUF_LEN_256] = {0};
    char name_buf[BUF_LEN_256] = {0};

    struct passwd *p = NULL;
    p = getpwnam("system");

    if (access(dump_path, F_OK) < 0)
    {
        wtlog_info("%s: %s directory not exists, create it.", __FUNCTION__, dump_path);
        mkPathDir(dump_path, 0755);
    }
    if (p) {
        chown(dump_path, p->pw_uid, p->pw_gid);
    }

    //logcat main log
    sprintf(cmd_buf, "%s >%s%s", "logcat -b main -b system -v threadtime -t 4096", dump_path, "log_main");
    SYSTEM(cmd_buf);
    sprintf(name_buf, "%s%s", dump_path, "log_main");
    if (p) {
        chown(name_buf, p->pw_uid, p->pw_gid);
    }
    asprintf(&add_input_list[add_count++], "%s", name_buf);
    asprintf(&del_input_list[del_count++], "%s", name_buf);

    //logcat events log
    sprintf(cmd_buf, "%s >%s%s", "logcat -b events -v threadtime -t 4096", dump_path, "log_events");
    SYSTEM(cmd_buf);
    sprintf(name_buf, "%s%s", dump_path, "log_events");
    if (p) {
        chown(name_buf, p->pw_uid, p->pw_gid);
    }
    asprintf(&add_input_list[add_count++], "%s", name_buf);
    asprintf(&del_input_list[del_count++], "%s", name_buf);

    //logcat radio log
    sprintf(cmd_buf, "%s >%s%s", "logcat -b radio -v threadtime -t 4096", dump_path, "log_radio");
    SYSTEM(cmd_buf);
    sprintf(name_buf, "%s%s", dump_path, "log_radio");
    if (p) {
        chown(name_buf, p->pw_uid, p->pw_gid);
    }
    asprintf(&add_input_list[add_count++], "%s", name_buf);
    asprintf(&del_input_list[del_count++], "%s", name_buf);

    //logcat kernel log
    sprintf(cmd_buf, "%s >%s%s", "dmesg", dump_path, "log_kmsg");
    SYSTEM(cmd_buf);
    sprintf(name_buf, "%s%s", dump_path, "log_kmsg");
    if (p) {
        chown(name_buf, p->pw_uid, p->pw_gid);
    }
    asprintf(&add_input_list[add_count++], "%s", name_buf);
    asprintf(&del_input_list[del_count++], "%s", name_buf);
}

static void process_event_by_imonitor(char *output_file_name) {
    /**compute the size about the dir of Wtlog_Monitor,
    **but maybe the code will be removed in future.
    */
    /*int size = GetDirectorySize(DIR_LOGFILE);
    if (size > MAX_LOG_SIZE) {
        wtlog_info("the size is over %d, the event will be dropped", MAX_LOG_SIZE);
        return;
    }*/
    if (GetDiskFree(DISK_DATA) < MIN_FREE_DATA) {
        wtlog_info("the disk of data free size is under 1G, the event will be dropped");
        return;
    }

    add_count = 0;
    del_count = 0;

    //begin reserve the all logs that needed to be captured
    dump_logcat(DIR_TMPFILE);
    capture_log_files();

    if (access(DIR_LOGFILE, F_OK) < 0)
    {
        wtlog_info("%s: %s directory not exists, create it.", __FUNCTION__, DIR_LOGFILE);
        mkPathDir(DIR_LOGFILE, 0755);

        struct passwd *p = getpwnam("system");
        if (p) {
            chown(DIR_LOGFILE, p->pw_uid, p->pw_gid);
        }
    }

    //zip the log to the path
    int i = 0;
    if (add_count > 0) {
        wt_compress_zip_files(add_count, (const char **)add_input_list , output_file_name,  0);
        //free space
        for (i = 0; i < add_count; i++) {
            free(add_input_list[i]);
        }
    }

    //free space
    for (i = 0; i < del_count; i++) {
        unlink(del_input_list[i]);
        free(del_input_list[i]);
    }
}

/**
 * Function to find the match event
 * e.g. anr, crash, tombstone, vmreboot
 * @param event
 *
 * @return if find success, return 1
 */
static int find_first_match(struct inotify_event *event, char *strpath) {
    int i = 0;
    int is_find =0;

    //wtlog_info("%s:Event name is %s.", __FUNCTION__, event->name);
    if (startsWith("mark_", event->name)) {
        return 0;
    }

    for (i = 0; i < FAULTMAX; i++) {
        struct LOG_FILE *plogs = wt_log_exception[i].plogs;

        while (NULL != plogs->pfile && !strcmp(plogs->pfile, strpath)) {
            //look for match
            if (NULL != plogs->match) {
                //main match
                if (NULL != strstr(event->name, plogs->match)) {
                    is_find = 1;
                    strncpy(plogs->actual_name, event->name, BUF_LEN_128);
                    break;
                }

                plogs++;
            }
        }

        if (is_find) {
            wtlog_info("%s:Event name is %s, type is %s.", __FUNCTION__, event->name, plogs->match);
            wt_cur_logfiles = plogs;
            wt_ppcur_logfiles = &(wt_log_exception[i].plogs); //wt_ppcur_logfiles = &wt_cur_logfiles
            break;
        }
    }

    return is_find;
}

int is_find_match = 0;

static int send_fifo_msg(log_info_s* log_info) {
    int res = -1;
    if (fd_notify < 0) {
        fd_notify = init_notify_fifo(O_WRONLY);
        if (fd_notify < 0) {
            wtlog_err("%s:mkfifo failed", __FUNCTION__);
            return -1;
        }
    }

    int retry = 0;
    while(write(fd_notify, log_info, sizeof(log_info_s)) < 0) {
        wtlog_err("%s: write fifo error.", __FUNCTION__);
        usleep(1000);
        if (retry++ > 1000 || errno != EAGAIN) {
            break;
        }
    }
    return 0;
}

static int process_one_event(struct inotify_event *event, char *strpath) {
    is_find_match = find_first_match(event, strpath);

    if (is_find_match) {
        log_info_s *log_info = NULL;
        log_info = (log_info_s *)malloc(sizeof(log_info_s));
        memset(log_info, 0, sizeof(log_info_s));
        get_log_info(log_info);

        process_event_by_imonitor(log_info->file_path);

        send_fifo_msg(log_info);

        free(log_info);

        return 0;
    } else {
        return -1;
    }

    return 0;
}

static int handle_logfile_events(int fd, int is_timeout)
{
    int i = 0;
    int wd = -1;
    struct inotify_event *event = NULL;
    char strpath[BUF_LEN_256] = {0};
    int tmp_len = 0;
    char *offset = NULL;
    int len;
    char buffer[BUF_LEN_1K] = {0};

    len = read(fd, buffer, BUF_LEN_1K);
    if (len > 0) {
        for (offset = buffer; offset < buffer + len;) {
            event = (struct inotify_event *) offset;
            memset((void *)strpath, 0, BUF_LEN_256);
            for (i=0; wd_array[i].name != NULL; i++) {
                if (event->wd == wd_array[i].wd) {
                    strncpy(strpath, wd_array[i].name, BUF_LEN_256 - 1);
                    break;
                }
            }
            if (event->len > 0) {
                process_one_event(event, strpath);
            }

            offset += sizeof(struct inotify_event) + event->len;
        }
    }

    return 0;
}

static int add_watch_dir(char *path, unsigned int event)
{
    int wd = -1;
    if (g_inotify_fd > 0)
    {
        wd = inotify_add_watch(g_inotify_fd, path, event);
        if (wd < 0) {
            wtlog_err("%s:%d error.", __FUNCTION__, __LINE__);
        }
    }
    return wd;
}


static void init_watch_dirs() {
    int i = 0;
    for (i = 0; path_array[i].path != NULL; i++) {
        if (access(path_array[i].path, F_OK)) {
            wtlog_info("%s:%s not exist,then make dir.", __FUNCTION__, path_array[i].path);
            mkPathDir(path_array[i].path, path_array[i].mode);
        }
        struct passwd *p = getpwnam("system");
        if (p)
            chown(path_array[i].path, p->pw_uid, p->pw_gid);
    }

}

static void create_apanic_file_name(char *output_file_name, char *type) {
    strcat(output_file_name, DIR_LOGFILE);
    strcat(output_file_name, "ErrorType_");
    strcat(output_file_name, type);

    time_t local_time;
    struct tm *tm;
    local_time = time(NULL);
    tm = localtime(&local_time);
    sprintf(output_file_name,
                "%s_%04d%02d%02d%02d%02d%02d_all", output_file_name,
                tm->tm_year+1900,tm->tm_mon+1, tm->tm_mday, tm->tm_hour, tm->tm_min,
                tm->tm_sec);
    strcat(output_file_name, ".tar.gz");
}

static void check_and_compress_apanic_log() {
    if(!access(PANIC_LOG_FOLDER, F_OK)
            && access(PANIC_LOG_COMPRESSED_FLAG_FILE, F_OK)) {
        wtlog_info("%s, Compress panic log...", __FUNCTION__);
        char output_file_name[BUF_LEN_256] = {0};
        create_apanic_file_name(output_file_name, "apanic");
        add_count = 0;
        asprintf(&add_input_list[add_count++], "%s", PANIC_LOG_FOLDER);
        wtlog_info("%s, Compress panic log...\n", __FUNCTION__);
        wt_compress_zip_files(add_count, (const char **)add_input_list, output_file_name, 0);

        log_info_s *log_info = NULL;
        log_info = (log_info_s *)malloc(sizeof(log_info_s));
        memset(log_info, 0, sizeof(log_info_s));

        strncpy(log_info->file_path, output_file_name, sizeof(log_info->file_path) - 1);
        strncpy(log_info->stack_info1, "unknown", sizeof(log_info->stack_info1) - 1);
        strncpy(log_info->stack_info2, "unknown", sizeof(log_info->stack_info2) - 1);
        strncpy(log_info->proc_name, "kernel", sizeof(log_info->proc_name) - 1);
        strncpy(log_info->app_version, "unknown", sizeof(log_info->app_version) - 1);

        send_fifo_msg(log_info);
        free(log_info);

        int fd = open(PANIC_LOG_COMPRESSED_FLAG_FILE, O_CREAT, 0600);
        if (fd < 0)
        {
            wtlog_err("%s, Could not create file %s", __FUNCTION__, PANIC_LOG_COMPRESSED_FLAG_FILE);
            return;
        }
        close(fd);
    }
}

static void *logfile_exception_monitor(void *arg)
{
    int fd;
    int i = 0,fdmax;
    fd_set fds,fdsback;

    init_watch_dirs();
    check_and_compress_apanic_log();

    g_inotify_fd = fd = inotify_init();
    if (fd == -1) {
        wtlog_err("%s:%d error:%d", __FUNCTION__, __LINE__, fd);
        pthread_exit(0);
    }

    for(i = 0; wd_array[i].name != NULL; i++) {
        wd_array[i].wd = add_watch_dir(wd_array[i].name, wd_array[i].event);
    }

    while(1) {
        FD_ZERO(&fds);
        FD_SET(fd, &fds);

        int ret = select(fd+1, &fds, NULL, NULL, NULL);
        if (ret > 0) {
            handle_logfile_events(fd, 0);
        } else {
            continue;
        }
    }

    pthread_exit(0);
    return NULL;
}

int start_log_monitor()
{
    if (pthread_create(&g_tid_logfile_monitor, NULL, logfile_exception_monitor, NULL))
    {
        wtlog_err("Start monitor thread fail.");
        return -1;
    }
    wtlog_info("Start monitor thread success.");

    return 0;
}

void stop_log_monitor()
{
    if ( -1 != g_tid_logfile_monitor)
    {
        pthread_join(g_tid_logfile_monitor, NULL);
        g_tid_logfile_monitor = -1;
    }
}