package com.example.a.testduoxuantupian;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Created by kangou on 2016/3/18.
 * 图片加载类
 */
public class ImageLoader {

    //******************************单例模式**********************************************
    // 1.声明一个静态私有实例
    private static ImageLoader mInstance;

    // 2.构造函数私有化，外界无法直接new 对象
    private ImageLoader(int threadCount, Type type) {

        init(threadCount, type);
    }

    /**
     * 初始化操作
     *
     * @param threadCount
     * @param type
     */
    private void init(int threadCount, Type type) {
//        后台轮询线程
        mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();

                mPoolThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
//                        线程池去取出一个任务执行
                        mThreadPool.execute(getTask());
                        try {
//                            假如我们的信号量为3，执行第4个方法时，
//                            执行下面的acquire时，此线程会停住，知道执行完一个信号量后release后才会继续执行
                            mSemaphoreThreadPool.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
//                释放一个信号量，
// 只有执行了这一步，下面的mSemaphorePoolThreadHandler.acquire()才不会阻塞（防止mSemaphorePoolThreadHandler空指针异常），才会继续执行下面的程序
                mSemaphorePoolThreadHandler.release();
                Looper.loop();//在后台不断轮循
            }
        };
        mPoolThread.start();

//        获取我们应用的最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
//        缓存内存
        int cacheMemory = maxMemory / 8;

        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //该方法去测量每个bitmap的值(每一行占据的字节数*高度)
                return value.getRowBytes() * value.getHeight();
//                return value.getByteCount();//Android官方写法
            }
        };

//        创建线程池
        mThreadPool = Executors.newFixedThreadPool(threadCount);
//        创建任务队列
        mTaskQueue = new LinkedList<Runnable>();
//        创建队列调度策略
        mType = type;

//       初始化信号量，告诉系统只能同时处理几个线程
        mSemaphoreThreadPool = new Semaphore(threadCount);

    }

    /**
     * 从任务队列取出一个方法
     *
     * @return
     */
    private Runnable getTask() {
        if (mType == Type.FIFO) {
//            先进先出，从队列中移除队首(即：先得到队首的Task)
            return mTaskQueue.removeFirst();
        } else if (mType == Type.LIFO) {
//            后进先出，从队列中移除队尾
            return mTaskQueue.removeLast();
        }
        return null;
    }

    // 3.一个静态公共的方法，供外界得到该单例对象
    public static ImageLoader getInstance() {
        //    懒加载，提高效率
        if (mInstance == null) {//只有第一次的时候会走进if内部，以后不会在进入(加锁后会很慢，不要轻易执行同步锁)
            synchronized (ImageLoader.class) {//同步处理，防止new多个实例
                if (mInstance == null) {
                    mInstance = new ImageLoader(DEFAULT_THREAD_COUNT, Type.LIFO);
                }
            }
        }
        return mInstance;
    }

    public static ImageLoader getInstance(int threadCount, Type type) {
        //    懒加载，提高效率
        if (mInstance == null) {//只有第一次的时候会走进if内部，以后不会在进入(加锁后会很慢，不要轻易执行同步锁)
            synchronized (ImageLoader.class) {//同步处理，防止new多个实例
                if (mInstance == null) {
                    mInstance = new ImageLoader(threadCount, type);
                }
            }
        }
        return mInstance;
    }
    //******************************单例模式**********************************************
    /**
     * 图片缓存的核心对象
     * String:图片路径
     * Bitmap：图片
     */
    private LruCache<String, Bitmap> mLruCache;
    /**
     * 线程池（维护多个线程）
     */
    private ExecutorService mThreadPool;
    /**
     * 默认的线程数
     */
    private static final int DEFAULT_THREAD_COUNT = 1;

    /**
     * 枚举类型，
     */
    public enum Type {
        FIFO, LIFO;//先进先出，后进先出
    }

    /**
     * 队列调度方式(策略)
     */
    private Type mType = Type.LIFO;

    /**
     * 任务队列，使用LinkedList而非ArrayList：
     * 可以从头部或尾部取对象的方法，LinkedList采用链表结构，不需要一个连续的内存
     */
    private LinkedList<Runnable> mTaskQueue;

    /**
     * 后台轮循线程
     */
    private Thread mPoolThread;

    /**
     * 与mPoolThread绑定，给线程中的MessageQuery发送消息
     */
    private Handler mPoolThreadHandler;

    /**
     * UI线程中的Handler
     */
    private Handler mUIHandler;

    /**
     * 确保mPoolThreadHandler不为null的信号量
     */
    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
    /**
     * 所有的Task要在其执行完毕后release一个信号量，好让后面的Task进入
     */
    private Semaphore mSemaphoreThreadPool;


    /**
     * 核心方法：
     * 根据path为ImageView设置图片
     * 默认compressExpand为1，一般情况使用这个方法即可，遇到能够缩放的图片使用三个参数的方法
     *
     * @param path      路径
     * @param imageView 要设置图片的控件
     */
    public void loadImage(String path, ImageView imageView) {
        loadImage(path, imageView, 1);

    }

    /**
     * 核心方法：
     * 根据path为ImageView设置图片
     *
     * @param path      路径
     * @param imageView 要设置图片的控件
     * @compressExpand 这个值是为了像预览图片这样的需求，他要比所要显示的imageview高宽要大一点，放大才能清晰
     *                  此致不能太大，否则OOM（建议1~5之间）
     */

    public void loadImage(final String path, final ImageView imageView, final int compressExpand) {
//        根据tag对比path防止ImageView复用加载混乱
        imageView.setTag(path);
        if (mUIHandler == null) {
            mUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
//                    获取得到的图片，为ImageView加载图片
                    ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
                    Bitmap bm = holder.bitmap;
                    ImageView imageView = holder.imageView;
                    String path = holder.path;

//                    将path与getTag存储路径进行比较，如果一致才设置图片（预防“异步加载+复用对象=图片加载错位”）
//                    imageView是复用了上一屏同一位置的对象，而其path是自己新的path，不应该加载上一屏对应的path图片
                    if (imageView.getTag().toString().equals(path)) {
                        imageView.setImageBitmap(bm);
                    }
                }
            };
        }
//        根据path在缓存中获取bitmap
        Bitmap bm = getBitmapFromLruCache(path);

        if (bm != null) {
//          bm不为空，就发送消息改变UI，给ImageView设置图片
            refreshBitmap(path, imageView, bm);
        } else {
//            bm为空，创建一个Task，添加到TaskQueue中，去得到图片
            addTask(new Runnable() {

                @Override
                public void run() {
//                  加载图片
//                  图片压缩
//                    1.获得图片需要显示的大小
                    ImageSize imageSize = getImageViewSize(imageView);
//                    2.得到压缩后的图片（从path中得到图片太大，要压缩后才可以使用）
                    Bitmap bm = decodeSampledBitmapFromPath(path, imageSize.width, imageSize.height, compressExpand);
//                    3.把图片加入到缓存
                    addBitmapToCache(path, bm);
//                    4.得到bm，并把它存到缓存后，就发送消息改变UI，给ImageView设置图片
                    refreshBitmap(path, imageView, bm);
//                    释放信号量
                    mSemaphoreThreadPool.release();
                }
            });


        }
    }

    private void refreshBitmap(String path, ImageView imageView, Bitmap bm) {
        Message message = Message.obtain();
        ImgBeanHolder holder = new ImgBeanHolder();
        holder.bitmap = bm;
        holder.path = path;
        holder.imageView = imageView;
        message.obj = holder;
        mUIHandler.sendMessage(message);
    }

    /**
     * 将图片加到缓存LruCache
     *
     * @param path
     * @param bm
     */
    private void addBitmapToCache(String path, Bitmap bm) {
        if (getBitmapFromLruCache(path) == null) {
            if (bm != null) {
                mLruCache.put(path, bm);
            }
        }
    }

    /**
     * 根据图片要显示的宽和高，对图片进行压缩，避免OOM
     *
     * @param path
     * @param width  要显示的imageview的宽度
     * @param height 要显示的imageview的高度
     * @return
     * @compressExpand 这个值是为了像预览图片这样的需求，他要比所要显示的imageview高宽要大一点，放大才能清晰
     */
    private Bitmap decodeSampledBitmapFromPath(String path, int width, int height, int compressExpand) {

//      获取图片的宽和高，并不把他加载到内存当中
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = caculateInSampleSize(options, width, height, compressExpand);
//      使用获取到的inSampleSize再次解析图片(此时options里已经含有压缩比 options.inSampleSize，再次解析会得到压缩后的图片，不会oom了 )
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;

    }

    /**
     * 根据需求的宽和高以及图片实际的宽和高计算SampleSize
     *
     * @param options
     * @param reqWidth  要显示的imageview的宽度
     * @param reqHeight 要显示的imageview的高度
     * @return
     * @compressExpand 这个值是为了像预览图片这样的需求，他要比所要显示的imageview高宽要大一点，放大才能清晰
     */
    private int caculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight, int compressExpand) {
        int width = options.outWidth;
        int height = options.outHeight;

        int inSampleSize = 1;

        if (width >= reqWidth || height >= reqHeight) {

            int widthRadio = Math.round(width * 1.0f / reqWidth);
            int heightRadio = Math.round(width * 1.0f / reqHeight);

            inSampleSize = Math.max(widthRadio, heightRadio) /compressExpand;

        }

        return inSampleSize;
    }


    /**
     * 根据ImageView获取适当的压缩的宽和高
     * 尽可能得到ImageView的精确的宽高
     *
     * @param imageView
     * @return
     */
    private ImageSize getImageViewSize(ImageView imageView) {

//      得到屏幕的宽度
        DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();


        ImageSize imageSize = new ImageSize();
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();

//      ------------------------------------尽可能得到ImageView的精确的宽高-------------------------------------------------------------
//      得到imageView的实际宽度（为了压缩图片，这里一定要得到imageview的宽高）必须压缩！，否则OOM！！！！！！！！！！
        int width = imageView.getWidth();

        if (width <= 0) {//可能imageView刚new出来还没有添加到容器当中，width可能为0
            width = lp.width;//获取imageView在layout中声明的宽度
        }
        if (width <= 0) {//如果imageView设置的是WRAP_CONTENT=-1或者MATHC_PARENT=-2，得到的width还是0
            // TODO: 2016/3/19 此方法在API16以上才可以使用，为了兼容低版本，一会用反射获取,已解决
//            width = imageView.getMaxWidth();//检查最大值（此方法在API16以上才可以使用，为了兼容低版本，一会用反射获取）
            width = getImageViewFieldValue(imageView, "mMaxWidth");//检查最大值（此方法在API16以上才可以使用，为了兼容低版本，一会用反射获取）
        }
        if (width <= 0) {//如果还是得不到width，就设置为屏幕的宽度
            width = displayMetrics.widthPixels;
        }

//                                        ----------------------------------------

        //      得到imageView的实际高度（为了压缩图片，这里一定要得到imageview的宽高）必须压缩！，否则OOM！！！！！！！！！！
        int height = imageView.getHeight();

        if (height <= 0) {//可能imageView刚new出来还没有添加到容器当中，width可能为0
            height = lp.height;//获取imageView在layout中声明的高度
        }
        if (height <= 0) {//如果imageView设置的是WRAP_CONTENT=-1或者MATHC_PARENT=-2，得到的width还是0
            // TODO: 2016/3/19 此方法在API16以上才可以使用，为了兼容低版本，一会用反射获取，已解决
//            height = imageView.getMaxHeight();//检查最大值（此方法在API16以上才可以使用，为了兼容低版本，一会用反射获取）
            height = getImageViewFieldValue(imageView, "mMaxHeight");//检查最大值（此方法在API16以上才可以使用，为了兼容低版本，一会用反射获取）
        }
        if (height <= 0) {//如果还是得不到width，就设置为屏幕的高度
            height = displayMetrics.heightPixels;
        }
//       --------------------------------尽可能得到ImageView的精确的宽高------------------------------------------------------------------

        imageSize.width = width;
        imageSize.height = height;
        return imageSize;
    }

    /**
     * 通过反射获取imageview的某个属性值（imageView.getMaxWidth()这个方法在api16以上才可以用，所以使用反射得到这个width属性值）
     *
     * @param object
     * @param fieldName
     * @return
     */
    private static int getImageViewFieldValue(Object object, String fieldName) {

        int value = 0;

        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = field.getInt(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return value;
    }

    private synchronized void addTask(Runnable runnable) {//这里用了同步锁，避免多个线程进来以后都进行acquire()，造成死锁
        // TODO: 2016/3/19 信号量Semaphore和同步锁synchronized的问题，再看资料研究 ，目前感觉缺少锁（对象或.Class）,他是怎么锁住的呢
//      将runnable添加到mTaskQueue队列中
        mTaskQueue.add(runnable);
//mPoolThreadHandle初始化是在另一个子线程中执行的，或者说下面这句话和mPoolThreadHandle初始化是两个并行线程，
// 不能保证下面的话一定在mPoolThreadHandle初始化之后进行，所以要加入信号量来控制执行顺序


        try {
            if (mPoolThreadHandler == null) {

                mSemaphorePoolThreadHandler.acquire();//由于mSemaphorePoolThreadHandler默认为0，这一步就会被阻塞
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//      发送一个通知，提醒后台轮循线程（此刻mPoolThreadHandle很可能还没有被初始化）
        mPoolThreadHandler.sendEmptyMessage(0x110);

    }


    /**
     * 根据path在缓存中获取bitmap
     *
     * @param key
     * @return
     */
    private Bitmap getBitmapFromLruCache(String key) {
        return mLruCache.get(key);
    }

    class ImageSize {
        public int width;
        public int height;
    }

    /**
     * 为了防止加载时 bitmap、imageView、path混乱，
     * 这里建立ImgBeanHolder对象，使其一一对应起来
     */
    private class ImgBeanHolder {
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }
}
