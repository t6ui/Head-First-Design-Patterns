#ifdef __cplusplus
    extern "C" {
#endif

#define FIFO_LOG_CONTROL "/data/log/logcontrol"
#define SOCKET_NODE_LOG_CONTROL "logcontrol"
#define MONITOR_CONFIG_DIR "/data/log/monitor_config"
#define SOCKET_RESERVED_FILE "/data/log/monitor_config/reserved_msg"

#define SOCKET_MSG_LEN      (16*1024)

int init_notify_fifo(int open_mode);
int start_log_control();
void stop_log_control();

#ifdef __cplusplus
    }
#endif