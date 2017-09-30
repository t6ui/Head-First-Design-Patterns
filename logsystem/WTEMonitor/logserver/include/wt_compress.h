/*
 * Copyright (C) 2012-2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//+bug241393,shihaikuo.wt,add,20170512,add for logcatkmsg tool

#ifdef __cplusplus
    extern "C" {
#endif


#ifndef _WT_TAR_ZIP_H
#define _WT_TAR_ZIP_H

typedef enum {
    E_PACK_TAR = 0, /* only do tar, do not do compress */
    E_PACK_ZIP = 1, /* compress */
    E_PACK_MOVE = 2, /*move to the sep*/
}E_PACK_TYPE;

void wt_compress_zip_files(int name_count, const char** input_names, char* output_name, int keep_parent);
void wt_compress_files(int counts, const char** input, char* output);

#endif //_LOG_TAR_ZIP_H

#ifdef __cplusplus
    }
#endif

//-bug241393,shihaikuo.wt,add,20170512,add for logcatkmsg tool
