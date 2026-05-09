package app.jammes.thepro.presentation.ui.common

import android.content.Context
import android.net.Uri
import java.io.InputStream

object CsvImport {
    fun openStream(context: Context, uri: Uri): InputStream? =
        context.contentResolver.openInputStream(uri)
}
