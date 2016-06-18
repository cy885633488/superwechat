package cn.ucai.fulicenter.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cn.ucai.fulicenter.D;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.GoodDetailsActivity;
import cn.ucai.fulicenter.bean.BoutiqueBean;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.utils.ImageUtils;
import cn.ucai.fulicenter.view.FooterViewHolder;

import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * Created by clawpo on 16/6/15.
 */
public class BoutiqueAdapter extends RecyclerView.Adapter<ViewHolder> {
    Context mContext;
    ArrayList<BoutiqueBean> mBoutiqueList;

    BoutiqueItemViewHolder boutiqueHolder;
    FooterViewHolder footerHolder;

    private String footerText;
    private boolean isMore;

    public void setFooterText(String footerText) {
        this.footerText = footerText;
        notifyDataSetChanged();
    }

    public boolean isMore() {
        return isMore;
    }

    public void setMore(boolean more) {
        isMore = more;
    }

    public BoutiqueAdapter(Context mContext, ArrayList<BoutiqueBean> mBoutiqueList) {
        this.mContext = mContext;
        this.mBoutiqueList = mBoutiqueList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewHolder holder = null;
        switch (viewType){
            case I.TYPE_ITEM:
                holder = new BoutiqueItemViewHolder(inflater.inflate(R.layout.item_boutique,parent,false));
                break;
            case I.TYPE_FOOTER:
                holder = new FooterViewHolder(inflater.inflate(R.layout.item_footer,parent,false));
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(holder instanceof FooterViewHolder){
            footerHolder = (FooterViewHolder) holder;
            footerHolder.tvFooter.setText(footerText);
            footerHolder.tvFooter.setVisibility(View.VISIBLE);
        }
        if(holder instanceof BoutiqueItemViewHolder){
            boutiqueHolder = (BoutiqueItemViewHolder) holder;
            final BoutiqueBean boutique = mBoutiqueList.get(position);
            Log.e("main","mBoutiqueList = " +boutique.getTitle()+"");
            Log.e("main","holder = " +holder.toString());
            boutiqueHolder.tv1Boutique.setText(boutique.getTitle());
            boutiqueHolder.tv2Boutique.setText(boutique.getName());
            boutiqueHolder.tv3Boutique.setText(boutique.getDescription());
            ImageUtils.setNewGoodThumb(boutique.getImageurl(),boutiqueHolder.nivThumb);

            boutiqueHolder.layoutBoutique.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(mContext, GoodDetailsActivity.class)
                            .putExtra(D.Boutique.KEY_ID,boutique.getId()));
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mBoutiqueList==null?1:mBoutiqueList.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if(position==getItemCount()-1){
            return I.TYPE_FOOTER;
        }else{
            return I.TYPE_ITEM;
        }
    }

    public void initItems(ArrayList<BoutiqueBean> list) {
        if(mBoutiqueList!=null && !mBoutiqueList.isEmpty()){
            mBoutiqueList.clear();
        }
        mBoutiqueList.addAll(list);
        notifyDataSetChanged();
    }

    public void addItems(ArrayList<BoutiqueBean> list) {
        mBoutiqueList.addAll(list);
        notifyDataSetChanged();
    }

    class BoutiqueItemViewHolder extends ViewHolder{
        RelativeLayout layoutBoutique;
        NetworkImageView nivThumb;
        TextView tv1Boutique;
        TextView tv2Boutique;
        TextView tv3Boutique;

        public BoutiqueItemViewHolder(View itemView) {
            super(itemView);
            layoutBoutique = (RelativeLayout) itemView.findViewById(R.id.layout_boutique);
            nivThumb = (NetworkImageView) itemView.findViewById(R.id.niv_boutique_thumb);
            tv1Boutique = (TextView) itemView.findViewById(R.id.tv_boutique_1);
            tv2Boutique = (TextView) itemView.findViewById(R.id.tv_boutique_2);
            tv3Boutique = (TextView) itemView.findViewById(R.id.tv_boutique_3);
        }
    }
}
