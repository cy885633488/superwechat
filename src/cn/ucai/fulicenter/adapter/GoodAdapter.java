package cn.ucai.fulicenter.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.utils.ImageUtils;

/**
 * Created by Administrator on 2016/6/16.
 */
public class GoodAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    Context mContext;
    ArrayList<NewGoodBean> mGoodList;
    NewGoodViewHolder mGoodHolder;
    FooterViewHolder mFooterHolder;
    String footerText;
    private boolean isMore;
    int sortBy;

    public void setSortBy(int sortBy){
        this.sortBy = sortBy;
        sort(sortBy);
        notifyDataSetChanged();
    }

    private void sort(final int sortBy) {
        Collections.sort(mGoodList, new Comparator<NewGoodBean>() {
            @Override
            public int compare(NewGoodBean g1, NewGoodBean g2) {
                int result = 0;
                switch (sortBy){
                    case I.SORT_BY_ADDTIME_ASC:
                        result = (int) (g1.getAddTime()-g2.getAddTime());
                        break;
                    case I.SORT_BY_ADDTIME_DESC:
                        result = (int) (g2.getAddTime()-g1.getAddTime());
                        break;
                    case I.SORT_BY_PRICE_ASC:
                    {
                        int p1 = converPrice(g1.getCurrencyPrice());
                        int p2 = converPrice(g2.getCurrencyPrice());
                        result = p2-p1;
                    }
                    break;
                }
                return result;
            }

            private int converPrice(String currencyPrice) {
                currencyPrice = currencyPrice.substring(currencyPrice.indexOf("￥")+1);
                int p1 = Integer.parseInt(currencyPrice);
                return p1;
            }
        });
    }

    public void setFooterText(String footerText){
        this.footerText = footerText;
        notifyDataSetChanged();
    }

    public boolean isMore() {
        return isMore;
    }

    public void setMore(boolean more) {
        isMore = more;
    }

    public GoodAdapter(Context mContext, ArrayList<NewGoodBean> mGoodList,int sortBy) {
        this.mContext = mContext;
        this.mGoodList = mGoodList;
        this.sortBy = sortBy;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        RecyclerView.ViewHolder holder = null;
        switch (viewType){
            case I.TYPE_FOOTER:
                holder = new FooterViewHolder(inflater.inflate(R.layout.item_footer,parent,false));
                break;
            case I.TYPE_ITEM:
                holder = new NewGoodViewHolder(inflater.inflate(R.layout.item_new_good,parent,false));
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FooterViewHolder){
            ((FooterViewHolder) holder).tvFooter.setText(footerText);
            ((FooterViewHolder) holder).tvFooter.setVisibility(View.VISIBLE);
        }
        if (holder instanceof NewGoodViewHolder){
            NewGoodBean good = mGoodList.get(position);
            ((NewGoodViewHolder) holder).tvGoodName.setText(good.getGoodsName());
            ((NewGoodViewHolder) holder).tvGoodPrice.setText(good.getCurrencyPrice());
            NetworkImageView niv = ((NewGoodViewHolder) holder).nivGoodThumb;
            ImageUtils.setNewGoodThumb(good.getGoodsThumb(),niv);
        }
    }

    @Override
    public int getItemCount() {
        return mGoodList==null?1:mGoodList.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position==getItemCount()-1){
            return I.TYPE_FOOTER;
        }else {
            return I.TYPE_ITEM;
        }
    }

    public void initList(ArrayList<NewGoodBean> list) {
        if (mGoodList!=null && mGoodList.isEmpty()){
            mGoodList.clear();
            mGoodList.addAll(list);
            setSortBy(sortBy);
            notifyDataSetChanged();
        }
    }

    public void addItem(ArrayList<NewGoodBean> list) {
        for (NewGoodBean ngb:mGoodList){
            if (!mGoodList.contains(ngb)){
                mGoodList.add(ngb);
                setSortBy(sortBy);
                notifyDataSetChanged();
            }
        }
    }
    class NewGoodViewHolder extends RecyclerView.ViewHolder{
        LinearLayout llGoodLayout;
        NetworkImageView nivGoodThumb;
        TextView tvGoodName,tvGoodPrice;

        public NewGoodViewHolder(View itemView) {
            super(itemView);
            llGoodLayout = (LinearLayout) itemView.findViewById(R.id.layout_new_good);
            nivGoodThumb = (NetworkImageView) itemView.findViewById(R.id.niv_good_thumb);
            tvGoodName = (TextView) itemView.findViewById(R.id.good_name);
            tvGoodPrice = (TextView) itemView.findViewById(R.id.good_price);
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder{
        TextView tvFooter;
        public FooterViewHolder(View itemView) {
            super(itemView);
            tvFooter = (TextView) itemView.findViewById(R.id.tv_good_footer);
        }
    }
}
