package www.hesvit.golf

import android.app.Application
import www.ble.sixsix.Manager

class App : Application() {

    companion object {
        lateinit var app: App
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        Manager.getInstance().init(this)
    }
}