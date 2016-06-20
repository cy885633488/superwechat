package cn.ucai.fulicenter.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.fragment.BoutiqueFragment;
import cn.ucai.fulicenter.fragment.CategoryFragment;
import cn.ucai.fulicenter.fragment.NewGoodFragment;
import cn.ucai.fulicenter.fragment.PersonanCenterFragment;

public class FuliCenterMainActivity extends BaseActivity {
    TextView mtvCartHint;
    RadioButton mrbNewGood,mrbBoutique,mrbCategory,mrbCart,mrbPersonCenter;
    RadioButton[] radios = new RadioButton[5];
    private int index;
    // 当前fragment的index
    private int currentTabIndex;
    NewGoodFragment mNewGoodFragment;
    BoutiqueFragment mBoutiqueFragment;
    CategoryFragment mCategoryFragment;
    PersonanCenterFragment mPersonanCenterFragment;
    Fragment[] mFragments;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuli_center_main);
        mFragments = new Fragment[5];
        initview();
        initFragment();
        getSupportFragmentManager().beginTransaction()
                .add(cn.ucai.fulicenter.R.id.fragment_container, mNewGoodFragment)
                .add(cn.ucai.fulicenter.R.id.fragment_container, mBoutiqueFragment)
                .hide(mBoutiqueFragment)
                .add(cn.ucai.fulicenter.R.id.fragment_container, mCategoryFragment)
                .hide(mCategoryFragment)
                .show(mNewGoodFragment)
                .commit();
    }

    private void initFragment() {
        mNewGoodFragment = new NewGoodFragment();
        mBoutiqueFragment = new BoutiqueFragment();
        mCategoryFragment = new CategoryFragment();
        mPersonanCenterFragment = new PersonanCenterFragment();
        mFragments[0] = mNewGoodFragment;
        mFragments[1] = mBoutiqueFragment;
        mFragments[2] = mCategoryFragment;
        mFragments[4] = mPersonanCenterFragment;
    }

    private void initview() {
        mtvCartHint = (TextView) findViewById(R.id.tv_cart_hint);
        mrbNewGood = (RadioButton) findViewById(R.id.rb_newgood);
        mrbBoutique = (RadioButton) findViewById(R.id.rb_boutique);
        mrbCategory = (RadioButton) findViewById(R.id.rb_category);
        mrbCart = (RadioButton) findViewById(R.id.rb_cart);
        mrbPersonCenter = (RadioButton) findViewById(R.id.rb_person_center);

        radios[0] = mrbNewGood;
        radios[1] = mrbBoutique;
        radios[2] = mrbCategory;
        radios[3] = mrbCart;
        radios[4] = mrbPersonCenter;
    }

    public void onRadioClicked(View view){
        switch (view.getId()){
            case R.id.rb_newgood:
                index = 0;
                break;
            case R.id.rb_boutique:
                index = 1;
                break;
            case R.id.rb_category:
                index = 2;
                break;
            case R.id.rb_cart:
                index = 3;
                break;
            case R.id.rb_person_center:
                index = 4;
                break;
        }
        if (currentTabIndex!=index){
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            trx.hide(mFragments[currentTabIndex]);
            if (!mFragments[index].isAdded()) {
                trx.add(cn.ucai.fulicenter.R.id.fragment_container, mFragments[index]);
            }
            trx.show(mFragments[index]).commit();
            setRadioChecked(index);
            currentTabIndex = index;
        }
    }

    private void setRadioChecked(int index) {
        for (int i=0;i<radios.length;i++){
            if (i==index){
                radios[i].setChecked(true);
            }else {
                radios[i].setChecked(false);
            }
        }
    }
}
