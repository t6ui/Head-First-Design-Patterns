#include "utils.h"
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <pwd.h>
#include <utils/Log.h>
#include <cutils/properties.h>
#include <sys/stat.h>
#include <sys/statfs.h>
#include <dirent.h>
#include <sys/types.h>
#include <ctype.h>
#include "logserver.h"

void rm_tail_space(char *str) {
    if (!str)
        return;

    for (int i = (int)strlen(str)-1; i >= 0; i-- ){
        if (isspace(str[i]))
            str[i] = '\0';
        else
            break;
    }
}

int startsWith(const char *pre, const char *str)
{
    size_t lenpre = strlen(pre),
           lenstr = strlen(str);
    return lenstr < lenpre ? false : strncmp(pre, str, lenpre) == 0;
}

/**
* Function to exec cmd
* 
* @param command
*
* @return
*/
int exec_cmd_line(const char *command, const char *file,
            const char *function_name, const int line_number) {
    if (command == NULL || function_name == NULL || file == NULL) {
        wtlog_err("exec cmd:%s failed.", command);
        return -1;
    }
    return system(command);
}


/**
* Function to mkdir circul if the path not exist
* 
* @param path
*
* @return if success return 0;
*/
int mkdir_p (const char *input_path, mode_t mode) {
    int len;
    char path[BUF_LEN_512] = {0};
    struct passwd *p = getpwnam("system");

    if (NULL == input_path || strlen(input_path) >= BUF_LEN_512)
    {
        wtlog_err("input_path is null or too length.");       
        return -1;
    }
    memset((void *)path, 0, BUF_LEN_512);
    strcpy(path, input_path);

    len = strlen(input_path);
    if ('/' != path[len-1]) {
        path[len++] = '/';
    }

    struct stat st;
    int err;
    char *d = path;
    
    if (*d != '/')
        return -1;
    
    if (stat(path, &st) == 0)
        return 0;
    
    while (*++d == '/');
    
    while ((d = strchr(d, '/'))) {
        *d = '\0';
        if (access(path, F_OK)) {
            if (mkdir(path, mode)) {
                wtlog_err("make file failed.");
                return -1;
            }

            if (p) {
                chown(path, p->pw_uid, p->pw_gid);
            }

        }
        *d++ = '/';
        while (*d == '/')
            ++d;
    }

    return 0;
}

int is_system_ok() {
    if (0 == access ("/system/bin", F_OK)) {
        return 1;
    }
    return 0;
}

/**
* Function to mkdir success if the path is not exist.
* 
* @param path
*
* @return if success return 0;
*/
int mkPathDir (const char *input_path, mode_t mode) {
    int system_status;
    int try_count = 10;

    system_status = is_system_ok();
    if (system_status == 0) {
        try_count = 0;
    }

    while (try_count-- >0) {
        if (access(input_path, F_OK)) {
            int ret = mkdir_p(input_path, mode);
            if (ret) {
                wtlog_err("mkdir failed,we will try again.");
                sleep(10);
                continue;
            }
        }

        struct passwd *p = getpwnam("system");
        if (p) {
            chown(input_path, p->pw_uid, p->pw_gid);
        }
        return 0;
    }
    return -1;
}

void wtlog_info (const char *fmt, ...) {
    va_list ap;
    char buf [BUF_LEN_512];
    va_start (ap, fmt);
    vsnprintf(buf, BUF_LEN_512, fmt, ap);
    va_end(ap);
    char buf1 [BUF_LEN_512];
    snprintf(buf1, BUF_LEN_512, "wtlog-%s", buf);
    ALOGI("%s", buf1);
    return;
}

void wtlog_err (const char *fmt, ...) {
    va_list ap;
    char buf [BUF_LEN_512];
    va_start (ap, fmt);
    vsnprintf(buf, BUF_LEN_512, fmt, ap);
    va_end(ap);
    char buf1 [BUF_LEN_512];
    snprintf(buf1, BUF_LEN_512, "wtlog-%s", buf);
    ALOGE("%s", buf1);
    return;
}

/**
* Function to get what user version, debug or user
*
* @param none
*
* @return if debug return 1, if user return 0
*/

int is_debug_version () {
    char buf[PROPERTY_VALUE_MAX] = { };
    bool enable = property_get("persist.sys.wt_debug", buf, "false") >= 0 &&
                 strncmp(buf, "true", sizeof (buf)) == 0;
    if (enable) {
        wtlog_info("this is debug version, enable wtmonitor");
        return TRUE;
    }
    wtlog_info("this is user version, disable wtmonitor");

    return FALSE;
}

/**
* Function to compute the size about the dir of Wtlog_Monitor
*
* @param path
*
* @return the size
*/
int GetDirectorySize(const char *dir) {

    DIR *dp;
    struct dirent *entry;
    struct stat statbuf;
    int totalSize = 0;

    if ((dp = opendir(dir)) == NULL)
    {
    	wtlog_err("%s:the dir:%s maybe not exist.", __FUNCTION__, dir);
    	return -1;
    }


    while ((entry = readdir(dp)) != NULL)
    {
        if (strcmp(".", entry->d_name) == 0 ||
        		strcmp("..", entry->d_name) == 0)
        {
        	continue;
        }

        char subdir[256];
    	sprintf(subdir, "%s%s", dir, entry->d_name);
    	lstat(subdir, &statbuf);

    	if (S_ISDIR(statbuf.st_mode))
    	{
    		int subDirSize = GetDirectorySize(subdir);
    		totalSize += subDirSize;
    	}
    	else
    	{
    		totalSize += statbuf.st_size;
    	}
    }

    closedir(dp);
    return totalSize;
}

/**
* Function to compute the available size about disk
*
* @param path
*
* @return the size
*/
int64_t GetDiskFree(const char *dir) {

    struct statfs sfs;
    if (statfs(dir, &sfs) == 0) {
        return sfs.f_bavail * sfs.f_bsize;
    } else {
        wtlog_err("%s:Couldn't statfs", __FUNCTION__);
        return -1;
    }

}
