/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.example.android.notepad;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 定义 Note Pad 内容提供者与其客户端之间的契约。契约定义了客户端访问提供者所需的信息，
 * 作为一个或多个数据表。契约是一个公共的、不可扩展的（最终）类，包含定义列名和 URI 的常量。
 * 一个写得好的客户端只依赖于契约中的常量。
 */
public final class NotePad {
    public static final String AUTHORITY = "com.google.provider.NotePad";

    // 此类无法被实例化
    private NotePad() {
    }

    /**
     * 笔记表契约
     */
    public static final class Notes implements BaseColumns {

        // 此类无法被实例化
        private Notes() {}

        /**
         * 本提供者提供的表名
         */
        public static final String TABLE_NAME = "notes";

        /*
         * URI 定义
         */

        /**
         * 本提供者 URI 的 scheme 部分
         */
        private static final String SCHEME = "content://";

        /**
         * URI 的路径部分
         */

        /**
         * 笔记 URI 的路径部分
         */
        private static final String PATH_NOTES = "/notes";

        /**
         * 笔记 ID URI 的路径部分
         */
        private static final String PATH_NOTE_ID = "/notes/";

        /**
         * 笔记 ID URI 的路径中相对位置的 0 基索引
         */
        public static final int NOTE_ID_PATH_POSITION = 1;

        /**
         * 活动文件夹 URI 的路径部分
         */
        private static final String PATH_LIVE_FOLDER = "/live_folders/notes";

        /**
         * 此表的 content:// 风格的 URL
         */
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);

        /**
         * 单个笔记的内容 URI 基础。调用者必须
         * 在此 URI 后面附加一个数字笔记 ID 以检索笔记
         */
        public static final Uri CONTENT_ID_URI_BASE
                = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID);

        /**
         * 针对单个笔记的内容 URI 匹配模式，通过其 ID 指定。使用此模式来匹配
         * 传入的 URIs 或构造 Intent。
         */
        public static final Uri CONTENT_ID_URI_PATTERN
                = Uri.parse(SCHEME + AUTHORITY + PATH_NOTE_ID + "/#");

        /**
         * 活动文件夹笔记列表的内容 URI 模式
         */
        public static final Uri LIVE_FOLDER_URI
                = Uri.parse(SCHEME + AUTHORITY + PATH_LIVE_FOLDER);

        /*
         * MIME 类型定义
         */

        /**
         * 提供笔记目录的 {@link #CONTENT_URI} 的 MIME 类型。
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.note";

        /**
         * {@link #CONTENT_URI} 单个笔记子目录的 MIME 类型。
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.note";

        /**
         * 此表的默认排序顺序
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        /*
         * 列定义
         */

        /**
         * 笔记标题的列名
         * <P>类型: TEXT</P>
         */
        public static final String COLUMN_NAME_TITLE = "title";

        /**
         * 笔记内容的列名
         * <P>类型: TEXT</P>
         */
        public static final String COLUMN_NAME_NOTE = "note";

        /**
         * 创建时间戳的列名
         * <P>类型: INTEGER (来自 System.currentTimeMillis() 的 long)</P>
         */
        public static final String COLUMN_NAME_CREATE_DATE = "created";

        /**
         * 修改时间戳的列名
         * <P>类型: INTEGER (来自 System.currentTimeMillis() 的 long)</P>
         */
        public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";
    }
}
