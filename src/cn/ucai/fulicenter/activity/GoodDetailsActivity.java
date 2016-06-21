package cn.ucai.fulicenter.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;

import cn.ucai.fulicenter.D;
import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.AlbumBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.bean.MessageBean;
import cn.ucai.fulicenter.bean.User;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
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
    ImageView mivCollect,mivAddCart,mivShare;
    TextView mtvCartCount,tvGoodName,tvGoodEnglishName,tvShopPrice,tvCurrencyPrice;
    WebView wvGoodBrief;

    // 当前颜色值
    int mCurrentColor;
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_good_details);
        mContext = this;
        initView();
        initData();
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
            mivCollect.setImageResource(R.drawable.bg_collect_in);
        }
    }

    private Response.Listener<MessageBean> responseIsCollectListener() {
        return new Response.Listener<MessageBean>() {
            @Override
            public void onResponse(MessageBean messageBean) {
                if (messageBean.isSuccess()){
                    mivCollect.setImageResource(R.drawable.bg_collect_out);
                }else {
                    mivCollect.setImageResource(R.drawable.bg_collect_in);
                }
            }
        };
    }
}
