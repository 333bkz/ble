package www.hesvit.golf

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import www.ble.sixsix.Manager
import www.ble.sixsix.base.callback.IBleCallback
import www.ble.sixsix.base.callback.IConnectCallback
import www.ble.sixsix.base.callback.IScanCallback
import www.ble.sixsix.core.DeviceInfo
import www.ble.sixsix.device.golf.Golf
import www.ble.sixsix.device.golf.GolfData
import www.ble.sixsix.exception.Exception
import www.hesvit.golf.rxPermissions.RxPermissions

open class MainActivity : AppCompatActivity(), View.OnClickListener,
        IScanCallback, IConnectCallback, IBleCallback {

    private var device: DeviceInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.connect.setOnClickListener(this)
        this.scan.setOnClickListener(this)
        this.disconnect.setOnClickListener(this)

        RxPermissions(this).requestEach(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CAMERA
        ).subscribe()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.scan -> {
                if (TextUtils.isEmpty(deviceName.text)) {
                    toast("请输入设备名")
                } else {
                    this.progressBar.visibility = View.VISIBLE
                    Manager.getInstance().startScan(this)
                }
            }
            R.id.connect -> {
                if (this.device != null) {
                    this.progressBar.visibility = View.VISIBLE
                    val conn = Manager.getInstance().connectDevice(
                            DeviceInfo(this.device!!.address, this.device!!.name, this.device!!.rssi),
                            true,
                            this,
                            Golf::class.java)
                    if (!conn) {
                        this.progressBar.visibility = View.INVISIBLE
                    }
                }
            }
            R.id.disconnect -> {
                this.device = null;
                Manager.getInstance().disconnect()
            }
        }
    }

    override fun onScan(_device: DeviceInfo, rssi: Int) {
        this.device = _device
        this.deviceInfo.text = this.device.toString()
        Manager.getInstance().stopScan()
    }

    override fun onFilter(_address: String, _name: String, _rssi: Int): DeviceInfo? {
        val name = deviceName.text.trim().toString()
        if (!TextUtils.isEmpty(name)
                && !TextUtils.isEmpty(_name)
                && _name.contains(name)) {
            return DeviceInfo(_address, _name, _rssi)
        }
        return null
    }

    override fun cancel() {
        this.progressBar.visibility = View.INVISIBLE
        if (this.device == null) {
            toast("未搜索到指定设备")
        }
    }

    override fun onConnectSuccess(deviceInfo: DeviceInfo?) {
        this.progressBar.visibility = View.INVISIBLE
        this.device = deviceInfo
        Manager.getInstance().setBleCallback(this)
        this.deviceInfo.text = deviceInfo.toString()
        toast("连接成功")
    }

    override fun onConnectFailure(deviceInfo: DeviceInfo?, exception: Exception?) {
        this.progressBar.visibility = View.INVISIBLE
        this.deviceInfo.text = ""
        this.contentTv.text = ""
        this.device = null;
        toast("连接错误")
    }

    override fun onDisconnect(deviceInfo: DeviceInfo?, isActive: Boolean) {
        this.progressBar.visibility = View.INVISIBLE
        this.deviceInfo.text = ""
        this.contentTv.text = ""
        this.device = null;
        toast("连接断开")
    }

    override fun onSuccess(success: Boolean, data: GolfData) {
        this.contentTv.text = data.toString()
    }

    override fun onFailure(exception: Exception) {
        this.progressBar.visibility = View.INVISIBLE
        toast("命令发送超时")
    }

    override fun onBackPressed() {
        this.progressBar.visibility = View.INVISIBLE
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v != null) {
                if (isShouldHideKeyboard(v, event)) {
                    hideKeyboard(v.windowToken)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun isShouldHideKeyboard(v: View?, event: MotionEvent): Boolean {
        if (v != null && v is EditText) {
            val l = intArrayOf(0, 0)
            v.getLocationInWindow(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.height
            val right = left + v.width
            return !(event.x > left && event.x < right
                    && event.y > top && event.y < bottom)
        }
        return false
    }

    private fun hideKeyboard(token: IBinder?) {
        if (token != null) {
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Manager.getInstance().disconnect()
        Manager.getInstance().clear()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}