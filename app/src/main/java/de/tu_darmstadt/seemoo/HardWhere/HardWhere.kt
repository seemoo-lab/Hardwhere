package de.tu_darmstadt.seemoo.HardWhere

import android.app.Application
import android.content.Context
import org.acra.config.dialog
import org.acra.config.httpSender
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.acra.security.TLS
import org.acra.sender.HttpSender

/**
 * Only used for initializing crash reporting
 */
class HardWhere : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        if (!BuildConfig.DEBUG) {
            initAcra {
                //core configuration:
                buildConfigClass = BuildConfig::class.java
                reportFormat = StringFormat.JSON
                //each plugin you chose above can be configured in a block like this:
                dialog {
                    //required
                    text = getString(R.string.crash_dialog_text)
                    //optional, enables the dialog title
                    title = getString(R.string.crash_dialog_title)
                    //defaults to android.R.string.ok
                    positiveButtonText = getString(R.string.crash_dialog_positive)
                    //defaults to android.R.string.cancel
                    negativeButtonText = getString(R.string.crash_dialog_negative)
                    //optional, enables the comment input
                    commentPrompt = getString(R.string.crash_dialog_comment)
                    //optional, enables the email input
                    emailPrompt = getString(R.string.crash_dialog_email)
                    //defaults to android.R.drawable.ic_dialog_alert
                    //resIcon = R.drawable.crash_dialog_icon
                    //optional, defaults to @android:style/Theme.Dialog
                    //resTheme = R.style.
                    //allows other customization
                    //reportDialogClass = MyCustomDialog::class.java
                }
                mailSender {
                    //required
                    mailTo = "features@seemoo.de"
                    //defaults to true
                    reportAsFile = true
                    //defaults to ACRA-report.stacktrace
                    reportFileName = "Crash.json"
                    //defaults to "<applicationId> Crash Report"
                    //subject = getString(R.string.mail_subject)
                    //defaults to empty
                    //body = getString(R.string.mail_body)
                }
            }
        }
    }
}