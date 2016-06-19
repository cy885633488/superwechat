package cn.ucai.fulicenter.view;

import android.view.View;
import android.app.Activity;
import android.widget.TextView;

import cn.ucai.fulicenter.R;

/**
 * Created by Administrator on 2016/6/18.
 */
public class DisplayUtils {
    public static void initBack(final Activity activity){
        View clickArea = activity.findViewById(R.id.backClickArea);
        clickArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
    }

    public static void initBackWithTitle(Activity activity,String title){
        TextView tvTitle = (TextView) activity.findViewById(R.id.tv_head_title);
        tvTitle.setText(title);
        initBack(activity);
    }
}