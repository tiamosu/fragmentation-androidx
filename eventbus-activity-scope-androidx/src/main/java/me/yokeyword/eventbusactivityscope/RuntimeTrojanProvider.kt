package me.yokeyword.eventbusactivityscope

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * Internal class to initialize EventBusActivityScope.
 *
 * @hide Created by YoKey on 17/10/17.
 */
class RuntimeTrojanProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        EventBusActivityScope.init(context)
        return true
    }

    override fun query(uri: Uri, strings: Array<String>?, s: String?, strings1: Array<String>?, s1: String?): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, s: String?, strings: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, contentValues: ContentValues?, s: String?, strings: Array<String>?): Int {
        return 0
    }
}
