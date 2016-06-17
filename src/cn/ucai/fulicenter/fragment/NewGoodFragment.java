package cn.ucai.fulicenter.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.FuliCenterMainActivity;
import cn.ucai.fulicenter.adapter.GoodAdapter;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by Administrator on 2016/6/16.
 */
public class NewGoodFragment extends Fragment {
    FuliCenterMainActivity mContext;
    ArrayList<NewGoodBean> mGoodList;
    int pageId = 0;
    GoodAdapter mGoodAdapter;
    private int action = I.ACTION_DOWNLOAD;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    TextView mtvHint;
    GridLayoutManager mGridLayoutManager;
    String path;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = (FuliCenterMainActivity) getActivity();
        View layout = View.inflate(mContext,R.layout.fragment_new_goods,null);
        mGoodList = new ArrayList<NewGoodBean>();
        initView(layout);
        setListener();
        initData();
        return layout;
    }

    private void setListener() {
        setPullDownRefreshListener();
        setPullUpRefreshListener();
    }

    private void setPullUpRefreshListener() {
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastItemPosition;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState==RecyclerView.SCROLL_STATE_IDLE &&
                        lastItemPosition==mGoodAdapter.getItemCount()-1){
                    if (mGoodAdapter.isMore()){
                        mSwipeRefreshLayout.setRefreshing(true);
                        action = I.ACTION_PULL_DOWN;
                        pageId += I.PAGE_SIZE_DEFAULT;
                        getPath(pageId);
                        mContext.executeRequest(new GsonRequest<NewGoodBean[]>(path,NewGoodBean[].class,
                                reponseDownloadNewGoodsListener(),mContext.errorListener()));
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastItemPosition = mGridLayoutManager.findLastVisibleItemPosition();
                mSwipeRefreshLayout.setEnabled(mGridLayoutManager
                        .findFirstCompletelyVisibleItemPosition()==0);
            }
        });
    }

    private void setPullDownRefreshListener() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mtvHint.setVisibility(View.VISIBLE);
                pageId = 0;
                action = I.ACTION_PULL_DOWN;
                getPath(pageId);
                mContext.executeRequest(new GsonRequest<NewGoodBean[]>(path,NewGoodBean[].class,
                        reponseDownloadNewGoodsListener(),mContext.errorListener()));
            }
        });
    }

    private void initData() {
        getPath(pageId);
        mContext.executeRequest(new GsonRequest<NewGoodBean[]>(path,NewGoodBean[].class,
                reponseDownloadNewGoodsListener(),mContext.errorListener()));
    }

    private Response.Listener<NewGoodBean[]> reponseDownloadNewGoodsListener() {
        return new Response.Listener<NewGoodBean[]>() {
            @Override
            public void onResponse(NewGoodBean[] newGoodBeen) {
                if (newGoodBeen!=null){
                    mGoodAdapter.setMore(true);
                    mSwipeRefreshLayout.setRefreshing(false);
                    mtvHint.setVisibility(View.GONE);
                    mGoodAdapter.setFooterText(getResources().getString(R.string.load_more));
                    ArrayList<NewGoodBean> list = Utils.array2List(newGoodBeen);
                    if (action==I.ACTION_DOWNLOAD || action==I.ACTION_PULL_DOWN){
                        mGoodAdapter.initList(list);
                    }else if (action==I.ACTION_PULL_UP){
                        mGoodAdapter.addItem(list);
                    }
                    if (newGoodBeen.length<I.PAGE_SIZE_DEFAULT){
                        mGoodAdapter.setMore(false);
                        mGoodAdapter.setFooterText(getResources().getString(R.string.no_more));
                    }
                }
            }
        };
    }

    private String getPath(int pageId) {
        // http://10.0.2.2/ FuLiCenterServer/Server?
      //  request=find_new_boutique_goods&cat_id=&page_id=&page_size=
        try {
            path = new ApiParams()
                    .with(I.NewAndBoutiqueGood.CAT_ID,I.CAT_ID+"")
                    .with(I.PAGE_SIZE,I.PAGE_SIZE_DEFAULT+"")
                    .with(I.PAGE_ID,pageId+"")
                    .getRequestUrl(I.REQUEST_FIND_NEW_BOUTIQUE_GOODS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    private void initView(View layout) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.new_good_srl);
        mSwipeRefreshLayout.setColorSchemeColors(
                R.color.google_blue,
                R.color.google_green,
                R.color.google_red,
                R.color.google_yellow
        );
        mtvHint = (TextView) layout.findViewById(R.id.fragment_tv_hint);
        mGridLayoutManager = new GridLayoutManager(mContext,I.COLUM_NUM);
        mGridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.rv_new_good);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mGoodAdapter = new GoodAdapter(mContext,mGoodList);
        mRecyclerView.setAdapter(mGoodAdapter);
    }
}
