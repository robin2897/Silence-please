package com.inc.rims.silenceplease.util

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("ApplySharedPref")
class SharedPrefUtil {

    fun editIntPref(context: Context, file: String, key: String, value: Int) {
        context.getSharedPreferences(file, 0).edit().putInt(key, value).commit()
    }

    fun getIntPref(context: Context, file: String, key: String, def: Int): Int {
        return context.getSharedPreferences(file, 0).getInt(key, def)
    }

    fun editBoolPref(context: Context, file: String, key: String, value: Boolean) {
        context.getSharedPreferences(file, 0).edit().putBoolean(key, value).commit()
    }

    fun editLongPref(context: Context, file: String, key: String, value: Long) {
        context.getSharedPreferences(file, 0).edit().putLong(key, value).commit()
    }

    fun getBoolPref(context: Context, file: String, key: String, def: Boolean): Boolean {
        return context.getSharedPreferences(file, 0).getBoolean(key, def)
    }

    fun editStringPref(context: Context, file: String, key: String, value: String) {
        context.getSharedPreferences(file, 0).edit().putString(key, value).commit()
    }

    fun getStringPref(context: Context, file: String, key: String, def: String): String {
        return context.getSharedPreferences(file, 0).getString(key, def)
    }

    fun getLongPref(context: Context, file: String, key: String, def: Long): Long {
        return context.getSharedPreferences(file, 0).getLong(key, def)
    }

    fun clear(context: Context, file: String) {
        context.getSharedPreferences(file, 0).edit().clear().commit()
    }

    fun remove(context: Context, file: String, key: String) {
        context.getSharedPreferences(file, 0).edit().remove(key).commit()
    }

    fun all(context: Context, file: String): Map<String, *> {
        return context.getSharedPreferences(file, 0).all
    }
}