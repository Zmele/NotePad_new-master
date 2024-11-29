### 基本功能

#### 添加笔记时间戳

1. 发现在`NotePad.java`中已经定义了时间戳常量![c6204c9e5bec48f7b1ad69649b3f551b](file:///C:/Users/zjinh/Pictures/Typedown/c6204c9e-5bec-48f7-b1ad-69649b3f551b.png?msec=1732894868160)
  
2. 在游标适配器所需的列所在数组中多添加一个时间戳常量![1c61a9e0fe1f410e92e94c76cd6b69f1](file:///C:/Users/zjinh/Pictures/Typedown/1c61a9e0-fe1f-410e-92e9-4c76cd6b69f1.png?msec=1732894868161)
  
3. 给要绑定到视图的列中添加时间戳常量![64a81bbc0ae54dc2813d6f7878cf0eef](file:///C:/Users/zjinh/Pictures/Typedown/64a81bbc-0ae5-4dc2-813d-6f7878cf0eef.png?msec=1732894868161)
  
4. 将要显示游标列的视图ID，初始化为`noteslist_item.xml` 中的 `TextView`![2d30282e5f444e9a99e681602b819f7d](file:///C:/Users/zjinh/Pictures/Typedown/2d30282e-5f44-4e9a-99e6-81602b819f7d.png?msec=1732894868173)`noteslist_item.xml`：
  
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
  
5. 最终呈现的结果为一整串数字，所以要将这串数字装为正确的日期格式，这里通过内部类来实现![342fc7613d854e54be4dbfc1ba8d9620](file:///C:/Users/zjinh/Pictures/Typedown/342fc761-3d85-4e54-be4d-bfc1ba8d9620.png?msec=1732894868173)最终的效果：![3a42c53815c1431b859486eaee6d8c26](file:///C:/Users/zjinh/Pictures/Typedown/3a42c538-15c1-431b-8594-86eaee6d8c26.png?msec=1732894868174)
  

#### 添加笔记查询功能

1. 实现`searchNotes()`方法用于在数据库中模糊查找笔记的标题![5c20485476e44062837a1158bf02c01a](file:///C:/Users/zjinh/Pictures/Typedown/5c204854-76e4-4062-837a-1158bf02c01a.png?msec=1732894868175)
  
2. 在`NotesList.java` 的 `OnCreat()`中添加新的布局文件`search_view.xml`（在`res/layout`中新建`search_view.xml`布局文件）
  
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
  
3. 设置搜索视图，根据用户的输入实时更新笔记列表![a2f4c1d469d6481da94c69033cb55612](file:///C:/Users/zjinh/Pictures/Typedown/a2f4c1d4-69d6-481d-a94c-69033cb55612.png?msec=1732894868175)后面新增了改变搜索框文本颜色的代码![c7e23571a84c4bcb9024594ef0374056](file:///C:/Users/zjinh/Pictures/Typedown/c7e23571-a84c-4bcb-9024-594ef0374056.png?msec=1732894868176)
  
4. 效果呈现：![0bfa789d9592475b9ac7795d0401cd7a](file:///C:/Users/zjinh/Pictures/Typedown/0bfa789d-9592-475b-9ac7-795d0401cd7a.png?msec=1732894868177)![801335d761874daaba804da9d5d47153](file:///C:/Users/zjinh/Pictures/Typedown/801335d7-6187-4daa-ba80-4da9d5d47153.png?msec=1732894868177)
  

* * *

### 扩展功能

#### 在`NoteEditor.java`中添加修改笔记背景颜色的功能

1. ###### 更新菜单资源：
  
  <1>在 `res/menu/editor_options_menu.xml` 中，添加一个用于修改背景颜色的菜单项<2>同时，在 `res/values/strings.xml` 中添加字符串资源
  
2. ###### 更新 `onOptionsItemSelected` 方法
  
  在 `NoteEditor` 类中，处理新菜单项的选择。在 `onOptionsItemSelected` 中添加相应的代码![ba453e1c0df740748ade74ccea23aaa6](file:///C:/Users/zjinh/Pictures/Typedown/ba453e1c-0df7-4074-8ade-74ccea23aaa6.png?msec=1732894868178)实现 `showColorPickerDialog` 方法![d1846b6de6b74212a84c4b0d9d929a09](file:///C:/Users/zjinh/Pictures/Typedown/d1846b6d-e6b7-4212-a84c-4b0d9d929a09.png?msec=1732894868178)这样修改后，颜色无法保存，重新打开笔记后，颜色又恢复默认，改进：使用 `SharedPreferences` 来实现这一点修改`showColorPickerDialog`方法：
  
  > `SharedPreferences` 是 Android 提供的一种轻量级的数据存储方式，非常适合存储一些简单的键值对数据，例如用户设置、应用状态、以及小型数据等。
  
  ![bc0900b6e0394b81989c396b7dcf34f7](file:///C:/Users/zjinh/Pictures/Typedown/bc0900b6-e039-4b81-989c-396b7dcf34f7.png?msec=1732894868178)在 `onCreate` 方法添加代码从 `SharedPreferences` 中读取存储的颜色，并设置到 `EditText` 的背景中。![5858b117889a4bb3889f467d4d105758](file:///C:/Users/zjinh/Pictures/Typedown/5858b117-889a-4bb3-889f-467d4d105758.png?msec=1732894868178)这样就能实现修改并保存背景颜色的功能
  
  > `Color` 类提供了一些预定义的颜色常量（前面用到的颜色有的太亮，不太友好），但这样后面添加新颜色比较麻烦，使用修改为用color.xml中定义的颜色，这样颜色的选择比较多样，修改也比较方便
  
3. 使用color.xml来代替Color类
  
  > `Color` 类提供了一些预定义的颜色常量（前面用到的颜色有的太亮，不太友好），但这样后面添加新颜色比较麻烦，使用修改为用color.xml中定义的颜色，这样颜色的选择比较多样，修改也比较方便
  
  在`res/value`中新建一个color.xml，加入想要的颜色![b305315f636c4b9ebdd926a446e11dcb](file:///C:/Users/zjinh/Pictures/Typedown/b305315f-636c-4b9e-bdd9-26a446e11dcb.png?msec=1732894868178)修改colors数组和显示文本![a073812dbf96456f8590219385876512](file:///C:/Users/zjinh/Pictures/Typedown/a073812d-bf96-456f-8590-219385876512.png?msec=1732894868178)
  
4. 效果展示：![f55b6287e49f4bda8a97ab0b73ae3146](file:///C:/Users/zjinh/Pictures/Typedown/f55b6287-e49f-4bda-8a97-ab0b73ae3146.png?msec=1732894868179)![180111c7b64444869a646276e69de6b9](file:///C:/Users/zjinh/Pictures/Typedown/180111c7-b644-4486-9a64-6276e69de6b9.png?msec=1732894868179)选择LightGreen![6e569a367c56437f9c67917849b2cb5c](file:///C:/Users/zjinh/Pictures/Typedown/6e569a36-7c56-437f-9c67-917849b2cb5c.png?msec=1732894868180)
  

#### 美化UI

1. 笔记列表项视图美化列表项圆角背景（首先在 `res/drawable` 目录下创建一个名为 `rounded_background.xml` 的文件）
  
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
  
  修改`noteslist_item.xml`
  
  <?xml version="1.0" encoding="utf-8"?>
  
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
  
  前后对比：![0e425c5614104baf9bad7e3dec7fa505](file:///C:/Users/zjinh/Pictures/Typedown/0e425c56-1410-4baf-9bad-7e3dec7fa505.png?msec=1732894868180)![248d5f7048e047d4af64cf21b95013f7](file:///C:/Users/zjinh/Pictures/Typedown/248d5f70-48e0-47d4-af64-cf21b95013f7.png?msec=1732894868181)后续新增：（修改标题栏，并更换了图标）在 `res/values` 目录下创建一个名为 `styles.xml` 的文件（这里可以设置标题栏的背景颜色还有文本颜色）
  
  <?xml version="1.0" encoding="utf-8"?>
  
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
  
  在`AndroidManifest.xml`中修改`NoteList.java`和`NoteEditor.java`的主题`Theme`![66909e777b444d289bae6355df8c5e52](file:///C:/Users/zjinh/Pictures/Typedown/66909e77-7b44-4d28-9bae-6355df8c5e52.png?msec=1732894868185)![68b944b757da4bf58ca0ee9738962b1a](file:///C:/Users/zjinh/Pictures/Typedown/68b944b7-57da-4bf5-8ca0-ee9738962b1a.png?msec=1732894868185)修改后：![4287afcacced4675b26ebefef2321365](file:///C:/Users/zjinh/Pictures/Typedown/4287afca-cced-4675-b26e-befef2321365.png?msec=1732894868171)
  
2. 搜索框美化自定义搜索框背景样式文件`search_view_background.xml`：
  
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
  
  修改`note_search_list.xml`：
  
  <?xml version="1.0" encoding="utf-8"?>
  
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
  
  前后对比：（后面还替换了图标）![248d5f7048e047d4af64cf21b95013f7](file:///C:/Users/zjinh/Pictures/Typedown/248d5f70-48e0-47d4-af64-cf21b95013f7.png?msec=1732818203675?msec=1732894868172)![6c9aff2ba372419cae15d4fb5c66d0ab](file:///C:/Users/zjinh/Pictures/Typedown/6c9aff2b-a372-419c-ae15-d4fb5c66d0ab.png?msec=1732818203677?msec=1732894868172)![3e811b4186eb4de9b7deb47dfffa9802](file:///C:/Users/zjinh/Pictures/Typedown/3e811b41-86eb-4de9-b7de-b47dfffa9802.png?msec=1732894868172)
  
3. 笔记编辑界面美化在 `res/drawable` 目录下创建一个名为 `edit_text_background.xml` 的文件
  
      <shape xmlns:android="http://schemas.android.com/apk/res/android">
          <solid android:color="@android:color/white"/> <!-- 设置背景色 -->
          <corners android:radius="16dp"/> <!-- 设置圆角 -->
      </shape>
  
  修改`note_editor.xml`
  
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
  
  修改`NoteEditor.java`中的`OnCreate()`方法 ![a0ae92b692f343c4af2184d6754cce09](file:///C:/Users/zjinh/Pictures/Typedown/a0ae92b6-92f3-43c4-af21-84d6754cce09.png?msec=1732826627164?msec=1732826789080?msec=1732826814399?msec=1732894868172) 展示：![afa9a0eab35141f798cb4ab6878755da](file:///C:/Users/zjinh/Pictures/Typedown/afa9a0ea-b351-41f7-98cb-4ab6878755da.png?msec=1732894868173)后续：（字体设置，在NoteEditor中添加导出功能）
  

<?xml version="1.0" encoding="utf-8"?>

<?xml version="1.0" encoding="utf-8"?>
