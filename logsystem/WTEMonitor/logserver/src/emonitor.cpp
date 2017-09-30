#include "logserver.h"
#include "utils.h"
#include <stdio.h>
#include <utils/Log.h>
#include <cutils/properties.h>
#include <string.h>

int main(int argc, char **argv __unused) {
    char pmd_enable[PROPERTY_VALUE_MAX] = {'\0'};
    property_get("persist.sys.pdm.enable", pmd_enable, "false");
    if (!strncmp(pmd_enable, "true", PROPERTY_VALUE_MAX)) {
        logserver_init();
    } else {
        wtlog_info("pdm is not supported on this device.");
    }
    return 0;
}
