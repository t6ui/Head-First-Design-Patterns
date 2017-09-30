#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/time.h>
#include <errno.h>
#include <fcntl.h>
#include <utils/Log.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <cutils/sockets.h>
#include <cutils/fs.h>
#include <pthread.h>
#include <pwd.h>
#include <inttypes.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <private/android_filesystem_config.h>
#include "logserver.h"
#include "socket.h"
#include "utils.h"

static int fd_pipe_read = -1;
static int fd_pipe_write = -1;

int fd_socket_server = -1;
int fd_socket_client = -1;

pthread_t g_tid_log_socket = -1;


static int get_maxfd() {
    int max;
    max = fd_socket_server > fd_socket_client ? fd_socket_server : fd_socket_client;
    max = max > fd_pipe_read ? max : fd_pipe_read;
    return max;
}

static int get_client_sokcet() {
    return fd_socket_client;
}

static void set_client_sokcet(int client) {
    fd_socket_client = client;
}

static int get_server_sokcet() {
    return fd_socket_server;
}

static void set_server_sokcet(int server) {
    fd_socket_server = server;
}

int init_notify_fifo(int open_mode) {
    int ret = 0;
    int pipe_fd = -1;
    struct passwd *p = getpwnam("system");

    if (access(FIFO_LOG_CONTROL, F_OK) == -1) {
        ret = mkfifo(FIFO_LOG_CONTROL, 0600);

        if (ret != 0) {
            wtlog_err("%s:mkfifo failed", __FUNCTION__);
            return -1;
        }
    }

    if (p) {
        chown(FIFO_LOG_CONTROL, p->pw_uid, p->pw_gid);
    }

    pipe_fd = open(FIFO_LOG_CONTROL, open_mode | O_NONBLOCK); //非阻塞管道

    if (pipe_fd < 0) {
        wtlog_err("Failed to open pipe : %s\n", strerror(errno));
        return -1;
    }

    if (O_RDONLY & open_mode) {
        fd_pipe_read = pipe_fd;
    } else if (O_WRONLY & open_mode) {
        fd_pipe_write = pipe_fd;
    }

    return pipe_fd;
}

static int create_socket(const char *name, int type, mode_t perm, uid_t uid,
                  gid_t gid)
{
    struct sockaddr_un addr;
    int fd, ret;

    fd = socket(PF_UNIX, type, 0);
    if (fd < 0) {
        wtlog_err("Failed to open socket '%s': %s\n", name, strerror(errno));
        return -1;
    }

    memset(&addr, 0 , sizeof(addr));
    addr.sun_family = AF_UNIX;
    snprintf(addr.sun_path, sizeof(addr.sun_path), ANDROID_SOCKET_DIR"/%s",
             name);

    ret = unlink(addr.sun_path);
    if (ret != 0 && errno != ENOENT) {
        wtlog_err("Failed to unlink old socket '%s': %s\n", name, strerror(errno));
        goto out_close;
    }

    ret = bind(fd, (struct sockaddr *) &addr, sizeof (addr));
    if (ret) {
        wtlog_err("Failed to bind socket '%s': %s\n", name, strerror(errno));
        goto out_unlink;
    }

    chown(addr.sun_path, uid, gid);
    chmod(addr.sun_path, perm);

    wtlog_info("%s bind success, with mode '%o', user '%d', group '%d'\n",
         addr.sun_path, perm, uid, gid);

    return fd;

out_unlink:
    unlink(addr.sun_path);
out_close:
    close(fd);
    return -1;
}

static int create_socket_control (const char * socketPath) {
    int s = create_socket(socketPath, SOCK_STREAM, 0660, AID_SYSTEM, AID_SYSTEM);

    if (s < 0) {
        wtlog_err("start wtsocket creation failed: %s\n", strerror(errno));
        return -1;
    }

    fcntl(s, F_SETFD, FD_CLOEXEC);

    if (listen(s, 5) < 0) {
        wtlog_err("socket listen fail.\n");
        return -1;
    }

    set_server_sokcet(s);
    return s;
}


int init_reserve_file(int open_mode)
{
    int ret = 0;
    int fd = -1;
    const char *file_path = SOCKET_RESERVED_FILE;

    if (access(MONITOR_CONFIG_DIR, F_OK) < 0)
    {
        wtlog_info("%s: %s directory not exists, create it.", __FUNCTION__, MONITOR_CONFIG_DIR);
        mkPathDir(MONITOR_CONFIG_DIR, 0755);
    }

    if (access(SOCKET_RESERVED_FILE, F_OK) == -1)
    {
        if (open_mode == O_RDONLY)
        {
            wtlog_err("%s, %s isn't existed", __FUNCTION__, SOCKET_RESERVED_FILE);
            return -1;
        }
    }
    if (open_mode == O_RDONLY)
    {
        if (rename(SOCKET_RESERVED_FILE, SOCKET_RESERVED_FILE "_bak") < 0)
        {
            wtlog_err("%s, rename fail %s", __FUNCTION__, strerror(errno));
            return -1;
        }
        file_path = SOCKET_RESERVED_FILE "_bak";
    }

    fd = open(file_path, open_mode, 0660);

//    struct passwd *p = NULL;
//    p = getpwnam("system");
//    if (p) {
//        chown(reserved_file_name, p->pw_uid, p->pw_gid);
//    }

    if (fd < 0)
    {
        wtlog_err("%s, Could not create file %s", __FUNCTION__, file_path);
        return -1;
    }

    return fd;
}


int send_to_client(const char* msg)
{
    wtlog_info("%s, send socket message: %s\n", __FUNCTION__, msg);

    int sockfd;
    sockfd = get_client_sokcet();

    if (-1 != sockfd)
    {
        int res;
        res = send(sockfd, msg, strlen(msg), MSG_NOSIGNAL); //客户端如果挂掉，进程不会终止。
        if (res < 0)
        {
            wtlog_err("%s, send failed, errno=%s\n", __FUNCTION__, strerror(errno));
            close(sockfd);
            set_client_sokcet(-1);
        }
    }
    if (-1 == get_client_sokcet())
    {
        int fd = init_reserve_file(O_WRONLY | O_CREAT | O_APPEND);
        if (fd < 0)
        {
            return -1;
        }
        struct stat st;
        if (fstat(fd, &st) < 0)
        {
            wtlog_err("%s, fstat failed, errno=%s\n", __FUNCTION__, strerror(errno));
            close(fd);
            return 0;
        }
        wtlog_info("Send to client fail, save message in %s.\n", SOCKET_RESERVED_FILE);
        if (write(fd, msg, strlen(msg)) < 0)
        {
            wtlog_err("%s, write failed, errno=%s\n", __FUNCTION__, strerror(errno));
        }
        close(fd);
    }
    return 0;
}

static void build_socket_string(const log_info_s *log_info, char * socketMsg)
{
    memset((void*)socketMsg, 0, sizeof(socketMsg));
    snprintf(socketMsg, SOCKET_MSG_LEN,
             "FILE_PATH=%s,STACK_INFO1=%s,STACK_INFO2=%s,PROC_NAME=%s,APP_VERSION=%s\n",
              log_info->file_path, log_info->stack_info1, log_info->stack_info2,
              log_info->proc_name, log_info->app_version);
}

int handle_fifo_event(int fifo_fd)
{
    log_info_s *log_info = NULL;
    log_info = (log_info_s *)malloc(sizeof(log_info_s));
    memset(log_info, 0, sizeof(log_info_s));

    char msg[SOCKET_MSG_LEN] = {0};
    int ret = -1;

    while ((ret = read(fifo_fd, log_info, sizeof(log_info_s))) > 0)
    {
        build_socket_string(log_info, msg);
        send_to_client(msg);
    }
    free(log_info);
    //close(fifo_fd); //管道在while(1)循环中使用中，不需要关闭
    return 0;
}

static void send_reserved_msg() {
    int fd = init_reserve_file(O_RDONLY);
    if (fd < 0)
    {
        return;
    }
    FILE *fp = fdopen(fd, "r");
    if (fp == NULL)
    {
        close(fd);
        return;
    }
    char *line = NULL;
    ssize_t read;
    size_t len = 0;
    while ((read = getline(&line, &len, fp)) != -1)
    {
        send_to_client(line);
    }

    free(line);
    fclose(fp);
    remove(SOCKET_RESERVED_FILE "_bak");
}

static int handle_connect_event (int socket_server) {
    sockaddr addr;
    socklen_t alen;
    int sockfd;

    alen = sizeof(addr);

    sockfd = get_client_sokcet();
    if (-1 != sockfd) {
        close(sockfd);
        sockfd = -1;
        set_client_sokcet(sockfd);
    }

    sockfd = accept(socket_server, &addr, &alen);
    if (sockfd < 0) {
        wtlog_info("%s: connect socket client fail.\n", __FUNCTION__);
    }
    fcntl(sockfd, F_SETFD, FD_CLOEXEC);
    set_client_sokcet(sockfd);

    wtlog_info("%s: connect client socket fd: %d\n", __FUNCTION__, sockfd);

    send_reserved_msg();

    return sockfd;
}



static void *thread_log_socket(void *arg)
{
    int fd;
    int i = 0,fdmax;
    fd_set fds,fdsback;
    int ret = -1;
    int fifo_fd;
    int socket_server = -1;
    char buffer[BUF_LEN_64] = {0};


    fifo_fd = init_notify_fifo(O_RDONLY);

    if (-1 == fifo_fd) {
        wtlog_err("%s: fifo make failed.", __FUNCTION__);
        pthread_exit(NULL);
        return NULL;
    }

    socket_server = create_socket_control(SOCKET_NODE_LOG_CONTROL);

    if (socket_server < 0) {
        wtlog_err("%s: socket_server make failed.", __FUNCTION__);
        pthread_exit(NULL);
        return NULL;
    }

    while(1) {
        FD_ZERO(&fds);
        FD_SET(fifo_fd, &fds);
        FD_SET(socket_server, &fds);

        fdmax = get_maxfd();

        ret = select(fdmax + 1, &fds, NULL, NULL, NULL);

        if (ret > 0) {

            if (FD_ISSET(fifo_fd, &fds)) {
                FD_CLR(fifo_fd, &fds);
                handle_fifo_event(fifo_fd);
            }
            if (FD_ISSET(socket_server, &fds)) {
                FD_CLR(socket_server, &fds);
                handle_connect_event(socket_server);
            }
        } else {
            continue;
        }
    }

    pthread_exit(NULL);
    return NULL;
}


int start_log_control ()
{

    if (pthread_create(&g_tid_log_socket, NULL, thread_log_socket, NULL))
    {
        wtlog_err("Start socket thread fail.");
        return -1;
    }
    wtlog_info("Start socket thread success.");

    return 0;
}

void stop_log_control()
{
    if (-1 != g_tid_log_socket)
    {
        pthread_join(g_tid_log_socket, NULL);
        g_tid_log_socket = -1;
    }
}


