



int start_apanic_monitor()
{
    if (pthread_create(&g_tid_logfile_monitor, NULL, logfile_exception_monitor, NULL))
    {
        wtlog_err("Start monitor thread fail.");
        return -1;
    }
    wtlog_info("Start monitor thread success.");

    return 0;
}

void stop_apanic_monitor()
{
    if ( -1 != g_tid_logfile_monitor)
    {
        pthread_join(g_tid_logfile_monitor, NULL);
        g_tid_logfile_monitor = -1;
    }
}