package cn.ucai.fulicenter.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.activity.BaseActivity;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.Contact;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by Administrator on 2016/5/23.
 */
public class DownloadCartListTask extends BaseActivity {
    private final static String TAG = DownloadCartListTask.class.getCanonicalName();
    Context mContext;
    String username;
    String path;
    int pageId;
    int pageSize;
    int listSize;
    ArrayList<CartBean> list;

    public DownloadCartListTask(Context mContext, String username,int pageId,int pageSize) {
        this.mContext = mContext;
        this.username = FuLiCenterApplication.getInstance().getUserName();
        this.pageId = pageId;
        this.pageSize = pageSize;
        initPath();
    }

    private void initPath() {
        // http://10.0.2.2:8080/SuperWeChatServer/Server?request=download_contact_all_list&m_contact_user_name=  下载所有好友列表
        try {
            path = new ApiParams().with(I.Cart.USER_NAME,username)
                    .with(I.PAGE_ID,pageId+"")
                    .with(I.PAGE_SIZE,pageSize+"")
                    .getRequestUrl(I.REQUEST_FIND_CARTS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<CartBean[]>(path,CartBean[].class,
                responseDownloadCartListTaskListener(),errorListener()));
    }

    private Response.Listener<CartBean[]> responseDownloadCartListTaskListener() {
        return new Response.Listener<CartBean[]>() {
            @Override
            public void onResponse(CartBean[] cartBean) {
                if (cartBean!=null){
                    list = Utils.array2List(cartBean);
                    try {
                        for (CartBean cart : list) {
                            path = new ApiParams()
                                    .with(I.Cart.GOODS_ID, cart.getGoodsId() + "")
                                    .getRequestUrl(I.REQUEST_FIND_GOOD_DETAILS);
                            executeRequest(new GsonRequest<GoodDetailsBean>(path,GoodDetailsBean.class,
                                    responseDownloadGoodDeteilsListener(cart),errorListener()));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private Response.Listener<GoodDetailsBean> responseDownloadGoodDeteilsListener(final CartBean cart) {
        return new Response.Listener<GoodDetailsBean>() {
            @Override
            public void onResponse(GoodDetailsBean goodDetailsBean) {
                if (goodDetailsBean!=null){
                    listSize++;
                    cart.setGoods(goodDetailsBean);
                    ArrayList<CartBean> cartList = FuLiCenterApplication.getInstance().getCartList();
                    if (!cartList.contains(cart)){
                        cartList.addAll(list);
                    }
                }
                if (listSize==list.size()){
                    mContext.sendStickyBroadcast(new Intent("update_cart_list"));
                }
            }
        };
    }
}
