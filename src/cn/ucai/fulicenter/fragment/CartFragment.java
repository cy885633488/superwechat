package cn.ucai.fulicenter.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.FuliCenterMainActivity;
import cn.ucai.fulicenter.adapter.CartAdapter;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by Administrator on 2016/6/18.
 */
public class CartFragment extends Fragment {
    FuliCenterMainActivity mContext;
    ArrayList<CartBean> mCartList;
    CartAdapter mAdapter;
    private int action = I.ACTION_DOWNLOAD;
    String path;
    int pageId = 0;
    /** 下拉刷新控件*/
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    TextView mtvHint,mtvCurrencyPrice,mtvSavePrice;
    LinearLayoutManager mLinearLayoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mContext = (FuliCenterMainActivity) getActivity();
        View layout = View.inflate(mContext, R.layout.fragment_cart,null);
        mCartList = new ArrayList<CartBean>();
        initView(layout);
        setListener();
        initData();
        return layout;
    }
    private void setListener() {
        setPullDownRefreshListener();
        setPullUpRefreshListener();
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
                                getPath();
                                mContext.executeRequest(new GsonRequest<CartBean[]>(path,
                                        CartBean[].class, responseDownloadCartListener(),
                                        mContext.errorListener()));
                            }
                        }
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        //获取最后列表项的下标
                        lastItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();
                        //解决RecyclerView和SwipeRefreshLayout共用存在的bug
                        mSwipeRefreshLayout.setEnabled(mLinearLayoutManager
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
                        action = I.ACTION_PULL_DOWN;
                        getPath();
                        mContext.executeRequest(new GsonRequest<CartBean[]>(path,
                                CartBean[].class, responseDownloadCartListener(),
                                mContext.errorListener()));
                    }
                }
        );
    }

    private void initData() {
        try {
            getPath();
            ArrayList<CartBean> cartList = FuLiCenterApplication.getInstance().getCartList();
            mCartList.clear();
            mCartList.addAll(cartList);
            sumCartPrice();
            mContext.executeRequest(new GsonRequest<CartBean[]>(path,
                    CartBean[].class, responseDownloadCartListener(),
                    mContext.errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String getPath(){
        try {
            path = new ApiParams()
                    .with(I.PAGE_ID,pageId+"")
                    .with(I.PAGE_SIZE,I.PAGE_SIZE_DEFAULT+"")
                    .with(I.Cart.USER_NAME, FuLiCenterApplication.getInstance().getUserName())
                    .getRequestUrl(I.REQUEST_FIND_CARTS);
            return path;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Response.Listener<CartBean[]> responseDownloadCartListener() {
        return new Response.Listener<CartBean[]>() {
            @Override
            public void onResponse(CartBean[] cartBeen) {
                if(cartBeen!=null) {
                    mAdapter.setMore(true);
                    mSwipeRefreshLayout.setRefreshing(false);
                    mtvHint.setVisibility(View.GONE);
                    //将数组转换为集合
                    ArrayList<CartBean> list = Utils.array2List(cartBeen);
                    if (action == I.ACTION_DOWNLOAD || action == I.ACTION_PULL_DOWN) {
                        mAdapter.initItems(list);
                    } else if (action == I.ACTION_PULL_UP) {
                        mAdapter.addItems(list);
                    }
                    if(cartBeen.length<I.PAGE_SIZE_DEFAULT){
                        mAdapter.setMore(false);
                    }
                }
            }
        };
    }

    private void initView(View layout) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.srl_cart);
        mSwipeRefreshLayout.setColorSchemeColors(
                R.color.google_blue,
                R.color.google_green,
                R.color.google_red,
                R.color.google_yellow
        );
        mtvHint = (TextView) layout.findViewById(R.id.tv_cart_hint);
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.rv_cart);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mAdapter = new CartAdapter(mContext, mCartList);
        mRecyclerView.setAdapter(mAdapter);
        mtvCurrencyPrice = (TextView) layout.findViewById(R.id.tv_cart_good_price_zonghe);
        mtvSavePrice = (TextView) layout.findViewById(R.id.tv_cart_shengqian);
    }

    public void sumCartPrice(){
        int rankPrice = 0;
        int currencyPrice = 0;
        if (mCartList!=null && mCartList.size()>0){
            for (CartBean cart:mCartList){
                if (cart!=null && cart.isChecked()){
                    currencyPrice += cart.getCount()*convertPrice(cart.getGoods().getCurrencyPrice());
                    rankPrice += cart.getCount()*convertPrice(cart.getGoods().getRankPrice());
                }
            }
            int savePrice = currencyPrice-rankPrice;
            mtvCurrencyPrice.setText("合计：￥"+currencyPrice);
            mtvSavePrice.setText("节省：￥"+savePrice);
        }
    }

    private int convertPrice(String price){
        price = price.substring(price.indexOf("￥")+1);
        int p1 = Integer.parseInt(price);
        return p1;
    }
}
