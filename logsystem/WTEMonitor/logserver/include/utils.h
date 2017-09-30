#ifdef __cplusplus
extern "C"
{
#endif

#include <sys/stat.h>
#include <stdint.h>

    #define TRUE 1
    #define FALSE 0
    #define PROPERTY_VALUE_MAX 40

    extern int exec_cmd_line(const char *command, const char *file,
            const char *function_name, const int line_number);

    #define SYSTEM(s) do { exec_cmd_line(s, __FILE__, __FUNCTION__, __LINE__); } while(0)

    int mkdir_p (const char *path, mode_t mode);
    int is_system_ok();
    int mkPathDir (const char *input_path, mode_t mode);

    void wtlog_err(const char *fmt,...);
    void wtlog_info(const char *fmt,...);

    int is_debug_version ();
    int GetDirectorySize(const char *dir);
    int64_t GetDiskFree(const char *dir);

    void rm_tail_space(char *str);
    void replace_slash(char *str);
    int startsWith(const char *pre, const char *str);


#ifdef __cplusplus
}
#endif
