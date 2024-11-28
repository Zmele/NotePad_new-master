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

import com.example.android.notepad.NotePad;

import android.content.ClipDescription;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.ContentProvider.PipeDataWriter;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.LiveFolders;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * 提供对笔记数据库的访问。每个笔记都有一个标题、笔记内容、创建日期和修改日期。
 */
public class NotePadProvider extends ContentProvider implements PipeDataWriter<Cursor> {
    // 用于调试和日志记录
    private static final String TAG = "NotePadProvider";

    /**
     * 提供者使用的数据库作为其底层数据存储
     */
    private static final String DATABASE_NAME = "note_pad.db";

    /**
     * 数据库版本
     */
    private static final int DATABASE_VERSION = 2;

    /**
     * 用于从数据库中选择列的投影映射
     */
    private static HashMap<String, String> sNotesProjectionMap;

    /**
     * 用于从数据库中选择列的投影映射
     */
    private static HashMap<String, String> sLiveFolderProjectionMap;

    /**
     * 标准投影，包含普通笔记的兴趣列。
     */
    private static final String[] READ_NOTE_PROJECTION = new String[] {
            NotePad.Notes._ID,               // 投影位置 0，笔记的 ID
            NotePad.Notes.COLUMN_NAME_NOTE,  // 投影位置 1，笔记的内容
            NotePad.Notes.COLUMN_NAME_TITLE, // 投影位置 2，笔记的标题
    };
    private static final int READ_NOTE_NOTE_INDEX = 1;
    private static final int READ_NOTE_TITLE_INDEX = 2;

    /*
     * Uri 匹配器使用的常量，根据传入的 URI 模式选择操作
     */
    // 传入的 URI 匹配笔记 URI 模式
    private static final int NOTES = 1;

    // 传入的 URI 匹配笔记 ID URI 模式
    private static final int NOTE_ID = 2;

    // 传入的 URI 匹配活动文件夹 URI 模式
    private static final int LIVE_FOLDER_NOTES = 3;

    /**
     * UriMatcher 实例
     */
    private static final UriMatcher sUriMatcher;

    // 新的 DatabaseHelper 的句柄。
    private DatabaseHelper mOpenHelper;

    /**
     * 一个块，实例化和设置静态对象。
     */
    static {

        /*
         * 创建并初始化 URI 匹配器
         */
        // 创建一个新的实例
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // 添加一个模式，将以 "notes" 结束的 URI 路由到 NOTES 操作
        sUriMatcher.addURI(NotePad.AUTHORITY, "notes", NOTES);

        // 添加一个模式，将以 "notes" 加整数结尾的 URI 路由到笔记 ID 操作
        sUriMatcher.addURI(NotePad.AUTHORITY, "notes/#", NOTE_ID);

        // 添加一个模式，将以 live_folders/notes 结束的 URI 路由到活动文件夹操作
        sUriMatcher.addURI(NotePad.AUTHORITY, "live_folders/notes", LIVE_FOLDER_NOTES);

        /*
         * 创建并初始化返回所有列的投影映射
         */

        // 创建新的投影映射实例。该映射根据字符串返回列名。
        sNotesProjectionMap = new HashMap<String, String>();

        // 将字符串 "_ID" 映射到列名 "_ID"
        sNotesProjectionMap.put(NotePad.Notes._ID, NotePad.Notes._ID);

        // 将 "title" 映射到 "title"
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_TITLE);

        // 将 "note" 映射到 "note"
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_NOTE, NotePad.Notes.COLUMN_NAME_NOTE);

        // 将 "created" 映射到 "created"
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE,
                NotePad.Notes.COLUMN_NAME_CREATE_DATE);

        // 将 "modified" 映射到 "modified"
        sNotesProjectionMap.put(
                NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,
                NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE);

        /*
         * 创建并初始化处理活动文件夹的投影映射
         */

        // 创建新的投影映射实例
        sLiveFolderProjectionMap = new HashMap<String, String>();

        // 将 "_ID" 映射到 "_ID AS _ID" 用于活动文件夹
        sLiveFolderProjectionMap.put(LiveFolders._ID, NotePad.Notes._ID + " AS " + LiveFolders._ID);

        // 将 "NAME" 映射到 "title AS NAME"
        sLiveFolderProjectionMap.put(LiveFolders.NAME, NotePad.Notes.COLUMN_NAME_TITLE + " AS " +
                LiveFolders.NAME);
    }

    /**
     *
     * 此类帮助打开、创建和升级数据库文件。为测试目的设置为包可见性。
     */
    static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {

            // 调用超类构造函数，请求默认的游标工厂。
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
         *
         * 创建底层数据库，其表名和列名来源于 NotePad 类。
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + NotePad.Notes.TABLE_NAME + " ("
                    + NotePad.Notes._ID + " INTEGER PRIMARY KEY,"
                    + NotePad.Notes.COLUMN_NAME_TITLE + " TEXT,"
                    + NotePad.Notes.COLUMN_NAME_NOTE + " TEXT,"
                    + NotePad.Notes.COLUMN_NAME_CREATE_DATE + " INTEGER,"
                    + NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " INTEGER"
                    + ");");
        }

        /**
         *
         * 演示提供者必须考虑在底层数据存储更改时发生的情况。
         * 在这个示例中，通过销毁现有数据来升级数据库。
         * 真实的应用程序应该就地升级数据库。
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            // 记录数据库正在升级的日志
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");

            // 删除表及现有数据
            db.execSQL("DROP TABLE IF EXISTS notes");

            // 使用新版本重新创建数据库
            onCreate(db);
        }
    }

    /**
     *
     * 通过创建一个新的 DatabaseHelper 来初始化提供者。
     * 当 Android 响应来自客户端的解析器请求时，会自动调用 onCreate()。
     */
    @Override
    public boolean onCreate() {

        // 创建一个新的助手对象。请注意，数据库本身在某些尝试访问它时才会被打开，
        // 并且仅在其不存在时才会被创建。
        mOpenHelper = new DatabaseHelper(getContext());

        // 假设任何失败将通过抛出异常来报告。
        return true;
    }

    /**
     * 当客户端调用
     * {@link android.content.ContentResolver#query(Uri, String[], String, String[], String)} 时调用此方法。
     * 查询数据库并返回一个包含结果的游标。
     *
     * @return 包含查询结果的游标。如果查询没有结果或发生异常，游标存在但为空。
     * @throws IllegalArgumentException 如果传入的 URI 模式无效。
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // 构造一个新的查询构建器并设置其表名
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(NotePad.Notes.TABLE_NAME);

        /**
         * 根据 URI 模式匹配选择投影和调整 "where" 子句。
         */
        switch (sUriMatcher.match(uri)) {
            // 如果传入的 URI 是针对笔记，则选择笔记投影
            case NOTES:
                qb.setProjectionMap(sNotesProjectionMap);
                break;

            /* 如果传入的 URI 是由其 ID 标识的单个笔记，则选择笔记 ID 投影，并附加 "_ID = <noteID>"
             * 到 where 子句中，以选择该单个笔记
             */
            case NOTE_ID:
                qb.setProjectionMap(sNotesProjectionMap);
                qb.appendWhere(
                        NotePad.Notes._ID +    // ID 列的名称
                                "=" +
                                // 来自传入 URI 的笔记 ID 本身的位置
                                uri.getPathSegments().get(NotePad.Notes.NOTE_ID_PATH_POSITION));
                break;

            case LIVE_FOLDER_NOTES:
                // 如果传入的 URI 来自活文件夹，则选择活文件夹投影。
                qb.setProjectionMap(sLiveFolderProjectionMap);
                break;

            default:
                // 如果 URI 不匹配任何已知模式，则抛出异常。
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        String orderBy;
        // 如果未指定排序顺序，则使用默认值
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = NotePad.Notes.DEFAULT_SORT_ORDER;
        } else {
            // 否则，使用传入的排序顺序
            orderBy = sortOrder;
        }

        // 以"只读"模式打开数据库对象，因为不需要进行写入。
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        /*
         * 执行查询。如果在尝试读取数据库时没有问题，则返回 Cursor 对象；否则，游标变量包含 null。
         * 如果未选择任何记录，则游标对象为空，Cursor.getCount() 返回 0。
         */
        Cursor c = qb.query(
                db,            // 要查询的数据库
                projection,    // 要从查询中返回的列
                selection,     // where 子句的列
                selectionArgs, // where 子句的值
                null,          // 不对行进行分组
                null,          // 不按行组过滤
                orderBy        // 排序顺序
        );

        // 告诉游标要监视的 URI，因此它知道何时其源数据发生更改
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    /**
     * 当客户端调用 {@link android.content.ContentResolver#getType(Uri)} 时被调用。
     * 返回给定 URI 的 MIME 数据类型。
     *
     * @param uri 需要 MIME 类型的 URI。
     * @return URI 的 MIME 类型。
     * @throws IllegalArgumentException 如果传入的 URI 模式无效。
     */
    @Override
    public String getType(Uri uri) {

        /**
         * 根据传入的 URI 模式选择 MIME 类型
         */
        switch (sUriMatcher.match(uri)) {

            // 如果模式是针对笔记或活动文件夹，则返回通用内容类型。
            case NOTES:
            case LIVE_FOLDER_NOTES:
                return NotePad.Notes.CONTENT_TYPE;

            // 如果模式是针对笔记 ID，则返回笔记 ID 内容类型。
            case NOTE_ID:
                return NotePad.Notes.CONTENT_ITEM_TYPE;

            // 如果 URI 模式不匹配任何允许的模式，则抛出异常。
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

//BEGIN_INCLUDE(stream)
    /**
     * 描述支持的 MIME 类型，以便将笔记 URI 作为流打开。
     */
    static ClipDescription NOTE_STREAM_TYPES = new ClipDescription(null,
            new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN });

    /**
     * 返回可用数据流的类型。 特定笔记的 URI 是支持的。
     * 应用程序可以将该笔记转换为纯文本流。
     *
     * @param uri 需要分析的 URI
     * @param mimeTypeFilter MIME 类型过滤器。此方法仅返回与过滤器匹配的 MIME 类型的数据流。
     * @return 数据流 MIME 类型。 当前仅返回 text/plain。
     * @throws IllegalArgumentException 如果 URI 模式与任何支持的模式不匹配。
     */
    @Override
    public String[] getStreamTypes(Uri uri, String mimeTypeFilter) {
        /**
         *  根据传入的 URI 模式选择数据流类型。
         */
        switch (sUriMatcher.match(uri)) {

            // 如果模式是针对笔记或活动文件夹，则返回 null。 不支持该类型 URI 的数据流。
            case NOTES:
            case LIVE_FOLDER_NOTES:
                return null;

            // 如果模式是针对笔记 ID 且 MIME 过滤器为 text/plain，则返回 text/plain
            case NOTE_ID:
                return NOTE_STREAM_TYPES.filterMimeTypes(mimeTypeFilter);

            // 如果 URI 模式不匹配任何允许的模式，则抛出异常。
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }


    /**
     * 返回每种支持流类型的数据流。此方法对传入 URI 进行查询，然后使用
     * {@link android.content.ContentProvider#openPipeHelper(Uri, String, Bundle, Object,
     * PipeDataWriter)} 启动另一个线程，将数据转换为流。
     *
     * @param uri 指向数据流的 URI 模式
     * @param mimeTypeFilter 包含 MIME 类型的字符串。此方法尝试获取具有此 MIME 类型的数据流。
     * @param opts 调用者提供的其他选项。可以根据内容提供者的要求进行解释。
     * @return AssetFileDescriptor 文件的句柄。
     * @throws FileNotFoundException 如果没有与传入的 URI 关联的文件。
     */
    @Override
    public AssetFileDescriptor openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts)
            throws FileNotFoundException {

        // 检查 MIME 类型过滤器是否与支持的 MIME 类型匹配。
        String[] mimeTypes = getStreamTypes(uri, mimeTypeFilter);

        // 如果 MIME 类型受支持
        if (mimeTypes != null) {

            // 检索此 URI 的笔记。 使用定义的查询方法，而不是使用数据库查询方法。
            Cursor c = query(
                    uri,                    // 笔记的 URI
                    READ_NOTE_PROJECTION,   // 获取一个投影，包含笔记的 ID、标题和内容
                    null,                   // 无 WHERE 子句，获取所有匹配记录
                    null,                   // 由于没有 WHERE 子句，因此没有选择条件
                    null                    // 使用默认排序顺序（修改日期，
                    // 降序
            );

            // 如果查询失败或游标为空，则停止
            if (c == null || !c.moveToFirst()) {

                // 如果游标为空，则简单地关闭游标并返回
                if (c != null) {
                    c.close();
                }

                // 如果游标为空，则抛出异常
                throw new FileNotFoundException("Unable to query " + uri);
            }

            // 启动一个新线程，将流数据通过管道返回给调用者。
            return new AssetFileDescriptor(
                    openPipeHelper(uri, mimeTypes[0], opts, c, this), 0,
                    AssetFileDescriptor.UNKNOWN_LENGTH);
        }

        // 如果 MIME 类型不受支持，返回文件的只读句柄。
        return super.openTypedAssetFile(uri, mimeTypeFilter, opts);
    }

    /**
     * {@link android.content.ContentProvider.PipeDataWriter} 的实现
     * 执行将光标中的数据转换为客户机可读取的数据流的实际工作。
     */
    @Override
    public void writeDataToPipe(ParcelFileDescriptor output, Uri uri, String mimeType,
                                Bundle opts, Cursor c) {
        // 我们当前仅支持从单个笔记条目转换为文本，因此这里无需进行游标数据类型检查。
        FileOutputStream fout = new FileOutputStream(output.getFileDescriptor());
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(fout, "UTF-8"));
            pw.println(c.getString(READ_NOTE_TITLE_INDEX));
            pw.println("");
            pw.println(c.getString(READ_NOTE_NOTE_INDEX));
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "Ooops", e);
        } finally {
            c.close();
            if (pw != null) {
                pw.flush();
            }
            try {
                fout.close();
            } catch (IOException e) {
            }
        }
    }
//END_INCLUDE(stream)

    /**
     * 当客户端调用 {@link android.content.ContentResolver#insert(Uri, ContentValues)} 时调用此方法。
     * 向数据库插入新行。此方法为任何未包含在传入映射中的列设置默认值。
     * 如果插入的行，则通知监听器数据更改。
     * @return 插入行的行 ID。
     * @throws SQLException 如果插入失败。
     */
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {

        // 验证传入的 URI。 仅允许完整提供者 URI 进行插入。
        if (sUriMatcher.match(uri) != NOTES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // 用于存储新记录值的映射。
        ContentValues values;

        // 如果传入的值映射不为空，则使用该值作为新值。
        if (initialValues != null) {
            values = new ContentValues(initialValues);

        } else {
            // 否则，创建一个新的值映射
            values = new ContentValues();
        }

        // 获取当前系统时间（以毫秒为单位）
        Long now = Long.valueOf(System.currentTimeMillis());

        // 如果值映射不包含创建日期，则将该值设置为当前时间。
        if (values.containsKey(NotePad.Notes.COLUMN_NAME_CREATE_DATE) == false) {
            values.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, now);
        }

        // 如果值映射不包含修改日期，则将该值设置为当前时间。
        if (values.containsKey(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE) == false) {
            values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, now);
        }

        // 如果值映射不包含标题，则将该值设置为默认标题。
        if (values.containsKey(NotePad.Notes.COLUMN_NAME_TITLE) == false) {
            Resources r = Resources.getSystem();
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, r.getString(android.R.string.untitled));
        }

        // 如果值映射不包含笔记文本，则将该值设置为空字符串。
        if (values.containsKey(NotePad.Notes.COLUMN_NAME_NOTE) == false) {
            values.put(NotePad.Notes.COLUMN_NAME_NOTE, "");
        }

        // 以“写入”模式打开数据库对象。
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // 执行插入并返回新笔记的 ID。
        long rowId = db.insert(
                NotePad.Notes.TABLE_NAME,        // 要插入的表。
                NotePad.Notes.COLUMN_NAME_NOTE,  // 垃圾，SQLite 将此列的值设置为 null
                // 如果值为空。
                values                           // 列名及要插入的值的映射
                // 到列中。
        );

        // 如果插入成功，则行 ID 存在。
        if (rowId > 0) {
            // 创建一个带有笔记 ID 模式的 URI，并将新行 ID 附加到其后。
            Uri noteUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_ID_URI_BASE, rowId);

            // 通知针对该提供者注册的观察者数据发生了更改。
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        // 如果插入未成功，则 rowID <= 0。抛出异常。
        throw new SQLException("Failed to insert row into " + uri);
    }

    /**
     * 当客户端调用
     * {@link android.content.ContentResolver#delete(Uri, String, String[])} 时调用此方法。
     * 从数据库中删除记录。 如果传入的 URI 匹配笔记 ID URI 模式，
     * 该方法将删除由 URI 中的 ID 指定的一个记录。 否则，它删除一组记录。
     * 记录或记录也必须匹配输入选择条件。
     *
     * 如果删除了行，则通知监听器数据更改。
     * @return 如果使用了"where" 子句，则返回受影响的行数，否则返回 0。
     * 删除所有行并获取行计数，请使用 "1" 作为 where 子句。
     * @throws IllegalArgumentException 如果传入的 URI 模式无效。
     */
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {

        // 以"写入"模式打开数据库对象。
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String finalWhere;

        int count;

        // 根据传入的 URI 模式执行删除。
        switch (sUriMatcher.match(uri)) {

            // 如果传入模式与一般的笔记模式匹配，则根据传入的 "where" 列和参数执行删除。
            case NOTES:
                count = db.delete(
                        NotePad.Notes.TABLE_NAME,  // 数据库表名
                        where,                     // 传入的 where 子句列名
                        whereArgs                  // 传入的 where 子句值
                );
                break;

            // 如果传入的 URI 匹配一个单独的笔记 ID，则根据传入的数据执行删除，
            // 但修改 where 子句将其限制为特定的笔记 ID。
            case NOTE_ID:
                /*
                 * 从所需的 note ID 限制开始最终 WHERE 子句。
                 */
                finalWhere =
                        NotePad.Notes._ID +                              // ID 列名
                                " = " +                                          // 测试相等
                                uri.getPathSegments().                           // 来自传入笔记 ID
                                        get(NotePad.Notes.NOTE_ID_PATH_POSITION)
                ;

                // 如果还有额外的选择标准，将其附加到最终 WHERE 子句中
                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                // 执行删除。
                count = db.delete(
                        NotePad.Notes.TABLE_NAME,  // 数据库表名。
                        finalWhere,                // 最终的 WHERE 子句
                        whereArgs                  // 传入的 where 子句值。
                );
                break;

            // 如果传入的模式无效，抛出异常。
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /* 获取当前上下文的内容解析器对象的句柄，并通知它
         * 传入的 URI 更改。该对象将此传递给解析器框架，
         * 注册到提供者的观察者会收到通知。
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // 返回删除的行数。
        return count;
    }

    /**
     * 当客户端调用
     * {@link android.content.ContentResolver#update(Uri,ContentValues,String,String[])}
     * 时调用此方法。 更新数据库中的记录。
     * 通过值映射中键指定的列名使用新数据更新。
     * 如果传入的 URI 匹配笔记 ID URI 模式，则方法将更新 URI 中 ID 指定的单个记录；
     * 否则，它更新一组记录。 记录或记录必须匹配输入
     * 选择条件，该条件由 where 和 whereArgs 指定。
     * 如果更新了行，则监听器会通知更改。
     *
     * @param uri 要匹配和更新的 URI 模式。
     * @param values 包含列名（键）和新值（值）的映射。
     * @param where 一个 SQL "WHERE" 子句，根据其列值选择记录。 如果为 null，则返回所有匹配 URI 模式的记录。
     * @param whereArgs 选择条件数组。 如果 "where" 参数包含值占位符 ("?")，则每个占位符都由数组中的相应元素替换。
     * @return 更新的行数。
     * @throws IllegalArgumentException 如果传入的 URI 模式无效。
     */
    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {

        // 以"写入"模式打开数据库对象。
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        String finalWhere;

        // 根据传入的 URI 模式执行更新
        switch (sUriMatcher.match(uri)) {

            // 如果传入的 URI 匹配一般笔记模式，则根据传入的数据执行更新。
            case NOTES:

                // 执行更新并返回更新的行数。
                count = db.update(
                        NotePad.Notes.TABLE_NAME, // 数据库表名。
                        values,                   // 要使用的列名和新值的映射。
                        where,                    // where 子句列名。
                        whereArgs                 // 用于选择的 where 子句值。
                );
                break;

            // 如果传入的 URI 匹配一个单独的笔记 ID，则根据传入的数据执行更新，
            // 但修改 where 子句将其限制为特定的笔记 ID。
            case NOTE_ID:
                // 从传入的 URI 中获取笔记 ID
                String noteId = uri.getPathSegments().get(NotePad.Notes.NOTE_ID_PATH_POSITION);

                /*
                 * 开始创建最终 WHERE 子句，通过限制为传入的笔记 ID。
                 */
                finalWhere =
                        NotePad.Notes._ID +                              // ID 列名
                                " = " +                                          // 测试相等
                                uri.getPathSegments().                           // 来自传入笔记 ID
                                        get(NotePad.Notes.NOTE_ID_PATH_POSITION)
                ;

                // 如果还有额外的选择标准，将其附加到最终 WHERE 子句中
                if (where !=null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                // 执行更新并返回更新的行数。
                count = db.update(
                        NotePad.Notes.TABLE_NAME, // 数据库表名。
                        values,                   // 要使用的列名和新值的映射。
                        finalWhere,               // 要使用的最终 WHERE 子句
                        // 对于 whereArgs 的占位符
                        whereArgs                 // 要在删除行时选择的 where 子句值，或者
                        // 如果值在 where 参数中，则为 null。
                );
                break;
            // 如果传入的模式无效，则抛出异常。
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /* 获取当前上下文的内容解析器对象的句柄，并通知它
         * 传入的 URI 更改。该对象将此传递给解析器框架，
         * 注册到提供者的观察者会收到通知。
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // 返回更新的行数。
        return count;
    }

    /**
     * 测试包可以调用此方法，以获取提供者的底层数据库的句柄，
     * 以便在数据库中插入测试数据。 测试用例类负责
     * 在测试上下文中实例化提供者； {@link android.test.ProviderTestCase2} 在调用 setUp() 的时候这样做
     *
     * @return 提供者数据的数据库助手对象的句柄。
     */
    DatabaseHelper getOpenHelperForTest() {
        return mOpenHelper;
    }
}
