package scut.carson_ho.borchshop.Web;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsoluteLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import scut.carson_ho.borchshop.Guiders.GuiderActivity1;
import scut.carson_ho.borchshop.Initialization.BaseApplication;
import scut.carson_ho.borchshop.Interfaces.WebViewProgressChangeListener;
import scut.carson_ho.borchshop.R;
import scut.carson_ho.borchshop.Utils.ScreenUtil;
import scut.carson_ho.borchshop.Utils.SdcardConfig;

/**
 * @Description: webview实体类
 */
public class WebviewEntity extends WebView {

    private ProgressBar mProgressBar;
    private WebViewProgressChangeListener progressChangeListener;
    private Context context;


    //在构造函数里进行初始化
    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public WebviewEntity(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        initContext(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public WebviewEntity(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initContext(context);
    }

    /**
     * @param context
     */
    public WebviewEntity(Context context) {
        super(context);
        this.context = context;
        initContext(context);
    }

    /**
     * @Title: initContext
     * @Description: 初始化context
     * @param context
     * @return void
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")

    // 初始化
    // 对WebView进行设置
    private void initContext(Context context) {
        requestFocus();
        setInitialScale(39);
        //支持与JS交互
        getSettings().setJavaScriptEnabled(true);
        //支持通过Js打开新窗口
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        //设置自适应屏幕
        getSettings().setUseWideViewPort(true);//将图片调整到适合Webview的大小
        getSettings().setLoadWithOverviewMode(true);//缩放至屏幕大小

        //设置是否开启DOM存储API权限
        getSettings().setDomStorageEnabled(true);

        //设置开启数据库存储API权限
        getSettings().setDatabaseEnabled(true);


        // 只有17及以上才可以
        if (android.os.Build.VERSION.SDK_INT >= 17)
            //设置WebView是否通过手势触发播放媒体，默认是true，需要手势触发。
            getSettings().setMediaPlaybackRequiresUserGesture(false);

        String cacheDirPath = SdcardConfig.WEB_CACHE_FOLDER;
        //设置当前Application缓存文件路径
        //设置网页缓存路径:数据库+Application
        getSettings().setDatabasePath(cacheDirPath);
        getSettings().setAppCachePath(cacheDirPath);
        //设置Application缓存权限
        getSettings().setAppCacheEnabled(true);

        //HTTP请求头部用来标识客户端信息的字符串-Set用来设置的,Get用来获取的
        //为了建立手机客户端的信息数据库，需要从手机的http请求中取到这一字符串
        getSettings().setUserAgentString(getSettings().getUserAgentString() + "  Flygift/android");

        //加入进度条
        addProgressBar();


        setWebViewClient(new WebViewClient() {

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (!request.getUrl().toString().equals("xmg://backToLoaclPage")){
                    loadMessageUrl("file:///android_asset/error_disconnect.html");
                }
                super.onReceivedError(view, request, error);
            }

            //API23前调用
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (!failingUrl.equals("xmg://backToLoaclPage")){
                    loadMessageUrl("file:///android_asset/error_disconnect.html");
                }
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            //API版本21才执行，用于根据Http状态码来判断是否跳转到错误页
            //看源码发现这个方法是http状态码大于等于400才会被调用，这样的话一些400-499的图片加载出错等都会调用
            //所以直接设为服务器出错才跳到错误页
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                //服务器出错
                if (errorResponse.getStatusCode() >= 500) {
                    loadMessageUrl("file:///android_asset/error_http.html");
                }
            }
        });

        setWebChromeClient(new WebChromeClient() {

            //支持javascript的确认框 confirm
            @Override
            public boolean onJsConfirm(WebView view, String url,
                                       String message, final android.webkit.JsResult result) {
                return true;
            }
            //支持javascript的警告框 alert
            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                                     final android.webkit.JsResult result) {
                return false;
            }

            //获取网页进度
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                //回调Listener
                if (progressChangeListener != null){
                    progressChangeListener.onWebViewProgressChange(newProgress);
                    System.out.println("progressChangeListener:"+newProgress);
                }
                //更新进度条
                if (newProgress == 100) {
                    mProgressBar.setVisibility(GONE);
                } else {
                    if (mProgressBar.getVisibility() == GONE)
                        mProgressBar.setVisibility(VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
    }

    /**
     * @Title: loadMessageUrl
     * @Description: 加载网页url
     * @param url
     * @return void
     */


    // 点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器

    public void loadMessageUrl(String url) {
        super.loadUrl(url);

    }

    /**
     * @Title: addProgressBar
     * @Description: 添加进度条
     * @return void
     */
    @SuppressWarnings("deprecation")
    public void addProgressBar() {
        mProgressBar = new ProgressBar(getContext(), null,
                android.R.attr.progressBarStyleHorizontal);
        mProgressBar.setLayoutParams(new AbsoluteLayout.LayoutParams(
                AbsoluteLayout.LayoutParams.MATCH_PARENT, ScreenUtil.dip2px(5), 0, 0));
        //调用自定义样式drawable
        mProgressBar.setProgressDrawable(getContext().getResources()
                .getDrawable(R.drawable.web_loading));
        addView(mProgressBar);
        mProgressBar.setVisibility(GONE);


    }

    public void setProgressChangeListener(WebViewProgressChangeListener progressChangeListener) {
        this.progressChangeListener = progressChangeListener;
    }


    /**
     * @Title: destroyWebView
     * @Description: 回收webview
     * @return void
     */

    public void destroyWebView() {
        clearCache(true);
        clearHistory();
    }

}
