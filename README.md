功能：
 1. 选择图片按钮，下方是一个GridView，用来展示选择到的图片
 2. 打开相册，选择多图（首位可控制是否现设拍照）
 3. 返回到前一页，在GridView里显示选取到的图片
 4. 点击某个item进入图片预览（一个ViewPager展示所有的图片的预览），可以直接点击删除按钮，删除某张图片
 

 

> 1. 使用了multi-image-selector
     github地址：[lovetuzitong/MultiImageSelector](https://github.com/lovetuzitong/MultiImageSelector/blob/master/multi-image-selector/src/main/java/me/nereo/multi_image_selector/MultiImageSelectorActivity.java)
     
     
> 2. 使用了PhotoView,注意：使用HackyViewPager解决PhotoView与ViewPager的触摸事件冲突问题 `compile 'co  .uwetrottmann.photoview:library:1.2.4'`

>3. 使用了hongyang博客   [Android-仿微信图片选择器](http://www.imooc.com/learn/489) 中的 ImageLoader类加载图片

