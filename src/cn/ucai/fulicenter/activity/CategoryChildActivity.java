package cn.ucai.fulicenter.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.adapter.GoodAdapter;
import cn.ucai.fulicenter.bean.CategoryChildBean;
import cn.ucai.fulicenter.bean.ColorBean;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.ImageUtils;
import cn.ucai.fulicenter.utils.Utils;
import cn.ucai.fulicenter.view.CatChildFilterButton;
import cn.ucai.fulicenter.view.ColorFilterButton;
import cn.ucai.fulicenter.view.DisplayUtils;

/**
 * Created by clawpo on 16/6/15.
 */
public class CategoryChildActivity extends BaseActivity {
    public static final String TAG = CategoryChildActivity.class.getName();

    CategoryChildActivity mContext;
    ArrayList<NewGoodBean> mGoodList;
    GoodAdapter mAdapter;
    private  int pageId = 0;
    private int action = I.ACTION_DOWNLOAD;
    String path;

    /** 下拉刷新控件*/
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    TextView mtvHint;
    GridLayoutManager mGridLayoutManager;
    // 按价格排序
    Button mbtnPriceSort;
    // 按上架时间排序
    Button mbtnAddTimeSort;
    // 商品按价格排序   true：升序排序   false：降序排序
    boolean mSortByPriceAsc;
    // 商品按上架时间排序  true：升序排序   false：降序排序
    boolean mSortByAddTimeAsc;
    // 当前排序
    private int sortBy;

    CatChildFilterButton mCatChildFilterButton;
    String groupName;
    ArrayList<CategoryChildBean> mChildList;
    ColorFilterButton mColorFilterButton;
    int catId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_child);
        mGoodList = new ArrayList<NewGoodBean>();
        mChildList = new ArrayList<CategoryChildBean>();
        mContext = this;
        sortBy = I.SORT_BY_ADDTIME_DESC;
        initView();
        initData();
        setListener();
    }

    private void setListener() {
        setPullDownRefreshListener();
        setPullUpRefreshListener();
        SortStateChangedListener sscl = new SortStateChangedListener();
        mbtnPriceSort.setOnClickListener(sscl);
        mbtnAddTimeSort.setOnClickListener(sscl);
        mCatChildFilterButton.setOnCatFilterClickListener(groupName,mChildList);
    }

    /**
     * 上拉刷新事件监听
     */
    private void setPullUpRefreshListener() {
        mRecyclerView.setOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    int lastItemPosition;
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if(newState == RecyclerView.SCROLL_STATE_IDLE &&
                                lastItemPosition == mAdapter.getItemCount()-1){
                            if(mAdapter.isMore()){
                                mSwipeRefreshLayout.setRefreshing(true);
                                action = I.ACTION_PULL_UP;
                                pageId +=I.PAGE_SIZE_DEFAULT;
                                getPath(pageId);
                                mContext.executeRequest(new GsonRequest<NewGoodBean[]>(path,
                                        NewGoodBean[].class,responseDownloadNewGoodListener(),
                                        mContext.errorListener()));
                            }
                        }
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        //获取最后列表项的下标
                        lastItemPosition = mGridLayoutManager.findLastVisibleItemPosition();
                        //解决RecyclerView和SwipeRefreshLayout共用存在的bug
                        mSwipeRefreshLayout.setEnabled(mGridLayoutManager
                                .findFirstCompletelyVisibleItemPosition() == 0);
                    }
                }
        );
    }

    /**
     * 下拉刷新事件监听
     */
    private void setPullDownRefreshListener() {
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener(){
                    @Override
                    public void onRefresh() {
                        mtvHint.setVisibility(View.VISIBLE);
                        pageId = 0;
                        action = I.ACTION_PULL_DOWN;
                        getPath(pageId);
                        mContext.executeRequest(new GsonRequest<NewGoodBean[]>(path,
                                NewGoodBean[].class,responseDownloadNewGoodListener(),
                                mContext.errorListener()));
                    }
                }
        );
    }

    private void initData() {
        try {
            getPath(pageId);
            mContext.executeRequest(new GsonRequest<NewGoodBean[]>(path,
                    NewGoodBean[].class,responseDownloadNewGoodListener(),
                    mContext.errorListener()));
            String path = new ApiParams().with(I.Color.CAT_ID,catId+"")
                    .getRequestUrl(I.REQUEST_FIND_COLOR_LIST);
            mContext.executeRequest(new GsonRequest<ColorBean[]>(path,ColorBean[].class,
                    responseDownloadColorListListener(),mContext.errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Response.Listener<ColorBean[]> responseDownloadColorListListener() {
        return new Response.Listener<ColorBean[]>() {
            @Override
            public void onResponse(ColorBean[] colorBeen) {
                if (colorBeen!=null){
                    ArrayList<ColorBean> list = Utils.array2List(colorBeen);
                    mColorFilterButton.setVisibility(View.VISIBLE);
                    mColorFilterButton.setOnColorFilterClickListener(groupName,mChildList,list);
                }
            }
        };
    }

    private String getPath(int pageId){
        try {
            mChildList = (ArrayList<CategoryChildBean>) getIntent().getSerializableExtra("childList");
            catId = getIntent().getIntExtra(I.CategoryChild.CAT_ID,0);
            path = new ApiParams()
                    .with(I.NewAndBoutiqueGood.CAT_ID, catId+"")
                    .with(I.PAGE_ID, pageId+"")
                    .with(I.PAGE_SIZE, I.PAGE_SIZE_DEFAULT+"")
                    .getRequestUrl(I.REQUEST_FIND_NEW_BOUTIQUE_GOODS);
            return path;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Response.Listener<NewGoodBean[]> responseDownloadNewGoodListener() {
        return new Response.Listener<NewGoodBean[]>() {
            @Override
            public void onResponse(NewGoodBean[] newGoodBeen) {
                if(newGoodBeen!=null) {
                    mAdapter.setMore(true);
                    mSwipeRefreshLayout.setRefreshing(false);
                    mtvHint.setVisibility(View.GONE);
                    mAdapter.setFooterText(getResources().getString(R.string.load_more));
                    //将数组转换为集合
                    ArrayList<NewGoodBean> list = Utils.array2List(newGoodBeen);
                    if (action == I.ACTION_DOWNLOAD || action == I.ACTION_PULL_DOWN) {
                        mAdapter.initItems(list);
                    } else if (action == I.ACTION_PULL_UP) {
                        mAdapter.addItems(list);
                    }
                    if(newGoodBeen.length<I.PAGE_SIZE_DEFAULT){
                        mAdapter.setMore(false);
                        mAdapter.setFooterText(getResources().getString(R.string.no_more));
                    }
                }
            }
        };
    }

    private void initView() {
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.category_child_srl);
        mSwipeRefreshLayout.setColorSchemeColors(
                R.color.google_blue,
                R.color.google_green,
                R.color.google_red,
                R.color.google_yellow
        );
        mtvHint = (TextView)findViewById(R.id.fragment_tv_hint);
        mGridLayoutManager = new GridLayoutManager(mContext, I.COLUM_NUM);
        mGridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = (RecyclerView)findViewById(R.id.rv_category_child);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mAdapter = new GoodAdapter(mContext,mGoodList,sortBy);
        mRecyclerView.setAdapter(mAdapter);
        groupName = getIntent().getStringExtra(I.CategoryGroup.NAME);
        DisplayUtils.initBack(mContext);
        mbtnPriceSort = (Button) findViewById(R.id.btn_price_sort);
        mbtnAddTimeSort = (Button) findViewById(R.id.btn_add_time_sort);
        mCatChildFilterButton = (CatChildFilterButton) findViewById(R.id.btnCatChildFilter);
        mColorFilterButton = (ColorFilterButton) findViewById(R.id.btnColorFilter);
        mColorFilterButton.setVisibility(View.INVISIBLE);
        mCatChildFilterButton.setText(groupName);
    }

    class SortStateChangedListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Drawable right = null;
            int resId;
            switch (v.getId()){
                case R.id.btn_price_sort:
                    if (mSortByPriceAsc){
                        sortBy = I.SORT_BY_PRICE_ASC;
                        right = mContext.getResources().getDrawable(R.drawable.arrow_order_up);
                        resId = R.drawable.arrow_order_up;
                    }else {
                        sortBy = I.SORT_BY_PRICE_DESC;
                        right = mContext.getResources().getDrawable(R.drawable.arrow_order_down);
                        resId = R.drawable.arrow_order_down;
                    }
                    mSortByPriceAsc=!mSortByPriceAsc;
                    right.setBounds(0,0, ImageUtils.getDrawableWidth(mContext,resId),
                            ImageUtils.getDrawableHeight(mContext,resId));
                    mbtnPriceSort.setCompoundDrawables(null,null,right,null);
                    break;
                case R.id.btn_add_time_sort:
                    if (mSortByAddTimeAsc){
                        sortBy = I.SORT_BY_ADDTIME_ASC;
                        right = mContext.getResources().getDrawable(R.drawable.arrow_order_up);
                        resId = R.drawable.arrow_order_up;
                    }else {
                        sortBy = I.SORT_BY_ADDTIME_DESC;
                        right = mContext.getResources().getDrawable(R.drawable.arrow_order_down);
                        resId = R.drawable.arrow_order_down;
                    }
                    mSortByAddTimeAsc=!mSortByAddTimeAsc;
                    right.setBounds(0,0, ImageUtils.getDrawableWidth(mContext,resId),
                            ImageUtils.getDrawableHeight(mContext,resId));
                    mbtnAddTimeSort.setCompoundDrawables(null,null,right,null);
                    break;
            }
            mAdapter.setSortBy(sortBy);
        }
    }
}
