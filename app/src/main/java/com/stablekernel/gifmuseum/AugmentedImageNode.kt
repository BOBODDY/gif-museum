package com.stablekernel.gifmuseum

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.FixedHeightViewSizer
import com.google.ar.sceneform.rendering.FixedWidthViewSizer
import com.google.ar.sceneform.rendering.ViewRenderable
import java.util.concurrent.CompletableFuture

class AugmentedImageNode(context: Context) : AnchorNode() {
    private val TAG = "AugmentedImageNode"

    private val gifViewLoader: CompletableFuture<ViewRenderable> = ViewRenderable
        .builder()
        .setView(context, R.layout.image_item)
        .setVerticalAlignment(ViewRenderable.VerticalAlignment.CENTER)
        .build()
    private var gifView: ViewRenderable? = null

    private lateinit var image: AugmentedImage

    init {
        gifViewLoader.thenAccept { viewRenderable ->
            run {
                gifView = viewRenderable
                val view = viewRenderable.view
                val image = view.findViewById<ImageView>(R.id.imageView)

                Glide.with(context)
                    .load(Uri.parse("https://media.giphy.com/media/TcdpZwYDPlWXC/giphy.gif"))
                    .into(image)
            }
        }
    }

    fun setImageToNode(image: AugmentedImage) {
        this.image = image

        if (!gifViewLoader.isDone) {
            gifViewLoader
                .thenAccept { 
                    gifView = it
                    setImageToNode(image) 
                }
            gifViewLoader.exceptionally { throwable: Throwable ->
                Log.e("AugmentedImageNode", "failed to load", throwable)
                null
            }
            return
        }

        anchor = image.createAnchor(image.centerPose)
        
        gifView?.sizer = FixedHeightViewSizer(image.extentZ)
        
        Log.d(TAG, "extentX ${image.extentX}, extentZ ${image.extentZ}")
        
        val pose = Pose.makeTranslation(0.0f, 0.0f, 0.0f)
        val localPosition = Vector3(pose.tx(), pose.ty(), pose.tz())
        val centerNode = Node()
        centerNode.setParent(this)
        centerNode.localPosition = localPosition
        centerNode.renderable = gifView
        centerNode.localRotation = Quaternion(pose.qx(), 90f, -90f, pose.qw())
        centerNode.localScale = Vector3(image.extentX * 15f, image.extentZ * 30f, 0f)
    }
}