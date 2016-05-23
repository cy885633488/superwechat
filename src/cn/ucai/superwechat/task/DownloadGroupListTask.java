package cn.ucai.superwechat.task;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.activity.BaseActivity;
import cn.ucai.superwechat.bean.Group;
import cn.ucai.superwechat.data.ApiParams;
import cn.ucai.superwechat.data.GsonRequest;
import cn.ucai.superwechat.utils.Utils;

/**
 * Created by Administrator on 2016/5/23.
 */
public class DownloadGroupListTask extends BaseActivity {
    private final static String TAG = DownloadGroupListTask.class.getCanonicalName();
    Context mContext;
    String username;
    String path;

    public DownloadGroupListTask(Context mContext, String username) {
        this.mContext = mContext;
        this.username = username;
        initPath();
    }

    private void initPath() {
        // http://10.0.2.2:8080/SuperWeChatServer/Server?request=download_groups&m_user_name=    下载用户群组地址
        try {
            path = new ApiParams().with(I.User.USER_NAME,username)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_GROUPS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executeRequest(new GsonRequest<Group[]>(path,Group[].class,reponseDownloadGroupListTask(),errorListener()));
    }

    private Response.Listener<Group[]> reponseDownloadGroupListTask() {
        return new Response.Listener<Group[]>() {
            @Override
            public void onResponse(Group[] groups) {
                if (groups!=null){
                    ArrayList<Group> groupList = SuperWeChatApplication.getInstance().getGroupList();
                    ArrayList<Group> list = Utils.array2List(groups);
                    groupList.clear();
                    groupList.addAll(list);
                    mContext.sendStickyBroadcast(new Intent("update_group_list"));
                }
            }
        };
    }
}
