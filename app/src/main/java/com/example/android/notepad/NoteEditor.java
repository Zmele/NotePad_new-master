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
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toolbar;

/**
 * 此 Activity 处理“编辑”笔记，其中的编辑响应 {@link Intent#ACTION_VIEW}（请求查看数据）、
 * 编辑笔记 {@link Intent#ACTION_EDIT}、插入笔记 {@link Intent#ACTION_INSERT}，或从当前剪贴板内容创建新笔记 {@link Intent#ACTION_PASTE}。
 * 注意：请注意，此 Activity 中的提供者操作是在 UI 线程上进行的。这不是一个好的实践。这里只是为了使代码更具可读性。
 * 实际应用程序应使用 {@link android.content.AsyncQueryHandler} 或 {@link android.os.AsyncTask} 对象在单独的线程上异步执行操作。
 */
public class NoteEditor extends Activity {
    // 用于记录和调试目的
    private static final String TAG = "NoteEditor";

    /*
     * 创建一个投影，返回便签 ID 和便签内容。
     */
    private static final String[] PROJECTION =
            new String[] {
                    NotePad.Notes._ID,
                    NotePad.Notes.COLUMN_NAME_TITLE,
                    NotePad.Notes.COLUMN_NAME_NOTE
            };

    // Activity 保存状态的标签
    private static final String ORIGINAL_CONTENT = "origContent";

    // 此 Activity 可以通过多个操作启动。每个操作作为一个“状态”常量表示
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    // 全局可变变量
    private int mState;
    private Uri mUri;
    private Cursor mCursor;
    private EditText mText;
    private String mOriginalContent;

    /**
     * 定义一个自定义 EditText 视图，它在显示的每行文本之间绘制线条。
     */
    public static class LinedEditText extends EditText {
        private Rect mRect;
        private Paint mPaint;

        // 此构造函数由 LayoutInflater 使用
        public LinedEditText(Context context, AttributeSet attrs) {
            super(context, attrs);

            // 创建 Rect 和 Paint 对象，并设置 Paint 对象的样式和颜色。
            mRect = new Rect();
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(0x800000FF);//修改线的颜色

            //设置EditText背景颜色
            setBackgroundColor(0xFFCCCCCC);
        }

        /**
         * 此方法用于绘制 LinedEditText 对象
         * @param canvas 在其上绘制背景的画布。
         */
        @Override
        protected void onDraw(Canvas canvas) {

            // 获取 View 中的文本行数。
            int count = getLineCount();

            // 获取全局 Rect 和 Paint 对象
            Rect r = mRect;
            Paint paint = mPaint;

            /*
             * 为 EditText 中的每一行文本在矩形中绘制一条线
             */
            for (int i = 0; i < count; i++) {

                // 获取当前文本行的基线坐标
                int baseline = getLineBounds(i, r);

                /*
                 * 在矩形的左侧到右侧绘制一条线，
                 * 在基线以下一 dip 的垂直位置使用“paint”对象进行细节处理。
                 */
                canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
            }

            // 完成后调用父方法
            super.onDraw(canvas);
        }
    }

    /**
     * 此方法在 Activity 首次启动时由 Android 调用。根据传入的 Intent，确定所需的编辑类型，然后执行。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * 创建一个 Intent，以便在 Activity 对象的结果返回给调用者时使用。
         */
        final Intent intent = getIntent();

        /*
         * 根据传入 Intent 指定的操作进行编辑设置。
         */

        // 获取触发此 Activity 的意图过滤器的操作
        final String action = intent.getAction();

        // 对于编辑操作：
        if (Intent.ACTION_EDIT.equals(action)) {

            // 将 Activity 状态设置为 EDIT，并获取要编辑数据的 URI。
            mState = STATE_EDIT;
            mUri = intent.getData();

            // 对于插入或粘贴操作：
        } else if (Intent.ACTION_INSERT.equals(action)
                || Intent.ACTION_PASTE.equals(action)) {

            // 将 Activity 状态设置为 INSERT，获取一般便签 URI，并在提供者中插入一个空记录
            mState = STATE_INSERT;
            mUri = getContentResolver().insert(intent.getData(), null);

            /*
             * 如果插入新笔记的尝试失败，则关闭此 Activity。发起 Activity 接收 RESULT_CANCELED 如果它请求结果。
             * 记录插入失败的信息。
             */
            if (mUri == null) {

                // 写入日志标识符、消息和失败的 URI。
                Log.e(TAG, "Failed to insert new note into " + getIntent().getData());

                // 关闭活动。
                finish();
                return;
            }

            // 由于新条目已创建，这会设置要返回的结果
            // 设置要返回的结果。
            setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));

            // 如果操作不是 EDIT 或 INSERT：
        } else {

            // 记录未理解的错误，结束 Activity，并返回 RESULT_CANCELED 给发起的 Activity。
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }

        /*
         * 使用通过触发 Intent 传入的 URI 从提供者获取便签或便签。
         * 注意：这是在 UI 线程上完成的。它会阻塞线程，直到查询完成。在示例应用程序中，针对基于本地数据库的简单提供者，这个阻塞会是瞬时的，但是在实际应用中应使用
         * android.content.AsyncQueryHandler 或 android.os.AsyncTask。
         */
        mCursor = managedQuery(
                mUri,         // 从提供者获取多个便签的 URI。
                PROJECTION,   // 返回便签 ID 和每个便签内容的投影。
                null,         // 没有“where”子句选择条件。
                null,         // 没有“where”子句选择值。
                null          // 使用默认排序顺序（修改日期，降序）
        );

        // 对于粘贴操作，从剪贴板初始化数据。
        // （必须在 mCursor 初始化后完成。）
        if (Intent.ACTION_PASTE.equals(action)) {
            // 执行粘贴
            performPaste();
            // 将状态切换为 EDIT 以便能修改标题。
            mState = STATE_EDIT;
        }

        // 设置此 Activity 的布局。参见 res/layout/note_editor.xml
        setContentView(R.layout.note_editor);

        // 获取布局中 EditText 的句柄。
        mText = (EditText) findViewById(R.id.note);

        // 从 SharedPreferences 中获取背景颜色
        SharedPreferences sharedPreferences = getSharedPreferences("MyNotesApp", MODE_PRIVATE);
        int savedColor = sharedPreferences.getInt("backgroundColor", Color.LTGRAY); // 默认颜色为浅灰
        mText.setBackgroundColor(savedColor); // 设置 EditText 背景颜色

        /*
         * 如果此 Activity 之前已停止，则其状态已写入保存的实例状态中的 ORIGINAL_CONTENT 位置。获取该状态。
         */
        if (savedInstanceState != null) {
            mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
        }
    }

    /**
     * 此方法在 Activity 即将转到前台时被调用。这发生在 Activity 位于任务栈顶时，或首次启动时。
     * 移动到列表中的第一条便签，根据用户选择的操作设置适当的标题， 将便签内容放入 TextView，并保存原始文本作为备份。
     */
    @Override
    protected void onResume() {
        super.onResume();

        /*
         * mCursor 被初始化，因为 onCreate() 总是在任何运行过程的 onResume 之前调用。此时测试它不为 null，因为它应该始终包含数据。
         */
        if (mCursor != null) {
            // 重新查询，以防在暂停期间发生了变化（例如标题）
            mCursor.requery();

            /* 访问 Cursor 数据前，总是调用 moveToFirst()。由于使用 Cursor 的语义，在创建时，它的内部索引指向指向第一个记录的“位置”之前。
             */
            mCursor.moveToFirst();

            // 根据当前 Activity 状态修改 Activity 窗口标题。
            if (mState == STATE_EDIT) {
                // 将 Activity 的标题设置为包含便签标题
                int colTitleIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE);
                String title = mCursor.getString(colTitleIndex);
                Resources res = getResources();
                String text = String.format(res.getString(R.string.title_edit), title);
                setTitle(text);
                // 将标题设置为“创建”以便插入
            } else if (mState == STATE_INSERT) {
                setTitle(getText(R.string.title_create));
            }

            /*
             * onResume() 可能在 Activity 失去焦点后被调用（被暂停）。用户在 Activity 暂停时正在编辑或创建便签。
             * Activity 应该重新显示先前检索的文本，但不应移动光标。这有助于用户继续编辑或输入。
             */

            // 从 Cursor 获取便签文本，并将其放入 TextView，但不更改文本光标的位置。
            int colNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);
            String note = mCursor.getString(colNoteIndex);
            mText.setTextKeepState(note);

            // 存储原始便签文本，以便允许用户撤销更改。
            if (mOriginalContent == null) {
                mOriginalContent = note;
            }

            /*
             * 有问题。Cursor 应始终包含数据。报告便签中的错误。
             */
        } else {
            setTitle(getText(R.string.error_title));
            mText.setText(getText(R.string.error_message));
        }
    }

    /**
     * 当 Activity 失去焦点时调用此方法。
     * 对于编辑信息的 Activity 对象，onPause() 可能是保存更改的唯一地方。
     * Android 应用程序模型的前提是“保存”和“退出”不是所需的操作。
     * 当用户导航离开 Activity 时，他们不应该需要返回到它来完成他们的工作。离开的行为应该保存所有内容，
     * 并使 Activity 处于可以被 Android 销毁的状态。
     * 如果用户没有做任何事情，则删除或清除便签；否则，将用户的工作写入提供者。
     */
    @Override
    protected void onPause() {
        super.onPause();

        /*
         * 测试查询操作是否没有失败（见 onCreate()）。Cursor 对象会存在，即使没有返回记录，除非查询因某些异常或错误而失败。
         *
         */
        if (mCursor != null) {

            // 获取当前便签文本。
            String text = mText.getText().toString();
            int length = text.length();

            /*
             * 如果 Activity 正在结束且当前便签中没有文本，则返回 RESULT_CANCELED 给调用者，并删除便签。即使便签正在编辑，假设用户想要“清空”（删除）便签，也是如此。
             */
            if (isFinishing() && (length == 0)) {
                setResult(RESULT_CANCELED);
                deleteNote();

                /*
                 * 将编辑写入提供者。如果便签已被编辑，则获取的便签被检索到编辑器中 *或* 如果插入了新便签。后一种情况下， onCreate() 在提供者中插入了一个空的便签，而它正在编辑的是这个新便签。
                 */
            } else if (mState == STATE_EDIT) {
                // 创建一个映射，以包含要更新的列的新值
                updateNote(text, null);
            } else if (mState == STATE_INSERT) {
                updateNote(text, text);
                mState = STATE_EDIT;
            }
        }
    }

    /**
     * 此方法在用户第一次单击设备的菜单按钮时被调用。Android 将传入一个 Menu 对象，该对象已填充项。
     * 为编辑和插入构建菜单，并添加注册为处理此应用程序 MIME 类型的替代操作。
     * @param menu 一个 Menu 对象，应该向其添加项目。
     * @return 返回 true 以显示菜单。
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 从 XML 资源中加载菜单
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editor_options_menu, menu);

        // 仅为保存的便签添加额外菜单项
        if (mState == STATE_EDIT) {
            // 将其他对可处理的 MIME 类型的活动附加到菜单项中。此外，这对系统进行了查询，
            // 查询任何实现 ALTERNATIVE_ACTION 的活动，为此添加一个菜单项。
            Intent intent = new Intent(null, mUri);
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                    new ComponentName(this, NoteEditor.class), null, intent, 0, null);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // 检查便签是否已更改，并启用/禁用还原选项
        int colNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);
        String savedNote = mCursor.getString(colNoteIndex);
        String currentNote = mText.getText().toString();
        if (savedNote.equals(currentNote)) {
            menu.findItem(R.id.menu_revert).setVisible(false);
        } else {
            menu.findItem(R.id.menu_revert).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * 当用户选择菜单项时调用此方法。Android 将传入所选项。
     * 此方法中的 switch 语句调用相应的方法，以执行用户选择的操作。
     *
     * @param item 选定的 MenuItem
     * @return 返回 true 表示该项已处理，无需进一步处理。返回 false 以继续处理，如 MenuItem 对象所示。
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 处理所有可能的菜单操作。
        switch (item.getItemId()) {
            case R.id.menu_save:
                String text = mText.getText().toString();
                updateNote(text, null);
                finish();
                break;
            case R.id.menu_delete:
                deleteNote();
                finish();
                break;
            case R.id.menu_revert:
                cancelNote();
                break;
            /*case R.id.menu_edit_title: // 处理编辑标题选项
                editTitle();
                break;*/
            case R.id.menu_edit_color: //修改笔记背景颜色选项
                showColorPickerDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

//BEGIN_INCLUDE(paste)
    /**
     * 一个辅助方法，用于用剪贴板的内容替换便签的数据。
     */
    private final void performPaste() {

        // 获取对剪贴板管理器的句柄
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

        // 获取内容解析器实例
        ContentResolver cr = getContentResolver();

        // 从剪贴板获取剪贴板数据
        ClipData clip = clipboard.getPrimaryClip();
        if (clip != null) {

            String text=null;
            String title=null;

            // 从剪贴板数据中获取第一个项目
            ClipData.Item item = clip.getItemAt(0);

            // 尝试将项目的内容作为指向便签的 URI
            Uri uri = item.getUri();

            // 测试该项目是否实际是 URI，以及 URI
            // 是指向提供者的内容 URI，其 MIME 类型与便签提供者支持的 MIME 类型相同。
            if (uri != null && NotePad.Notes.CONTENT_ITEM_TYPE.equals(cr.getType(uri))) {

                // 剪贴板包含对具有便签 MIME 类型的数据的引用。复制它。
                Cursor orig = cr.query(
                        uri,            // 提供者的 URI
                        PROJECTION,     // 获取投影中引用的列
                        null,           // 无选择变量
                        null,           // 无选择变量，因此没有标准需要
                        null            // 使用默认排序顺序
                );

                // 如果 Cursor 不为 null，并且包含至少一条记录
                // （moveToFirst() 返回 true），则从中获取便签数据。
                if (orig != null) {
                    if (orig.moveToFirst()) {
                        int colNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);
                        int colTitleIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_TITLE);
                        text = orig.getString(colNoteIndex);
                        title = orig.getString(colTitleIndex);
                    }

                    // 关闭光标。
                    orig.close();
                }
            }

            // 如果剪贴板的内容不是对便签的引用，则
            // 将其转换为文本。
            if (text == null) {
                text = item.coerceToText(this).toString();
            }

            // 用检索到的标题和文本更新当前便签。
            updateNote(text, title);
        }
    }
//END_INCLUDE(paste)

    /**
     * 用提供的文本和标题替换当前便签内容。
     * @param text 要使用的新便签内容。
     * @param title 要使用的新便签标题
     */
    private final void updateNote(String text, String title) {

        // 设置一个映射，用于更新提供者中的值。
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, System.currentTimeMillis());

        // 如果操作是插入新便签，则为其创建初始标题。
        if (mState == STATE_INSERT) {

            // 如果未提供标题作为参数，则从便签文本创建一个。
            if (title == null) {

                // 获取便签的长度
                int length = text.length();

                // 设置标题，通过获取长度为 31 字符的文本子字符串
                // 或便签中的字符数加 1（以较小者为准）。
                title = text.substring(0, Math.min(30, length));

                // 如果结果长度超过 30 字符，则剪掉任何
                // 尾随空格
                if (length > 30) {
                    int lastSpace = title.lastIndexOf(' ');
                    if (lastSpace > 0) {
                        title = title.substring(0, lastSpace);
                    }
                }
            }
            // 在值映射中设置标题的值
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, title);
        } else if (title != null) {
            // 在值映射中设置标题的值
            values.put(NotePad.Notes.COLUMN_NAME_TITLE, title);
        }

        // 将所需的便签文本放入映射中。
        values.put(NotePad.Notes.COLUMN_NAME_NOTE, text);

        /*
         * 使用映射中的新值更新提供者。ListView 会自动更新。
         * 提供者通过将查询的 Cursor 对象的通知 URI 设置为传入的 URI 来设置此功能。
         * 因此，当 URI 的 Cursor 更改时，内容解析器会自动通知，
         * 界面会更新。
         * 注意：这是在 UI 线程上完成的。它会阻塞线程，
         * 直到更新完成。在示例应用程序中，针对基于
         * 本地数据库的简单提供者，阻塞会是瞬时的，但在实际应用中应使用
         * android.content.AsyncQueryHandler 或 android.os.AsyncTask。
         */
        getContentResolver().update(
                mUri,    // 要更新的记录的 URI。
                values,  // 要应用于它们的新值的列名和新值的映射。
                null,    // 不使用选择标准，因此没有必要使用 where 列。
                null     // 不使用 where 列，因此没有必要使用 where 参数。
        );


    }

    /**
     * 此辅助方法取消对便签的操作。如果它是新创建的，则删除便签，否则则恢复便签的原始文本。
     */
    private final void cancelNote() {
        if (mCursor != null) {
            if (mState == STATE_EDIT) {
                // 将原始便签文本放回数据库中
                mCursor.close();
                mCursor = null;
                ContentValues values = new ContentValues();
                values.put(NotePad.Notes.COLUMN_NAME_NOTE, mOriginalContent);
                getContentResolver().update(mUri, values, null, null);
            } else if (mState == STATE_INSERT) {
                // 我们插入了一个空便签，确保删除它
                deleteNote();
            }
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * 处理删除便签。简单地删除条目。
     */
    private final void deleteNote() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
            getContentResolver().delete(mUri, null, null);
            mText.setText("");
        }
    }

    /**
     * 编辑标题。
     */
    private void editTitle() {
        Intent intent = new Intent(this, TitleEditor.class);
        intent.setData(mUri); // 将当前笔记的 URI 传递给 TitleEditor
        startActivity(intent);
    }
    private void showColorPickerDialog() {
        // 添加颜色选择器
        final int[] colors = {
                getResources().getColor(R.color.LightGrey),
                getResources().getColor(R.color.LightSkyBlue),
                getResources().getColor(R.color.LightYellow),
                getResources().getColor(R.color.LightGreen),
                getResources().getColor(R.color.LightPink),
                getResources().getColor(R.color.MistyRose),
                getResources().getColor(R.color.Orange)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择背景颜色");
        builder.setItems(new CharSequence[] {"LightGrey", "LightSkyBlue", "LightYellow", "LightGreen", "LightPink", "MistyRose", "Orange"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 根据用户选择的颜色更改 EditText 背景
                int selectedColor = colors[which];
                mText.setBackgroundColor(selectedColor);

                // 保存选择的颜色到 SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("MyNotesApp", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("backgroundColor", selectedColor);
                editor.apply();
            }
        });
        builder.create().show();
    }
}