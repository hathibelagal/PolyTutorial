package com.tutsplus.polytutorial

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpDownload
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.activity_main.*
import processing.android.PFragment
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PShape
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        const val key = "YOUR_API_KEY"
        const val baseURL = "https://poly.googleapis.com/v1"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
        * Run the following method one after another. At any time, only
        * one them must be uncommented.
        */
        // listAssets()
        // downloadAsset()
        // displayAsset()
    }

    private fun downloadAsset() {
        val assetID = "assets/3yiIERrKNQr"
        val assetURL = "$baseURL/$assetID"

        assetURL.httpGet(listOf("key" to key)).responseJson { _, _, result ->
            result.fold({
                val asset = it.obj()

                var objFileURL:String? = null
                var mtlFileURL:String? = null
                var mtlFileName:String? = null

                val formats = asset.getJSONArray("formats")

                // Loop through all formats
                for(i in 0 until formats.length()) {
                    val currentFormat = formats.getJSONObject(i)

                    // Check if current format is OBJ
                    if(currentFormat.getString("formatType") == "OBJ") {
                        // Get .obj file details
                        objFileURL = currentFormat.getJSONObject("root")
                                                .getString("url")

                        // Get the first .mtl file details
                        mtlFileURL = currentFormat.getJSONArray("resources")
                                        .getJSONObject(0)
                                        .getString("url")

                        mtlFileName = currentFormat.getJSONArray("resources")
                                        .getJSONObject(0)
                                        .getString("relativePath")
                        break
                    }
                }

                objFileURL!!.httpDownload().destination { _, _ ->
                    File(filesDir, "asset.obj")
                }.response { _, _, result ->
                    result.fold({}, {
                        Log.e("POLY", "An error occurred")
                    })
                }

                mtlFileURL!!.httpDownload().destination { _, _ ->
                    File(filesDir, mtlFileName)
                }.response { _, _, result ->
                    result.fold({}, {
                        Log.e("POLY", "An error occurred")
                    })
                }

            }, {
                Log.e("POLY", "An error occurred")
            })
        }
    }

    private fun listAssets() {
        val listURL = "$baseURL/assets"

        listURL.httpGet(listOf(
                "category" to "animals",
                "key" to key,
                "format" to "OBJ"
        )).responseJson { _, _, result ->
            result.fold({
                // Get assets array
                val assets = it.obj().getJSONArray("assets")

                // Loop through array
                for(i in 0 until assets.length()) {
                    // Get id and displayName
                    val id = assets.getJSONObject(i).getString("name")
                    val displayName = assets.getJSONObject(i).getString("displayName")

                    // Print id and displayName
                    Log.d("POLY", "(ID: $id) -- (NAME: $displayName)")
                }
            }, {
                Log.e("POLY", "An error occurred")
            })
        }
    }

    private fun displayAsset() {
        val canvas = object : PApplet() {

            var myPolyAsset: PShape? = null

            override fun settings() {
                fullScreen(PConstants.P3D)
            }

            override fun setup() {
                myPolyAsset = loadShape(File(filesDir, "asset.obj").absolutePath)
            }

            override fun draw() {
                background(0)
                scale(-50f)
                translate(-4f,-14f)
                shape(myPolyAsset)
            }
        }
        val fragment = PFragment(canvas)
        fragment.setView(canvas_holder, this)
    }
}
