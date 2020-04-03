package com.steelsoft.floatingwidgetkotlin

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.Toast

class FloatingWidgetService : Service() {
    private var mWindowManager: WindowManager? = null
    private var liveChatView: View? = null
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        //Inflate the floating view layout we created
        liveChatView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null)
        val LAYOUT_FLAG: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )


        //Specify the view position
        params.gravity =
            Gravity.TOP or Gravity.END //Initially view will be added to top-left corner
        params.x = 30
        params.y = 500

        //Add the view to the window
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (mWindowManager != null) mWindowManager!!.addView(liveChatView, params)

        //The root element of the collapsed view layout
        val collapsedView = liveChatView!!.findViewById<View>(R.id.collapse_view)
        //The root element of the expanded view layout
        /*  val expandedView =
              mFloatingView!!.findViewById<View>(R.id.expanded_container)*/


        //Set the close button
        val closeButtonCollapsed =
            liveChatView!!.findViewById<View>(R.id.close_btn) as ImageView
        closeButtonCollapsed.setOnClickListener { //close the service and remove the from from the window
            stopSelf()
        }

        //Set the view while floating view is expanded.
        //Set the play button.
        val playButton =
            liveChatView!!.findViewById<View>(R.id.play_btn) as ImageView
        playButton.setOnClickListener {
            Toast.makeText(this@FloatingWidgetService, "Playing the song.", Toast.LENGTH_LONG)
                .show()
        }

        //Set the next button.
        val nextButton =
            liveChatView!!.findViewById<View>(R.id.next_btn) as ImageView
        nextButton.setOnClickListener {
            Toast.makeText(this@FloatingWidgetService, "Playing next song.", Toast.LENGTH_LONG)
                .show()
        }

        //Set the pause button.
        val prevButton =
            liveChatView!!.findViewById<View>(R.id.prev_btn) as ImageView
        prevButton.setOnClickListener {
            Toast.makeText(
                this@FloatingWidgetService,
                "Playing previous song.",
                Toast.LENGTH_LONG
            ).show()
        }


        //Set the close button
        val closeButton =
            liveChatView!!.findViewById<View>(R.id.close_button) as ImageView
        closeButton.setOnClickListener {
            collapsedView.visibility = View.VISIBLE
            //  expandedView.visibility = View.GONE
        }


        //Drag and move floating view using user's touch action.
        liveChatView!!.findViewById<View>(R.id.root_container)
            .setOnTouchListener(object : OnTouchListener {
                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f
                override fun onTouch(
                    v: View,
                    event: MotionEvent
                ): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {

                            //remember the initial position.
                            initialX = params.x
                            initialY = params.y

                            //get the touch location
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            val Xdiff = (event.rawX - initialTouchX).toInt()
                            val Ydiff = (event.rawY - initialTouchY).toInt()

                            //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                            //tıklarken view'ın kaymaması için Xdiff <10 && Ydiff <10 kontrolüne bakılıyor.
                            //So that is click event.
                            if (Xdiff < 10 && Ydiff < 10) {
                                if (isViewCollapsed) {
                                    //When user clicks on the image view of the collapsed layout,
                                    //visibility of the collapsed layout will be changed to "View.GONE"
                                    //and expanded view will become visible.
                                    Toast.makeText(
                                        this@FloatingWidgetService,
                                        "tiklandi.",
                                        Toast.LENGTH_LONG
                                    )
                                        .show()

                                    //    collapsedView.visibility = View.GONE
                                    //        expandedView.visibility = View.VISIBLE
                                }
                            }
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            //Calculate the X and Y coordinates of the view.
                            /**
                             * x axisindeki hareketini kısıtlamak için params.x'i dikkate almıyoruz sadece y ekseninde
                             * hareketi sağlamış olduk.
                             */
                            // params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (event.rawY - initialTouchY).toInt()

                            //Update the layout with new X & Y coordinate
                            mWindowManager!!.updateViewLayout(liveChatView, params)
                            return true
                        }
                    }
                    return false
                }
            })
    }

    /**
     * Detect if the floating view is collapsed or expanded.
     *
     * @return true if the floating view is collapsed.
     */
    private val isViewCollapsed: Boolean
        get() = liveChatView == null || liveChatView!!.findViewById<View>(R.id.collapse_view)
            .visibility == View.VISIBLE

    override fun onDestroy() {
        super.onDestroy()
        if (liveChatView != null) mWindowManager!!.removeView(liveChatView)
    }
}
