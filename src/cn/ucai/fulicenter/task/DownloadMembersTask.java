package cn.ucai.fulicenter.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.SuperWeChatApplication;
import cn.ucai.fulicenter.activity.BaseActivity;
import cn.ucai.fulicenter.bean.Member;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by Administrator on 2016/5/31.
 */
public class DownloadMembersTask extends BaseActivity {
    private final static String TAG = DownloadMembersTask.class.getCanonicalName();
    Context mContext;
    String hxid;
    String path;

    public DownloadMembersTask(Context mContext, String hxid) {
        this.mContext = mContext;
        this.hxid = hxid;
        initPath();
    }

    private void initPath() {
        // http://10.0.2.2:8080/SuperWeChatServer/Server?request=download_groups&m_user_name=    下载用户群组地址
        try {
            path = new ApiParams().with(I.Member.GROUP_HX_ID,hxid)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_GROUP_MEMBERS_BY_HXID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<Member[]>(path,Member[].class,reponseDownloadMembersTask(),errorListener()));
    }

    private Response.Listener<Member[]> reponseDownloadMembersTask() {
        return new Response.Listener<Member[]>() {
            @Override
            public void onResponse(Member[] members) {
                if (members!=null){
                    ArrayList<Member> list = Utils.array2List(members);
                    HashMap<String, ArrayList<Member>> groupMembers = SuperWeChatApplication.getInstance().getGroupMembers();
                    ArrayList<Member> memberList = groupMembers.get(hxid);
                    if (memberList!=null){
                        memberList.clear();
                        memberList.addAll(list);
                    }else {
                        groupMembers.put(hxid,list);
                    }
                    mContext.sendStickyBroadcast(new Intent("update_member_list"));
                }
            }
        };
    }
}
