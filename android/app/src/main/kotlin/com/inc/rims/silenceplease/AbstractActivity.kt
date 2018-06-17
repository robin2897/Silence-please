package com.inc.rims.silenceplease

import android.app.LoaderManager
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Patterns
import com.google.gson.Gson
import com.inc.rims.silenceplease.room.DataDatabase
import com.inc.rims.silenceplease.service.BootReceiver
import com.inc.rims.silenceplease.service.ForeService
import io.flutter.app.FlutterActivity
import io.flutter.plugin.common.EventChannel
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

abstract class AbstractActivity: FlutterActivity() {
    protected val disposable = mutableMapOf<String, Disposable>()
    protected val channelMethod = "com.inc.rims.silenceplease/database"
    protected val channelEventService = "com.inc.rims.silenceplease/service-event"
    protected val channelEventContacts = "com.inc.rims.silenceplease/contacts-event"
    val contactSearchAction = "com.inc.rims.silenceplease/action/contact-search"

    @Inject
    protected lateinit var gson: Gson

    protected fun serviceWatcherReceiver(eventSink: EventChannel.EventSink): BroadcastReceiver {
        return object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent!!.action == ForeService.STOP_SERVICE_ACTION) {
                    eventSink.success("service-stopped")
                } else if (intent.action == ForeService.SERVICE_STARTED_ACTION) {
                    eventSink.success("service-started")
                }
            }
        }
    }

    protected fun shouldStartBootReceiver() {
        if (disposable.contains("shouldStartBoot")) {
            disposable["shouldStartBoot"]?.dispose()
        }
        val db = DataDatabase.getInstance(this)!!
        val calender = Calendar.getInstance()
        calender.add(Calendar.DAY_OF_MONTH, 1)
        disposable["shouldStartBoot"] = Completable.fromAction {
            val list = db.getAllModelsAtParticularDay(calender[Calendar.DAY_OF_WEEK])
            if (list.isEmpty()) {
                val componentName = ComponentName(this, BootReceiver::class.java)
                packageManager.setComponentEnabledSetting(componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP)
            } else {
                val componentName = ComponentName(this, BootReceiver::class.java)
                packageManager.setComponentEnabledSetting(componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP)
            }
        }.subscribeOn(Schedulers.io()).subscribe({
            disposable["shouldStartBoot"]?.dispose()
        })
    }

    protected fun contactLoader(eventSink: EventChannel.EventSink):
            BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent!!.action == contactSearchAction) {
                    val contactsProvider = ContactsProvider(eventSink, context!!)
                    loaderManager.restartLoader(0, intent.extras, contactsProvider)
                }
            }
        }
    }

    private class ContactsProvider(private var eventSink: EventChannel.EventSink, context: Context):
            ContextWrapper(context), LoaderManager.LoaderCallbacks<Cursor> {

        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
            val queryString = args!!.getString("query")
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Uri.withAppendedPath(ContactsContract.CommonDataKinds.Contactables.
                        CONTENT_FILTER_URI, Uri.encode(queryString))
            } else {
                Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
                        Uri.encode(queryString))
            }

            val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                "${ContactsContract.CommonDataKinds.Contactables.HAS_PHONE_NUMBER} = 1"
            } else {
                "${ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER} = 1"
            }

            val columns = arrayOf(
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            )

            return CursorLoader(this, uri, columns, selection, null,
                    null)
        }

        override fun onLoadFinished(loader: Loader<Cursor>?, data: Cursor?) {
            val contacts = arrayListOf<String>()
            if (data!!.count == 0) {
                eventSink.success("no-data")
                return
            }

            data.moveToFirst()
            do {
                val name = data.getString(0)
                val phone = data.getString(1)
                if (!Patterns.PHONE.matcher(phone).matches()) {
                    continue
                }
                contacts.add(template(name, phone))
            } while (data.moveToNext())
            eventSink.success(contacts)
        }

        override fun onLoaderReset(loader: Loader<Cursor>?) {
        }

        private fun template(name: String, phone: String): String {
            return "{\"name\": \"$name\", \"phone\":\"$phone\"}"
        }
    }

    protected interface PermissionCallback {
        fun sendResult(send: String)
    }
}