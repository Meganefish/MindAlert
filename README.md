tjcs-STIP项目，编写一个android应用，检测驾驶人员疲劳程度，另外添加了音乐播放、定位等功能。

应用名为MindAlert，mainActivity为主类，在MainActivity中存在四个Fragment内容，分别为AnlyzeFragment、MapFragment、MusicFragment、OptionsFragment，分别实现分析功能、定位功能、音乐播放功能、主题设置功能。
此外，为实现音乐播放、数据读取在后台实现，添加了AnlyzeService、MusicService服务类，与MainActivity绑定。
【已删除】为实现实时监听数据读取内容，添加了AnalyzeViewModel、AnalyzeViewModelFactory两个类，后因为功能多余和模型简化删去了这两个类。

已实现的功能：
音乐播放功能
根据raw/result.txt中数据显示疲劳程度
绘制折线图、显示疲劳清醒程度柱状图
主题更换功能、语言更换功能

todoList:
实现定位功能
与疲劳程度分析算法功能对接
bugfix

需要修改的bug:
音乐播放界面的专辑封面图片轮转效果，在切换版面后失效
设置界面的更换主题、语言功能，仅在切换至不同的主题才发生加载，且加载后之前创立的线程均销毁
跳转定位界面时程序闪退
折线图的textcolor不随主题变化
