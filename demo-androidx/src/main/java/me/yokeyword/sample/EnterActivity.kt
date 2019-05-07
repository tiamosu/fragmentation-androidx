package me.yokeyword.sample

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import me.yokeyword.sample.demo_flow.MainActivity as FlowMainActivity
import me.yokeyword.sample.demo_wechat.MainActivity as WeChatMainActivity
import me.yokeyword.sample.demo_zhihu.MainActivity as ZhiHuMainActivity

/**
 * Created by YoKeyword on 16/6/5.
 */
class EnterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter)

        initView()
    }

    private fun initView() {
        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolBar)

        findViewById<View>(R.id.btn_flow).setOnClickListener {
            startActivity(Intent(this@EnterActivity, FlowMainActivity::class.java))
        }

        findViewById<View>(R.id.btn_wechat).setOnClickListener {
            startActivity(Intent(this@EnterActivity, WeChatMainActivity::class.java))
        }

        findViewById<View>(R.id.btn_zhihu).setOnClickListener {
            startActivity(Intent(this@EnterActivity, ZhiHuMainActivity::class.java))
        }
    }
}
