package com.translator.app.speak.to.world.speaktoworld.data

import androidx.annotation.Keep
import com.google.firebase.database.DatabaseError
import java.lang.Exception
@Keep
data class ResponseAppList(
    var items: ArrayList<DataAppList>? = null,
    var exception:Exception? = null,
    var error: DatabaseError? = null

)
