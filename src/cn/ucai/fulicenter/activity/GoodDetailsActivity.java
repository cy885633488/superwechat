package cn.ucai.fulicenter.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import cn.ucai.fulicenter.D;
import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.AlbumBean;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.bean.MessageBean;
import cn.ucai.fulicenter.bean.User;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.task.DownloadCollectCountTask;
import cn.ucai.fulicenter.utils.ImageUtils;
import cn.ucai.fulicenter.utils.Utils;
import cn.ucai.fulicenter.view.DisplayUtils;
import cn.ucai.fulicenter.view.FlowIndicator;
import cn.ucai.fulicenter.view.SlideAutoLoopView;

/**
 * Created by Administrator on 2016/6/17.
 */
public class GoodDetailsActivity extends BaseActivity {
    Context mContext;
    GoodDetailsBean mGoodDetailsBean;
    int mGoodsId;

    SlideAutoLoopView mSlideAutoLoopView;
    FlowIndicator mFlowIndicator;
    // 显示颜色的容器布局
    LinearLayout mLinearColors;
    ImageView mivCollect,mivShare,mivAddCart;
    TextView mtvCartCount,tvGoodName,tvGoodEnglishName,tvShopPrice,tvCurrencyPrice;
    WebView wvGoodBrief;

    // 当前颜色值
    int mCurrentColor;
    boolean isCollect;
    int actionCollect;
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_good_details);
        mContext = this;
        initView();
        initData();
        setListener();
    }

    private void setListener() {
        setCollectListener();
        setAddCartListener();
        registerUpdateCartCountReceiver();
    }

    private void setAddCartListener() {
        Log.i("main","setAddCartListener");
        mivAddCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.addCart(mContext,mGoodDetailsBean);
            }
        });
    }

    private void setCollectListener() {
        mivCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = FuLiCenterApplication.getInstance().getUser();
                if (user!=null){
                    try {
                        String path;
                        if (isCollect) {
                            actionCollect = I.ACTION_DEL_COLLECT;
                            path = new ApiParams()
                                    .with(I.Collect.USER_NAME, user.getMUserName())
                                    .with(I.Collect.GOODS_ID,mGoodsId+"")
                                    .getRequestUrl(I.REQUEST_DELETE_COLLECT);
                        }else {
                            actionCollect = I.ACTION_ADD_COLLECT;
                            path = new ApiParams()
                                    .with(I.Collect.USER_NAME,user.getMUserName())
                                    .with(I.Collect.GOODS_ID,mGoodsId+"")
                                    .with(I.Collect.GOODS_NAME,mGoodDetailsBean.getGoodsName())
                                    .with(I.Collect.GOODS_ENGLISH_NAME,mGoodDetailsBean.getGoodsEnglishName())
                                    .with(I.Collect.GOODS_THUMB,mGoodDetailsBean.getGoodsThumb())
                                    .with(I.Collect.GOODS_IMG,mGoodDetailsBean.getGoodsImg())
                                    .with(I.Collect.ADD_TIME,mGoodDetailsBean.getAddTime()+"")
                                    .getRequestUrl(I.REQUEST_ADD_COLLECT);
                        }
                        executeRequest(new GsonRequest<MessageBean>(path,MessageBean.class,
                                responseSetCollectListener(),errorListener()));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else {
                    startActivity(new Intent(GoodDetailsActivity.this,LoginActivity.class));
                }
            }
        });
    }

    private Response.Listener<MessageBean> responseSetCollectListener() {
        return new Response.Listener<MessageBean>() {
            @Override
            public void onResponse(MessageBean messageBean) {
                if (messageBean.isSuccess()){
                    if (actionCollect==I.ACTION_ADD_COLLECT){
                        isCollect = true;
                        mivCollect.setImageResource(R.drawable.bg_collect_out);
                    }else if (actionCollect==I.ACTION_DEL_COLLECT){
                        isCollect = false;
                        mivCollect.setImageResource(R.drawable.bg_collect_in);
                    }
                    new DownloadCollectCountTask(mContext).execute();
                }
                Utils.showToast(mContext,messageBean.getMsg(),Toast.LENGTH_SHORT);
            }
        };
    }

    private void initData() {
        mGoodsId = getIntent().getIntExtra(D.NewGood.KEY_GOODS_ID,0);
        try {
            String path = new ApiParams()
                    .with(D.NewGood.KEY_GOODS_ID,mGoodsId+"")
                    .getRequestUrl(I.REQUEST_FIND_GOOD_DETAILS);
            executeRequest(new GsonRequest<GoodDetailsBean>(path,GoodDetailsBean.class,
                    responseDownloadGoodDetails(),errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Response.Listener<GoodDetailsBean> responseDownloadGoodDetails() {
        return new Response.Listener<GoodDetailsBean>() {
            @Override
            public void onResponse(GoodDetailsBean goodDetailsBean) {
                if (goodDetailsBean!=null){
                    mGoodDetailsBean = goodDetailsBean;
                    DisplayUtils.initBackWithTitle(GoodDetailsActivity.this,
                            getResources().getString(R.string.title_good_details));
                    tvCurrencyPrice.setText(mGoodDetailsBean.getCurrencyPrice());
                    tvGoodEnglishName.setText(mGoodDetailsBean.getGoodsEnglishName());
                    tvGoodName.setText(mGoodDetailsBean.getGoodsName());
                    wvGoodBrief.loadDataWithBaseURL(null,mGoodDetailsBean.getGoodsBrief().trim(),
                            D.TEXT_HTML,D.UTF_8,null);
                    //初始化颜色面板
                    initColorsBanner();
                }else {
                    Utils.showToast(mContext,"商品详情下载失败", Toast.LENGTH_LONG);
                    finish();
                }
            }
        };
    }

    private void initColorsBanner() {
        // 设置第一个颜色的图片轮播
        updateColor(0);
        for (int i=0;i<mGoodDetailsBean.getProperties().length;i++){
            mCurrentColor = i;
            View layout = View.inflate(mContext,R.layout.layout_property_color,null);
            NetworkImageView ivColor = (NetworkImageView) layout.findViewById(R.id.ivColorItem);
            String colorImg = mGoodDetailsBean.getProperties()[i].getColorImg();
            if (colorImg.isEmpty()){
                continue;
            }
            ImageUtils.setGoodDetailThumb(colorImg,ivColor);
            mLinearColors.addView(layout);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateColor(mCurrentColor);
                }
            });
        }
    }

    /**
     * 设置指定属性的图片轮播
     * @param i
     */
    private void updateColor(int i) {
        AlbumBean[] albums = mGoodDetailsBean.getProperties()[i].getAlbums();
        String[] albumsImgUrl = new String[albums.length];
        for (int j=0;j<albumsImgUrl.length;j++){
            albumsImgUrl[j] = albums[j].getImgUrl();
        }
        mSlideAutoLoopView.startPlayLoop(mFlowIndicator,albumsImgUrl,albumsImgUrl.length);
    }

    private void initView() {
        mSlideAutoLoopView = (SlideAutoLoopView) findViewById(R.id.salv);
        mFlowIndicator = (FlowIndicator) findViewById(R.id.indicator);
        mLinearColors = (LinearLayout) findViewById(R.id.layoutColor);
        mivCollect = (ImageView) findViewById(R.id.ivCollect);
        mivAddCart = (ImageView) findViewById(R.id.ivAddCart);
        mivShare = (ImageView) findViewById(R.id.ivShare);
        mtvCartCount = (TextView) findViewById(R.id.tvCartCount);
        tvGoodName = (TextView) findViewById(R.id.tvGoodName);
        tvGoodEnglishName = (TextView) findViewById(R.id.tvGoodEnglishName);
        tvShopPrice = (TextView) findViewById(R.id.tvShopPrice);
        tvCurrencyPrice = (TextView) findViewById(R.id.tvCurrencyPrice);
        wvGoodBrief = (WebView) findViewById(R.id.wvGoodBrief);
        WebSettings settings = wvGoodBrief.getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setBuiltInZoomControls(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCollectStatus();
        initCartCountStatus();
    }

    private void initCartCountStatus() {
        int count = Utils.sumCartCount();
        if (count>0){
            mtvCartCount.setVisibility(View.VISIBLE);
            mtvCartCount.setText(""+count);
        }else {
            mtvCartCount.setVisibility(View.GONE);
        }
    }

    private void initCollectStatus() {
        User user = FuLiCenterApplication.getInstance().getUser();
        if (user!=null){
            // http://10.0.2.2:8080/FuLiCenterServer/Server?request=is_collect&goods_id=&userName=
            try {
                String path = new ApiParams()
                        .with(I.Collect.GOODS_ID,mGoodsId+"")
                        .with(I.Collect.USER_NAME,FuLiCenterApplication.getInstance().getUserName())
                        .getRequestUrl(I.REQUEST_IS_COLLECT);
                executeRequest(new GsonRequest<MessageBean>(path,MessageBean.class,
                        responseIsCollectListener(),errorListener()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            isCollect = false;
            mivCollect.setImageResource(R.drawable.bg_collect_in);
        }
    }

    private Response.Listener<MessageBean> responseIsCollectListener() {
        return new Response.Listener<MessageBean>() {
            @Override
            public void onResponse(MessageBean messageBean) {
                if (messageBean.isSuccess()){
                    isCollect = true;
                    mivCollect.setImageResource(R.drawable.bg_collect_out);
                }else {
                    isCollect = false;
                    mivCollect.setImageResource(R.drawable.bg_collect_in);
                }
            }
        };
    }

    class UpdateCartCountReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            initCartCountStatus();
        }
    }
    UpdateCartCountReceiver mReceiver;
    public void registerUpdateCartCountReceiver(){
        mReceiver = new UpdateCartCountReceiver();
        IntentFilter filter = new IntentFilter("update_cart");
        registerReceiver(mReceiver,filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver!=null){
            unregisterReceiver(mReceiver);
        }
    }
}
