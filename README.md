### 基本功能

#### 添加笔记时间戳

1. 发现在`NotePad.java`中已经定义了时间戳常量
   ![c6204c9e-5bec-48f7-b1ad-69649b3f551b](./images/c6204c9e-5bec-48f7-b1ad-69649b3f551b.png)

2. 在游标适配器所需的列所在数组中多添加一个时间戳常量
   ![1c61a9e0-fe1f-410e-92e9-4c76cd6b69f1](./images/1c61a9e0-fe1f-410e-92e9-4c76cd6b69f1.png)

3. 给要绑定到视图的列中添加时间戳常量
   ![64a81bbc-0ae5-4dc2-813d-6f7878cf0eef](./images/64a81bbc-0ae5-4dc2-813d-6f7878cf0eef.png)

4. 将要显示游标列的视图ID，初始化为`noteslist_item.xml` 中的 `TextView`
   ![2d30282e-5f44-4e9a-99e6-81602b819f7d](./images/2d30282e-5f44-4e9a-99e6-81602b819f7d.png)

   `noteslist_item.xml`：
   
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
       android:orientation="vertical"
       android:layout_width="match_parent"
       android:layout_height="match_parent">
   
       <TextView
           android:id="@+id/textTitle"
           android:layout_width="match_parent"
           android:layout_height="wrap_content"
           android:text="TextView" />
   
       <TextView
           android:id="@+id/textDate"
           android:layout_width="match_parent"
           android:layout_height="wrap_content"
           android:text="TextView"
           android:gravity="end" />
   </LinearLayout>
   ```

6. 最终呈现的结果为一整串数字，所以要将这串数字装为正确的日期格式，这里通过内部类来实现
   ![342fc761-3d85-4e54-be4d-bfc1ba8d9620](./images/342fc761-3d85-4e54-be4d-bfc1ba8d9620.png)
   
   最终的效果：
   
   ![3a42c538-15c1-431b-8594-86eaee6d8c26](./images/3a42c538-15c1-431b-8594-86eaee6d8c26.png)

#### 添加笔记查询功能

1. 实现`searchNotes()`方法用于在数据库中模糊查找笔记的标题
   ![5c204854-76e4-4062-837a-1158bf02c01a](./images/5c204854-76e4-4062-837a-1158bf02c01a.png)

2. 在`NotesList.java` 的 `OnCreat()`中添加新的布局文件`search_view.xml`（在`res/layout`中新建`search_view.xml`布局文件）
   
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
       android:layout_width="match_parent"
       android:layout_height="match_parent">
   
       <SearchView
           android:id="@+id/search_view"
           android:layout_width="match_parent"
           android:layout_height="wrap_content"
           android:iconifiedByDefault="false"
           android:queryHint="搜索笔记" />
   
       <ListView
           android:id="@android:id/list"
           android:layout_width="match_parent"
           android:layout_height="match_parent"
           android:layout_below="@id/search_view" />
   
   </RelativeLayout>
   ```

3. 设置搜索视图，根据用户的输入实时更新笔记列表![a2f4c1d4-69d6-481d-a94c-69033cb55612](./images/a2f4c1d4-69d6-481d-a94c-69033cb55612.png)
   
   后面新增了改变搜索框文本颜色的代码![c7e23571-a84c-4bcb-9024-594ef0374056](./images/c7e23571-a84c-4bcb-9024-594ef0374056.png)

4. 效果呈现：

   ![0bfa789d-9592-475b-9ac7-795d0401cd7a](./images/0bfa789d-9592-475b-9ac7-795d0401cd7a.png)
   ![801335d7-6187-4daa-ba80-4da9d5d47153](./images/801335d7-6187-4daa-ba80-4da9d5d47153.png)

---

### 扩展功能

#### 添加修改笔记背景颜色的功能

1. ###### 更新菜单资源：
   
   <1>在 `res/menu/editor_options_menu.xml` 中，添加一个用于修改背景颜色的菜单项
   <2>同时，在 `res/values/strings.xml` 中添加字符串资源

2. ###### 更新 `onOptionsItemSelected` 方法
   
   在 `NoteEditor` 类中，处理新菜单项的选择。在 `onOptionsItemSelected` 中添加相应的代码
   ![ba453e1c-0df7-4074-8ade-74ccea23aaa6](./images/ba453e1c-0df7-4074-8ade-74ccea23aaa6.png)
   
   实现 `showColorPickerDialog` 方法
   ![d1846b6d-e6b7-4212-a84c-4b0d9d929a09](./images/d1846b6d-e6b7-4212-a84c-4b0d9d929a09.png)
   这样修改后，颜色无法保存，重新打开笔记后，颜色又恢复默认，改进：使用 `SharedPreferences` 来实现这一点
   修改`showColorPickerDialog`方法：
   
   > `SharedPreferences` 是 Android 提供的一种轻量级的数据存储方式，非常适合存储一些简单的键值对数据，例如用户设置、应用状态、以及小型数据等。
   
   ![bc0900b6-e039-4b81-989c-396b7dcf34f7](./images/bc0900b6-e039-4b81-989c-396b7dcf34f7.png)
   在 `onCreate` 方法添加代码从 `SharedPreferences` 中读取存储的颜色，并设置到 `EditText` 的背景中。
   ![5858b117-889a-4bb3-889f-467d4d105758](./images/5858b117-889a-4bb3-889f-467d4d105758.png)
   这样就能实现修改并保存背景颜色的功能
   
   > `Color` 类提供了一些预定义的颜色常量（前面用到的颜色有的太亮，不太友好），但这样后面添加新颜色比较麻烦，使用修改为用color.xml中定义的颜色，这样颜色的选择比较多样，修改也比较方便

4. 使用color.xml来代替Color类
   
   > `Color` 类提供了一些预定义的颜色常量（前面用到的颜色有的太亮，不太友好），但这样后面添加新颜色比较麻烦，使用修改为用color.xml中定义的颜色，这样颜色的选择比较多样，修改也比较方便
   
   在`res/value`中新建一个color.xml，加入想要的颜色
   ![b305315f-636c-4b9e-bdd9-26a446e11dcb](./images/b305315f-636c-4b9e-bdd9-26a446e11dcb.png)

   修改colors数组和显示文本
   
   ![a073812d-bf96-456f-8590-219385876512](./images/a073812d-bf96-456f-8590-219385876512.png)

6. 效果展示：
   
   ![f55b6287-e49f-4bda-8a97-ab0b73ae3146](./images/f55b6287-e49f-4bda-8a97-ab0b73ae3146.png)
   ![180111c7-b644-4486-9a64-6276e69de6b9](./images/180111c7-b644-4486-9a64-6276e69de6b9.png)
   
   选择LightGreen
   
   ![6e569a36-7c56-437f-9c67-917849b2cb5c](./images/6e569a36-7c56-437f-9c67-917849b2cb5c.png)

#### UI美化

##### 笔记列表项视图美化

列表项圆角背景（首先在 `res/drawable` 目录下创建一个名为 `rounded_background.xml` 的文件）

```xml
<!-- res/drawable/rounded_background.xml -->
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/white" /> <!-- 背景颜色 -->
    <corners android:radius="18dp" /> <!-- 设置圆角半径 -->
    <padding
        android:left="8dp"
        android:top="8dp"
        android:right="8dp"
        android:bottom="8dp" /> <!-- 内边距 -->
</shape>
```

修改`noteslist_item.xml`

<?xml version="1.0" encoding="utf-8"?>

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="4dp">

    <LinearLayout
        android:orientation="vertical"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="1dp"
        android:background="@drawable/rounded_background"
        android:clipToPadding="false"
        android:elevation="2dp"
        android:padding="8dp">

        <TextView
            android:id="@+id/textTitle"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="8dp"
            android:text="Title TextView"
            android:textColor="@android:color/black"
            android:textSize="20sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/textDate"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:gravity="end"
            android:text="Date TextView"
            android:textColor="@android:color/background_dark"
            android:textSize="12sp" />
    </LinearLayout>

</LinearLayout>
```

前后对比：

![0e425c56-1410-4baf-9bad-7e3dec7fa505](./images/0e425c56-1410-4baf-9bad-7e3dec7fa505.png)
![248d5f70-48e0-47d4-af64-cf21b95013f7](./images/248d5f70-48e0-47d4-af64-cf21b95013f7.png)

后续新增：（修改标题栏，并更换了图标）
在 `res/values` 目录下创建一个名为 `styles.xml` 的文件（这里可以设置标题栏的背景颜色还有文本颜色）

<?xml version="1.0" encoding="utf-8"?>

```xml
  <resources>  
    <style name="MyCustomTheme" parent="@android:style/Theme.Holo.Light">  
        <item name="android:actionBarStyle">@style/MyActionBar</item>  
        <item name="android:colorBackground">@android:color/white</item>  
    </style>  
    <style name="MyActionBar" parent="@android:style/Widget.Holo.ActionBar">  
        <item name="android:background">@color/LightSkyBlue</item> <!-- 设置标题栏背景为天空蓝 -->  
        <item name="android:titleTextStyle">@style/MyActionBarTitleText</item>  
    </style>  
    <style name="MyActionBarTitleText" parent="@android:style/TextAppearance.Holo.Widget.ActionBar.Title">  
        <item name="android:textColor">@android:color/black</item> <!-- 设置标题文本颜色为黑色 -->  
        <item name="android:textSize">20sp</item> <!-- 可调整标题文字大小 -->  
        <item name="android:fontFamily">sans-serif-medium</item> <!-- 设置字体 → 可以更改为其他字体 -->  
    </style>  

    <style name="AppTheme">  
        <item name="android:fontFamily">sans-serif-medium</item>  
        <item name="android:actionBarStyle">@style/MyActionBar</item>  
    </style></resources>
```

在`AndroidManifest.xml`中修改`NoteList.java`和`NoteEditor.java`的主题`Theme`
![66909e77-7b44-4d28-9bae-6355df8c5e52](./images/66909e77-7b44-4d28-9bae-6355df8c5e52.png)
![68b944b7-57da-4bf5-8ca0-ee9738962b1a](./images/68b944b7-57da-4bf5-8ca0-ee9738962b1a.png)
修改后：
![4287afca-cced-4675-b26e-befef2321365](./images/4287afca-cced-4675-b26e-befef2321365.png)

##### 搜索框美化

自定义搜索框背景样式文件`search_view_background.xml`：

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <corners android:radius="18dp" /> <!-- 圆角半径 -->
    <solid android:color="#FFFFFF" />   <!-- 背景色 -->
    <padding
        android:left="16dp"
        android:right="16dp"
        android:top="8dp"
        android:bottom="8dp" />
</shape>
```

修改`note_search_list.xml`：

<?xml version="1.0" encoding="utf-8"?>

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="0dp">

    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="1dp"
        android:layout_weight="1"
        android:padding="4dp">

        <android.widget.SearchView
            android:id="@+id/search_view"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:iconifiedByDefault="false"
            android:queryHint="@null"
            android:background="@drawable/search_view_background"
            android:padding="10dp"
            android:searchIcon="@drawable/ic_menu_select_black" />

        <ListView
            android:id="@android:id/list"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_below="@id/search_view" />

    </RelativeLayout>

</LinearLayout>
```

前后对比：（后面还替换了图标）

![248d5f7048e047d4af64cf21b95013f7](./images/248d5f70-48e0-47d4-af64-cf21b95013f7.png?msec=1732818203675)
![6c9aff2ba372419cae15d4fb5c66d0ab](./images/6c9aff2b-a372-419c-ae15-d4fb5c66d0ab.png?msec=1732818203677)

![3e811b41-86eb-4de9-b7de-b47dfffa9802](./images/3e811b41-86eb-4de9-b7de-b47dfffa9802.png)

##### 笔记编辑界面美化

在 `res/drawable` 目录下创建一个名为 `edit_text_background.xml` 的文件

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@android:color/white"/> <!-- 设置背景色 -->
    <corners android:radius="16dp"/> <!-- 设置圆角 -->
</shape>
```

修改`note_editor.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="12dp"
    android:gravity="top|start">

    <!-- 包含自定义的 LinedEditText 控件 -->
    <view
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@+id/note"
        class="com.example.android.notepad.NoteEditor$LinedEditText"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@drawable/edit_text_background"
        android:capitalize="sentences"
        android:fadingEdge="vertical"
        android:gravity="top|start"
        android:scrollbars="vertical"
        android:textSize="22sp"
        android:padding="10dp" /> <!-- 设置内边距 -->

</LinearLayout>
```

修改`NoteEditor.java`中的`OnCreate()`方法 
![a0ae92b692f343c4af2184d6754cce09](./images/a0ae92b6-92f3-43c4-af21-84d6754cce09.png?msec=1732826627164?msec=1732826789080?msec=1732826814399) 

展示：

![afa9a0ea-b351-41f7-98cb-4ab6878755da](./images/afa9a0ea-b351-41f7-98cb-4ab6878755da.png)

#### 添加笔记导出功能

1. 在 `res/menu/editor_options_menu.xml` 文件中，添加一个新的菜单项
   ![58f34cd6-87ea-4f1f-9061-ba86a46250dc](./images/58f34cd6-87ea-4f1f-9061-ba86a46250dc.png)

2. 在 `onOptionsItemSelected` 方法的 switch 语句中处理导出操作：
   ![7b85d3dd-147b-4592-a34b-6ba093c68019](./images/7b85d3dd-147b-4592-a34b-6ba093c68019.png)

3. 实现`exportNote`方法：
   ![af4f2b89-3521-4825-9ae8-0215dc6ff639](./images/af4f2b89-3521-4825-9ae8-0215dc6ff639.png)

<?xml version="1.0" encoding="utf-8"?>

<?xml version="1.0" encoding="utf-8"?>

---

### 更新SDK后

![77026b0a-6503-4d55-b039-4edfbd1ac492](./images/77026b0a-6503-4d55-b039-4edfbd1ac492.png)

报错问题解决（`AndroidManifest.xml`）：
给定义了意图过滤器（`intent filter`）的组件（`activity`）设置 `android:exported` 属性
![dcf50e4d-bb27-45c1-b012-3c52f67f7232](./images/dcf50e4d-bb27-45c1-b012-3c52f67f7232.png)
![84534308-1983-4a40-93f1-a16e7255dc77](./images/84534308-1983-4a40-93f1-a16e7255dc77.png)
![bea83eee-1f41-4640-a63f-d1f12498b218](./images/bea83eee-1f41-4640-a63f-d1f12498b218.png)

更新后页面展示：

![2e0e44f7-5a5e-4af6-bf30-29ded42971a7](./images/2e0e44f7-5a5e-4af6-bf30-29ded42971a7.png)

#### 添加字体样式修改功能

1. 右键点击 `main` 文件夹：在 `main` 文件夹上右键点击，选择 `New -> Folder`。 选择 `Assets Folder`，然后点击 `Finish`。这样就会在 `src/main` 创建一个 `assets` 文件夹。 在 `assets` 文件夹中创建一个`fonts`文件夹，在里面添加字体文件（字体文件下载地址<格式：.ttf>：[Browse Fonts - Google Fonts](https://fonts.google.com/?preview.text=%E4%BD%A0%E5%A5%BD)）

   ![812f318b-f0bd-42b1-b5d5-0a8e1e23bd8d](./images/812f318b-f0bd-42b1-b5d5-0a8e1e23bd8d.png)

3. 添加一个菜单项用来显示修改字体样式（`onOptionsItemSelected()`方法）
   
   ![b0df53c2-8850-4c9a-93ce-af2368a7e45d](./images/b0df53c2-8850-4c9a-93ce-af2368a7e45d.png)

5. 编写一个方法<`showFontOptionsDialog()`>来生成一个对话框来选择字体样式并修改
   ![108334fb-0c3a-47a5-921e-593c81f93297](./images/108334fb-0c3a-47a5-921e-593c81f93297.png)

   `setCustomFont()`方法：
   
   ![67b2424b-0451-4a6f-974f-94ae3ce676da](./images/67b2424b-0451-4a6f-974f-94ae3ce676da.png)

##### 添加字体样式后不能保存——解决（没解决）

1. 修改数据库结构
   在 `NotePadProvider` 类中修改数据库表的结构，以添加新的列来存储字体样式。
   ![4976300b-e196-4bef-b607-e124f6e3d1bd](./images/4976300b-e196-4bef-b607-e124f6e3d1bd.png)

2. 更新 `NotePad` 类的定义
   ![e3d8ee84-2121-425f-9305-63c4492e6f71](./images/e3d8ee84-2121-425f-9305-63c4492e6f71.png)

3. 更新 `insert` 和 `update` 方法

   `insert`方法
   
   ![71569c46-6b82-4544-938c-bdd0e41cb6bc](./images/71569c46-6b82-4544-938c-bdd0e41cb6bc.png)

   `update`方法
   
   ![44b9c5ba-3d61-4d15-a02c-b24813ddb264](./images/44b9c5ba-3d61-4d15-a02c-b24813ddb264.png)

5. 在 UI 中保存和加载字体样式
   
   


