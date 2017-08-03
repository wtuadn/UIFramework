# UIFramework
使用自定义view代替activity的一个框架

曾经在项目中被fragment产生的各种问题搞得晕头转向，

加之读了[Square：从今天开始抛弃Fragment吧！](https://github.com/hehonghui/android-tech-frontier/blob/master/issue-13/Square%EF%BC%9A%E4%BB%8E%E4%BB%8A%E5%A4%A9%E5%BC%80%E5%A7%8B%E6%8A%9B%E5%BC%83Fragment%E5%90%A7%EF%BC%81.md)

这篇文章，便开始抵触使用fragment

受到文章启发后开始探索用自定义view代替activity（Container）,
Container里用自定义view代替fragment的UI结构

此项目实现了：
 1. 一套完整可用的生命周期，包括onCreate、onResume、onPause、onDestroy、
 onBackPressed、onSave、onRestore
 2. 过场动画
 3. 滑动返回

还有很多activity的特性没实现，例如权限请求的回调。

目前使用来看，除了新页面打开更快，View层级更少，GPU绘制更轻这几点，并没有比activity好用，
仅是作为一次尝试