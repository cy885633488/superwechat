package cn.ucai.fulicenter.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.utils.ImageUtils;

/**
 * Created by Administrator on 2016/6/22.
 */
public class CartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context mContext;
    ArrayList<CartBean> mCartList;
    boolean isMore;

    public boolean isMore() {
        return isMore;
    }

    public void setMore(boolean more) {
        isMore = more;
    }

    public CartAdapter(Context mContext, ArrayList<CartBean> cartList) {
        this.mContext = mContext;
        this.mCartList = cartList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        CartViewHolder holder = new CartViewHolder(inflater.inflate(R.layout.item_cart,parent,false));
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CartBean cart = mCartList.get(position);
        CartViewHolder cartHolder = (CartViewHolder) holder;
        GoodDetailsBean good = cart.getGoods();
        if (good==null){
            return;
        }
        cartHolder.tvCartGoodName.setText(good.getGoodsName());
        cartHolder.tvCartGoodJiaGe.setText(cart.getGoods().getCurrencyPrice());
        cartHolder.tvCartGoodNum.setText("("+cart.getCount()+")");
        cartHolder.cbCart.setChecked(cart.isChecked());
        ImageUtils.setNewGoodThumb(good.getGoodsThumb(),cartHolder.nivGoodThumb);
    }

    @Override
    public int getItemCount() {
        return mCartList==null?0:mCartList.size();
    }

    public void initItems(ArrayList<CartBean> list) {
        if (mCartList!=null && mCartList.isEmpty()){
            mCartList.clear();
            mCartList.addAll(list);
            notifyDataSetChanged();
        }
    }

    public void addItems(ArrayList<CartBean> list) {
        for (CartBean cart:list){
            if (!mCartList.contains(cart)){
                mCartList.add(cart);
                notifyDataSetChanged();
            }
        }
    }

    class CartViewHolder extends RecyclerView.ViewHolder{
        CheckBox cbCart;
        NetworkImageView nivGoodThumb;
        TextView tvCartGoodName,tvCartGoodNum,tvCartGoodJiaGe;
        ImageView ivCartAdd,ivCartDel;
        public CartViewHolder(View itemView) {
            super(itemView);
            cbCart = (CheckBox) itemView.findViewById(R.id.cb_cart);
            nivGoodThumb = (NetworkImageView) itemView.findViewById(R.id.niv_cart_good_thumb);
            tvCartGoodName = (TextView) itemView.findViewById(R.id.tv_cart_good_name);
            tvCartGoodJiaGe = (TextView) itemView.findViewById(R.id.tv_cart_good_jiage);
            tvCartGoodNum = (TextView) itemView.findViewById(R.id.tv_cart_good_num);
            ivCartAdd = (ImageView) itemView.findViewById(R.id.iv_cart_add);
            ivCartDel = (ImageView) itemView.findViewById(R.id.iv_cart_del);
        }
    }
}
