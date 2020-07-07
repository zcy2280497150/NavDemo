package zcy.android.navbardemo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import zcy.android.navbardemo.widget.NavBarView;

public class MainActivity extends AppCompatActivity {

    private NavBarView navBarView;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        tv = findViewById(R.id.tv);

        navBarView = findViewById(R.id.nav_bar_view);

        //添加图片资源 第一个参数为选中资源，第二个参数为默认未选中资源
        navBarView.addItem(R.drawable.icon_light1, R.drawable.icon_normal1);
        navBarView.addItem(R.drawable.icon_light2, R.drawable.icon_normal2);
        navBarView.addItem(R.drawable.icon_light3, R.drawable.icon_normal3);
        navBarView.addItem(R.drawable.icon_light4, R.drawable.icon_normal4);
        navBarView.addItem(R.drawable.icon_light5, R.drawable.icon_normal5);

        //背景颜色默认为白色，提供了两个方法修改颜色，可以自己添加 declare-styleable 和响应的解析，然后在XML布局里面设置
//        navBarView.setBgColorRes(android.R.color.black);
//        navBarView.setBgColorInt(Color.RED);

        navBarView.setOnSelectListener(new NavBarView.OnSelectListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSelect(int position) {
                //此处监听切换 可以根据position来切换对应的页面
                tv.setText("选中第 " + (position + 1) + " 个Item");
            }
        });
    }
}
