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

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * 这个活动允许用户编辑笔记的标题。它显示一个包含 EditText 的浮动窗口。
 * 注意：请注意，此活动中的提供者操作在 UI 线程上进行。这不是一种好的实践。
 * 这样做只是为了使代码更易读。真实的应用程序应该使用 {@link android.content.AsyncQueryHandler}
 * 或 {@link android.os.AsyncTask} 对象在单独的线程上异步执行操作。
 */
public class TitleEditor extends Activity {

    /**
     * 这是一个特殊的意图操作，意味着“编辑笔记的标题”。
     */
    public static final String EDIT_TITLE_ACTION = "com.android.notepad.action.EDIT_TITLE";

    // 创建一个返回笔记 ID 和笔记内容的投影。
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
    };

    // 在提供者返回的游标中的标题列的位置。
    private static final int COLUMN_INDEX_TITLE = 1;

    // 一个游标对象，在查询提供者以获取笔记时将包含结果。
    private Cursor mCursor;

    // 一个 EditText 对象，用于保存编辑后的标题。
    private EditText mText;

    // 一个用于编辑的笔记 URI 对象。
    private Uri mUri;

    /**
     * 当活动首次启动时，Android 调用此方法。根据传入的意图，它确定所需的编辑类型，
     * 然后执行相应的操作。
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置此活动对象的 UI 视图。
        setContentView(R.layout.title_editor);

        // 获取激活此活动的意图，并从中获取需要编辑标题的笔记的 URI。
        mUri = getIntent().getData();

        /*
         * 使用传入的 URI 检索笔记。
         *
         * 注意：这一操作是在 UI 线程上进行的。这将阻塞线程，直到查询完成。
         * 在一个简单的基于本地数据库的提供者的示例应用程序中，该阻塞将是短暂的，
         * 但在真实的应用程序中，应使用 android.content.AsyncQueryHandler 或 android.os.AsyncTask。
         */

        mCursor = managedQuery(
                mUri,        // 要检索的笔记的 URI。
                PROJECTION,  // 要检索的列
                null,        // 没有使用选择标准，因此不需要 where 列。
                null,        // 没有使用 where 列，因此不需要 where 值。
                null         // 不需要排序顺序。
        );

        // 获取 EditText 框的视图 ID
        mText = (EditText) this.findViewById(R.id.title);
    }

    /**
     * 当活动即将进入前台时调用此方法。此情况发生在活动位于任务栈顶端时，或者
     * 它首次启动时。
     * 显示所选笔记的当前标题。
     */
    @Override
    protected void onResume() {
        super.onResume();

        // 验证 onCreate() 中的查询确实有效。如果有效，则游标对象不为 null。
        // 如果为空，则 mCursor.getCount() == 0。
        if (mCursor != null) {

            // 游标刚刚检索到，因此其索引设置为第一个
            // 记录之前的一个记录。这将游标移动到第一条记录。
            mCursor.moveToFirst();

            // 在 EditText 对象中显示当前标题文本。
            mText.setText(mCursor.getString(COLUMN_INDEX_TITLE));
        }
    }

    /**
     * 当活动失去焦点时调用此方法。
     * 对于编辑信息的活动对象，onPause() 可能是保存更改的唯一地方。 Android 应用程序模型假设
     * “保存”和“退出”不是必需的操作。当用户导航离开一个活动时，他们不应该必须返回
     * 以完成他们的工作。离开的行为应该保存所有内容，并让活动处于可以被 Android 摧毁的状态（如果必要）。
     * 更新笔记中当前在文本框中的文字。
     */
    @Override
    protected void onPause() {
        super.onPause();

        // 验证 onCreate() 中的查询确实有效。如果有效，则游标对象不为 null。
        // 如果为空，则 mCursor.getCount() == 0。

        if (mCursor != null) {

            // 为更新提供者创建一个值映射。
            ContentValues values = new ContentValues();

            // 在值映射中，将标题设置为编辑框的当前内容。
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, mText.getText().toString());

            /*
             * 使用笔记的新标题更新提供者。
             *
             * 注意：这一操作是在 UI 线程上进行的。这将阻塞线程，直到
             * 更新完成。在示例应用程序中，访问基于本地数据库的简单提供者，
             * 阻塞将是短暂的，但在真实应用程序中，您应使用
             * android.content.AsyncQueryHandler 或 android.os.AsyncTask。
             */
            getContentResolver().update(
                    mUri,    // 要更新的笔记的 URI。
                    values,  // 包含要更新的列及其对应值的值映射。
                    null,    // 没有使用选择标准，因此不需要“where”列。
                    null     // 没有使用“where”列，因此不需要“where”值。
            );

        }
    }

    public void onClickOk(View v) {
        finish();
    }
}