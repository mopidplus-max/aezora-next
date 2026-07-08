package com.aezora.next

import android.app.Application
import com.aezora.next.data.db.AezoraDatabase

class AezoraApp : Application() {
    val database by lazy { AezoraDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
    }
}
