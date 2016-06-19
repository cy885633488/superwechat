package cn.ucai.fulicenter.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.CategoryChildBean;
import cn.ucai.fulicenter.bean.CategoryGroupBean;
import cn.ucai.fulicenter.utils.ImageUtils;

/**
 * Created by Administrator on 2016/6/19.
 */
public class CategoryAdapter extends BaseExpandableListAdapter {
    Context mContext;
    ArrayList<CategoryGroupBean> mCategoryGroupList;
    ArrayList<ArrayList<CategoryChildBean>> mCategoryChildList;

    public CategoryAdapter(Context mContext, ArrayList<CategoryGroupBean> mCategoryGroupList,
                           ArrayList<ArrayList<CategoryChildBean>> mCategoryChildList) {
        this.mContext = mContext;
        this.mCategoryGroupList = mCategoryGroupList;
        this.mCategoryChildList = mCategoryChildList;
    }

    @Override
    public int getGroupCount() {
        return mCategoryGroupList==null?0:mCategoryGroupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mCategoryChildList==null || mCategoryChildList.get(groupPosition)==null?
                0:mCategoryChildList.get(groupPosition).size();
    }

    @Override
    public CategoryGroupBean getGroup(int groupPosition) {
        return mCategoryGroupList.get(groupPosition);
    }

    @Override
    public CategoryChildBean getChild(int groupPosition, int childPosition) {
        return mCategoryChildList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View layout, ViewGroup parent) {
        ViewGroupHolder holder = null;
        if (layout==null){
            layout = View.inflate(mContext, R.layout.item_category_group,null);
            holder = new ViewGroupHolder();
            holder.ivIndicator = (ImageView) layout.findViewById(R.id.ivIndicator);
            holder.ivGroupThumb = (NetworkImageView) layout.findViewById(R.id.ivGroupThumb);
            holder.tvGroupName = (TextView) layout.findViewById(R.id.tvGroupName);
            layout.setTag(holder);
        }else {
            holder = (ViewGroupHolder) layout.getTag();
        }
        CategoryGroupBean group = getGroup(groupPosition);
        holder.tvGroupName.setText(group.getName());
        String imgUrl = group.getImageUrl();
        String url = I.DOWNLOAD_DOWNLOAD_CATEGORY_GROUP_IMAGE_URL + imgUrl;
        ImageUtils.setThumb(url,holder.ivGroupThumb);
        if (isExpanded){
            holder.ivIndicator.setImageResource(R.drawable.expand_off);
        }else {
            holder.ivIndicator.setImageResource(R.drawable.expand_on);
        }
        return layout;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View layout, ViewGroup parent) {
        ViewChildHolder holder = null;
        if (layout==null){
            layout = View.inflate(mContext,R.layout.item_category_child,null);
            holder = new ViewChildHolder();
            holder.layoutCategoryChild = (RelativeLayout) layout.findViewById(R.id.layoutCategoryChild);
            holder.ivCategoryChildThumb = (NetworkImageView) layout.findViewById(R.id.ivCategoryChildThumb);
            holder.tvCategoryChildName = (TextView) layout.findViewById(R.id.tvCategoryChildName);
            layout.setTag(holder);
        }else {
            holder = (ViewChildHolder) layout.getTag();
        }
        final CategoryChildBean child = getChild(groupPosition,childPosition);
        String name = child.getName();
        holder.tvCategoryChildName.setText(name);
        String imgUrl = child.getImageUrl();
        String url = I.DOWNLOAD_DOWNLOAD_CATEGORY_CHILD_IMAGE_URL + imgUrl;
        ImageUtils.setThumb(url,holder.ivCategoryChildThumb);
        return layout;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    class ViewGroupHolder{
        NetworkImageView ivGroupThumb;
        TextView tvGroupName;
        ImageView ivIndicator;
    }

    class ViewChildHolder{
        RelativeLayout layoutCategoryChild;
        NetworkImageView ivCategoryChildThumb;
        TextView tvCategoryChildName;
    }

    public void addItems(ArrayList<CategoryGroupBean> groupList,
                         ArrayList<ArrayList<CategoryChildBean>> childList){
        this.mCategoryGroupList.addAll(groupList);
        this.mCategoryChildList.addAll(childList);
        notifyDataSetChanged();
    }
}
