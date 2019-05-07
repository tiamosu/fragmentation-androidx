package me.yokeyword.sample.demo_flow.ui.fragment.account

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import me.yokeyword.sample.R
import me.yokeyword.sample.demo_flow.base.BaseBackFragment

/**
 * Created by YoKeyword on 16/2/14.
 */
class RegisterFragment : BaseBackFragment() {
    private var mEtAccount: EditText? = null
    private var mEtPassword: EditText? = null
    private var mEtPasswordConfirm: EditText? = null
    private var mBtnRegister: Button? = null
    private var mOnLoginSuccessListener: LoginFragment.OnLoginSuccessListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is LoginFragment.OnLoginSuccessListener) {
            mOnLoginSuccessListener = context
        } else {
            throw RuntimeException("$context must implement OnLoginSuccessListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        initView(view)
        return view
    }

    private fun initView(view: View) {
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        mEtAccount = view.findViewById(R.id.et_account)
        mEtPassword = view.findViewById(R.id.et_password)
        mEtPasswordConfirm = view.findViewById(R.id.et_password_confirm)
        mBtnRegister = view.findViewById(R.id.btn_register)

        showSoftInput(mEtAccount!!)

        toolbar.setTitle(R.string.register)
        initToolbarNav(toolbar)

        mBtnRegister!!.setOnClickListener {
            val strAccount = mEtAccount!!.text.toString()
            val strPassword = mEtPassword!!.text.toString()
            val strPasswordConfirm = mEtPasswordConfirm!!.text.toString()
            if (TextUtils.isEmpty(strAccount.trim { it <= ' ' })) {
                Toast.makeText(mActivity, R.string.error_username, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(strPassword.trim { it <= ' ' }) || TextUtils.isEmpty(strPasswordConfirm.trim { it <= ' ' })) {
                Toast.makeText(mActivity, R.string.error_pwd, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 注册成功
            mOnLoginSuccessListener?.onLoginSuccess(strAccount)
            popTo(LoginFragment::class.java, true)
        }
    }

    override fun onSupportInvisible() {
        super.onSupportInvisible()
        hideSoftInput()
    }

    override fun onDetach() {
        super.onDetach()
        mOnLoginSuccessListener = null
    }

    companion object {

        fun newInstance(): RegisterFragment {
            val args = Bundle()
            val fragment = RegisterFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
