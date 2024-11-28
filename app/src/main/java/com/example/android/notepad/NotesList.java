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

import android.app.ListActivity;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import android.widget.SearchView;


import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 显示笔记列表。如果启动此 Activity 的 Intent 中提供了 URI，则显示该 URI 的笔记，否则默认显示笔记提供者的内容。
 * 注意：此 Activity 中的内容提供者操作是在 UI 线程上执行的。这不是一个好的实践。这里只这样做是为了使代码更易于阅读。
 * 一个真实的应用程序应该使用 AsyncQueryHandler 或 AsyncTask 在单独的线程上异步执行操作。
 */
public class NotesList extends ListActivity {

    // 用于日志记录和调试
    private static final String TAG = "NotesList";

    /**
     * 游标适配器所需的列
     */
    private static final String[] PROJECTION = new String[]{
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE // 2
    };

    /**
     * 标题列的索引
     */
    private static final int COLUMN_INDEX_TITLE = 1;

    private SearchView searchView; // 声明 SearchView
    private SimpleCursorAdapter adapter; // 声明适配器

    private Cursor searchNotes(String query) {
        Uri uri = NotePad.Notes.CONTENT_URI; // 笔记的URI
        String selection = NotePad.Notes.COLUMN_NAME_TITLE + " LIKE ?"; // 查询条件使用笔记标题
        String[] selectionArgs = new String[]{"%" + query + "%"}; // 参数，`%` 用于模糊匹配

        // 执行查询
        return getContentResolver().query(uri, PROJECTION, selection, selectionArgs, NotePad.Notes.DEFAULT_SORT_ORDER);
    }

    /**
     * 当 Android 从头开始启动此 Activity 时调用 onCreate。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_search_list); // 使用新的布局文件

        // 用户不需要长按快捷键即可使用菜单快捷键。
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        /* 如果启动此 Activity 的 Intent 没有指定数据 URI，那么此 Activity 是在 Intent 过滤器匹配到 MAIN 动作时启动的。在这种情况下，我们应该使用默认的内容 URI 来获取笔记列表。
         */
        // 获取启动此 Activity 的 Intent。
        Intent intent = getIntent();

        // 如果 Intent 没有指定数据 URI，设置为默认的笔记列表 URI。
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }

        /*
         * 为 ListView 设置上下文菜单的创建监听器。这样，当用户长按列表项时，会显示上下文菜单，并由 NotesList 类中的方法处理。
         */
        getListView().setOnCreateContextMenuListener(this);

        /* 执行一个管理查询，获取笔记数据。Activity 会自动管理 Cursor 的关闭和重新查询。
         *
         * 请注意，这里的查询操作是在 UI 线程上执行的，这在实际应用中可能不是最佳实践。在生产环境中，应该使用异步操作来避免阻塞 UI 线程。
         */
        Cursor cursor = managedQuery(
                getIntent().getData(),            // 使用 Intent 中指定的 URI 或默认的笔记列表 URI。
                PROJECTION,                       // 指定要查询的列，包括笔记的 ID 和标题。
                null,                             // 不使用 WHERE 子句，查询所有笔记。
                null,                             // 不使用 WHERE 子句，因此不需要 WHERE 子句的值。
                NotePad.Notes.DEFAULT_SORT_ORDER  // 使用默认的排序方式，按修改日期降序排列。
        );

        /*
         * 下面的两个数组定义了如何将 Cursor 中的数据列映射到 ListView 中的视图 ID。dataColumns 数组包含了要显示的列名，而 viewIDs 数组包含了对应的视图 ID。
         * SimpleCursorAdapter 会根据这些映射关系将数据绑定到视图上。
         */

        // 要在视图中显示的游标列的名称，初始化为标题列和修改日期列
        String[] dataColumns = {NotePad.Notes.COLUMN_NAME_TITLE, NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE};

        // 将要显示游标列的视图ID，初始化为 noteslist_item2.xml 中的 TextView
        int[] viewIDs = {R.id.textTitle, R.id.textDate};

        // 为 ListView 创建适配器。
        adapter = new SimpleCursorAdapter(
                this,                             // ListView 的上下文
                R.layout.noteslist_item,          // 指向列表项的 XML 布局文件
                cursor,                           // 从中获取项目的游标
                dataColumns,                      // 要绑定到视图的列
                viewIDs                           // 要绑定列的视图
        );

        // 将 ListView 的适配器设置为刚刚创建的游标适配器。
        setListAdapter(adapter);

        // 设置搜索视图
        searchView = (SearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // 用户提交查询，可以选择处理
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 根据输入文本搜索笔记
                Cursor updatedCursor = searchNotes(newText);
                adapter.changeCursor(updatedCursor); // 更新适配器的 Cursor
                return true;
            }
        });

        // 设置自定义视图绑定器，用于修改时间格式显示。这里使用内部类来实现SimpleCursorAdapter.ViewBinder接口。
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                // 检查视图是否是TextView类型，因为我们要修改的是文本显示。
                if (view instanceof TextView) {
                    // 获取当前列的列名，以便判断是否需要特殊处理。
                    String columnName = cursor.getColumnName(columnIndex);

                    // 判断当前列是否是修改日期列（假设NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE是修改日期的列名）。
                    if (columnName.equals(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE)) {
                        // 从Cursor中获取修改日期的时间戳（毫秒为单位）。
                        long dateInMillis = cursor.getLong(columnIndex);

                        // 将视图转换为TextView，以便设置文本。
                        TextView textView = (TextView) view;

                        // 创建一个SimpleDateFormat对象，用于格式化日期。这里使用"yyyy-MM-dd HH:mm:ss"格式，并指定默认语言环境。
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                        // 使用SimpleDateFormat格式化时间戳，并将结果设置为TextView的文本。
                        textView.setText(dateFormat.format(new Date(dateInMillis)));

                        // 返回true表示我们已经处理了该视图的值，不需要进一步处理。
                        return true;
                    }
                }

                // 如果视图不是TextView或者当前列不是修改日期列，返回false表示我们没有处理该视图的值，可能需要其他处理。
                return false;
            }
        });
    }

    /**
     * 当用户第一次点击设备的 Menu 按钮时调用。Android 传递一个填充了项目的 Menu 对象。
     * 设置一个提供 Insert 选项以及此 Activity 的替代操作列表的菜单。
     * 其他希望处理笔记的应用可以通过提供一个 intent 过滤器来“注册”自己，
     * 该过滤器包括 ALTERNATIVE 类别和 NotePad.Notes.CONTENT_TYPE 类型。
     * 如果他们这样做，此代码将在 onCreateOptionsMenu() 中添加包含 intent 过滤器的 Activity 到其选项列表中。
     * 实际上，菜单将为用户提供其他可以处理笔记的应用。
     *
     * @param menu 要添加菜单项的 Menu 对象。
     * @return 始终为 True。应该显示菜单。
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 从 XML 资源中填充菜单
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_options_menu, menu);

        // 生成可以在整个列表上执行的任何额外操作。在正常安装中，这里没有找到额外的操作，但此方法允许其他应用程序扩展我们的菜单，添加他们自己的操作。
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(
                Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // 如果剪贴板上有数据，Paste 菜单项将被启用。
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

        MenuItem mPasteItem = menu.findItem(R.id.menu_paste);

        // 如果剪贴板包含项目，则在菜单上启用 Paste 选项。
        if (clipboard.hasPrimaryClip()) {
            mPasteItem.setEnabled(true);
        } else {
            // 如果剪贴板为空，则禁用菜单的 Paste 选项。
            mPasteItem.setEnabled(false);
        }

        // 获取当前正在显示的笔记数量。
        final boolean haveItems = getListAdapter().getCount() > 0;

        // 如果列表中有任何笔记（这意味着其中一项被选中），那么我们需要生成可以在当前选择上执行的操作。这将是我们自己特定操作以及可以找到的任何扩展的组合。
        if (haveItems) {
            // 这是被选中的项。
            Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());

            // 创建一个包含一个元素的 Intent 数组。这将用于发送基于所选菜单项的 Intent。
            Intent[] specifics = new Intent[1];

            // 将数组中的 Intent 设置为对所选笔记 URI 的 EDIT 动作。
            specifics[0] = new Intent(Intent.ACTION_EDIT, uri);

            // 创建一个包含一个元素的菜单项数组。这将包含 EDIT 选项。
            MenuItem[] items = new MenuItem[1];

            // 创建一个没有特定动作的 Intent，使用所选笔记的 URI。
            Intent intent = new Intent(null, uri);

            /* 将类别 ALTERNATIVE 添加到 Intent 中，以笔记 ID URI 作为其数据。这将 Intent 作为菜单中替代选项的组合位置进行准备。
             */
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);

            /*
             * 将替代项添加到菜单中
             */
            menu.addIntentOptions(
                    Menu.CATEGORY_ALTERNATIVE,  // 作为替代组中的选项添加 Intent。
                    Menu.NONE,                  // 不需要唯一的项目 ID。
                    Menu.NONE,                  // 替代项不需要按顺序排列。
                    null,                       // 调用者的名称不从组中排除。
                    specifics,                  // 这些特定选项必须首先出现。
                    intent,                     // 这些 Intent 对象映射到 specifics 中的选项。
                    Menu.NONE,                  // 不需要标志。
                    items                       // 从 specifics 到 Intent 的映射生成的菜单项
            );
            // 如果 Edit 菜单项存在，则为其添加快捷方式。
            if (items[0] != null) {
                // 将 Edit 菜单项的快捷方式设置为数字 "1"，字母 "e"
                items[0].setShortcut('1', 'e');
            }
        } else {
            // 如果列表为空，则从菜单中移除任何现有的替代动作。
            menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
        }

        // 显示菜单
        return true;
    }

    /**
     * 当用户从菜单中选择一个选项时调用此方法，但列表中没有选择的项。如果选项是 INSERT，则会发送一个新的 Intent，动作为 ACTION_INSERT。
     * 来自传入 Intent 的数据将放入新 Intent 中。实际上，这会触发 NotePad 应用程序中的 NoteEditor 活动。
     * <p>
     * 如果项目不是 INSERT，那么它很可能是来自另一个应用程序的替代选项。调用父方法以处理该项。
     *
     * @param item 用户选择的菜单项
     * @return 如果选择了 INSERT 菜单项，则返回 true；否则，返回调用父方法的结果。
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                /*
                 * 启动一个新的活动，使用 Intent。该活动的 Intent 过滤器
                 * 必须有动作 ACTION_INSERT。没有设置类别，因此假定为 DEFAULT。
                 * 实际上，这会启动 NotePad 中的 NoteEditor 活动。
                 */
                startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
                return true;
            case R.id.menu_paste:
                /*
                 * 启动一个新的活动，使用 Intent。该活动的 Intent 过滤器
                 * 必须有动作 ACTION_PASTE。没有设置类别，因此假定为 DEFAULT。
                 * 实际上，这会启动 NotePad 中的 NoteEditor 活动。
                 */
                startActivity(new Intent(Intent.ACTION_PASTE, getIntent().getData()));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 当用户在列表中右击笔记时调用此方法。NotesList 在其 ListView 中注册自己作为上下文菜单的处理程序（这在 onCreate() 中完成）。
     * <p>
     * 唯一可用的选项是 COPY 和 DELETE。
     * <p>
     * 右击相当于长按。
     *
     * @param menu     要向其添加项目的 ContexMenu 对象。
     * @param view     正在构建上下文菜单的视图。
     * @param menuInfo 与视图相关的数据。
     * @throws ClassCastException
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {

        // 从菜单项中获取的数据。
        AdapterView.AdapterContextMenuInfo info;

        // 尝试获取在 ListView 中长按的项的位置。
        try {
            // 将传入的数据对象强制转换为 AdapterView 对象的类型。
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            // 如果菜单对象无法转换，记录错误。
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        /*
         * 获取与所选位置项相关的数据。getItem() 返回
         * ListView 的后备适配器与该项关联的内容。在 NotesList 中，适配器为每个笔记
         * 都关联了所有数据。因此，getItem() 返回该数据作为游标。
         */
        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);

        // 如果游标为空，那么出于某种原因适配器无法从提供者获取数据，因此返回 null 给调用者。
        if (cursor == null) {
            // 出于某种原因，请求的项目不可用，不做任何操作
            return;
        }

        // 从 XML 资源中填充菜单
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);

        // 设置菜单头为所选笔记的标题。
        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));

        // 向菜单项附加其他可以处理它的活动。
        // 这会查询系统以获取实现 ALTERNATIVE_ACTION 的所有活动，为每个找到的活动添加菜单项。
        Intent intent = new Intent(null, Uri.withAppendedPath(getIntent().getData(),
                Integer.toString((int) info.id)));
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);
    }

    /**
     * 当用户从上下文菜单中选择一个项目时调用此方法
     * （参见 onCreateContextMenu()）。实际上处理的唯一菜单项是 DELETE 和
     * COPY。其他任何项都是替代选项，应进行默认处理。
     *
     * @param item 选择的菜单项
     * @return 如果菜单项是 DELETE，并且不需要默认处理，则返回 true；否则返回 false，
     * 触发菜单项的默认处理。
     * @throws ClassCastException
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // 从菜单项中获取的数据。
        AdapterView.AdapterContextMenuInfo info;

        /*
         * 从菜单项获取额外信息。当长按 Notes 列表中的一个笔记时，
         * 一个上下文菜单出现。菜单项自动获取与长按项相关的数据。
         * 数据来自支撑列表的提供者。
         *
         * 笔记的数据通过 ContextMenuInfo 对象传递给上下文菜单创建例程。
         *
         * 当单击上下文菜单中的一个项时，传递相同的数据，以及
         * 笔记 ID，通过 item 参数传递给 onContextItemSelected()。
         */
        try {
            // 将项目中的数据对象强制转换为 AdapterView 对象的类型。
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {

            // 如果对象无法转换，记录错误
            Log.e(TAG, "bad menuInfo", e);

            // 触发菜单项的默认处理。
            return false;
        }
        // 将所选笔记的 ID 附加到随传入 Intent 发送的 URI。
        Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);

        /*
         * 获取菜单项的 ID 并将其与已知操作进行比较。
         */
        switch (item.getItemId()) {
            case R.id.context_open:
                // 启动活动以查看/编辑当前选择的项
                startActivity(new Intent(Intent.ACTION_EDIT, noteUri));
                return true;
//BEGIN_INCLUDE(copy)
            case R.id.context_copy:
                // 获取剪贴板服务的句柄。
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);

                // 将笔记 URI 复制到剪贴板。实际上，这复制了笔记本身
                clipboard.setPrimaryClip(ClipData.newUri(   // 持有 URI 的新剪贴板项
                        getContentResolver(),               // 获取 URI 信息的解析器
                        "Note",                             // 剪贴板的标签
                        noteUri)                            // URI
                );

                // 返回给调用者并跳过进一步处理。
                return true;
//END_INCLUDE(copy)
            case R.id.context_delete:

                // 通过传入笔记 ID 格式的 URI 从提供者中删除笔记。
                // 请参见有关在 UI 线程上执行提供者操作的简介说明。
                getContentResolver().delete(
                        noteUri,  // 提供者的 URI
                        null,     // 不需要 where 子句，因为仅传入一个笔记 ID。
                        //
                        null      // 不使用 where 子句，因此不需要 where 参数。
                );

                // 返回给调用者并跳过进一步处理。
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * 当用户点击显示列表中的笔记时调用此方法。
     * <p>
     * 此方法处理 PICK（从提供者获取数据）或
     * GET_CONTENT（获取或创建数据）的传入操作。如果传入操作是 EDIT，此方法会发送一个
     * 新的 Intent 启动 NoteEditor。
     *
     * @param l        包含被点击项目的 ListView
     * @param v        单个项目的视图
     * @param position v 在显示列表中的位置
     * @param id       被点击项目的行 ID
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        // 根据传入 URI 和行 ID 构造一个新 URI
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);

        // 从传入 Intent 获取操作
        String action = getIntent().getAction();

        // 处理笔记数据请求
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {

            // 设置结果以返回到调用此活动的组件。结果包含新 URI
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {

            // 发送 Intent 启动可处理 ACTION_EDIT 的活动。Intent 的数据是笔记 ID URI。效果是调用 NoteEdit。
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }
}