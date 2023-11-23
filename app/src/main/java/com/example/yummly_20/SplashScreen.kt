package com.example.yummly_20

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.yummly_20.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {

    private val SPLASH_TIME: Long = 10000
    private val TEXT_CHANGE_INTERVAL: Long = 1000 // 0.5 seconds
    private lateinit var binding: ActivitySplashScreenBinding
    private val textList = listOf(
        "Try our delicious recipes!",
        "Discover new flavors!",
        "Cooking made easy!",
        "Explore Yummly!",
        "Enjoy tasty meals at home!",
        "Create culinary masterpieces!",
        "Unlock the art of cooking!",
        "Delight your taste buds!"
    )
    private var currentTextIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load GIF with Glide
        Glide.with(this)
            .asGif()
            .load(R.drawable.yummly)
            .listener(object : RequestListener<GifDrawable> {
                override fun onLoadFailed(
                    e: com.bumptech.glide.load.engine.GlideException?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: GifDrawable?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Set the loop count to infinite for looping
                    resource?.setLoopCount(GifDrawable.LOOP_FOREVER)
                    return false
                }
            })
            .into(binding.gifImageView)

        // Delayed start of MainActivity
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, SPLASH_TIME)

        // Update text every 0.5 seconds
        val textHandler = Handler()
        textHandler.postDelayed(object : Runnable {
            override fun run() {
                binding.textView.text = textList[currentTextIndex]
                currentTextIndex = (currentTextIndex + 1) % textList.size
                textHandler.postDelayed(this, TEXT_CHANGE_INTERVAL)
            }
        }, TEXT_CHANGE_INTERVAL)
    }
}
