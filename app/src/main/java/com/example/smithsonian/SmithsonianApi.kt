package com.example.smithsonian

import org.json.JSONObject
import java.net.URL


data class Objects(
    val id: String,
    val title: String,
    val image: String,

)

class SmithsonianApi {

    val key = "yYnf3mDl3MHUEU0e08pGBvqVzdliAA7FfpaAUosn"

    // General search function, searches by keyword among all available objects
    fun searchGeneral(keyword: String, start: Int = 0, rows: Int = 10): List<Objects> {
        val result = mutableListOf<Objects>()
        val urlstring = "https://api.si.edu/openaccess/api/v1.0/search?q=\"$keyword\"&start=$start&rows=$rows&api_key=$key"
        val url = URL(urlstring)
        val response = url.readText()
        val json = JSONObject(response)
        val objectList = json.getJSONObject("response").getJSONArray("rows")
        for(i in 0 until objectList.length()) {
            val obj = objectList[i] as JSONObject
            val content = obj.getJSONObject("content")
            val descriptive = content.getJSONObject("descriptiveNonRepeating")
            // Check if the object has a usable image url
            if(descriptive.has("online_media")) {
                val media = descriptive.getJSONObject("online_media").getJSONArray("media")[0] as JSONObject
                val type = media.getString("type")
                // If it has a usable image
                if(type == "Images") {
                    val id = obj.getString("id")
                    val title = obj.getString("title")
                    val image = media.getString("content")
                    val newObject = Objects(id = id, title = title, image = image)
                    result.add(newObject)
                }
                else {
                    continue
                }
            }
            else {
                continue
            }
        }
        return result
    }

    // Category search function,searches by keyword within a category
    fun searchCategory(keyword: String, category: String, start: Int = 0, rows: Int = 10): List<Objects> {
        val result = mutableListOf<Objects>()
        val urlstring = "https://api.si.edu/openaccess/api/v1.0/category/$category/search?api_key=$key&q=\"$keyword\"&start=$start&rows=$rows"
        val url = URL(urlstring)
        val response = url.readText()
        val json = JSONObject(response)
        val objectList = json.getJSONObject("response").getJSONArray("rows")
        for(i in 0 until objectList.length()) {
            val obj = objectList[i] as JSONObject
            val content = obj.getJSONObject("content")
            val descriptive = content.getJSONObject("descriptiveNonRepeating")
            // Check if the object has a usable image url
            if(descriptive.has("online_media")) {
                val media = descriptive.getJSONObject("online_media").getJSONArray("media")[0] as JSONObject
                val type = media.getString("type")
                // If it has a usable image
                if(type == "Images") {
                    val id = obj.getString("id")
                    val title = obj.getString("title")
                    val image = media.getString("content")
                    val newObject = Objects(id = id, title = title, image = image)
                    result.add(newObject)
                }
                else {
                    continue
                }
            }
            else {
                continue
            }
        }
        return result
    }
}