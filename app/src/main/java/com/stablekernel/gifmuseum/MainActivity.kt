package com.stablekernel.gifmuseum

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.AugmentedImage
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainArActivity"

    private val augmentedImageMap: MutableMap<AugmentedImage, AugmentedImageNode> = mutableMapOf()

    private lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.arFragment = supportFragmentManager.findFragmentById(R.id.arFrag) as ArFragment
        arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)
    }

    override fun onResume() {
        super.onResume()

        if (augmentedImageMap.isEmpty()) {
            fitToScan.visibility = View.VISIBLE
        }
    }

    private fun onUpdateFrame(frameTime: FrameTime) {
        val frame = arFragment.arSceneView.arFrame

        frame?.let {
            val updatedAugmentedImages = it.getUpdatedTrackables(AugmentedImage::class.java)
            updatedAugmentedImages.forEach { augmentedImage ->
                when (augmentedImage.trackingState) {
                    TrackingState.PAUSED -> {
                        val message = "Detected image " + augmentedImage.name
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                    TrackingState.TRACKING -> {
                        fitToScan.visibility = View.GONE

                        if (!augmentedImageMap.containsKey(augmentedImage)) {
                            val node = AugmentedImageNode(this)
                            node.setImageToNode(augmentedImage)
                            augmentedImageMap.put(augmentedImage, node)
                            arFragment.arSceneView.scene.addChild(node)
                        }
                    }
                    TrackingState.STOPPED -> {
                        augmentedImageMap.remove(augmentedImage)
                    }
                }
            }
        }
    }
}
