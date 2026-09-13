package com.kxnst.bugsgame.data.zodiac

import android.content.res.Resources
import android.util.Xml
import com.kxnst.bugsgame.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser

class AndroidResourceZodiacRepository(
    private val resources: Resources
) : ZodiacRepository {
    override suspend fun getSigns(): List<ZodiacSign> = withContext(Dispatchers.IO) {
        val result = mutableListOf<ZodiacSign>()
        val parser = resources.getXml(R.xml.zodiac_signs)

        parser.use {
            while (it.next() != XmlPullParser.END_DOCUMENT) {
                if (it.eventType == XmlPullParser.START_TAG && it.name == "sign") {
                    result += ZodiacSign(
                        name = resources.getString(it.getAttributeResourceValue(null, "name", 0)),
                        contentDescription = resources.getString(
                            it.getAttributeResourceValue(null, "contentDescription", 0)
                        ),
                        iconResourceName = it.getAttributeValue(null, "icon").orEmpty(),
                        monthStart = it.getAttributeIntValue(null, "monthStart", 1),
                        dayStart = it.getAttributeIntValue(null, "dayStart", 1),
                        monthEnd = it.getAttributeIntValue(null, "monthEnd", 1),
                        dayEnd = it.getAttributeIntValue(null, "dayEnd", 1)
                    )
                }
            }
        }
        result
    }
}
